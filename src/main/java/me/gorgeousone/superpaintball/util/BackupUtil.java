package me.gorgeousone.superpaintball.util;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class BackupUtil {

	public static void saveBackup(Player player, Location spawn, JavaPlugin plugin) {
		YamlConfiguration backup = new YamlConfiguration();
		backup.set("gamemode", player.getGameMode().name());
		backup.set("health", player.getHealth());
		backup.set("food", player.getFoodLevel());
		backup.set("xp", player.getLevel() + player.getExp());
		backup.set("spawn", spawn);

		ConfigurationSection itemSection = backup.createSection("items");
		PlayerInventory inv = player.getInventory();
		ItemStack[] contents = inv.getContents();

		for (int i = 0; i < contents.length; ++i) {
			ItemStack item = contents[i];

			if (item != null) {
				itemSection.set("" + i, contents[i]);
			}
		}
		String backupPath = "backups/" + player.getName() + player.getUniqueId();
		ConfigUtil.saveConfig(backup, backupPath, plugin);
		inv.clear();
		player.setHealth(20);
		player.setFoodLevel(20);
		player.setLevel(0);
		player.setExp(0);
	}

	public static boolean loadBackup(Player player, JavaPlugin plugin) {
		File backupFile = findBackup(player, plugin);

		if (backupFile == null) {
			return false;
		}
		YamlConfiguration backup = YamlConfiguration.loadConfiguration(backupFile);
		player.setGameMode(GameMode.valueOf(backup.getString("gamemode")));
		
		if (player.getGameMode() == GameMode.CREATIVE) {
			player.setAllowFlight(true);
		}
		player.setHealth(backup.getDouble("health"));
		player.setFoodLevel(backup.getInt("food"));
		player.setLevel(backup.getInt("level"));
		float xp = (float) backup.getDouble("xp");
		player.setLevel((int) xp);
		player.setExp(xp % 1);
		player.teleport((Location) backup.get("spawn"));

		ConfigurationSection itemSection = backup.getConfigurationSection("items");
		PlayerInventory inv = player.getInventory();
		inv.clear();

		for (String slot : itemSection.getKeys(false)) {
			inv.setItem(Integer.valueOf(slot), (ItemStack) itemSection.get(slot));
		}
		backupFile.delete();
		return true;
	}

	private static File findBackup(Player player, JavaPlugin plugin) {
		File backupFolder = new File(plugin.getDataFolder() + "/backups");
		String playerId = player.getUniqueId().toString();
		File[] backups = backupFolder.listFiles((dir, name) -> name.contains(playerId));

		if (backups != null && backups.length > 0) {
			return backups[0];
		}
		return null;
	}
}
