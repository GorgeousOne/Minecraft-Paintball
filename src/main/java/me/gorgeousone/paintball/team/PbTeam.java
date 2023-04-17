package me.gorgeousone.paintball.team;

import me.gorgeousone.paintball.game.PbGame;
import me.gorgeousone.paintball.kit.KitType;
import me.gorgeousone.paintball.kit.PbKitHandler;
import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class PbTeam {
	
	private final TeamType teamType;
	private final PbKitHandler kitHandler;
	private final PbGame game;
	private final ItemStack[] teamArmorSet;
	private final Set<UUID> players;
	private final Set<UUID> alivePlayers;
	private final Map<UUID, Integer> playerHealth;
	private int maxHealthPoints;
	
	private final Map<UUID, List<Integer>> uncoloredArmorSlots;
	//key: armorstand, value: player
	private final Map<UUID, UUID> reviveSkellies;
	private final Random rng = new Random();
	
	public PbTeam(TeamType teamType, PbGame game, PbKitHandler kitHandler) {
		this.teamType = teamType;
		this.game = game;
		this.kitHandler = kitHandler;
		this.players = new HashSet<>();
		this.alivePlayers = new HashSet<>();
		this.playerHealth = new HashMap<>();
		this.uncoloredArmorSlots = new HashMap<>();
		this.reviveSkellies = new HashMap<>();
		this.teamArmorSet = TeamUtil.createColoredArmoSet(teamType.armorColor, ChatColor.WHITE + "Team " + teamType.displayName);
	}
	
	public void startGame(List<Location> spawns, int maxHealthPoints) {
		this.maxHealthPoints = maxHealthPoints;
		int i = 0;

		for (UUID playerId : players) {
			Player player = Bukkit.getPlayer(playerId);
			player.teleport(spawns.get(i % spawns.size()));
			alivePlayers.add(playerId);
			healPlayer(player);
			equipPlayer(player);
			++i;
		}
	}
	
	public TeamType getType() {
		return teamType;
	}
	
	public Set<UUID> getPlayers() {
		return new HashSet<>(players);
	}

	public int size() {
		return players.size();
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
		StringUtil.msg(player, "You are in team %s.", teamType.displayName);
	}

	public void removePlayer(UUID playerId) {
		if (!players.contains(playerId)) {
			throw new IllegalArgumentException("Can't remove player with id: " + playerId + ". They are not in this team.");
		}
		Player player = Bukkit.getPlayer(playerId);
		player.removePotionEffect(PotionEffectType.SPEED);
		setSpectator(player, false);
		
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
			game.onTeamKill(this);
		}
	}

	public void reset() {
		for (UUID playerId : players) {
			Player player = Bukkit.getPlayer(playerId);
			setSpectator(player, false);
			healPlayer(player);
			player.getInventory().setArmorContents(null);
			//TODO find way to let Kits reset their effects
			player.removePotionEffect(PotionEffectType.SPEED);
		}
		players.clear();
		playerHealth.clear();
		alivePlayers.clear();
		uncoloredArmorSlots.clear();
		reviveSkellies.keySet().forEach(id -> Bukkit.getEntity(id).remove());
		reviveSkellies.clear();
	}

	public void paintBlock(Block shotBlock) {
		TeamUtil.paintBlot(shotBlock, teamType, 5, 1);
	}

	public void damagePlayer(Player target, Player shooter, int bulletDmg) {
		UUID targetId = target.getUniqueId();
		
		if (!alivePlayers.contains(targetId)) {
			return;
		}
		shooter.playSound(shooter.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f);
		int healthPoints = playerHealth.get(targetId);
		
		if (bulletDmg < healthPoints) {
			target.damage(2 * bulletDmg);
			playerHealth.put(targetId, healthPoints - bulletDmg);
			target.setNoDamageTicks(0);
			paintArmor(targetId);
		} else {
			knockoutPlayer(target);
			game.broadcastKill(target, shooter);
		}
	}
	
	public void knockoutPlayer(Player player) {
		UUID playerId = player.getUniqueId();
		alivePlayers.remove(player.getUniqueId());
		
		setSpectator(player, true);
		healPlayer(player);
		player.addPotionEffect(TeamUtil.KNOCKOUT_BLINDNESS);
		
		ArmorStand skelly = TeamUtil.createSkelly(TeamUtil.DEATH_ARMOR_SET, player, teamType, kitHandler.getKitType(playerId));
		reviveSkellies.put(skelly.getUniqueId(), playerId);
		game.updateAliveScores();
		
		if (alivePlayers.isEmpty()) {
			game.onTeamKill(this);
		}
	}

	private void paintArmor(UUID playerId) {
		Player player = Bukkit.getPlayer(playerId);
		PlayerInventory inv = player.getInventory();
		ItemStack[] playerAmor = inv.getArmorContents();
		List<Integer> uncoloredSlots = uncoloredArmorSlots.get(playerId);
		int healthPoints = playerHealth.get(playerId);
		int newSlotCount = (int) Math.ceil(4f * healthPoints / maxHealthPoints);
		int oldSlotCount = uncoloredSlots.size();
		
		for (int i = newSlotCount; i < oldSlotCount; ++i) {
			int rndSlot = uncoloredSlots.get(rng.nextInt(uncoloredSlots.size()));
			playerAmor[rndSlot] = TeamUtil.DEATH_ARMOR_SET[rndSlot];
			uncoloredSlots.remove(Integer.valueOf(rndSlot));
		}
		inv.setArmorContents(playerAmor);
	}

	private void setSpectator(Player player, boolean isSpectator) {
		player.setCollidable(!isSpectator);
		player.setAllowFlight(isSpectator);
		player.setFlying(isSpectator);

		if (isSpectator) {
			player.teleport(player.getLocation().add(0, 1, 0));
			game.hidePlayer(player);
		} else {
			game.showPlayer(player);
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
		playerHealth.put(playerId, maxHealthPoints);
		alivePlayers.add(playerId);
		game.updateAliveScores();
	}
	
	public void healPlayer(Player player) {
		player.setFoodLevel(20);
		player.setMaxHealth(2 * maxHealthPoints);
		player.setHealth(2 * maxHealthPoints);
		player.getInventory().setArmorContents(teamArmorSet);

		UUID playerId = player.getUniqueId();
		playerHealth.put(player.getUniqueId(), maxHealthPoints);
		uncoloredArmorSlots.put(playerId, new ArrayList<>(Arrays.asList(0, 1, 2, 3)));
	}
	
	private void equipPlayer(Player player) {
		PlayerInventory inv = player.getInventory();
		inv.clear();
		KitType kitType = kitHandler.getKitType(player.getUniqueId());
		PbKitHandler.getKit(kitType).prepPlayer(player);
		
		inv.setItem(0, kitType.getGun());
		inv.setItem(1, PbKitHandler.getWaterBombs());
		inv.setItem(8, teamArmorSet[2]);
		inv.setArmorContents(teamArmorSet);
	}
}
