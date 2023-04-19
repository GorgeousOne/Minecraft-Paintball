package me.gorgeousone.paintball.kit;

import me.gorgeousone.paintball.util.version.VersionUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public enum KitType {
	
	RIFLE("Paintball Rifle", "Semi-automatic paintball rifle.", "High range and accuracy."),
	SHOTGUN("Paintball Shotgun", "Pump action paintball shotgun.", "Shoots a cloud of bullets with", "low range and accuracy."),
	MACHINE_GUN("Paintball Machine Gun", "Full-automatic paintball gun.", "Accuracy drops with longer use."),
//	SNIPER(ChatColor.YELLOW + "Paintball Sniper", "Long range sniper rifle.", "Higher damage the longer scoped (sneak).")
	;
	
	public final String gunName;
	public final String[] gunLore;
	public Material gunMaterial;
	private ItemStack gunItem;
	
	KitType(String displayName, String... lore) {
		this.gunName = ChatColor.YELLOW + displayName;
		this.gunLore = lore;
		
		for (int i = 0; i < lore.length; ++i) {
			lore[i] = ChatColor.GRAY + lore[i];
		}
	}
	
	private void setGunMaterial(Material gunMaterial) {
		this.gunMaterial = gunMaterial;
		this.gunItem = new ItemStack(gunMaterial);
		ItemMeta meta = gunItem.getItemMeta();
		meta.setDisplayName(gunName);
		meta.setLore(Arrays.asList(gunLore));
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
//		SNIPER.setGunMaterial(Material.DIAMOND_HOE);
	}

	public static KitType valueOf(ItemStack itemStack) {
		for (KitType kitType : values()) {
			if (kitType.gunItem.isSimilar(itemStack)) {
				return kitType;
			}
		}
		return null;
	}
}
