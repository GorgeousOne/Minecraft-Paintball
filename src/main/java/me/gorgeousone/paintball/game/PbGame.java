package me.gorgeousone.paintball.game;

import me.gorgeousone.paintball.GameBoard;
import me.gorgeousone.paintball.arena.PbArena;
import me.gorgeousone.paintball.equipment.Equipment;
import me.gorgeousone.paintball.equipment.IngameEquipment;
import me.gorgeousone.paintball.equipment.SlotClickEvent;
import me.gorgeousone.paintball.kit.KitType;
import me.gorgeousone.paintball.kit.PbKitHandler;
import me.gorgeousone.paintball.team.PbTeam;
import me.gorgeousone.paintball.team.TeamType;
import me.gorgeousone.paintball.util.SoundUtil;
import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	private PbArena arena;
	
	private GameStats gameStats;
	
	public PbGame(JavaPlugin plugin, PbKitHandler kitHandler, Runnable onGameEnd) {
		this.plugin = plugin;
		this.kitHandler = kitHandler;
		this.onGameEnd = onGameEnd;
		
		this.state = GameState.LOBBYING;
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
		String playerName = Bukkit.getOfflinePlayer(playerId).getName();
		allPlayers(p -> StringUtil.msg(p, playerName + " joined."));
	}
	
	public void removePlayer(UUID playerId) {
		if (!players.contains(playerId)) {
			return;
		}
		if (isRunning()) {
			getTeam(playerId).removePlayer(playerId);
			updateAliveScores();
		}
		players.remove(playerId);
	}
	
	public GameState getState() {
		return state;
	}
	
	public boolean isRunning() {
		return state != GameState.LOBBYING;
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
	
	public void start(PbArena arenaToPlay, TeamQueue teamQueue, int maxHealthPoints) {
		if (state != GameState.LOBBYING) {
			throw new IllegalStateException("The game is already running.");
		}
		teamQueue.assignTeams(players, teams);
		teams.values().forEach(t -> t.startGame(arenaToPlay.getSpawns(t.getType()), maxHealthPoints));

		createScoreboard();
		startCountdown();
		gameStats = new GameStats();
		
		allPlayers(p -> {
			StringUtil.msg(p, "Playing map %s!", ChatColor.WHITE + arenaToPlay.getSpacedName() + StringUtil.MSG_COLOR);
			gameStats.addPlayer(p.getUniqueId(), kitHandler.getKitType(p.getUniqueId()));
		});
		arena = arenaToPlay;
		state = GameState.COUNTING_DOWN;
	}
	
	private void createScoreboard() {
		gameBoard = new GameBoard(3 * teams.size() + 1);
		gameBoard.setTitle("" + ChatColor.GOLD + ChatColor.BOLD + "SUPER PAINTBALL");
		allPlayers(p -> gameBoard.addPlayer(p));
		int i = 2;
		
		for (TeamType teamType : teams.keySet()) {
			PbTeam team = teams.get(teamType);
			Team boardTeam = gameBoard.createTeam(teamType.name(), team.getType().prefixColor);
			
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
	}
	
	//TODO find nice wrapper class?
	private void startCountdown() {
		allPlayers(p -> p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, .5f, 1f));
		BukkitRunnable countdown = new BukkitRunnable() {
			int time = 8 * 10;
			
			@Override
			public void run() {
				if (time == 80) {
					allPlayers(p -> p.sendTitle("Shoot enemies", "to paint them"));
				} else if (time == 40) {
					allPlayers(p -> p.sendTitle("Throw water bombs", "to revive team mates"));
				}
				time -= 1;
				
				if (time <= 0) {
					setRunning();
					this.cancel();
					return;
				}
				if (time % 10 == 0) {
					allPlayers(p -> p.playSound(p.getLocation(), SoundUtil.RELOAD_SOUND, .5f, 1f));
				}
			}
		};
		countdown.runTaskTimer(plugin, 0, 2);
	}
	
	private void setRunning() {
		allPlayers(p -> p.playSound(p.getLocation(), SoundUtil.GAME_START_SOUND, 1.5f, 2f));
		state = GameState.RUNNING;
		
		for (PbTeam team : teams.values()) {
			if (team.getAlivePlayers().size() == 0) {
				onTeamKill(team);
				break;
			}
		}
	}
	
	private void onShoot(SlotClickEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		
		if (state != GameState.RUNNING || !getTeam(playerId).isAlive(playerId)) {
			event.setCancelled(true);
			return;
		}
		KitType kitType = kitHandler.getKitType(playerId);
		List<Player> coplayers = players.stream().map(Bukkit::getPlayer).collect(Collectors.toList());
		boolean didShoot = PbKitHandler.getKit(kitType).launchShot(player, getTeam(playerId), coplayers);
		
		if (didShoot) {
			gameStats.addGunShot(playerId);
		}
	}
	
	private void onThrowWaterBomb(SlotClickEvent event) {
		UUID playerId = event.getPlayer().getUniqueId();
		
		if (state != GameState.RUNNING || !getTeam(playerId).isAlive(playerId)) {
			event.setCancelled(true);
		}
	}
	
	public void damagePlayer(Player target, Player shooter, int bulletDmg) {
		PbTeam team = getTeam(target.getUniqueId());
		UUID shooterId = shooter.getUniqueId();
		
		if (!team.hasPlayer(shooterId)) {
			team.damagePlayer(target, shooter, bulletDmg);
			gameStats.addBulletHit(shooterId);
		}
	}
	
	public void healPlayer(Player target, Player healer) {
		if (state != GameState.RUNNING) {
			return;
		}
		PbTeam team = getTeam(healer.getUniqueId());
		
		if (team.hasPlayer(target.getUniqueId())) {
			team.healPlayer(target);
		}
	}
	
	public void revivePlayer(ArmorStand skelly, Player healer) {
		if (state != GameState.RUNNING) {
			return;
		}
		PbTeam team = getTeam(healer.getUniqueId());
		
		if (team.hasReviveSkelly(skelly)) {
			team.revivePlayer(skelly);
			gameStats.addRevive(healer.getUniqueId());
		}
	}
	
	public void broadcastKill(Player target, Player shooter) {
		UUID shooterId = shooter.getUniqueId();
		UUID targetId = target.getUniqueId();
		
		TeamType targetTeam = getTeam(targetId).getType();
		TeamType shooterTeam = getTeam(shooterId).getType();
		String message = targetTeam.prefixColor + target.getDisplayName() + ChatColor.WHITE + " was painted by " + shooterTeam.prefixColor + shooter.getDisplayName() + ".";
		allPlayers(p -> StringUtil.msgPlain(p, message));
		gameStats.addKill(shooterId, targetId);
	}
	
	public void onTeamKill(PbTeam killedTeam) {
		if (state != GameState.RUNNING) {
			return;
		}
		PbTeam leftTeam = null;
		
		for (PbTeam team : teams.values()) {
			if (team.getAlivePlayers().size() > 0) {
				if (leftTeam != null) {
					return;
				}
				leftTeam = team;
			}
		}
		allPlayers(p -> p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, .5f, 1f));
		announceWinners(leftTeam);
		leftTeam.getPlayers().forEach(id -> gameStats.setWin(id));
		scheduleRestart();
	}
	
	private void announceWinners(PbTeam winningTeam) {
		state = GameState.OVER;
		
		if (winningTeam != null) {
			allPlayers(p -> p.sendTitle(String.format("Team %s wins!", winningTeam.getType().displayName + ChatColor.WHITE), ""));
		} else {
			allPlayers(p -> p.sendTitle("It's a draw?", ""));
		}
	}
	
	private void scheduleRestart() {
		BukkitRunnable restartTimer = new BukkitRunnable() {
			@Override
			public void run() {
				gameStats.save(plugin);
				state = GameState.LOBBYING;
				teams.values().forEach(PbTeam::reset);
				allPlayers(p -> {
					gameBoard.removePlayer(p);
					arena.reset();
					onGameEnd.run();
				});
			}
		};
		restartTimer.runTaskLater(plugin, 8*20);
	}
	
	public void updateAliveScores() {
		if (state == GameState.LOBBYING) {
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
