package me.gorgeousone.paintball.util;

import me.gorgeousone.paintball.ConfigSettings;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public abstract class ItemUtil {
	
	private static final String BACKUPS_FOLDER = "backups/inventory/";
	
	private ItemUtil() {}
	
	public static ItemStack nameItem(ItemStack item, String displayName) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);
		item.setItemMeta(meta);
		return item;
	}
	
	public static void addMagicGlow(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
	}
	
	public static void removeMagicGlow(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.removeEnchant(Enchantment.ARROW_INFINITE);
		item.setItemMeta(meta);
	}
	
	public static void saveInventory(Player player, Location spawn, JavaPlugin plugin) {
		YamlConfiguration backup = new YamlConfiguration();
		backup.set("gamemode", player.getGameMode().name());
		backup.set("max-health", player.getMaxHealth());
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
		String backupPath = BACKUPS_FOLDER + player.getName() + player.getUniqueId();
		ConfigUtil.saveConfig(backup, backupPath, plugin);
		inv.clear();
		player.setMaxHealth(2 * ConfigSettings.PLAYER_HEALTH_POINTS);
		player.setHealth(2 * ConfigSettings.PLAYER_HEALTH_POINTS);
		player.setFoodLevel(20);
		player.setLevel(0);
		player.setExp(0);
	}
	
	public static boolean loadInventory(Player player, JavaPlugin plugin, boolean isShutdown) {
		File backupFile = ConfigUtil.matchFirstFile(player.getUniqueId().toString(), BACKUPS_FOLDER, plugin);
		
		if (backupFile == null) {
			return false;
		}
		YamlConfiguration backup = YamlConfiguration.loadConfiguration(backupFile);
		player.setGameMode(GameMode.valueOf(backup.getString("gamemode")));
		
		if (player.getGameMode() == GameMode.CREATIVE) {
			player.setAllowFlight(true);
		}
		player.setMaxHealth(backup.getDouble("max-health"));
		player.setHealth(backup.getDouble("health"));
		player.setFoodLevel(backup.getInt("food"));
		player.setLevel(backup.getInt("level"));
		float xp = (float) backup.getDouble("xp");
		player.setLevel((int) xp);
		player.setExp(xp % 1);

		Location spawn = (Location) backup.get("spawn");

		if (isShutdown) {
			player.teleport(spawn);
		} else {
			LocationUtil.tpTick(player, spawn, plugin);
		}
		ConfigurationSection itemSection = backup.getConfigurationSection("items");
		PlayerInventory inv = player.getInventory();
		inv.clear();
		
		for (String slot : itemSection.getKeys(false)) {
			inv.setItem(Integer.valueOf(slot), (ItemStack) itemSection.get(slot));
		}
		backupFile.delete();
		return true;
	}
}
