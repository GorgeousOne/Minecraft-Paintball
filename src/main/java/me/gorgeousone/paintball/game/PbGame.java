package me.gorgeousone.paintball.game;

import me.gorgeousone.paintball.CommandTrigger;
import me.gorgeousone.paintball.GameBoard;
import me.gorgeousone.paintball.Message;
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

/**
 * Class to run all logic in a paintball game, like
 * running start/end timers,
 * player joining/leaving,
 * triggering game mechanics and
 * broadcasting game events.
 */
public class PbGame {
	
	private final JavaPlugin plugin;
	private final PbKitHandler kitHandler;
	private final CommandTrigger commandTrigger;
	private GameState state;
	private final Set<UUID> players;
	private final Map<TeamType, PbTeam> teams;
	private GameBoard gameBoard;
	private final Runnable onGameEnd;
	private Equipment equipment;
	private PbArena playedArena;
	
	private GameStats gameStats;
	private PbTeam winnerTeam;
	private PbTeam loserTeam;

	public PbGame(
			JavaPlugin plugin,
			PbKitHandler kitHandler,
			Runnable onGameEnd,
			CommandTrigger commandTrigger) {
		this.plugin = plugin;
		this.kitHandler = kitHandler;
		this.onGameEnd = onGameEnd;
		this.commandTrigger = commandTrigger;

		this.state = GameState.LOBBYING;
		this.players = new HashSet<>();
		this.teams = new HashMap<>();
		
		for (TeamType teamType : TeamType.values()) {
			teams.put(teamType, new PbTeam(teamType, this, plugin, this.kitHandler));
		}
		this.equipment = new IngameEquipment(this::onShoot, this::onThrowWaterBomb, kitHandler);
	}
	
	public void updateUi() {
		this.equipment = new IngameEquipment(this::onShoot, this::onThrowWaterBomb, kitHandler);
		createScoreboard();
		updateAliveScores();
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
		allPlayers(p -> Message.LOBBY_PLAYER_JOIN.send(p, playerName));
	}
	
	public void removePlayer(UUID playerId) {
		if (!players.contains(playerId)) {
			return;
		}
		Player player = Bukkit.getPlayer(playerId);

		if (isRunning()) {
			if (!getTeam(playerId).isAlive(playerId)) {
				showPlayer(player);
			}
			getTeam(playerId).removePlayer(playerId);
			updateAliveScores();
		}
		if (gameBoard != null) {
			gameBoard.removePlayer(player);
		}
		players.remove(playerId);
	}
	
	public GameState getState() {
		return state;
	}

	/**
	 * Returns true if the players are currently inside the game's arena
	 */
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
			throw new IllegalStateException(Message.LOBBY_RUNNING.format());
		}
		teamQueue.assignTeams(players, teams);
		teams.values().forEach(t -> t.startGame(arenaToPlay.getSpawns(t.getType()), maxHealthPoints));
		
		createScoreboard();
		startCountdown();
		gameStats = new GameStats();
		
		allPlayers(p -> {
			Message.MAP_ANNOUNCE.send(p, ChatColor.WHITE + arenaToPlay.getSpacedName() + StringUtil.MSG_COLOR);
			gameStats.addPlayer(p.getUniqueId(), kitHandler.getKitType(p.getUniqueId()));
		});
		playedArena = arenaToPlay;
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
			gameBoard.setLine(i, Message.UI_ALIVE_PLAYERS.format(team.getAlivePlayers().size()) + StringUtil.pad(i));
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
					allPlayers(p -> p.sendTitle(Message.UI_TITLE_GUNS[0], Message.UI_TITLE_GUNS[1]));
				} else if (time == 40) {
					allPlayers(p -> p.sendTitle(Message.UI_TITLE_WATER_BOMBS[0], Message.UI_TITLE_WATER_BOMBS[1]));
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
			if (team.getAlivePlayers().isEmpty()) {
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
		
		if (!team.hasPlayer(shooterId) || bulletDmg == 9001) {
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
		allPlayers(p -> Message.PLAYER_PAINT.send(p,
				targetTeam.prefixColor + target.getDisplayName() + ChatColor.WHITE,
				shooterTeam.prefixColor + shooter.getDisplayName() + ChatColor.WHITE));
		
		gameStats.addKill(shooterId, targetId);
	}
	
	public void onTeamKill(PbTeam killedTeam) {
		if (state != GameState.RUNNING) {
			return;
		}
		loserTeam = killedTeam;

		for (PbTeam team : teams.values()) {
			if (team != killedTeam) {
				winnerTeam = team;
				break;
			}
		}
		allPlayers(p -> p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, .5f, 1f));
		announceWinners(winnerTeam);
		winnerTeam.getPlayers().forEach(id -> gameStats.setWin(id));
		scheduleRestart();
	}
	
	private void announceWinners(PbTeam winningTeam) {
		state = GameState.OVER;
		
		if (winningTeam != null) {
			allPlayers(p -> p.sendTitle(Message.UI_TITLE_WINNER.format(winningTeam.getType().displayName + ChatColor.WHITE), ""));
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
				commandTrigger.triggerGameEndCommands();
				commandTrigger.triggerPlayerWinCommands(winnerTeam);
				commandTrigger.triggerPlayerLoseCommands(loserTeam);
				teams.values().forEach(PbTeam::reset);
				allPlayers(p -> {
					gameBoard.removePlayer(p);
					playedArena.resetSchem();
					onGameEnd.run();
				});
				gameBoard = null;
			}
		};
		restartTimer.runTaskLater(plugin, 8 * 20);
	}
	
	public void updateAliveScores() {
		if (state == GameState.LOBBYING) {
			return;
		}
		int i = 2;
		
		for (TeamType teamType : teams.keySet()) {
			PbTeam team = teams.get(teamType);
			//padding is for creating unique text :(
			gameBoard.setLine(i, Message.UI_ALIVE_PLAYERS.format(team.getAlivePlayers().size()) + StringUtil.pad(i));
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
	
	public PbArena getPlayedArena() {
		return playedArena;
	}
}
