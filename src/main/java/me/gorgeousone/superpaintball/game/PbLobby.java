package me.gorgeousone.superpaintball.game;

import me.gorgeousone.superpaintball.GameBoard;
import me.gorgeousone.superpaintball.arena.PbArena;
import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.equipment.*;
import me.gorgeousone.superpaintball.kit.KitType;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.kit.AbstractKit;
import me.gorgeousone.superpaintball.team.PbTeam;
import me.gorgeousone.superpaintball.team.TeamType;
import me.gorgeousone.superpaintball.util.ConfigUtil;
import me.gorgeousone.superpaintball.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
	private GameBoard gameBoard;

	private final Map<GameState, Equipment> equips;

	public PbLobby(String name, Location spawnPos, JavaPlugin plugin, PbLobbyHandler lobbyHandler, PbKitHandler kitHandler) {
		this.lobbyHandler = lobbyHandler;
		this.plugin = plugin;

		this.name = name;
		this.spawnPos = GameUtil.cleanSpawn(spawnPos);

		this.arenas = new LinkedList<>();
		this.teams = new HashMap<>();
		this.players = new HashSet<>();
		this.shootCooldowns = new HashMap<>();

		this.state = GameState.LOBBYING;
		this.equips = new HashMap<>();

		for (TeamType teamType : TeamType.values()) {
			teams.put(teamType, new PbTeam(teamType, this, lobbyHandler, kitHandler));
		}
		equips.put(GameState.LOBBYING, new LobbyEquipment(this::onChooseTeam, this::onSelectKit, kitHandler));
		equips.put(GameState.RUNNING, new IngameEquipment(this::onShoot, this::onThrowWaterBomb, kitHandler));
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

	public Equipment getEquip() {
		return equips.getOrDefault(state, null);
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

	public void removePlayer(Player player) {
		UUID playerId = player.getUniqueId();

		if (!players.contains(playerId)) {
			throw new IllegalArgumentException("Can't remove player with id: " + playerId + ". They are not in this game");
		}
		players.remove(playerId);
		PbTeam team = getTeam(playerId);
		team.removePlayer(playerId);
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

	public void joinPlayer(Player player, TeamType teamType) {
		UUID playerId = player.getUniqueId();

		if (players.contains(playerId)) {
			throw new IllegalArgumentException(String.format("You already are in lobby '%s'.", name));
		}
		//TODO if game started, join as spectator if not full?
		//TODO heal and nourish
		player.teleport(spawnPos);
		player.sendMessage(String.format("Joined lobby '%s'.", name));

		players.add(playerId);
		teams.get(teamType).addPlayer(player);
		equips.get(GameState.LOBBYING).equip(player);
	}

	private void onChooseTeam(SlotClickEvent event) {
		int slot = event.getClickedSlot();
		TeamType newTeamType = TeamType.values()[slot];
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		PbTeam team = getTeam(playerId);

		//if is full
		if (team.getType() == newTeamType) {
			player.sendMessage(String.format("You already are in team %s.", newTeamType.displayName));
		} else {
			team.removePlayer(playerId);
			teams.get(newTeamType).addPlayer(player);
		}
	}

	private void onSelectKit(SlotClickEvent event) {
		PbKitHandler.openKitSelector(event.getPlayer());
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

	public void onShoot(SlotClickEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();

		if (!getTeam(playerId).isAlive(playerId)) {
			return;
		}
		if (shootCooldowns.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
			return;
		}
		ItemStack gun = event.getClickedItem();
		KitType kitType = KitType.valueOf(gun);
		AbstractKit kit = PbKitHandler.getKit(kitType);
		long cooldownTicks = kit.launchShot(player, getTeam(playerId));

		if (cooldownTicks > 0) {
			shootCooldowns.put(playerId, System.currentTimeMillis() + cooldownTicks * 50);
		}
	}

	private void onThrowWaterBomb(SlotClickEvent event) {
		UUID playerId = event.getPlayer().getUniqueId();

		if (!getTeam(playerId).isAlive(playerId)) {
			event.setCancelled(true);
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
		for (UUID playerId : players) {
			gameBoard.addPlayer(Bukkit.getPlayer(playerId));
		}
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
	}

	private void announceWinners(TeamType winningTeam) {
		state = GameState.OVER;

		if (winningTeam != null) {
			sendTitle(winningTeam.displayName + " won!");
		} else {
			sendTitle("It's a draw?");
		}
	}

	public void broadcastKill(Player target, Player shooter) {
		TeamType targetTeam = getTeam(target.getUniqueId()).getType();
		TeamType shooterTeam = getTeam(shooter.getUniqueId()).getType();
		String message = targetTeam.prefixColor + target.getDisplayName() + ChatColor.RESET + " was painted by " + shooterTeam.prefixColor + shooter.getDisplayName();

		for (UUID playerId : players) {
			Player player = Bukkit.getPlayer(playerId);
			player.sendMessage(message);
		}
	}

	private void sendTitle(String text) {
		for (UUID playerId : players) {
			Player player = Bukkit.getPlayer(playerId);
			player.sendTitle(text, "");
		}
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
