package me.gorgeousone.superpaintball.game;

import me.gorgeousone.superpaintball.util.BackupUtil;
import me.gorgeousone.superpaintball.arena.PbArena;
import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.equipment.*;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.team.PbTeam;
import me.gorgeousone.superpaintball.util.ConfigUtil;
import me.gorgeousone.superpaintball.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PbLobby {

	private static final int MIN_PLAYERS = 2;
	private static final int MAX_PLAYERS = 16;
	private static final int COUNTDOWN_SECONDS = 10;

	private final JavaPlugin plugin;
	private final PbLobbyHandler lobbyHandler;
	private final PbKitHandler kitHandler;
	private final String name;
	private Location spawnPos;
	private final List<PbArena> arenas;
	private final PbGame game;
	private final TeamQueue teamQueue;
	private final Equipment equipment;
	private final PbCountdown countdown;
	
	public PbLobby(String name, Location spawnPos, JavaPlugin plugin, PbLobbyHandler lobbyHandler, PbKitHandler kitHandler) {
		this.lobbyHandler = lobbyHandler;
		this.kitHandler = kitHandler;
		this.plugin = plugin;

		this.name = name;
		this.spawnPos = LocationUtil.cleanSpawn(spawnPos);

		this.arenas = new LinkedList<>();
		this.teamQueue = new TeamQueue();
		this.game = new PbGame(plugin, kitHandler, this::returnToLobby);
		
		this.equipment = new LobbyEquipment(teamQueue::onQueueForTeam, this::onSelectKit, kitHandler);
		this.countdown = new PbCountdown(COUNTDOWN_SECONDS, this::onAnnounceTime, this::startGame, plugin);
	}

	public String getName() {
		return name;
	}

	public Location getSpawnPos() {
		return spawnPos;
	}

	public Equipment getEquip() {
		return game.isRunning() ? game.getEquip() : equipment;
	}
	
	public PbGame getGame() {
		return game;
	}
	
	public boolean hasPlayer(UUID playerId) {
		return game.hasPlayer(playerId);
	}
	
	public void joinPlayer(Player player) {
		UUID playerId = player.getUniqueId();
		
		if (game.hasPlayer(playerId)) {
			throw new IllegalArgumentException(String.format("You already are in lobby '%s'.", name));
		}
		if (game.size() >= MAX_PLAYERS) {
			throw new IllegalStateException(String.format("Lobby '%s' is full!", name));
		}
		game.joinPlayer(playerId);
		BackupUtil.saveBackup(player, lobbyHandler.getExitSpawn(), plugin);
		player.setGameMode(GameMode.ADVENTURE);
		player.sendMessage(String.format("Joined lobby '%s'.", name));
		
		//TODO if game running, join as spectator?
		player.teleport(spawnPos);
		equipment.equip(player);
		
		if (game.size() == MIN_PLAYERS) {
			countdown.start();
		}
	}
	
	public void removePlayer(Player player) {
		UUID playerId = player.getUniqueId();

		if (!game.hasPlayer(playerId)) {
			throw new IllegalArgumentException("Can't remove player with id: " + playerId + ". They are not in this game");
		}
		game.removePlayer(playerId);
		BackupUtil.loadBackup(player, plugin);
		player.sendMessage(String.format("You left lobby '%s'.", name));
		
		if (!game.isRunning() && game.size() < MIN_PLAYERS) {
			countdown.cancel();
			game.allPlayers(p -> p.sendMessage("Not enough players to start the game."));
		}
	}
	
	public PbTeam getTeam(UUID playerId) {
		return game.getTeam(playerId);
	}
	
	private void onSelectKit(SlotClickEvent event) {
		kitHandler.openKitSelector(event.getPlayer());
	}
	
	public List<PbArena> getArenas() {
		return arenas;
	}
	
	public void linkArena(PbArena arena) {
		if (arenas.contains(arena)) {
			throw new IllegalArgumentException(String.format("Arena '%s' already linked to this lobby!", arena.getName()));
		}
		arenas.add(arena);
	}
	
	public void unlinkArena(PbArena arena) {
		if (arenas.contains(arena)) {
			throw new IllegalArgumentException(String.format("Arena '%s' is not linked to this lobby!", arena.getName()));
		}
		arenas.remove(arena);
	}
	
	private void onAnnounceTime(int secondsLeft) {
		game.allPlayers(p -> p.sendMessage(String.format("Game starts in %d seconds.", secondsLeft)));
	}
	
	public void startGame() {
		if (game.getState() != GameState.IDLING) {
			throw new IllegalStateException("The game is already running.");
		}
		PbArena arenaToPlay = pickArena();
		arenaToPlay.assertIsPlayable();
		arenaToPlay.reset();
		game.start(arenaToPlay, teamQueue);
	}
	
	private PbArena pickArena() {
		if (arenas.isEmpty()) {
			throw new IllegalStateException(String.format(
					"Lobby '%s' cannot start a game because no arenas to play are linked to it. /pb link '%s' <arena name>", name, name));
		}
		//TODO take in account votes and/or pick different arena than last time
		return arenas.get((int) (Math.random() * arenas.size()));
	}
	
	public void returnToLobby() {
		game.allPlayers(p -> {
			p.teleport(spawnPos);
			getEquip().equip(p);
		});
		if (game.size() >= MIN_PLAYERS) {
			countdown.start();
		}
	}
	
	public void reset() {
		game.allPlayers(p -> {
			BackupUtil.loadBackup(p, plugin);
			p.sendMessage(String.format("Lobby '%s' closed.", name));
		});
		game.reset();
	}
	
	public void toYml(ConfigurationSection parentSection) {
		ConfigurationSection section = parentSection.createSection(name);
		section.set("spawn", ConfigUtil.spawnToYmlString(spawnPos));
		List<String> arenaNames = arenas.stream().map(PbArena::getName).collect(Collectors.toList());
		section.set("arenas", arenaNames);
	}
	
	public static PbLobby fromYml(
			String name,
			ConfigurationSection parentSection,
			JavaPlugin plugin,
			PbLobbyHandler lobbyHandler,
			PbArenaHandler arenaHandler,
			PbKitHandler kitHandler) {
		ConfigurationSection section = parentSection.getConfigurationSection(name);

		try {
			ConfigUtil.assertKeyExists(section, "spawn");
			ConfigUtil.assertKeyExists(section, "arenas");
			Location spawnPos = ConfigUtil.spawnFromYmlString(section.getString("spawn"));
			PbLobby lobby = new PbLobby(name, spawnPos, plugin, lobbyHandler, kitHandler);

			List<String> arenaNames = section.getStringList("arenas");
			//TODO (somehow) check if arena is arelady linked
			arenaNames.forEach(n -> lobby.linkArena(arenaHandler.getArena(n)));
			Bukkit.getLogger().log(Level.INFO, String.format("'%s' loaded", name));
			return lobby;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(String.format("Could not load lobby '%s': %s", name, e.getMessage()));
		}
	}
}
