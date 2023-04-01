package me.gorgeousone.paintball.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class StringUtil {
	
	public static final ChatColor MSG_COLOR = ChatColor.GRAY;
	public static final String PLUGIN_PREFIX =
			ChatColor.DARK_GRAY + "[" +
			ChatColor.GOLD + "Paintball" +
			ChatColor.DARK_GRAY + "] " +
			ChatColor.GRAY;
	
	private StringUtil() {}

	public static void msg(CommandSender sender, String message, Object... args) {
		sender.sendMessage(format(message, args));
	}
	
	public static void msgPlain(CommandSender sender, String message, Object... args) {
		sender.sendMessage(format(message, false, args));
	}
	
	public static String replace(String message, String configPlaceholder, String value) {
		return message.replace("%" + configPlaceholder + "%", value);
	}
	
	public static String pad(int n) {
		return pad(n, ' ');
	}
	
	public static String pad(int n, char c) {
		return new String(new char[n]).replace('\0', c);
	}
	
	public static String format(String message, Object... args) {
		return format(message, true, args);
	}
	public static String format(String message, boolean withPrefix, Object... args) {
		message = message.replaceAll("%-?\\d*\\.?\\d*[a-zA-D]", ChatColor.RESET + "$0" + MSG_COLOR);
		return (withPrefix ? PLUGIN_PREFIX : "") + ChatColor.GRAY + String.format(message, args);
	}
}
