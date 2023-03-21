package me.gorgeousone.superpaintball.game;

import me.gorgeousone.superpaintball.GameBoard;
import me.gorgeousone.superpaintball.arena.PbArena;
import me.gorgeousone.superpaintball.equipment.Equipment;
import me.gorgeousone.superpaintball.equipment.IngameEquipment;
import me.gorgeousone.superpaintball.equipment.SlotClickEvent;
import me.gorgeousone.superpaintball.kit.KitType;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.team.PbTeam;
import me.gorgeousone.superpaintball.team.TeamType;
import me.gorgeousone.superpaintball.util.LocationUtil;
import me.gorgeousone.superpaintball.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PbGame {
	
	private final JavaPlugin plugin;
	private final PbKitHandler kitHandler;
	private GameState state;
	private final Set<UUID> players;
	private final Map<TeamType, PbTeam> teams;
	private GameBoard gameBoard;
	private final Runnable onGameEnd;
	
	private final Equipment equipment;
	
	public PbGame(JavaPlugin plugin, PbKitHandler kitHandler, Runnable onGameEnd) {
		this.plugin = plugin;
		this.kitHandler = kitHandler;
		this.onGameEnd = onGameEnd;
		
		this.state = GameState.IDLING;
		this.players = new HashSet<>();
		this.teams = new HashMap<>();
		
		for (TeamType teamType : TeamType.values()) {
			teams.put(teamType, new PbTeam(teamType, this, this.kitHandler));
		}
		equipment = new IngameEquipment(this::onShoot, this::onThrowWaterBomb, kitHandler);
	}
	
	public Equipment getEquip() {
		return equipment;
	}
	
	public int size() {
		return players.size();
	}
	
	public boolean hasPlayer(UUID playerId) {
		return players.contains(playerId);
	}
	
	public void joinPlayer(UUID playerId) {
		players.add(playerId);
	}
	
	public void removePlayer(Player player) {
		UUID playerId = player.getUniqueId();
		
		if (!players.contains(playerId)) {
			return;
		}
		getTeam(playerId).removePlayer(playerId);
		updateAliveScores();
	}
	
	public GameState getState() {
		return state;
	}
	
	public boolean isRunning() {
		return state != GameState.IDLING;
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
	
	public void start(PbArena arenaToPlay, TeamQueue teamQueue) {
		if (state != GameState.IDLING) {
			throw new IllegalStateException("The game is already running.");
		}
		teamQueue.assignTeams(players, teams);
		
		teams.values().forEach(t -> t.startGame(arenaToPlay.getSpawns(t.getType())));
		state = GameState.COUNTING_DOWN;
		createScoreboard();
		startCountdown();
	}
	
	private void createScoreboard() {
		gameBoard = new GameBoard("alive", 3 * teams.size() + 1);
		gameBoard.setTitle("" + ChatColor.GOLD + ChatColor.BOLD + "SUPER PAINTBALL");
		int i = 2;
		
		for (TeamType teamType : teams.keySet()) {
			PbTeam team = teams.get(teamType);
			Team boardTeam = gameBoard.addTeam(teamType.name(), team.getType().prefixColor + "");
			boardTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
			boardTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
			
			for (UUID playerId : team.getPlayers()) {
				Player player = Bukkit.getPlayer(playerId);
				boardTeam.addEntry(player.getName());
			}
			gameBoard.setLine(i, "" + ChatColor.BOLD + team.getAlivePlayers().size() + " Alive" + StringUtil.pad(i));
			gameBoard.setLine(i + 1, teamType.displayName);
			i += 3;
		}
		allPlayers(p -> gameBoard.addPlayer(p));
	}
	
	private void startCountdown() {
		allPlayers(p -> p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, .5f, 1f));
		BukkitRunnable countdown = new BukkitRunnable() {
			int time = 2 * 10;
			
			@Override
			public void run() {
				time -= 1;
				
				if (time <= 0) {
					allPlayers(p -> p.playSound(p.getLocation(), LocationUtil.GAME_START_SOUND, 1.5f, 2f));
					state = GameState.RUNNING;
					this.cancel();
					return;
				}
				if (time % 10 == 0) {
					allPlayers(p -> p.playSound(p.getLocation(), LocationUtil.RELOAD_SOUND, .5f, 1f));
				}
			}
		};
		countdown.runTaskTimer(plugin, 0, 2);
	}
	
	private void onShoot(SlotClickEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		
		if (!getTeam(playerId).isAlive(playerId)) {
			event.setCancelled(true);
			return;
		}
		KitType kitType = kitHandler.getKitType(playerId);
		PbKitHandler.getKit(kitType).launchShot(player, getTeam(playerId), players.stream().map(Bukkit::getPlayer).collect(Collectors.toList()));
	}
	
	private void onThrowWaterBomb(SlotClickEvent event) {
		UUID playerId = event.getPlayer().getUniqueId();
		
		if (!getTeam(playerId).isAlive(playerId)) {
			event.setCancelled(true);
		}
	}
	
	public void broadcastKill(Player target, Player shooter) {
		TeamType targetTeam = getTeam(target.getUniqueId()).getType();
		TeamType shooterTeam = getTeam(shooter.getUniqueId()).getType();
		String message = targetTeam.prefixColor + target.getDisplayName() + ChatColor.RESET + " was painted by " + shooterTeam.prefixColor + shooter.getDisplayName();
		allPlayers(p -> p.sendMessage(message));
	}
	
	public void onTeamKill(PbTeam killedTeam) {
		if (state != GameState.RUNNING) {
			return;
		}
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
		scheduleRestart();
	}
	
	private void announceWinners(TeamType winningTeam) {
		state = GameState.OVER;
		
		if (winningTeam != null) {
			allPlayers(p -> p.sendTitle(winningTeam.displayName + " won!", ""));
		} else {
			allPlayers(p -> p.sendTitle("It's a draw?", ""));
		}
	}
	
	private void scheduleRestart() {
		BukkitRunnable restartTimer = new BukkitRunnable() {
			@Override
			public void run() {
				state = GameState.IDLING;
				teams.values().forEach(PbTeam::reset);
				allPlayers(p -> {
					gameBoard.removePlayer(p);
					onGameEnd.run();
					//TODO cancel spectator states
				});
			}
		};
		restartTimer.runTaskLater(plugin, 3*20);
	}
	
	public void updateAliveScores() {
		if (state == GameState.IDLING) {
			return;
		}
		int i = 2;
		
		for (TeamType teamType : teams.keySet()) {
			PbTeam team = teams.get(teamType);
			gameBoard.setLine(i, "" + ChatColor.BOLD + team.getAlivePlayers().size() + " Alive" + StringUtil.pad(i));
			i += 3;
		}
	}
	
	public void hidePlayer(Player player) {
		allPlayers(p -> p.hidePlayer(player));
	}
	
	public void showPlayer(Player player) {
		allPlayers(p -> p.showPlayer(player));
	}
	
	public void reset() {
		teams.values().forEach(PbTeam::reset);
		
		if (gameBoard != null) {
			allPlayers(p -> gameBoard.removePlayer(p));
		}
		players.clear();
	}
	
	public void allPlayers(Consumer<Player> consumer) {
		for (UUID playerId : players) {
			consumer.accept(Bukkit.getPlayer(playerId));
		}
	}
}
