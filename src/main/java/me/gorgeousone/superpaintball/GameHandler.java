package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.kit.AbstractKit;
import me.gorgeousone.superpaintball.kit.KitType;
import me.gorgeousone.superpaintball.kit.MachineGunKit;
import me.gorgeousone.superpaintball.kit.RifleKit;
import me.gorgeousone.superpaintball.kit.ShotgunKit;
import me.gorgeousone.superpaintball.kit.SniperKit;
import me.gorgeousone.superpaintball.team.Team;
import me.gorgeousone.superpaintball.team.TeamType;
import me.gorgeousone.superpaintball.team.TeamUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
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
	private final ItemStack[] deathArmorSet;
	
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
		this.deathArmorSet = TeamUtil.createColoredArmoSet(TeamType.DEATH_COLOR);
	}
	
	public GameInstance createGame() {
		GameInstance game = new GameInstance(this, plugin);
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
	
	public boolean isPlaying(UUID playerId) {
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
	
	public Team getTeam(ArmorStand reviveSkelly) {
		for (GameInstance game : games.values()) {
			for (Team team : game.getTeams()) {
				if (team.hasReviveSkelly(reviveSkelly)) {
					return team;
				}
			}
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
	
	public boolean isWaterBomb(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		
		if (!(meta instanceof PotionMeta)) {
			return false;
		}
		PotionMeta potionMeta = (PotionMeta) meta;
		return potionMeta.getBasePotionData().getType() == PotionType.AWKWARD;
	}
	
	public ArmorStand createSkelly(Player player, ChatColor teamColor) {
		Location deathLoc = player.getLocation();
		String name =
				ChatColor.BOLD + "" + ChatColor.MAGIC + "XX" + ChatColor.RESET + " " +
				ChatColor.BOLD + "" + teamColor + player.getName() + ChatColor.RESET + " " +
				ChatColor.BOLD + "" + ChatColor.MAGIC + "XX";
		
		deathLoc.setX((int) deathLoc.getX() + .5);
		deathLoc.setY((int) deathLoc.getY() + .5);
		deathLoc.setZ((int) deathLoc.getZ() + .5);
		
		ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(deathLoc, EntityType.ARMOR_STAND);
		stand.getEquipment().setArmorContents(deathArmorSet);
		stand.setCustomNameVisible(true);
		stand.setCustomName(name);
		stand.setInvulnerable(true);
		return stand;
	}
}
