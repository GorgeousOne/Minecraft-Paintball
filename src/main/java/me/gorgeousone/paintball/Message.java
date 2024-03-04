package me.gorgeousone.paintball;

import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class Message {
	
	public static Message LOBBY_ITEM_NAME;
	
	public static String NAME_TEAM_BLUE,
			NAME_TEAM_RED,
			NAME_RIFLE,
			NAME_SHOTGUN,
			NAME_MACHINE_GUN,
			LORE_RIFLE,
			LORE_SHOTGUN,
			LORE_MACHINE_GUN,
			TEAM,
			VOTE_MAP,
			KIT,
			QUIT,
			KIT_SELECT,
	
	
	public static Message
			ARENA_ADD_SPAWN,
ARENA_ALREADY_LINKED,
			ARENA_COPY,
			ARENA_CREATE,
			ARENA_EXISTS,
			ARENA_MISSING,
			ARENA_MOVE,
			ARENA_REMOVE,
			ARENA_RESET,
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
			MAP_ANNOUNCE,
			NOT_IN_LOBBY,
			PLAYER_JOIN,
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
			TEAM_UNQUEUE;
	
	private final String text;
	private final String[] tokens;
	
	private Message(String text, String... tokens) {
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
		if (args.length != tokens.length) {
			throw new IllegalArgumentException(String.format("Expected %d arguments, %d provided", tokens.length, args.length));
		}
		String formattedMessage = text;
		
		for (int i = 0; i < tokens.length; ++i) {
			formattedMessage = formattedMessage.replace(tokens[i], String.valueOf(args[i]));
		}
		return formattedMessage;
	}
	
	public static void loadLanguage(FileConfiguration config) {
		ARENA_ADD_SPAWN = create(config, "config.arena-added-spawn", "spawn-point", "arena", "team");
		ARENA_COPY = create(config, "config.arena-copied", "new-arena", "destination-arena");
		ARENA_CREATE = create(config, "config.arena-created", "new-arena", "location");
		ARENA_EXISTS = create(config, "config.arena-exists", "arena");
		ARENA_MISSING = create(config, "config.arena-missing", "arena");
		ARENA_MOVE = create(config, "config.arena-moved", "source-arena", "destination-arena");
		ARENA_REMOVE = create(config, "config.arena-removed", "arena");
		ARENA_RESET = create(config, "config.arena-reset", "arena");
		LOBBY_ARENA_DETACH = create(config, "config.lobby-arena-detached", "arena", "lobby");
		LOBBY_ARENA_LINK = create(config, "config.lobby-arena-linked", "arena", "lobby");
		LOBBY_CREATE = create(config, "config.lobby-created", "new-lobby", "location");
		LOBBY_EXIT_SET = create(config, "config.lobby-set-exit", "lobby", "exit-point");
		LOBBY_SPAWN_SET = create(config, "config.lobby-set-spawn", "lobby", "spawn-point");
		TEAM_MISSING = create(config, "config.team-missing", "team");
		TEAM_SPAWN_BAD_INDEX = create(config, "config.team-spawn-bad-index", "spawn-number", "team", "max-spawns", "arena");
		TEAM_SPAWN_MISSING = create(config, "config.team-spawn-missing", "team", "arena");
		TEAM_SPAWN_REMOVE = create(config, "config.team-spawn-removed", "spawn-number", "team", "arena", "previous-spawn");
		
		LOBBY_ARENA_MISSING = create(config, "game.lobby-arena-missing", "lobby", "arena");
		LOBBY_CLOSE = create(config, "game.lobby-closed", "lobby");
		LOBBY_COUNTDOWN = create(config, "game.lobby-countdown", false, "seconds-left");
		LOBBY_MISSING = create(config, "game.lobby-missing", "lobby");
		LOBBY_NOT_JOINED = create(config, "game.lobby-not-joined");
		PLAYER_NOT_ONLINE = create(config, "game.player-not-online", "player");
		STATS_PLAYER_MISSING = create(config, "game.stats-player-missing", "player");
		STATS_PLAYER_REQUIRED = create(config, "game.stats-player-required");
		MAP_ANNOUNCE = create(config, "game.map-announce", "map");
		NOT_IN_LOBBY = create(config, "game.not-in-lobby");
		PLAYER_JOIN = create(config, "game.player-joined", "lobby");
		PLAYER_LEAVE = create(config, "game.player-left", "lobby");
		PLAYER_PAINT = create(config, "game.player-painted", false, "target-player", "shooter-player");
		TEAM_JOIN = create(config, "game.team-joined", "team");
		TEAM_QUEUE = create(config, "game.team-queued", "team");
		TEAM_UNQUEUE = create(config, "game.team-unqueued", "team");
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
