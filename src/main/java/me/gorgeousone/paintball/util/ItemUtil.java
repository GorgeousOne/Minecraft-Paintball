package me.gorgeousone.paintball.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class ItemUtil {

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
}
