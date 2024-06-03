package me.gorgeousone.paintball;

import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;

public class Message {
	
	public static Message
			LOBBY_ITEM_NAME,
			UI_COUNTDOWN;
	
	public static String
			NAME_RIFLE,
			NAME_SHOTGUN,
			NAME_MACHINE_GUN,
			LORE_RIFLE,
			LORE_SHOTGUN,
			LORE_MACHINE_GUN,
			UI_KIT,
			UI_KIT_SELECT,
			UI_LOBBY,
			UI_PLAYERS,
			UI_QUIT,
			UI_TEAM,
			UI_VOTE_MAP,
			UI_WAIT_FOR_PLAYERS,
			UI_WATER_BOMB;
		
	public static String[]
			UI_TITLE_GUNS,
			UI_TITLE_WATER_BOMBS;
	
	public static Message
			ARENA_ADD_SPAWN,
			ARENA_ALREADY_LINKED,
			ARENA_COPY,
			ARENA_CREATE,
			ARENA_EXISTS,
			ARENA_MISSING,
			ARENA_MOVE,
			ARENA_NOT_LINKED,
			ARENA_REMOVE,
			ARENA_REMOVE_SCHEM_MISSING,
			ARENA_RESET,
			ARENA_SCHEM_MISSING,
			LOBBY_ARENA_DETACH,
			LOBBY_ARENA_LINK,
			LOBBY_ARENA_MISSING,
			LOBBY_CLOSE,
			LOBBY_COUNTDOWN,
			LOBBY_CREATE,
			LOBBY_EXISTS,
			LOBBY_EXIT_SET,
			LOBBY_MISSING,
			LOBBY_NOT_JOINED,
			LOBBY_PLAYER_JOIN,
			LOBBY_SPAWN_SET,
			LOBBY_UNDERFULL,
			LOBBY_YOU_JOIN,
			MAP_ANNOUNCE,
			NOT_IN_LOBBY,
			PLAYER_LEAVE,
			PLAYER_NOT_ONLINE,
			PLAYER_PAINT,
			STATS_PLAYER_MISSING,
			STATS_PLAYER_REQUIRED,
			TEAM_JOIN,
			TEAM_MISSING,
			TEAM_QUEUE,
			TEAM_SPAWN_BAD_INDEX,
			TEAM_SPAWN_MISSING,
			TEAM_SPAWN_REMOVE,
			LOBBY_ALREADY_JOINED,
			LOBBY_RUNNING,
			LOBBY_FULL,
			TEAM_UNQUEUE,
			UI_ALIVE_PLAYERS,
			UI_TITLE_WINNER;
	
	public final String text;
	private final String[] tokens;
	
	public Message(String text, String... tokens) {
		this.text = text;
		this.tokens = new String[tokens.length];
		
		for (int i = 0; i < tokens.length; ++i) {
			this.tokens[i] = String.format("%%%s%%", tokens[i]);
		}
	}
	
	public void send(CommandSender receiver, Object... args) {
		receiver.sendMessage(format(args));
	}
	
	public String format(Object... args) {
		String formattedMessage = text;
		
		for (int i = 0; i < Math.min(tokens.length, args.length); ++i) {
			formattedMessage = formattedMessage.replace(tokens[i], String.valueOf(args[i]));
		}
		return formattedMessage;
	}
	
	public static void loadLanguage(FileConfiguration config) {
		ARENA_ADD_SPAWN = create(config, "commands.arena-added-spawn", "spawn-point", "arena", "team");
		ARENA_ALREADY_LINKED = create(config, "commands.arena-already-linked", "arena", "lobby");
		ARENA_COPY = create(config, "commands.arena-copied", "new-arena", "destination-arena");
		ARENA_CREATE = create(config, "commands.arena-created", "new-arena", "location");
		ARENA_EXISTS = create(config, "commands.arena-exists", "arena");
		ARENA_MISSING = create(config, "commands.arena-missing", "arena");
		ARENA_MOVE = create(config, "commands.arena-moved", "source-arena", "destination-arena");
		ARENA_NOT_LINKED = create(config, "commands.arena-not-linked", "arena");
		ARENA_REMOVE = create(config, "commands.arena-removed", "arena");
		ARENA_REMOVE_SCHEM_MISSING = create(config, "commands.arena-removed-schematic-missing", "arena");
		ARENA_RESET = create(config, "commands.arena-reset", "arena");
		ARENA_SCHEM_MISSING = create(config, "commands.arena-schematic-missing", "file", "arena");
		LOBBY_ARENA_DETACH = create(config, "commands.lobby-arena-detached", "arena", "lobby");
		LOBBY_ARENA_LINK = create(config, "commands.lobby-arena-linked", "arena", "lobby");
		LOBBY_CREATE = create(config, "commands.lobby-created", "new-lobby", "location");
		LOBBY_EXISTS = create(config, "commands.lobby-exists", "lobby");
		LOBBY_EXIT_SET = create(config, "commands.lobby-set-exit", "lobby", "exit-point");
		LOBBY_SPAWN_SET = create(config, "commands.lobby-set-spawn", "lobby", "spawn-point");
		TEAM_MISSING = create(config, "commands.team-missing", "team");
		TEAM_SPAWN_BAD_INDEX = create(config, "commands.team-spawn-bad-index", "spawn-number", "team", "max-spawns", "arena");
		TEAM_SPAWN_MISSING = create(config, "commands.team-spawn-missing", "team", "arena");
		TEAM_SPAWN_REMOVE = create(config, "commands.team-spawn-removed", "spawn-number", "team", "arena", "previous-spawn");
		
		LOBBY_ALREADY_JOINED = create(config, "game.lobby-already-joined", "lobby");
		LOBBY_ARENA_MISSING = create(config, "game.lobby-arena-missing", "lobby");
		LOBBY_CLOSE = create(config, "game.lobby-closed", "lobby");
		LOBBY_COUNTDOWN = create(config, "game.lobby-countdown", false, "seconds-left");
		LOBBY_FULL = create(config, "game.lobby-full", "lobby");
		LOBBY_MISSING = create(config, "game.lobby-missing", "lobby");
		LOBBY_NOT_JOINED = create(config, "game.lobby-not-joined");
		LOBBY_PLAYER_JOIN = create(config, "game.lobby-player-joined", "player");
		LOBBY_RUNNING = create(config, "game.lobby-running");
		LOBBY_UNDERFULL = create(config, "game.lobby-underfull", "current-players", "needed-players");
		LOBBY_YOU_JOIN = create(config, "game.lobby-you-joined", "lobby");
		PLAYER_NOT_ONLINE = create(config, "game.player-not-online", "player");
		STATS_PLAYER_MISSING = create(config, "game.stats-player-missing", "player");
		STATS_PLAYER_REQUIRED = create(config, "game.stats-player-required");
		MAP_ANNOUNCE = create(config, "game.map-announce", "map");
		NOT_IN_LOBBY = create(config, "game.not-in-lobby");
		PLAYER_LEAVE = create(config, "game.player-left", "lobby");
		PLAYER_PAINT = create(config, "game.player-painted", false, "target", "shooter");
		TEAM_JOIN = create(config, "game.team-joined", "team");
		TEAM_QUEUE = create(config, "game.team-queued", "team");
		TEAM_UNQUEUE = create(config, "game.team-unqueued", "team");
		
		LOBBY_ITEM_NAME = new Message(String.format("%s%%name%% %s(%s)", ChatColor.WHITE, ChatColor.GRAY, config.getString("ui.right-click")), "name");
		NAME_RIFLE = config.getString("guns.rifle");
		NAME_SHOTGUN = config.getString("guns.shotgun");
		NAME_MACHINE_GUN = config.getString("guns.machine-gun");
		LORE_RIFLE = config.getString("guns.lore-rifle");
		LORE_SHOTGUN = config.getString("guns.lore-shotgun");
		LORE_MACHINE_GUN = config.getString("guns.lore-machine-gun");
		
		UI_ALIVE_PLAYERS = new Message(ChatColor.BOLD + config.getString("ui.alive-players"), "alive-players");
		UI_COUNTDOWN = new Message(ChatColor.BOLD + config.getString("ui.countdown").replace("%seconds%", ChatColor.GOLD + "%seconds%"), "seconds");
		UI_KIT = config.getString("ui.kit");
		UI_KIT_SELECT = config.getString("ui.kit-select");
		UI_LOBBY = ChatColor.GREEN + "" + ChatColor.BOLD + config.getString("ui.lobby");
		UI_PLAYERS = ChatColor.GREEN + "" + ChatColor.BOLD + config.getString("ui.players");
		UI_TEAM = ChatColor.YELLOW + config.getString("ui.team") + ChatColor.RESET;
		UI_VOTE_MAP = config.getString("ui.vote-map");
		UI_QUIT = config.getString("ui.quit");
		UI_WAIT_FOR_PLAYERS = ChatColor.BOLD + config.getString("ui.waiting-for-players");
		UI_WATER_BOMB = ChatColor.YELLOW + config.getString("ui.water-bomb");
		
		UI_TITLE_GUNS = getTitles(config, "ui.title-guns");
		UI_TITLE_WATER_BOMBS = getTitles(config, "ui.title-water-bombs");
		UI_TITLE_WINNER = create(config, "ui.title-winner", false, "team");
		
	}
	
	private static String[] getTitles(ConfigurationSection section, String path) {
		String message = section.getString(path);
		String[] titles = message.split("\\\\n");
		
		if (titles.length == 2) {
			return titles;
		} else if (titles.length == 1) {
			return new String[] {message, "Bottom Text"};
		} else {
			return Arrays.copyOfRange(titles, 0, 2);
		}
	}
	
	private static Message create(ConfigurationSection section,
			String path,
			String... tokens) {
		return create(section, path, true, tokens);
	}
	
	private static Message create(ConfigurationSection section,
			String path,
			boolean withPrefix,
			String... tokens) {
		String message = (withPrefix ? StringUtil.PLUGIN_PREFIX : "") + ChatColor.GRAY + section.getString(path);
		return new Message(message, tokens);
	}
}
