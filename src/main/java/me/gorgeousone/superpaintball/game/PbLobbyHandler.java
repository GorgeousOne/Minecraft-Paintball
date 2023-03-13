package me.gorgeousone.superpaintball.game;

import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.team.PbTeam;
import me.gorgeousone.superpaintball.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PbLobbyHandler {

	private final JavaPlugin plugin;
	private final YamlConfiguration backupConfig;
	private final PbKitHandler kitHandler;
	private final Map<String, PbLobby> lobbies;

	public PbLobbyHandler(JavaPlugin plugin, PbKitHandler kitHandler) {
		this.plugin = plugin;
		this.backupConfig = ConfigUtil.loadConfig("lobbies", plugin);
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
		ConfigurationSection lobbiesSection = backupConfig.getConfigurationSection("lobbies");
		lobby.toYml(lobbiesSection);
		ConfigUtil.saveConfig(backupConfig, "lobbies", plugin);
	}

	public PbLobby getLobby(String lobbyName) {
		return lobbies.getOrDefault(lobbyName, null);
	}

	public PbLobby getLobby(UUID playerId) {
		for (PbLobby lobby : lobbies.values()) {
			if (lobby.hasPlayer(playerId)) {
				return lobby;
			}
		}
		return null;
	}

	public Collection<PbLobby> getLobbies() {
		return lobbies.values();
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

	public void saveLobbies() {
		Logger logger = Bukkit.getLogger();
		logger.log(Level.INFO, "  Saving lobbies:");

		if (!backupConfig.contains("lobbies")) {
			backupConfig.createSection("lobbies");
		}
		ConfigurationSection lobbiesSection = backupConfig.getConfigurationSection("lobbies");
		lobbies.values().forEach(l -> l.toYml(lobbiesSection));
		ConfigUtil.saveConfig(backupConfig, "lobbies", plugin);
		logger.log(Level.INFO, String.format("  Saved %d lobbies", lobbies.size()));
	}

	public void loadLobbies(PbArenaHandler arenaHandler) {
		Logger logger = Bukkit.getLogger();
		logger.log(Level.INFO, "  Loading lobbies:");
		ConfigUtil.assertKeyExists(backupConfig, "lobbies");
		ConfigurationSection lobbySection = backupConfig.getConfigurationSection("lobbies");

		for (String name : lobbySection.getKeys(false)) {
			try {
				PbLobby lobby = PbLobby.fromYml(name, lobbySection, plugin, this, arenaHandler, kitHandler);
				registerLobby(lobby);
			} catch (IllegalArgumentException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		}
		logger.log(Level.INFO, String.format("  Loaded %d lobbies", lobbies.size()));
	}
}
