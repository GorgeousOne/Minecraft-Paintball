package me.gorgeousone.superpaintball.kit;

import me.gorgeousone.superpaintball.util.version.VersionUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum KitType {
	
	RIFLE(ChatColor.YELLOW + "Paintball Rifle"),
	SHOTGUN(ChatColor.YELLOW + "Paintball Shotgun"),
	MACHINE_GUN(ChatColor.YELLOW + "Paintball Machine Gun"),
	SNIPER(ChatColor.YELLOW + "Paintball Sniper");
	
	public final String gunName;
	public Material gunMaterial;
	private ItemStack gunItem;
	
	KitType(String displayName) {this.gunName = displayName;}
	
	private void setGunMaterial(Material gunMaterial) {
		this.gunMaterial = gunMaterial;
		this.gunItem = new ItemStack(gunMaterial);
		ItemMeta meta = gunItem.getItemMeta();
		meta.setDisplayName(gunName);
		gunItem.setItemMeta(meta);
	}
	
	public ItemStack getGun() {
		return gunItem.clone();
	}
	
	//kinda uncool for an enum but i cant set the item before loading plugin version
	public static void setup() {
		RIFLE.setGunMaterial(VersionUtil.IS_LEGACY_SERVER ? Material.valueOf("IRON_BARDING") : Material.valueOf("IRON_HORSE_ARMOR"));
		SHOTGUN.setGunMaterial(VersionUtil.IS_LEGACY_SERVER ? Material.valueOf("GOLD_BARDING") : Material.valueOf("GOLDEN_HORSE_ARMOR"));
		MACHINE_GUN.setGunMaterial(VersionUtil.IS_LEGACY_SERVER ? Material.valueOf("DIAMOND_BARDING") : Material.valueOf("DIAMOND_HORSE_ARMOR"));
		SNIPER.setGunMaterial(Material.DIAMOND_HOE);
	}
}
