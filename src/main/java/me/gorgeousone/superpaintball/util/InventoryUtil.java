package me.gorgeousone.superpaintball.util;

import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class InventoryUtil {

	public static void backupInv(Player player, JavaPlugin plugin) {
		YamlConfiguration backup = new YamlConfiguration();
		backup.set("gamemode", player.getGameMode().name());
		backup.set("health", player.getHealth());
		backup.set("food", player.getFoodLevel());

		ConfigurationSection itemSection = backup.createSection("items");
		PlayerInventory inv = player.getInventory();
		ItemStack[] contents = inv.getContents();

		for (int i = 0; i < contents.length; ++i) {
			ItemStack item = contents[i];

			if (item != null) {
				itemSection.set("" + i, contents[i].serialize());
			}
		}
		String backupPath = "backups/" + player.getName() + player.getUniqueId();
		ConfigUtil.saveConfig(backup, backupPath, plugin);
		inv.clear();
		player.setHealth(20);
		player.setFoodLevel(20);
	}

	public static boolean loadInv(Player player, JavaPlugin plugin) {
		File backupFolder = new File(plugin.getDataFolder() + "/backups");
		String playerId = player.getUniqueId().toString();
		File[] backups = backupFolder.listFiles((dir, name) -> name.contains(playerId));

		if (backups == null) {
			return false;
		}
		YamlConfiguration backup = YamlConfiguration.loadConfiguration(backups[0]);
		player.setGameMode(GameMode.valueOf(backup.getString("gamemode")));
		player.setHealth(backup.getDouble("health"));
		player.setFoodLevel(backup.getInt("food"));

		ConfigurationSection itemSection = backup.getConfigurationSection("items");
		PlayerInventory inv = player.getInventory();
		inv.clear();

		for (String slot : itemSection.getKeys(false)) {
			ItemStack item = ItemStack.deserialize(itemSection.getConfigurationSection(slot).getValues(false));
			inv.setItem(Integer.valueOf(slot), item);
		}
		backups[0].delete();
		return true;
	}
}
