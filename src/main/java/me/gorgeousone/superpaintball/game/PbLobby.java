package me.gorgeousone.superpaintball.game;

import me.gorgeousone.superpaintball.ConfigSettings;
import me.gorgeousone.superpaintball.util.BackupUtil;
import me.gorgeousone.superpaintball.arena.PbArena;
import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.equipment.*;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.team.PbTeam;
import me.gorgeousone.superpaintball.util.ConfigUtil;
import me.gorgeousone.superpaintball.util.LocationUtil;
import me.gorgeousone.superpaintball.util.SoundUtil;
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
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PbLobby {

	private final JavaPlugin plugin;
	private final PbLobbyHandler lobbyHandler;
	private final PbKitHandler kitHandler;
	private final String name;
	private Location spawnPos;
	private final List<PbArena> arenas;
	private final PbGame game;
	private final TeamQueue teamQueue;
	private final MapVoting mapVoting;
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
		this.mapVoting = new MapVoting();
		this.game = new PbGame(plugin, kitHandler, this::returnToLobby);
		
		this.equipment = new LobbyEquipment(teamQueue::onQueueForTeam, this::onMapVote, this::onSelectKit, this::onQuit, kitHandler);
		this.countdown = new PbCountdown(ConfigSettings.COUNTDOWN_SECS, this::onAnnounceTime, this::onCountdownEnd, plugin);
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
		if (game.size() >= ConfigSettings.MAX_PLAYERS) {
			throw new IllegalStateException(String.format("Lobby '%s' is full!", name));
		}
		game.joinPlayer(playerId);
		BackupUtil.saveBackup(player, lobbyHandler.getExitSpawn(), plugin);
		player.setGameMode(GameMode.ADVENTURE);
		player.sendMessage(String.format("Joined lobby '%s'.", name));
		
		//TODO if game running, join as spectator?
		player.teleport(spawnPos);
		equipment.equip(player);
		
		if (game.size() == ConfigSettings.MIN_PLAYERS) {
			countdown.start();
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
		game.removePlayer(playerId);
		BackupUtil.loadBackup(player, plugin);
		player.sendMessage(String.format("You left lobby '%s'.", name));
		
		if (!game.isRunning() && game.size() < ConfigSettings.MIN_PLAYERS) {
			countdown.cancel();
			game.allPlayers(p -> p.sendMessage("Not enough players to start the game."));
		}
	}
	
	public PbTeam getTeam(UUID playerId) {
		return game.getTeam(playerId);
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
		
		if (arenaIdx >= arenas.size()) {
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
		game.allPlayers(p -> {
			p.sendMessage(String.format("Game starts in %d seconds.", secondsLeft));
			p.playSound(p.getLocation(), SoundUtil.COUNTDOWN_SOUND, .5f, 1f);
		});
	}
	
	private void onCountdownEnd() {
		try {
			startGame();
		} catch (IllegalArgumentException | IllegalStateException e) {
			game.allPlayers(p -> p.sendMessage(e.getMessage()));
		}
	}
	
	public void startGame() {
		if (game.getState() != GameState.IDLING) {
			throw new IllegalStateException("The game is already running.");
		}
		countdown.cancel();
		
		if (arenas.isEmpty()) {
			countdown.start();
			throw new IllegalStateException(String.format(
					"Lobby '%s' cannot start a game because no arenas to play are linked to it. /pb link '%s' <arena name>", name, name));
		}
		PbArena arenaToPlay = mapVoting.pickArena(arenas);
		arenaToPlay.assertIsPlayable();
		arenaToPlay.reset();
		game.start(arenaToPlay, teamQueue);
	}
	
	public void returnToLobby() {
		game.allPlayers(p -> {
			p.teleport(spawnPos);
			equipment.equip(p);
		});
		if (game.size() >= ConfigSettings.MIN_PLAYERS) {
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
