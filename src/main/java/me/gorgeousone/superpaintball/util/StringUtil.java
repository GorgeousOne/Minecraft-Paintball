package me.gorgeousone.superpaintball.util;

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
		sender.sendMessage(PLUGIN_PREFIX + String.format(message, args));
	}
	
	public static void msgPlain(CommandSender sender, String message, Object... args) {
		sender.sendMessage(ChatColor.GRAY + String.format(message, args));
	}
	
	public static String pad(int n) {
		return pad(n, ' ');
	}
	
	public static String pad(int n, char c) {
		return new String(new char[n]).replace('\0', c);
	}
}
