package me.gorgeousone.paintball;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

/**
 * A class to store and load settings from the plugin's config file.
 */
public class ConfigSettings {
	
	public static String SCHEM_FOLDER;
	public static int COUNTDOWN_SECS;
	public static int MIN_PLAYERS;
	public static int MAX_PLAYERS;
	public static int PLAYER_HEALTH_POINTS;
	public static int WATER_BOMB_COUNT;
	public static String CHAT_PREFIX_ALIVE;
	public static String CHAT_PREFIX_DEAD;
	
	public static int RIFLE_BULLET_DMG;
	public static float RIFLE_BULLET_SPEED;
	public static float RIFLE_BULLET_SPREAD;
	public static int RIFLE_PLAYER_SPEED;
	
	public static int SHOTGUN_BULLET_COUNT;
	public static int SHOTGUN_BULLET_DMG;
	public static float SHOTGUN_BULLET_SPEED;
	public static float SHOTGUN_BULLET_SPREAD;
	public static int MACHINE_GUN_PLAYER_SPEED;

	public static int MACHINE_GUN_BULLET_DMG;
	public static float MACHINE_GUN_BULLET_SPEED;
	public static float MACHINE_GUN_MAX_BULLET_SPREAD;
	public static int SHOTGUN_PLAYER_SPEED;

	/**
	 * Loads the schematics directory defined in config.yml
	 * Tries to auto-detect whether to use FAWE or WE folder on first plugin run
	 */
	public static void loadSchemFolder(JavaPlugin plugin, FileConfiguration config) {
		SCHEM_FOLDER = config.getString("schematics-folder", "placeholder");
		detectSchemFolder(plugin, config, plugin.getLogger());
		plugin.getLogger().info("Loading schematics from " + SCHEM_FOLDER + " (can be changed in config.yml).");

		if (!new File(SCHEM_FOLDER).isDirectory()) {
			String defaultSchemFolder = "plugins/WorldEdit/schematics";
			plugin.getLogger().warning(String.format("Schematic folder %s does not exist! Falling back to %s.  You can change the folder in config.yml>schematics-folder.", SCHEM_FOLDER, defaultSchemFolder));
			SCHEM_FOLDER = defaultSchemFolder;
		}
	}

	private static void detectSchemFolder(JavaPlugin plugin, FileConfiguration config, Logger logger) {
		if (!SCHEM_FOLDER.equals("placeholder")) {
			return;
		}
		logger.info("Trying to auto-detect schematics folder. You can change the folder in config.yml>schematics-folder.");
		boolean isFAWEAvailable = plugin.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit") != null;
		boolean isWorldEditAvailable = plugin.getServer().getPluginManager().getPlugin("WorldEdit") != null;

		// Determine the default schematics directory path
		if (isFAWEAvailable) {
			SCHEM_FOLDER = "plugins/FastAsyncWorldEdit/schematics";
			logger.info("  FAWE detected.");
		} else if (isWorldEditAvailable) {
			SCHEM_FOLDER = "plugins/WorldEdit/schematics";
			logger.info("  WorldEdit detected.");
		} else {
			SCHEM_FOLDER = "plugins/WorldEdit/schematics";
			logger.warning("  Neither FAWE nor WorldEdit detected.");
		}
		config.set("schematics-folder", ConfigSettings.SCHEM_FOLDER);
		plugin.saveConfig();
	}

	public static void loadSettings(FileConfiguration config) {
		COUNTDOWN_SECS = clamp(config.getInt("countdown.seconds"), 5, 600);

		ConfigurationSection gameSection = config.getConfigurationSection("game");
		MIN_PLAYERS = clamp(gameSection.getInt("min-players"), 2, 24);
		MAX_PLAYERS = clamp(gameSection.getInt("max-players"), MIN_PLAYERS, 24);
		PLAYER_HEALTH_POINTS = clamp(gameSection.getInt("player-health-points"), 2, 20);
		WATER_BOMB_COUNT = clamp(gameSection.getInt("water-bombs", 3), 0, 100);

		ConfigurationSection prefixSection = config.getConfigurationSection("chat-prefix");
		CHAT_PREFIX_ALIVE = prefixSection.getString("alive").replace('&', '§');
		CHAT_PREFIX_DEAD = prefixSection.getString("dead").replace('&', '§');

		ConfigurationSection kitSettingsSection = config.getConfigurationSection("kit-settings");
		RIFLE_BULLET_DMG = clamp(kitSettingsSection.getInt("rifle.bullet-dmg"), 1, 100);
		RIFLE_BULLET_SPEED = clamp((float) kitSettingsSection.getDouble("rifle.bullet-speed"), 0f, 5f);
		RIFLE_BULLET_SPREAD = clamp((float) kitSettingsSection.getDouble("rifle.bullet-spread"), 0f, 1f);
		RIFLE_PLAYER_SPEED = clamp(kitSettingsSection.getInt("rifle.player-speed"), 0, 3) - 1;

		SHOTGUN_BULLET_COUNT = clamp(kitSettingsSection.getInt("shotgun.bullet-count"), 1, 100);
		SHOTGUN_BULLET_DMG = clamp(kitSettingsSection.getInt("shotgun.bullet-dmg"), 1, 100);
		SHOTGUN_BULLET_SPEED = clamp((float) kitSettingsSection.getDouble("shotgun.bullet-speed"), 0f, 5f);
		SHOTGUN_BULLET_SPREAD = clamp((float) kitSettingsSection.getDouble("shotgun.bullet-spread"), 0f, 1f);
		SHOTGUN_PLAYER_SPEED = clamp(kitSettingsSection.getInt("shotgun.player-speed"), 0, 3) - 1;

		MACHINE_GUN_BULLET_DMG = clamp(kitSettingsSection.getInt("machine-gun.bullet-dmg"), 1, 100);
		MACHINE_GUN_BULLET_SPEED = clamp((float) kitSettingsSection.getDouble("machine-gun.bullet-speed"), 0f, 5f);
		MACHINE_GUN_MAX_BULLET_SPREAD = clamp((float) kitSettingsSection.getDouble("machine-gun.max-bullet-spread"), 0f, 1f);
		MACHINE_GUN_PLAYER_SPEED = clamp(kitSettingsSection.getInt("machine-gun.player-speed"), 0, 3) - 1;
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
