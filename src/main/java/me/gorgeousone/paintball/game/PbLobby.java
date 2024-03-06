package me.gorgeousone.paintball.game;

import me.gorgeousone.paintball.ConfigSettings;
import me.gorgeousone.paintball.GameBoard;
import me.gorgeousone.paintball.Message;
import me.gorgeousone.paintball.arena.PbArena;
import me.gorgeousone.paintball.arena.PbArenaHandler;
import me.gorgeousone.paintball.equipment.Equipment;
import me.gorgeousone.paintball.equipment.LobbyEquipment;
import me.gorgeousone.paintball.equipment.SlotClickEvent;
import me.gorgeousone.paintball.kit.PbKitHandler;
import me.gorgeousone.paintball.util.ConfigUtil;
import me.gorgeousone.paintball.util.ItemUtil;
import me.gorgeousone.paintball.util.LocationUtil;
import me.gorgeousone.paintball.util.SoundUtil;
import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Class to manage players joined in a lobby and to run a game start countdown when enough players are in the lobby.
 */
public class PbLobby {
	
	private static final Set<Integer> ANNOUNCEMENT_INTERVALS = Set.of(300, 240, 180, 120, 60, 30, 20, 10, 3, 2, 1);
	
	private final JavaPlugin plugin;
	private final PbLobbyHandler lobbyHandler;
	private final PbKitHandler kitHandler;
	private final String name;
	private Location joinSpawn;
	private Location exitSpawn;
	private final List<PbArena> arenas;
	private final PbGame game;
	private final TeamQueue teamQueue;
	private final MapVoting mapVoting;
	private final Equipment equipment;
	private final PbCountdown countdown;
	private GameBoard board;
	
	public PbLobby(String name,
			Location joinSpawn,
			JavaPlugin plugin,
			PbLobbyHandler lobbyHandler,
			PbKitHandler kitHandler) {
		this.lobbyHandler = lobbyHandler;
		this.kitHandler = kitHandler;
		this.plugin = plugin;
		
		this.name = name;
		this.joinSpawn = LocationUtil.cleanSpawn(joinSpawn);
		
		this.arenas = new LinkedList<>();
		this.teamQueue = new TeamQueue();
		this.mapVoting = new MapVoting();
		this.game = new PbGame(plugin, kitHandler, this::returnToLobby);
		
		this.equipment = new LobbyEquipment(teamQueue::onQueueForTeam, this::onMapVote, this::onSelectKit, this::onQuit, kitHandler);
		this.countdown = new PbCountdown(this::onCountdownTick, this::onCountdownEnd, plugin);
		createGameBoard();
	}
	
	public String getName() {
		return name;
	}
	
	public Location getJoinSpawn() {
		return joinSpawn;
	}
	
	public void setJoinSpawn(Location pos) {
		this.joinSpawn = LocationUtil.cleanSpawn(pos);
		lobbyHandler.saveLobby(this);
	}
	
	public Location getExitSpawn() {
		return exitSpawn != null ? exitSpawn : lobbyHandler.getExitSpawn();
	}
	
	public void setExitSpawn(Location pos) {
		this.exitSpawn = LocationUtil.cleanSpawn(pos);
		lobbyHandler.saveLobby(this);
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
			throw new IllegalArgumentException(Message.LOBBY_ALREADY_JOINED.format(name));
		}
		//TODO join as spectator?
		if (game.isRunning()) {
			throw new IllegalStateException(Message.LOBBY_RUNNING.format());
		}
		if (game.size() >= ConfigSettings.MAX_PLAYERS) {
			throw new IllegalStateException(Message.LOBBY_FULL.format(name));
		}
		ItemUtil.saveInventory(player, getExitSpawn(), plugin);
		player.setGameMode(GameMode.ADVENTURE);
		Message.LOBBY_YOU_JOIN.send(player, name);
		player.teleport(joinSpawn);
		equipment.equip(player);
		board.addPlayer(player);
		game.joinPlayer(playerId);
		updateLobbyBoard();
		
		if (game.size() >= ConfigSettings.MIN_PLAYERS && !countdown.isRunning()) {
			countdown.start(ConfigSettings.COUNTDOWN_SECS);
		}
		if (arenas.size() == 0) {
			Message.LOBBY_ARENA_MISSING.send(player, name);
		}
	}
	
	private void onQuit(SlotClickEvent slotClickEvent) {
		removePlayer(slotClickEvent.getPlayer());
	}
	
	public void removePlayer(Player player) {
		UUID playerId = player.getUniqueId();
		
		if (!game.hasPlayer(playerId)) {
			throw new IllegalArgumentException("Can't remove player with id: " + playerId + ". They are not in this game");
		}
		teamQueue.removePlayer(playerId);
		game.removePlayer(playerId);
		ItemUtil.loadInventory(player, plugin);
		Message.PLAYER_LEAVE.send(player, name);
		if (!game.isRunning()) {
			board.removePlayer(player);
			updateLobbyBoard();
		}
	}
	
	private void onSelectKit(SlotClickEvent event) {
		kitHandler.openKitSelectUI(event.getPlayer());
	}
	
	private void onMapVote(SlotClickEvent slotClickEvent) {
		Player player = slotClickEvent.getPlayer();
		MapVoting.openMapVoteUI(player, getArenas(), arenas.indexOf(mapVoting.getVote(player.getUniqueId())));
	}
	
	public void addMapVote(Player player, Inventory mapVoter, int arenaIdx) {
		UUID playerId = player.getUniqueId();
		
		if (arenaIdx < 0 || arenaIdx >= arenas.size()) {
			return;
		}
		PbArena lastVote = mapVoting.getVote(playerId);
		mapVoting.toggleVote(playerId, arenas.get(arenaIdx));
		MapVoting.toggleMapVote(mapVoter, arenaIdx, arenas.indexOf(lastVote));
		player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f);
	}
	
	public List<PbArena> getArenas() {
		return new LinkedList<>(arenas);
	}
	
	public void linkArena(PbArena arena) {
		if (arenas.contains(arena)) {
			throw new IllegalArgumentException(Message.ARENA_ALREADY_LINKED.format(arena.getName(), name));
		}
		arenas.add(arena);
		
		if (!game.isRunning() && !countdown.isRunning() && game.size() >= ConfigSettings.MIN_PLAYERS) {
			countdown.start(ConfigSettings.COUNTDOWN_SECS);
		}
	}
	
	public void unlinkArena(PbArena arena) {
		if (!arenas.contains(arena)) {
			throw new IllegalArgumentException(Message.ARENA_NOT_LINKED.format(arena.getName()));
		}
		arenas.remove(arena);
		lobbyHandler.saveLobby(this);
	}
	
	private void onCountdownTick(int secondsLeft) {
		updateLobbyBoard();
		
		if (ANNOUNCEMENT_INTERVALS.contains(secondsLeft)) {
			game.allPlayers(p -> {
				Message.LOBBY_COUNTDOWN.send(p, secondsLeft);
				p.playSound(p.getLocation(), SoundUtil.COUNTDOWN_SOUND, .5f, 1f);
			});
		}
	}
	
	private void onCountdownEnd() {
		try {
			startGame();
		} catch (IllegalArgumentException | IllegalStateException e) {
			game.allPlayers(p -> StringUtil.msg(p, e.getMessage()));
		}
	}
	
	public void startGame() {
		if (arenas.isEmpty()) {
			throw new IllegalStateException(Message.LOBBY_ARENA_MISSING.format(name, name));
		}
		if (game.size() < ConfigSettings.MIN_PLAYERS) {
			throw new IllegalStateException(Message.LOBBY_UNDERFULL.format(game.size(), ConfigSettings.MIN_PLAYERS));
		}
		if (game.getState() != GameState.LOBBYING) {
			throw new IllegalStateException(Message.LOBBY_RUNNING.format());
		}
		countdown.cancel();
		PbArena arenaToPlay = mapVoting.pickArena(arenas);
		arenaToPlay.assertIsPlayable();
		game.start(arenaToPlay, teamQueue, ConfigSettings.PLAYER_HEALTH_POINTS);
	}
	
	public void returnToLobby() {
		game.allPlayers(p -> {
			p.teleport(joinSpawn);
			p.getInventory().clear();
			equipment.equip(p);
			board.addPlayer(p);
		});
		if (game.size() >= ConfigSettings.MIN_PLAYERS) {
			countdown.start(ConfigSettings.COUNTDOWN_SECS);
		}
		updateLobbyBoard();
	}
	
	public void reset() {
		game.allPlayers(p -> {
			ItemUtil.loadInventory(p, plugin);
			Message.LOBBY_CLOSE.send(p, name);
		});
		game.reset();
	}
	
	private void createGameBoard() {
		board = new GameBoard(7);
		board.setTitle(Message.UI_WAIT_FOR_PLAYERS);
		board.setLine(6, Message.UI_PLAYERS);
		board.setLine(5, game.size() + "/" + ConfigSettings.MAX_PLAYERS);
		board.setLine(3, Message.UI_LOBBY);
		board.setLine(2, name);
	}
	
	private void updateLobbyBoard() {
		if (game.size() < ConfigSettings.MIN_PLAYERS) {
			board.setTitle(Message.UI_WAIT_FOR_PLAYERS);
			
		} else {
			board.setTitle(Message.UI_COUNTDOWN.format(countdown.getSecondsLeft()));
		}
		board.setLine(5, game.size() + "/" + ConfigSettings.MAX_PLAYERS);
	}
	
	public void toYml(ConfigurationSection parentSection) {
		ConfigurationSection section = parentSection.createSection(name);
		section.set("spawn", ConfigUtil.spawnToYmlString(joinSpawn, true));
		
		if (exitSpawn != null) {
			section.set("exit", ConfigUtil.spawnToYmlString(exitSpawn, true));
		}
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
			
			if (section.contains("exit")) {
				lobby.setExitSpawn(ConfigUtil.spawnFromYmlString(section.getString("exit")));
			}
			List<String> arenaNames = section.getStringList("arenas");
			arenaNames.forEach(n -> lobbyHandler.linkArena(lobby, arenaHandler.getArena(n)));
			
			Bukkit.getLogger().log(Level.INFO, String.format("%s loaded", name));
			return lobby;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(String.format("Could not load lobby %s: %s", name, e.getMessage()));
		}
	}
}
