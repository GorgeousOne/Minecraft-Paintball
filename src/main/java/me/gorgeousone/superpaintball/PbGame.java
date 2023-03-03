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
	
	private final Map<UUID, Long> shootCooldowns;
	private BukkitRunnable cooldownTimer;
	//	private Arena arena
	//	private long startTime
	//	private bool isRunning;
	
	private Scoreboard gameBoard;
	private Objective aliveObj;
	
	public PbGame(GameHandler gameHandler, JavaPlugin plugin) {
		this.plugin = plugin;
		this.gameId = UUID.randomUUID();
		this.teams = new HashMap<>();
		this.players = new HashSet<>();
		this.shootCooldowns = new HashMap<>();
		
		for (TeamType teamType : TeamType.values()) {
			teams.put(teamType, new PbTeam(teamType, this, gameHandler));
		}
		start();
	}
	
	public UUID getId() {
		return gameId;
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
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, .5f, .75f);
					}
				}
			}
		};
		cooldownTimer.runTaskTimer(plugin, 0, 1);
		createScoreboard();
	}
	
	public void addPlayer(UUID playerId, TeamType teamType) {
		players.add(playerId);
		teams.get(teamType).addPlayer(playerId);
	}
	
	public void removePlayer(UUID playerId) {
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
		}
		
		for (UUID playerId : players) {
			Player player = Bukkit.getPlayer(playerId);
			player.setScoreboard(gameBoard);
		}
		
		for (int i = 1; i <= 3 * teams.size() + 1; i += 3) {
			Score blank = aliveObj.getScore(pad(' ', i));
			blank.setScore(i);
		}
		updateAliveScores();
	}
	
	private void updateAliveScores() {
		int i = 2;
		
		for (TeamType teamType : teams.keySet()) {
			PbTeam team = teams.get(teamType);
			Score teamName = aliveObj.getScore("" + teamType.prefixColor + ChatColor.BOLD + teamType.displayName);
			Score teamScore = aliveObj.getScore("" + ChatColor.BOLD + team.getAlivePlayers().size() + " Alive" + pad(' ', i));
			
			teamName.setScore(i + 1);
			teamScore.setScore(i);
			i += 3;
		}
	}
	
	private String pad(char c, int n) {
		return new String(new char[n]).replace('\0', c);
	}
}
