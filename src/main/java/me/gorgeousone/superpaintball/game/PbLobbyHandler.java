package me.gorgeousone.superpaintball.game;

import me.gorgeousone.superpaintball.arena.PbArena;
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
			throw new IllegalArgumentException(String.format("Lobby with name '%s' already exists.", name));
		}
		PbLobby lobby = new PbLobby(name, spawn, plugin, this, kitHandler);
		lobbies.put(lobby.getName(), lobby);
		saveLobby(lobby);
		return lobby;
	}

	private void registerLobby(PbLobby lobby) {
		String name = lobby.getName();

		if (lobbies.containsKey(name)) {
			throw new IllegalArgumentException(String.format("Lobby with name '%s' already exists.", name));
		}
		lobbies.put(name, lobby);
	}

	public void deleteLobby(PbLobby lobby) {
		String name = lobby.getName();

		if (!lobbies.containsKey(name)) {
			return;
		}
		lobby.reset();
		lobbies.remove(name);

		ConfigurationSection lobbiesSection = backupConfig.getConfigurationSection("lobbies");
		lobbiesSection.set(name, null);
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
	
	public PbGame getGame(UUID playerId) {
		PbLobby lobby = getLobby(playerId);
		return lobby == null ? null : lobby.getGame();
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
			for (PbTeam team : lobby.getGame().getTeams()) {
				if (team.hasReviveSkelly(reviveSkelly)) {
					return team;
				}
			}
		}
		return null;
	}
	
	public void linkArena(PbLobby lobby, PbArena arena) {
		for (PbLobby other : lobbies.values()) {
			if (other.getArenas().contains(arena)) {
				throw new IllegalArgumentException(String.format("Arena '%s' already linked to lobby '%s'", arena.getName(), lobby.getName()));
			}
		}
		lobby.linkArena(arena);
		saveLobby(lobby);
	}
	
	public void unlinkArena(PbLobby lobby, PbArena arena) {
		lobby.unlinkArena(arena);
		saveLobby(lobby);
	}
	
	public Location getExitSpawn() {
		//TODO idk get config spawn pos
		return lobbies.values().iterator().next().getSpawnPos().getWorld().getSpawnLocation();
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
	
	private void saveLobby(PbLobby lobby) {
		if (!backupConfig.contains("lobbies")) {
			backupConfig.createSection("lobbies");
		}
		ConfigurationSection lobbiesSection = backupConfig.getConfigurationSection("lobbies");
		lobby.toYml(lobbiesSection);
		ConfigUtil.saveConfig(backupConfig, "lobbies", plugin);
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
	
	public void closeLobbies() {
		lobbies.values().forEach(PbLobby::reset);
	}
}
