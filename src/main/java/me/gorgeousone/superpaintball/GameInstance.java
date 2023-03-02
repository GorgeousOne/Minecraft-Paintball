package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.kit.AbstractKit;
import me.gorgeousone.superpaintball.team.Team;
import me.gorgeousone.superpaintball.team.TeamType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameInstance {
	
	private final UUID gameId;
	private final Map<TeamType, Team> teams;
	private final Set<UUID> players;
	
	private final Map<UUID, Long> shootCooldowns;
	private BukkitRunnable cooldownTimer;
//	private Arena arena
//	private long startTime
//	private bool isRunning;
	
	public GameInstance() {
		this.gameId = UUID.randomUUID();
		this.teams = new HashMap<>();
		this.players = new HashSet<>();
		this.shootCooldowns = new HashMap<>();
		
		for (TeamType teamType : TeamType.values()) {
			teams.put(teamType, new Team(teamType, this));
		}
		start();
	}
	
	public UUID getId() {
		return gameId;
	}
	
	public void start() {
		cooldownTimer = new BukkitRunnable() {
			@Override
			public void run() {
				for (UUID playerId : players) {
//					if (Pla)
				}
			}
		};
	}
	
	public void addPlayer(UUID playerId, TeamType teamType) {
		players.add(playerId);
		teams.get(teamType).addPlayer(playerId);
	}
	
	public void removePlayer(UUID playerId) {
	}
	
	public boolean hasPlayer(UUID playerId) {
		return players.contains(playerId);
	}
	
	public Team getTeam(UUID playerId) {
		for (Team team : teams.values()) {
			if (team.hasPlayer(playerId)) {
				return team;
			}
		}
		return null;
	}
	
	public void launchShot(Player player, AbstractKit kit) {
		UUID playerId = player.getUniqueId();
		
		if (shootCooldowns.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
			return;
		}
		long cooldownTicks = kit.launchShot(player, getTeam(playerId));
		
		if (cooldownTicks > 0) {
			shootCooldowns.put(playerId, System.currentTimeMillis() + cooldownTicks * 50);
		}
	}
}
