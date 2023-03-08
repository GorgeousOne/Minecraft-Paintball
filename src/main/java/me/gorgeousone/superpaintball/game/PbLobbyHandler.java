package me.gorgeousone.superpaintball.game;

import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.team.PbTeam;
import me.gorgeousone.superpaintball.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PbLobbyHandler {
	
	private final JavaPlugin plugin;
	private final PbKitHandler kitHandler;
	private final Map<String, PbLobby> lobbies;

	public PbLobbyHandler(JavaPlugin plugin, PbKitHandler kitHandler) {
		this.plugin = plugin;
		this.kitHandler = kitHandler;
		this.lobbies = new HashMap<>();
	}
	
	public PbLobby createLobby(String name, Location spawn) {
		if (lobbies.containsKey(name)) {
			throw new IllegalArgumentException(String.format("Lobby with name '%s' already exists."));
		}
		PbLobby lobby = new PbLobby(name, spawn, plugin, this, kitHandler);
		lobbies.put(lobby.getName(), lobby);
		return lobby;
	}

	public void registerLobby(PbLobby lobby) {
		if (lobbies.containsKey(lobby.getName())) {
			throw new IllegalArgumentException(String.format("Lobby with name '%s' already exists."));
		}
		lobbies.put(lobby.getName(), lobby);
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

	public void loadLobbies(YamlConfiguration lobbyConfig) {
		ConfigUtil.assertKeyExists(lobbyConfig, "lobbies");
		ConfigurationSection lobbies = lobbyConfig.getConfigurationSection("lobbies");

		for (String name : lobbies.getKeys(false)) {
			PbLobby lobby = PbLobby.fromYml(name, lobbies, plugin, this, kitHandler);
			registerLobby(lobby);
		}
	}

}
