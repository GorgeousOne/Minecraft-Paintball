package me.gorgeousone.paintball;

import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class Message {
	
	public static Message
			LINE_02,
			LINE_03,
			LINE_04,
			LINE_06,
			LINE_07,
			LINE_08,
			LINE_09,
			LINE_12,
			LINE_13,
			LINE_14,
			LINE_15,
			LINE_16,
			LINE_18,
			LINE_19,
			LINE_20,
			LINE_21,
			LINE_22,
			LINE_23,
			LINE_24,
			LINE_25,
			LINE_26,
			LINE_28,
			LINE_29,
			LINE_30,
			LINE_31,
			LINE_32,
			LINE_33,
			LINE_34,
			LINE_35,
			LINE_36,
			LINE_37,
			LINE_38,
			LINE_39
					;
	
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
		if (args.length != tokens.length) {
			throw new IllegalArgumentException(String.format("Expected %d arguments, %d provided", tokens.length, args.length));
		}
		String formattedMessage = text;
		
		for (int i = 0; i < tokens.length; ++i) {
			formattedMessage = formattedMessage.replace(tokens[i], String.valueOf(args[i]));
		}
		receiver.sendMessage(formattedMessage);
	}
	
	public static void loadLanguage(FileConfiguration config) {
		LINE_02 = create(config, "config.arena-missing", "arena-name");
		LINE_03 = create(config, "config.team-missing", "team-name");
		LINE_04 = create(config, "config.arena-added-spawn", "spawn-point", "arena-name", "team-name");
		LINE_06 = create(config, "config.arena-copied", "new-arena", "destination-arena");
		LINE_07 = create(config, "config.arena-already-exists", "arena-name");
		LINE_08 = create(config, "config.arena-created", "new-arena", "location");
		LINE_09 = create(config, "config.arena-removed", "arena-name");
		LINE_12 = create(config, "config.arena-moved", "source-arena", "destination-arena");
		LINE_13 = create(config, "config.team-spawn-missing", "team-name", "arena-name");
		LINE_14 = create(config, "config.team-spawn-index", "spawn-number", "team-name", "max-spawns", "arena-name");
		LINE_15 = create(config, "config.team-spawn-removed", "spawn-number", "team-name", "arena-name", "previous-spawn");
		LINE_16 = create(config, "config.arena-reset", "arena-name");
		LINE_18 = create(config, "game.lobby-missing", "lobby-name");
		LINE_19 = create(config, "game.player-not-online", "player-name");
		LINE_20 = create(config, "game.lobby-already-left");
		LINE_21 = create(config, "game.lobby-not-joined");
		LINE_22 = create(config, "game.stats-player-required");
		LINE_23 = create(config, "game.stats-player-missing", "player-name");
		LINE_24 = create(config, "game.player-painted", false, "target-player", "shooter-player");
		LINE_25 = create(config, "config.lobby-created", "new-lobby", "location");
		LINE_26 = create(config, "config.lobby-arena-linked", "arena-name", "lobby-name");
		LINE_28 = create(config, "config.lobby-set-exit", "lobby-name", "exit-point");
		LINE_29 = create(config, "config.lobby-set-spawn", "lobby-name", "spawn-point");
		LINE_30 = create(config, "config.lobby-arena-detached", "arena-name", "lobby-name");
		LINE_31 = create(config, "game.map-announce", "map-name");
		LINE_32 = create(config, "game.player-joined", "lobby-name");
		LINE_33 = create(config, "game.lobby-arena-missing", "lobby-name", "arena-name");
		LINE_34 = create(config, "game.player-left", "lobby-name");
		LINE_35 = create(config, "game.lobby-closed", "lobby-name");
		LINE_36 = create(config, "game.team-unqueued", "team-name");
		LINE_37 = create(config, "game.team-queued", "team-name");
		LINE_38 = create(config, "game.team-joined", "team-name");
		LINE_39 = create(config, "game.lobby-countdown", false, "seconds-left");
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
