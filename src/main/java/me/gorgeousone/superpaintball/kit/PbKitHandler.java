package me.gorgeousone.superpaintball.kit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PbKitHandler {
	
	private static Map<KitType, AbstractKit> KITS;
	private static ItemStack WATER_BOMBS;
	
	private final Map<UUID, KitType> playerKits;
	
	public PbKitHandler() {
		this.playerKits = new HashMap<>();
	}
	
	public static void setupKits(JavaPlugin plugin) {
		KITS = new HashMap<>();
		KITS.put(KitType.RIFLE, new RifleKit());
		KITS.put(KitType.SHOTGUN, new ShotgunKit(plugin));
		KITS.put(KitType.MACHINE_GUN, new MachineGunKit());
		KITS.put(KitType.SNIPER, new SniperKit());
		WATER_BOMBS = createWaterBombs();
	}
	
	public static AbstractKit getKit(KitType kitType) {
		return KITS.get(kitType);
	}
	
	public AbstractKit getKit(UUID playerId) {
		playerKits.putIfAbsent(playerId, KitType.RIFLE);
		return getKit(playerKits.get(playerId));
	}
	
	public void setKit(UUID playerId, KitType kitType) {
		playerKits.put(playerId, kitType);
	}
	
	public static ItemStack getWaterBombs() {
		return WATER_BOMBS.clone();
	}
	
	private static ItemStack createWaterBombs() {
		ItemStack waterBombs = new ItemStack(Material.SPLASH_POTION, 3);
		PotionMeta meta = (PotionMeta) waterBombs.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "Water Bomb");
		meta.setBasePotionData(new PotionData(PotionType.AWKWARD));
		waterBombs.setItemMeta(meta);
		return waterBombs;
	}
}
