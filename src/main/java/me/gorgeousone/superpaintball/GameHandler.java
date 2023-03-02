package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.kit.AbstractKit;
import me.gorgeousone.superpaintball.kit.KitType;
import me.gorgeousone.superpaintball.kit.MachineGunKit;
import me.gorgeousone.superpaintball.kit.RifleKit;
import me.gorgeousone.superpaintball.kit.ShotgunKit;
import me.gorgeousone.superpaintball.kit.SniperKit;
import me.gorgeousone.superpaintball.team.Team;
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

public class GameHandler {
	
	private final JavaPlugin plugin;
	private final Map<KitType, AbstractKit> kits;
	private final Map<UUID, KitType> playerKits;
	private final Map<UUID, GameInstance> games;
	private final ItemStack waterBombs;
	
	public GameHandler(JavaPlugin plugin) {
		this.plugin = plugin;
		this.kits = new HashMap<>();
		this.playerKits = new HashMap<>();
		this.games = new HashMap<>();
		
		kits.put(KitType.RIFLE, new RifleKit());
		kits.put(KitType.SHOTGUN, new ShotgunKit(this.plugin));
		kits.put(KitType.MACHINE_GUN, new MachineGunKit());
		kits.put(KitType.SNIPER, new SniperKit());
		
		this.waterBombs = createWaterBombs();
	}
	
	public GameInstance createGame() {
		GameInstance game = new GameInstance(this);
		games.put(game.getId(), game);
		return game;
	}
	
	public GameInstance getGame(UUID playerId) {
		for (GameInstance game : games.values()) {
			if (game.hasPlayer(playerId)) {
				return game;
			}
		}
		return null;
	}
	
	public boolean hasPlayer(UUID playerId) {
		for (GameInstance game : games.values()) {
			if (game.hasPlayer(playerId)) {
				return true;
			}
		}
		return false;
	}
	
	public Team getTeam(UUID playerId) {
		GameInstance game = getGame(playerId);
		
		if (game != null) {
			return game.getTeam(playerId);
		}
		return null;
	}
	
	
	public AbstractKit getKit(KitType kitType) {
		return kits.get(kitType);
	}
	
	public AbstractKit getKit(UUID playerId) {
		playerKits.putIfAbsent(playerId, KitType.RIFLE);
		return getKit(playerKits.get(playerId));
	}
	
	public ItemStack getWaterBombs() {
		return waterBombs.clone();
	}
	
	private ItemStack createWaterBombs() {
		ItemStack waterBombs = new ItemStack(Material.SPLASH_POTION, 3);
		PotionMeta meta = (PotionMeta) waterBombs.getItemMeta();
		meta.setDisplayName(ChatColor.YELLOW + "Water Bomb");
		meta.setBasePotionData(new PotionData(PotionType.AWKWARD));
		waterBombs.setItemMeta(meta);
		return waterBombs;
	}
}
