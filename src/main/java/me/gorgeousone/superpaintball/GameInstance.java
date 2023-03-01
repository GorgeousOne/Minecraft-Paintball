package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.team.Team;
import me.gorgeousone.superpaintball.team.TeamType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameInstance {
	
	private final UUID gameId;
	private final Map<TeamType, Team> teams;
	private final Set<UUID> players;
	
//	private Arena arena
//	private long startTime
//	private bool isRunning;
	
	
	public GameInstance() {
		this.gameId = UUID.randomUUID();
		this.teams = new HashMap<>();
		this.players = new HashSet<>();
		
		for (TeamType teamType : TeamType.values()) {
			teams.put(teamType, new Team(teamType));
		}
	}
	
	public UUID getId() {
		return gameId;
	}
	
	public void addPlayer(Player player, TeamType teamType) {
		players.add(player.getUniqueId());
		teams.get(teamType).addPlayer(player);
	}
	
	public void removePlayer(Player player) {
	
	}
	
	public boolean hasPlayer(Player player) {
		return players.contains(player.getUniqueId());
	}
	
	public Team getTeam(Player player) {
		for (Team team : teams.values()) {
			if (team.hasPlayer(player)) {
				return team;
			}
		}
		return null;
	}
}
