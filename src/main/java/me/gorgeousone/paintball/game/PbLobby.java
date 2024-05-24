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
	private final PbCountdown countdown;
	private Equipment equipment;
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
	
	/**
	 * Updates the UI of the lobby board and the equipment of all players in the lobby.
	 * Item names will only update after rejoining the lobby (too complicated to update
	 */
	public void updateUi() {
		this.equipment = new LobbyEquipment(teamQueue::onQueueForTeam, this::onMapVote, this::onSelectKit, this::onQuit, kitHandler);
		createGameBoard();
		updateLobbyBoard();
		
		if (game.isRunning()) {
			game.updateUi();
		}else {
			game.allPlayers(p -> {
				board.addPlayer(p);
				equipment.updateNames(p);
			});
		}
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
	
	/**
	 * Adds a player to the lobby and teleports them to the join spawn, saves and replaces their inventory.
	 */
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
		LocationUtil.tpTick(player, joinSpawn, plugin);
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
		removePlayer(slotClickEvent.getPlayer(), true, false);
	}
	
	public void removePlayer(Player player, boolean doTeleport, boolean isImmediate) {
		UUID playerId = player.getUniqueId();
		
		if (!game.hasPlayer(playerId)) {
			throw new IllegalArgumentException("Can't remove player with id: " + playerId + ". They are not in this game");
		}
		teamQueue.removePlayer(playerId);
		game.removePlayer(playerId);
		ItemUtil.loadPlayerBackup(player, plugin, doTeleport, isImmediate);
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
			game.allPlayers(p -> StringUtil.msgPlain(p, e.getMessage()));
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
			LocationUtil.tpTick(p, joinSpawn, plugin);
			p.getInventory().clear();
			equipment.equip(p);
			board.addPlayer(p);
		});
		if (game.size() >= ConfigSettings.MIN_PLAYERS) {
			countdown.start(ConfigSettings.COUNTDOWN_SECS);
		}
		updateLobbyBoard();
	}
	
	public void reset(boolean isImmediate) {
		game.allPlayers(p -> {
			ItemUtil.loadPlayerBackup(p, plugin, true, isImmediate);
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
	
	/**
	 * Updates the countdown in the title of the lobby board and current player count.
	 */
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
