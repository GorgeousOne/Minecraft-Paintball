package me.gorgeousone.superpaintball.game;

import me.gorgeousone.superpaintball.arena.PbArena;
import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.kit.AbstractKit;
import me.gorgeousone.superpaintball.team.PbTeam;
import me.gorgeousone.superpaintball.team.TeamType;
import me.gorgeousone.superpaintball.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PbLobby {

	private final JavaPlugin plugin;
	private final PbLobbyHandler lobbyHandler;
	private final String name;
	private Location spawnPos;
	private final List<PbArena> arenas;
	private final Map<TeamType, PbTeam> teams;
	private final Set<UUID> players;
	private GameState state;

	private final Map<UUID, Long> shootCooldowns;
	private BukkitRunnable cooldownTimer;

	private Scoreboard gameBoard;
	private Objective aliveObj;
	private Map<TeamType, String> aliveEntries;

	public PbLobby(String name, Location spawnPos, JavaPlugin plugin, PbLobbyHandler lobbyHandler, PbKitHandler kitHandler) {
		this.name = name;
		this.spawnPos = GameUtil.cleanSpawn(spawnPos);
		this.plugin = plugin;
		this.lobbyHandler = lobbyHandler;

		this.arenas = new LinkedList<>();
		this.teams = new HashMap<>();
		this.players = new HashSet<>();
		this.shootCooldowns = new HashMap<>();
		this.aliveEntries = new HashMap<>();

		for (TeamType teamType : TeamType.values()) {
			teams.put(teamType, new PbTeam(teamType, this, lobbyHandler, kitHandler));
		}
	}

	public String getName() {
		return name;
	}

	public Location getSpawnPos() {
		return spawnPos;
	}

	public GameState getState() {
		return state;
	}

	public void start() {
		PbArena arenaToPlay = pickArena();
		arenaToPlay.assertIsPlayable();

		for (PbTeam team : teams.values()) {
			team.start(arenaToPlay.getSpawns(team.getType()));
		}

		startCooldownTimer();
		createScoreboard();
		state = GameState.RUNNING;
	}

	private PbArena pickArena() {
		if (arenas.isEmpty()) {
			throw new IllegalStateException(String.format(
					"Lobby '%s' cannot start a game because no arenas to play are linked to it. /pb link '%s' <arena name>", name, name));
		}
		//TODO take in account votes and/or pick different arena than last time
		return arenas.get((int) (Math.random() * arenas.size()));
	}

	private void startCooldownTimer() {
		cooldownTimer = new BukkitRunnable() {
			@Override
			public void run() {
				long currentMillis = System.currentTimeMillis();

				for (UUID playerId : new HashSet<>(shootCooldowns.keySet())) {
					long cooldown = shootCooldowns.get(playerId);

					if (cooldown < currentMillis) {
						shootCooldowns.remove(playerId);
						Player player = Bukkit.getPlayer(playerId);
						player.playSound(player.getLocation(), GameUtil.RELOAD_SOUND, .2f, 1f);
					}
				}
			}
		};
		cooldownTimer.runTaskTimer(plugin, 0, 1);
	}

	public void joinPlayer(Player player, TeamType teamType) {
		UUID playerId = player.getUniqueId();

		if (players.contains(playerId)) {
			throw new IllegalArgumentException(String.format("You already are in lobby '%s'.", name));
		}
		//TODO if game started, join as spectator if not full?
		player.teleport(spawnPos);
		player.sendMessage(String.format("Joined lobby '%s'.", name));

		players.add(playerId);
		teams.get(teamType).addPlayer(player);
	}

	public void removePlayer(Player player) {
		UUID playerId = player.getUniqueId();

		if (!players.contains(playerId)) {
			throw new IllegalArgumentException("Can't remove player with id: " + playerId + ". They are not in this game");
		}
		PbTeam team = getTeam(playerId);
		team.removePlayer(playerId);
		players.remove(playerId);
		updateAliveScores();

		player.teleport(lobbyHandler.getExitSpawn());
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		player.sendMessage(String.format("You left lobby '%s'.", name));
		//TODO reset inventory & gamemode
	}

	public void kickPlayers() {
		for (PbTeam team : teams.values()) {
			team.kickPlayers();
		}
		Location exitSpawn = lobbyHandler.getExitSpawn();
		for (UUID playerId : players) {
			Player player = Bukkit.getPlayer(playerId);
			player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
			player.teleport(exitSpawn);
			player.sendMessage(String.format("Lobby '%s' closed.", name));
			//TODO reset invnetory and gamemode
		}
		players.clear();
		state = GameState.LOBBYING;
	}

	public boolean hasPlayer(UUID playerId) {
		return players.contains(playerId);
	}

	public PbTeam getTeam(UUID playerId) {
		for (PbTeam team : teams.values()) {
			if (team.hasPlayer(playerId)) {
				return team;
			}
		}
		return null;
	}

	public Collection<PbTeam> getTeams() {
		return teams.values();
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

	public List<PbArena> getArenas() {
		return arenas;
	}

	public void launchShot(Player player, AbstractKit kit) {
		UUID playerId = player.getUniqueId();

		if (shootCooldowns.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
			return;
		}
		long cooldownTicks = kit.launchShot(player, getTeam(playerId));

		if (cooldownTicks > 0) {
			shootCooldowns.put(playerId, System.currentTimeMillis() + cooldownTicks * 50);
		}
	}

	public void hidePlayer(Player player) {
		for (UUID playerId : players) {
			Player otherPlayer = Bukkit.getPlayer(playerId);
			otherPlayer.hidePlayer(plugin, player);
		}
	}

	public void showPlayer(Player player) {
		for (UUID playerId : players) {
			Player otherPlayer = Bukkit.getPlayer(playerId);
			otherPlayer.showPlayer(plugin, player);
		}
	}

	private void createScoreboard() {
		gameBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		aliveObj = gameBoard.registerNewObjective("alive", "dummy");
		aliveObj.setDisplaySlot(DisplaySlot.SIDEBAR);
		aliveObj.setDisplayName("" + ChatColor.GOLD + ChatColor.BOLD + "SUPER PAINTBALL");

		Score blank = aliveObj.getScore("");
		blank.setScore(1);
		int i = 2;

		for (TeamType teamType : teams.keySet()) {
			PbTeam team = teams.get(teamType);
			Team boardTeam = gameBoard.registerNewTeam(teamType.name());
			boardTeam.setPrefix(team.getType().prefixColor + "");
			boardTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
			boardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);

			for (UUID playerId : team.getPlayers()) {
				Player player = Bukkit.getPlayer(playerId);
				boardTeam.addEntry(player.getName());
			}
			Score teamScore = aliveObj.getScore("" + ChatColor.BOLD + team.getAlivePlayers().size() + " Alive" + pad(' ', i));
			Score teamName = aliveObj.getScore(teamType.displayName);
			blank = aliveObj.getScore(pad(' ', i));
			aliveEntries.put(teamType, teamScore.getEntry());

			teamScore.setScore(i);
			teamName.setScore(i + 1);
			blank.setScore(i + 2);
			i += 3;
		}
		for (UUID playerId : players) {
			Player player = Bukkit.getPlayer(playerId);
			player.setScoreboard(gameBoard);
		}
	}

	public void updateAliveScores() {
		int i = 2;

		for (TeamType teamType : teams.keySet()) {
			PbTeam team = teams.get(teamType);
			gameBoard.resetScores(aliveEntries.get(teamType));
			Score teamScore = aliveObj.getScore("" + ChatColor.BOLD + team.getAlivePlayers().size() + " Alive" + pad(' ', i));
			teamScore.setScore(i);
			aliveEntries.put(teamType, teamScore.getEntry());
			i += 3;
		}
	}

	public void onTeamKill(PbTeam killedTeam) {
		TeamType leftTeam = null;

		for (PbTeam team : teams.values()) {
			if (team.getAlivePlayers().size() > 0) {
				if (leftTeam != null) {
					return;
				}
				leftTeam = team.getType();
			}
		}
		announceWinners(leftTeam);
	}

	private void announceWinners(TeamType winningTeam) {
		state = GameState.OVER;

		if (winningTeam != null) {
			sendTitle(winningTeam.displayName + " won!");
		} else {
			sendTitle("It's a draw?");
		}
	}

	private void sendTitle(String text) {
		for (UUID playerId : players) {
			Player player = Bukkit.getPlayer(playerId);
			player.sendTitle(text, "");
		}
	}

	private String pad(char c, int n) {
		return new String(new char[n]).replace('\0', c);
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
