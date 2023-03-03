package me.gorgeousone.superpaintball.team;

import me.gorgeousone.superpaintball.GameHandler;
import me.gorgeousone.superpaintball.GameInstance;
import me.gorgeousone.superpaintball.kit.AbstractKit;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class Team {
	
	private final static PotionEffect KNOCKOUT_BLINDNESS = new PotionEffect(PotionEffectType.BLINDNESS, 30, 4);
	private static final int DMG_POINTS = 4;
	private static final int HEARTS_PER_DMG_POINT = 5;
	
	private final TeamType teamType;
	private final GameHandler gameHandler;
	private final GameInstance game;
	private final ItemStack[] teamArmorSet;
	private final Set<UUID> players;
	private final Set<UUID> remainingPlayers;
	private final Map<UUID, Integer> playerHealth;
	//key: armorstand, value: player
	private final Map<UUID, UUID> reviveSkellies;
	private final Random rng = new Random();
	
	
	public Team(TeamType teamType, GameInstance game, GameHandler gameHandler) {
		this.teamType = teamType;
		this.game = game;
		this.gameHandler = gameHandler;
		this.players = new HashSet<>();
		this.remainingPlayers = new HashSet<>();
		this.playerHealth = new HashMap<>();
		this.reviveSkellies = new HashMap<>();
		this.teamArmorSet = TeamUtil.createColoredArmoSet(teamType.armorColor);
	}
	
	public void start() {
		for (UUID playerId : remainingPlayers) {
			Player player = Bukkit.getPlayer(playerId);
			healPlayer(player);
			equipPlayers(player);
		}
	}
	
	public TeamType getType() {
		return teamType;
	}
	
	public GameInstance getGame() {
		return game;
	}
	
	public Set<UUID> getPlayers() {
		return new HashSet<>(players);
	}
	
	public Set<UUID> getRemainingPlayers() {
		return new HashSet<>(remainingPlayers);
	}
	
	public void addPlayer(UUID playerId) {
		//TODO if game started, throw
		players.add(playerId);
		remainingPlayers.add(playerId);
		playerHealth.put(playerId, DMG_POINTS);
	}
	
	public void removePlayer(UUID playerId) {}
	
	
	public boolean hasPlayer(UUID playerId) {
		return players.contains(playerId);
	}
	
	public void paintBlock(Block shotBlock) {
		paintBlot(shotBlock, 5, 1);
		//spawn fancy particle stuff
		//make break sound
	}
	
	public void damagePlayer(Player target, Player shooter, int bulletDmg) {
		UUID playerId = target.getUniqueId();
		
		if (!remainingPlayers.contains(playerId)) {
			return;
		}
		shooter.playSound(shooter.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f);
		updateHealth(playerId, bulletDmg);
		Bukkit.broadcastMessage(String.format("hit %s for %o dmg", target.getName(), bulletDmg));
	}
	
	private void updateHealth(UUID playerId, int damage) {
		Player player = Bukkit.getPlayer(playerId);
		int health = playerHealth.get(playerId);
		health = Math.max(0, health - damage);
		playerHealth.put(playerId, health);
		
		if (health == 0) {
			knockoutPlayer(player);
		}
		player.damage(damage * HEARTS_PER_DMG_POINT);
	}
	
	public void knockoutPlayer(Player player) {
		UUID playerId = player.getUniqueId();
		remainingPlayers.remove(player.getUniqueId());
		healPlayer(player);
		player.setCollidable(false);
		player.teleport(player.getLocation().add(0, 1, 0));
		player.setAllowFlight(true);
		player.setFlying(true);
		player.addPotionEffect(KNOCKOUT_BLINDNESS);
		game.hidePlayer(player);
		
		ArmorStand skelly = gameHandler.createSkelly(player, teamType.prefixColor);
		reviveSkellies.put(skelly.getUniqueId(), playerId);
	}
	
	public boolean hasReviveSkelly(ArmorStand reviveSkelly) {
		return reviveSkellies.containsKey(reviveSkelly.getUniqueId());
	}
	
	public void revivePlayer(ArmorStand skelly) {
		UUID skellyId = skelly.getUniqueId();
		
		if (!reviveSkellies.containsKey(skelly.getUniqueId())) {
			return;
		}
		UUID playerId = reviveSkellies.get(skellyId);
		Player player = Bukkit.getPlayer(playerId);
		player.setCollidable(true);
		player.setAllowFlight(false);
		player.setFlying(false);
		
		player.teleport(skelly.getLocation());
		skelly.remove();
		game.showPlayer(player);
		reviveSkellies.remove(skellyId);
		remainingPlayers.add(playerId);
	}
	
	public void healPlayer(Player player) {
		player.setHealth(DMG_POINTS * HEARTS_PER_DMG_POINT);
	}
	
	private void equipPlayers(Player player) {
		PlayerInventory inv = player.getInventory();
		inv.setArmorContents(teamArmorSet);
		AbstractKit kit = gameHandler.getKit(player.getUniqueId());
		inv.setItem(0, kit.getType().getGun());
		inv.setItem(1, gameHandler.getWaterBombs());
	}
	
	//TODO make this nicer block patterns :(
	//TODO add water/snoball & lava particles to painted blocks
	private void paintBlot(Block block, int blockCount, int range) {
		World world = block.getWorld();
		
		if (isTerracotta(block)) {
			teamType.blockColor.updateBlock(block, false);
			world.playSound(block.getLocation(), Sound.BLOCK_STONE_PLACE, .25f, .8f);
		}
		List<Block> neighbors = getNeighbors(block, range);
		
		for (int i = 0; i < blockCount - 1; ++i) {
			if (neighbors.isEmpty()) {
				break;
			}
			int rndIdx = rng.nextInt(neighbors.size());
			Block neighbor = neighbors.get(rndIdx);
			teamType.blockColor.updateBlock(neighbor, false);
			neighbors.remove(rndIdx);
			world.playSound(block.getLocation(), Sound.BLOCK_STONE_PLACE, .05f, .8f);
		}
	}
	
	private List<Block> getNeighbors(Block block, int range) {
		List<Block> terracotta = new LinkedList<>();
		
		for (int dz = -range; dz <= range; ++dz) {
			for (int dy = -range; dy <= range; ++dy) {
				for (int dx = -range; dx <= range; ++dx) {
					Block neighbor = block.getRelative(dx, dy, dz);
					
					if (isTerracotta(neighbor)) {
						terracotta.add(neighbor);
					}
				}
			}
		}
		terracotta.remove(block);
		return terracotta;
	}
	
	private boolean isTerracotta(Block block) {
		String matName = block.getType().name();
		return matName.contains("STAINED_CLAY") || matName.contains("TERRACOTTA");
	}
	
}
