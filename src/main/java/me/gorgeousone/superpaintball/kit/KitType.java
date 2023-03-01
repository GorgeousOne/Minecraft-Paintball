package me.gorgeousone.superpaintball.kit;

import org.bukkit.Material;

public enum KitType {
	
	RIFLE(Material.IRON_HORSE_ARMOR, 1, 1, 0, 1),
	SHOTGUN(Material.GOLDEN_HORSE_ARMOR, 8, 1, 1, 1),
	MACHINE_GUN(Material.DIAMOND_HORSE_ARMOR, 1, 1, 1, 1),
	SNIPER(Material.DIAMOND_HOE, -1, -1, 0, 1);
	
	public final Material gunItem;
	public final int bulletCount;
	public final float bulletSpeed;
	public final float bulletSpread;
	public final float fireRate;
	
	KitType(Material gunItem, int bulletCount, float bulletSpeed, float bulletSpread, float fireRate) {
		this.gunItem = gunItem;
		this.bulletCount = bulletCount;
		this.bulletSpeed = bulletSpeed;
		this.bulletSpread = bulletSpread;
		this.fireRate = fireRate;
	}
}
