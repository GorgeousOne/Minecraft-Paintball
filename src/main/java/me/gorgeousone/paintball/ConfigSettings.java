package me.gorgeousone.paintball;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.logging.Level;

public class ConfigSettings {
	
	public static String SCHEM_FOLDER;
	public static int COUNTDOWN_SECS;
	public static int MIN_PLAYERS;
	public static int MAX_PLAYERS;
	public static int PLAYER_HEALTH_POINTS;
	public static String CHAT_PREFIX_ALIVE;
	public static String CHAT_PREFIX_DEAD;
	
	public static void loadSettings(FileConfiguration config) {
		COUNTDOWN_SECS = clamp(config.getInt("countdown.seconds"), 5, 600);

		ConfigurationSection gameSection = config.getConfigurationSection("game");
		MIN_PLAYERS = clamp(gameSection.getInt("min-players"), 2, 24);
		MAX_PLAYERS = clamp(gameSection.getInt("max-players"), MIN_PLAYERS, 24);
		PLAYER_HEALTH_POINTS = clamp(gameSection.getInt("player-health-points"), 4, 20);
		SCHEM_FOLDER = config.getString("schematics-folder");
		
		if (!new File(SCHEM_FOLDER).isDirectory()) {
			String defaultSchemFolder = "plugins/WorldEdit/schematics";
			Bukkit.getLogger().log(Level.WARNING, String.format("Schematic folder %s does not exist! Falling back to %s", SCHEM_FOLDER, defaultSchemFolder));
			SCHEM_FOLDER = defaultSchemFolder;
		}
		ConfigurationSection prefixSection = config.getConfigurationSection("chat-prefix");
		CHAT_PREFIX_ALIVE = prefixSection.getString("alive").replace('&', '§');
		CHAT_PREFIX_DEAD = prefixSection.getString("dead").replace('&', '§');
	}
	
	public static <T extends Comparable<T>> T clamp(T val, T min, T max) {
		if (val.compareTo(min) < 0) {
			return min;
		}
		if (val.compareTo(max) > 0) {
			return max;
		}
		return val;
	}
}
