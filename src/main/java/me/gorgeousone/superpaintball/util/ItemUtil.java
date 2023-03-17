package me.gorgeousone.superpaintball.util;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class ItemUtil {

	private ItemUtil() {}

	public static void setItemName(String displayName, ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);
		item.setItemMeta(meta);
	}
}
