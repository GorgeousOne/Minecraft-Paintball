package me.gorgeousone.paintball.util.version;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VersionUtil {

	private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+\\.\\d+(?:\\.\\d+)?)");

	public static Version PLUGIN_VERSION;
	public static Version SERVER_VERSION;
	public static boolean IS_LEGACY_SERVER;

	private VersionUtil() {}

	public static void setup(JavaPlugin plugin) {
		PLUGIN_VERSION = new Version(plugin.getDescription().getVersion());
		SERVER_VERSION = new Version(getServerVersionString());
		IS_LEGACY_SERVER = SERVER_VERSION.isBelow(new Version("1.13.0"));
	}

	public static String getServerVersionString() {
		Matcher bukkitMatcher = VERSION_PATTERN.matcher(Bukkit.getBukkitVersion());
		if (bukkitMatcher.find()) {
			return bukkitMatcher.group(1);
		}
		Matcher versionMatcher = VERSION_PATTERN.matcher(Bukkit.getVersion());
		if (versionMatcher.find()) {
			return versionMatcher.group(1);
		}
		return Bukkit.getBukkitVersion().split("-")[0];
	}
}
