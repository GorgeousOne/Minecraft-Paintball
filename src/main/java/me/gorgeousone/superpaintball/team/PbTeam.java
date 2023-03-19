package me.gorgeousone.superpaintball.team;

import me.gorgeousone.superpaintball.kit.KitType;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.game.PbLobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class PbTeam {
	
	private final TeamType teamType;
	private final PbLobbyHandler lobbyHandler;
	private final PbKitHandler kitHandler;
	private final PbLobby lobby;
	private final ItemStack[] teamArmorSet;
	private final Set<UUID> players;
	private final Set<UUID> alivePlayers;
	private final Map<UUID, Integer> playerHealth;
	private final Map<UUID, List<Integer>> uncoloredArmorSlots;
	//key: armorstand, value: player
	private final Map<UUID, UUID> reviveSkellies;
	private final Random rng = new Random();
	
	
	public PbTeam(TeamType teamType, PbLobby lobby, PbLobbyHandler lobbyHandler, PbKitHandler kitHandler) {
		this.teamType = teamType;
		this.lobby = lobby;
		this.lobbyHandler = lobbyHandler;
		this.kitHandler = kitHandler;
		this.players = new HashSet<>();
		this.alivePlayers = new HashSet<>();
		this.playerHealth = new HashMap<>();
		this.uncoloredArmorSlots = new HashMap<>();
		this.reviveSkellies = new HashMap<>();
		this.teamArmorSet = TeamUtil.createColoredArmoSet(teamType.armorColor);
	}
	
	public void start(List<Location> spawns) {
		int i = 0;

		for (UUID playerId : alivePlayers) {
			Player player = Bukkit.getPlayer(playerId);
			player.teleport(spawns.get(i % spawns.size()));
			healPlayer(player);
			equipPlayers(player);
			++i;
		}
	}
	
	public TeamType getType() {
		return teamType;
	}
	
	public PbLobby getGame() {
		return lobby;
	}

	public Set<UUID> getPlayers() {
		return new HashSet<>(players);
	}

	public boolean hasPlayer(UUID playerId) {
		return players.contains(playerId);
	}

	public Set<UUID> getAlivePlayers() {
		return new HashSet<>(alivePlayers);
	}

	public boolean isAlive(UUID playerId) {
		return alivePlayers.contains(playerId);
	}

	public void addPlayer(Player player) {
		UUID playerId = player.getUniqueId();
		players.add(playerId);
		alivePlayers.add(playerId);
		player.sendMessage(String.format("You are now team %s.", teamType.displayName));
	}

	public void removePlayer(UUID playerId) {
		if (!players.contains(playerId)) {
			throw new IllegalArgumentException("Can't remove player with id: " + playerId + ". They are not in this team.");
		}
		players.remove(playerId);
		alivePlayers.remove(playerId);
		playerHealth.remove(playerId);
		uncoloredArmorSlots.remove(playerId);
		UUID skellyId = getReviveSkellyId(playerId);

		if (skellyId != null) {
			Bukkit.getEntity(skellyId).remove();
			reviveSkellies.remove(skellyId);
		}
		if (alivePlayers.isEmpty()) {
			lobby.onTeamKill(this);
		}
	}

	public void kickPlayers() {
		for (UUID skellyId : reviveSkellies.keySet()) {
			Bukkit.getEntity(skellyId).remove();
		}
		players.clear();
		alivePlayers.clear();
		playerHealth.clear();
		uncoloredArmorSlots.clear();
		reviveSkellies.clear();
	}

	public void paintBlock(Block shotBlock) {
		paintBlot(shotBlock, 5, 1);
	}

	public void damagePlayer(Player target, Player shooter, int bulletDmg) {
		UUID playerId = target.getUniqueId();

		if (!alivePlayers.contains(playerId)) {
			return;
		}
		shooter.playSound(shooter.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f);
		boolean isAlive = updateHealth(playerId, bulletDmg);

		if (isAlive) {
			paintArmor(playerId, bulletDmg);
		} else {
			lobby.broadcastKill(target, shooter);
		}
	}

	private boolean updateHealth(UUID playerId, int damage) {
		Player player = Bukkit.getPlayer(playerId);
		int health = playerHealth.get(playerId);
		health = Math.max(0, health - damage);
		playerHealth.put(playerId, health);

		if (health == 0) {
			knockoutPlayer(player);
			return false;
		}
		player.damage(damage * TeamUtil.HEARTS_PER_DMG_POINT);
		return true;
	}

	private void paintArmor(UUID playerId, int damage) {
		Player player = Bukkit.getPlayer(playerId);
		PlayerInventory inv = player.getInventory();
		ItemStack[] playerAmor = inv.getArmorContents();
		List<Integer> uncoloredSlots = uncoloredArmorSlots.get(playerId);

		for (int i = 0; i < damage; ++i) {
			int rndSlot = uncoloredSlots.get(rng.nextInt(uncoloredSlots.size()));
			playerAmor[rndSlot] = TeamUtil.DEATH_ARMOR_SET[rndSlot];
			uncoloredSlots.remove(Integer.valueOf(rndSlot));
		}
		inv.setArmorContents(playerAmor);
	}

	public void knockoutPlayer(Player player) {
		UUID playerId = player.getUniqueId();
		alivePlayers.remove(player.getUniqueId());
		setSpectator(player, true);

		ArmorStand skelly = TeamUtil.createSkelly(TeamUtil.DEATH_ARMOR_SET, player, teamType, kitHandler.getKitType(playerId));
		reviveSkellies.put(skelly.getUniqueId(), playerId);
		lobby.updateAliveScores();

		if (alivePlayers.isEmpty()) {
			lobby.onTeamKill(this);
		}
	}

	private void setSpectator(Player player, boolean isSpectator) {
		player.setCollidable(!isSpectator);
		player.setAllowFlight(isSpectator);
		player.setFlying(isSpectator);

		if (isSpectator) {
			healPlayer(player);
			player.teleport(player.getLocation().add(0, 1, 0));
			player.addPotionEffect(TeamUtil.KNOCKOUT_BLINDNESS);
			lobby.hidePlayer(player);
		} else {
			lobby.showPlayer(player);
		}
	}

	public boolean hasReviveSkelly(ArmorStand reviveSkelly) {
		return reviveSkellies.containsKey(reviveSkelly.getUniqueId());
	}

	public UUID getReviveSkellyId(UUID playerId) {
		for (UUID skellyId : reviveSkellies.keySet()) {
			if (reviveSkellies.get(skellyId) == playerId) {
				return skellyId;
			}
		}
		return null;
	}

	public void revivePlayer(UUID playerId) {
		for (UUID skellyId : reviveSkellies.keySet()) {
			if (reviveSkellies.get(skellyId) == playerId) {
				revivePlayer((ArmorStand) Bukkit.getEntity(skellyId));
				return;
			}
		}
	}

	public void revivePlayer(ArmorStand skelly) {
		UUID skellyId = skelly.getUniqueId();

		if (!reviveSkellies.containsKey(skelly.getUniqueId())) {
			return;
		}
		UUID playerId = reviveSkellies.get(skellyId);
		Player player = Bukkit.getPlayer(playerId);

		setSpectator(player, false);
		player.teleport(skelly.getLocation());
		skelly.remove();

		reviveSkellies.remove(skellyId);
		playerHealth.put(playerId, TeamUtil.DMG_POINTS);
		alivePlayers.add(playerId);
		lobby.updateAliveScores();
	}

	public void healPlayer(Player player) {
		player.setFoodLevel(20);
		player.setHealth(TeamUtil.DMG_POINTS * TeamUtil.HEARTS_PER_DMG_POINT);
		player.getInventory().setArmorContents(teamArmorSet);

		UUID playerId = player.getUniqueId();
		playerHealth.put(player.getUniqueId(), TeamUtil.DMG_POINTS);
		uncoloredArmorSlots.put(playerId, new ArrayList<>(Arrays.asList(0, 1, 2, 3)));
	}
	private void equipPlayers(Player player) {
		PlayerInventory inv = player.getInventory();
		inv.clear();
		KitType kitType = kitHandler.getKitType(player.getUniqueId());
		inv.setItem(0, kitType.getGun());
		inv.setItem(1, PbKitHandler.getWaterBombs());
	}
	//TODO make this nicer block patterns :(

	//TODO add water/snowball & lava particles to painted blocks

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
