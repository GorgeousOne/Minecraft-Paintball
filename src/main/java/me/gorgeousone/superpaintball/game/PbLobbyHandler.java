package me.gorgeousone.superpaintball.game;

import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.team.PbTeam;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PbLobbyHandler {
	
	private final JavaPlugin plugin;
	private final PbKitHandler kitHandler;
	private final Map<UUID, PbLobby> lobbies;
	
	private final Map<String, PbArena> arenas;
	
	public PbLobbyHandler(JavaPlugin plugin, PbKitHandler kitHandler) {
		this.plugin = plugin;
		this.kitHandler = kitHandler;
		this.lobbies = new HashMap<>();
		this.arenas = new HashMap<>();
	}
	
	public PbLobby createLobby() {
		PbLobby lobby = new PbLobby(this, plugin, kitHandler);
		lobbies.put(lobby.getId(), lobby);
		return lobby;
	}
	
	public PbLobby getLobby(UUID playerId) {
		for (PbLobby lobby : lobbies.values()) {
			if (lobby.hasPlayer(playerId)) {
				return lobby;
			}
		}
		return null;
	}
	
	public boolean isPlaying(UUID playerId) {
		for (PbLobby lobby : lobbies.values()) {
			if (lobby.hasPlayer(playerId)) {
				return true;
			}
		}
		return false;
	}
	
	public PbTeam getTeam(UUID playerId) {
		PbLobby lobby = getLobby(playerId);
		
		if (lobby != null) {
			return lobby.getTeam(playerId);
		}
		return null;
	}
	
	public PbTeam getTeam(ArmorStand reviveSkelly) {
		for (PbLobby lobby : lobbies.values()) {
			for (PbTeam team : lobby.getTeams()) {
				if (team.hasReviveSkelly(reviveSkelly)) {
					return team;
				}
			}
		}
		return null;
	}
	
	public boolean containsArena(String arenaName) {
		return arenas.containsKey(arenaName);
	}
	
	public void registerArena(PbArena newArena) {
		if (arenas.containsKey(newArena.getName())) {
			throw new IllegalArgumentException("Already registered arena with name " + newArena.getName());
		}
	}
}
