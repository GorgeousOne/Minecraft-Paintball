package me.gorgeousone.paintball.game;

import me.gorgeousone.paintball.CommandTrigger;
import me.gorgeousone.paintball.Message;
import me.gorgeousone.paintball.arena.PbArena;
import me.gorgeousone.paintball.arena.PbArenaHandler;
import me.gorgeousone.paintball.kit.PbKitHandler;
import me.gorgeousone.paintball.team.PbTeam;
import me.gorgeousone.paintball.util.ConfigUtil;
import me.gorgeousone.paintball.util.StringUtil;
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
import java.util.logging.Logger;

/**
 * Class to load and manage lobbies
 */
public class PbLobbyHandler {
	
	private final JavaPlugin plugin;
	private final YamlConfiguration backupConfig;
	private final PbKitHandler kitHandler;
	private final CommandTrigger commandTrigger;
	private final Map<String, PbLobby> lobbies;
	
	public PbLobbyHandler(JavaPlugin plugin, PbKitHandler kitHandler, CommandTrigger commandTrigger) {
		this.plugin = plugin;
		this.backupConfig = ConfigUtil.loadConfig("lobbies", plugin);
		this.kitHandler = kitHandler;
		this.commandTrigger = commandTrigger;
		this.lobbies = new HashMap<>();
	}
	
	public void updateLobbyUis() {
		lobbies.values().forEach(PbLobby::updateUi);
	}
	
	public PbLobby createLobby(String name, Location spawn) {
		if (lobbies.containsKey(name)) {
			throw new IllegalArgumentException(Message.LOBBY_EXISTS.format(name));
		}
		PbLobby lobby = new PbLobby(name, spawn, plugin, this, kitHandler, commandTrigger);
		lobbies.put(lobby.getName(), lobby);
		saveLobby(lobby);
		return lobby;
	}
	
	private void registerLobby(PbLobby lobby) {
		String name = lobby.getName();
		
		if (lobbies.containsKey(name)) {
			throw new IllegalArgumentException(Message.LOBBY_EXISTS.format(name));
		}
		lobbies.put(name, lobby);
	}
	
	public void deleteLobby(PbLobby lobby) {
		String name = lobby.getName();
		
		if (!lobbies.containsKey(name)) {
			return;
		}
		lobby.reset(false);
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
				throw new IllegalArgumentException(Message.ARENA_ALREADY_LINKED.format(arena.getName(), lobby.getName()));
			}
		}
		lobby.linkArena(arena);
		saveLobby(lobby);
	}
	
	public void unlinkArena(PbArena arena) {
		for (PbLobby lobby : lobbies.values()) {
			if (lobby.getArenas().contains(arena)) {
				lobby.unlinkArena(arena);
				return;
			}
		}
	}
	
	public Location getExitSpawn() {
		//TODO idk get config spawn pos
		return lobbies.values().iterator().next().getJoinSpawn().getWorld().getSpawnLocation();
	}
	
	public void saveLobby(PbLobby lobby) {
		if (!backupConfig.contains("lobbies")) {
			backupConfig.createSection("lobbies");
		}
		ConfigurationSection lobbiesSection = backupConfig.getConfigurationSection("lobbies");
		lobby.toYml(lobbiesSection);
		ConfigUtil.saveConfig(backupConfig, "lobbies", plugin);
	}
	
	public void loadLobbies(PbArenaHandler arenaHandler) {
		Logger logger = plugin.getLogger();
		logger.info("Loading lobbies:");
		ConfigUtil.assertKeyExists(backupConfig, "lobbies");
		ConfigurationSection lobbySection = backupConfig.getConfigurationSection("lobbies");
		
		for (String name : lobbySection.getKeys(false)) {
			try {
				PbLobby lobby = PbLobby.fromYml(name, lobbySection, plugin, this, arenaHandler, kitHandler, commandTrigger);
				registerLobby(lobby);
			} catch (IllegalArgumentException e) {
				logger.warning(e.getMessage());
			}
		}
		logger.info(String.format("Loaded %d lobbies", lobbies.size()));
	}
	
	public void closeLobbies() {
		lobbies.values().forEach(l -> l.reset(true));
	}
}
