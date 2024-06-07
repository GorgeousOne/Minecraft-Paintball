package me.gorgeousone.paintball.kit;

import me.gorgeousone.paintball.Message;
import me.gorgeousone.paintball.util.ItemUtil;
import me.gorgeousone.paintball.util.version.VersionUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Enum to store the different types of paintball guns.
 */
public enum KitType {
	
	RIFLE(69420),
	SHOTGUN(69421),
	MACHINE_GUN(69422);
	//	SNIPER;
	
	public String gunName;
	public String[] gunLore;
	public Material gunMaterial;
	private ItemStack gunItem;
	private final int modelData;

	KitType(int modelData) {
		this.modelData = modelData;
	}
	
	private void setDescription(String displayName, String lore) {
		this.gunName = ChatColor.YELLOW + displayName;
		this.gunLore = lore.split("\\\\n");
		
		for (int i = 0; i < gunLore.length; ++i) {
			gunLore[i] = ChatColor.GRAY + gunLore[i];
		}
	}
	
	private void setGunMaterial(Material gunMaterial) {
		this.gunMaterial = gunMaterial;
		this.gunItem = new ItemStack(gunMaterial);
		ItemMeta meta = gunItem.getItemMeta();
		meta.setDisplayName(gunName);
		meta.setLore(Arrays.asList(gunLore));
		ItemUtil.setModelData(meta, modelData);
		gunItem.setItemMeta(meta);
	}
	
	public ItemStack getGun() {
		return gunItem.clone();
	}
	
	//kinda uncool for an enum but i cant set the item before loading plugin version
	
	/**
	 * Creates items for the guns dependent on MC version AND LANGUAGE
	 */
	public static void updateLanguage() {
		RIFLE.setDescription(Message.NAME_RIFLE, Message.LORE_RIFLE);
		SHOTGUN.setDescription(Message.NAME_SHOTGUN, Message.LORE_SHOTGUN);
		MACHINE_GUN.setDescription(Message.NAME_MACHINE_GUN, Message.LORE_MACHINE_GUN);
//		SNIPER_RILE.setDescription(ChatColor.YELLOW + "Paintball Sniper", "Long range sniper rifle.", "Higher damage the longer scoped (sneak).")
	}
	
	public static void updateItems() {
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
