package me.gorgeousone.paintball.util.version;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class VersionUtil {
	
	public static Version PLUGIN_VERSION;
	public static Version SERVER_VERSION;
	public static boolean IS_LEGACY_SERVER;
	
	private VersionUtil() {}
	
	public static void setup(JavaPlugin plugin) {
		PLUGIN_VERSION = new Version(plugin.getDescription().getVersion());
		SERVER_VERSION = new Version(getServerVersionString(), "_");
		IS_LEGACY_SERVER = SERVER_VERSION.isBelow(new Version("1.13.0"));
	}
	
	public static String getServerVersionString() {
		return Bukkit.getServer().getClass().getName().split("\\.")[3].replaceAll("[a-zA-Z]", "");
	}
}
