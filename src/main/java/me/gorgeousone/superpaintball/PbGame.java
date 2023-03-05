package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.kit.AbstractKit;
import me.gorgeousone.superpaintball.team.PbTeam;
import me.gorgeousone.superpaintball.team.TeamType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PbGame {
	
	private final JavaPlugin plugin;
	private final UUID gameId;
	private final Map<TeamType, PbTeam> teams;
	private final Set<UUID> players;
	private GameState state;
	
	private final Map<UUID, Long> shootCooldowns;
	private BukkitRunnable cooldownTimer;
	
	//	private Arena arena
	//	private long startTime
	//	private bool isRunning;
	
	private Scoreboard gameBoard;
	private Objective aliveObj;
	private Map<TeamType, String> aliveEntries;
	
	
	public PbGame(GameHandler gameHandler, JavaPlugin plugin) {
		this.plugin = plugin;
		this.gameId = UUID.randomUUID();
		this.teams = new HashMap<>();
		this.players = new HashSet<>();
		this.shootCooldowns = new HashMap<>();
		this.aliveEntries = new HashMap<>();
		
		for (TeamType teamType : TeamType.values()) {
			teams.put(teamType, new PbTeam(teamType, this, gameHandler));
		}
		start();
	}
	
	public UUID getId() {
		return gameId;
	}
	
	public GameState getState() {
		return state;
	}
	
	public void start() {
		for (PbTeam team : teams.values()) {
			team.start();
		}
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
		createScoreboard();
		state = GameState.RUNNING;
	}
	
	public void addPlayer(UUID playerId, TeamType teamType) {
		players.add(playerId);
		teams.get(teamType).addPlayer(playerId);
	}
	
	public void removePlayer(UUID playerId) {
		if (!players.contains(playerId)) {
			throw new IllegalArgumentException("Can't remove player with id: " + playerId + ". They are not in this game");
		}
		PbTeam team = getTeam(playerId);
		team.removePlayer(playerId);
		players.remove(playerId);
		updateAliveScores();
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
		aliveObj = gameBoard.registerNewObjective("alive","dummy");
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
			Score teamName = aliveObj.getScore("" + teamType.prefixColor + ChatColor.BOLD + teamType.displayName);
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
			sendTitle(winningTeam.prefixColor + winningTeam.displayName + " won!");
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
}

