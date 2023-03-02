package me.gorgeousone.superpaintball.kit;

import me.gorgeousone.superpaintball.util.version.VersionUtil;
import org.bukkit.Material;

public enum KitType {
	
	RIFLE, SHOTGUN, MACHINE_GUN, SNIPER;
	
	public Material gunItem;
	
	KitType() {}
	
	//kinda uncool for an enum but i cant set the item before loading plugin version
	public static void setup() {
		RIFLE.gunItem = VersionUtil.IS_LEGACY_SERVER ? Material.valueOf("IRON_BARDING") : Material.valueOf("IRON_HORSE_ARMOR");
		SHOTGUN.gunItem = VersionUtil.IS_LEGACY_SERVER ? Material.valueOf("GOLD_BARDING") : Material.valueOf("GOLDEN_HORSE_ARMOR");
		MACHINE_GUN.gunItem = VersionUtil.IS_LEGACY_SERVER ? Material.valueOf("DIAMOND_BARDING") : Material.valueOf("DIAMOND_HORSE_ARMOR");
		SNIPER.gunItem = Material.DIAMOND_HOE;
	}
}
