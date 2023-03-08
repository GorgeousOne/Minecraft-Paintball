package me.gorgeousone.superpaintball.util;

import me.gorgeousone.superpaintball.game.GameUtil;
import me.gorgeousone.superpaintball.team.TeamType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class ConfigUtil {
	
	private ConfigUtil() {}
	
	public static YamlConfiguration loadConfig(String configName, JavaPlugin plugin) {
		File configFile = new File(plugin.getDataFolder() + File.separator + configName + ".yml");
		YamlConfiguration defConfig = loadDefaultConfig(configName, plugin);
		
		if (!configFile.exists()) {
			try {
				defConfig.save(configFile);
			} catch (IOException ignored) {
			}
		}
		
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		config.setDefaults(defConfig);
		config.options().copyDefaults(true);
		return config;
	}
	
	public static YamlConfiguration loadDefaultConfig(String configName, JavaPlugin plugin) {
		InputStream defConfigStream = plugin.getResource(configName + ".yml");
		return YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
	}
	
	public static String blockPosToYmlString(Location blockPos) {
		return String.format("world=%s x=%s y=%s z=%s", blockPos.getWorld().getName(), blockPos.getBlockX(), blockPos.getBlockY(), blockPos.getBlockZ());
	}
	
	public static String spawnToYmlString(Location spawn) {
		return String.format("world=%s x=%s y=%s z=%s facing=%s", spawn.getWorld().getName(), spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ(), GameUtil.yawToFace(spawn.getYaw()));
	}
	
	public static Location blockPosFromYmlString(String ymlBlockPos) {
		Map<String, String> dataMap = getDataMapFromString(ymlBlockPos);
		assertKeysExist(dataMap, "world", "x", "y", "z");

		World world = parseWorld(dataMap, "world");
		int x = parseInt(dataMap, "x");
		int y = parseInt(dataMap, "y");
		int z = parseInt(dataMap, "z");
		return new Location(world, x, y, z);
	}
	
	public static Location spawnFromYmlString(String ymlBlockPos) {
		Map<String, String> dataMap = getDataMapFromString(ymlBlockPos);
		assertKeysExist(dataMap, "world", "x", "y", "z", "facing");

		World world = parseWorld(dataMap, "world");
		double x = parseInt(dataMap, "x") + .5;
		double y = parseInt(dataMap, "y") + .5;
		double z = parseInt(dataMap, "z") + .5;
		BlockFace facing = parseBlockFace(dataMap, "facing");
		return new Location(world, x, y, z).setDirection(facing.getDirection());
	}
	
	private static Map<String, String> getDataMapFromString(String s) {
		return getDataMapFromString(s, ' ', '=');
	}
	
	private static Map<String, String> getDataMapFromString(String s, char itemSep, char keyValSep) {
		Map<String, String> dataMap = new HashMap<>();
		
		for (String item : s.split("" + itemSep)) {
			String[] keyVal = item.split("" + keyValSep);
			
			if (keyVal.length != 2) {
				continue;
			}
			dataMap.put(keyVal[0], keyVal[1]);
		}
		return dataMap;
	}

	public static void assertKeyExists(ConfigurationSection section, String key) {
		if (!section.contains(key)) {
			throw new IllegalArgumentException(String.format("Missing key '%s'.", key));
		}
	}
	private static void assertKeysExist(Map<?, ?> map, Object... keys) {
		for (Object key : keys) {
			if (!map.containsKey(key)) {
				throw new IllegalArgumentException(String.format("Missing key '%s'.", key.toString()));
			}
		}
	}

	private static World parseWorld(Map<String, String> dataMap, String key) {
		String value = dataMap.get(key);
		World world = Bukkit.getWorld(value);

		if (world != null) {
			return world;
		}
		throw new IllegalArgumentException(String.format("Could not find '%s' with name '%s'.", key, value));
	}

	private static int parseInt(Map<String, String> dataMap, String key) {
		String value = dataMap.get(key);

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(String.format("Could not read integer '%s' for '%s'.", value, key));
		}
	}

	private static BlockFace parseBlockFace(Map<String, String> dataMap, String key) {
		String value = dataMap.get(key);

		try {
			return BlockFace.valueOf(value);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(String.format("Could not read facing '%s' as '%s'.", value, key));
		}
	}

	public static File schemFileFromYml(String fileName, String dataFolder) {
		File file = new File(dataFolder + File.separator + "schematics" + fileName);

		if (file.exists()) {
			throw new IllegalArgumentException(String.format("Could not find schematic '%s' in plugin's 'schematics' folder.", fileName));
		}
		String[] nameParts = fileName.split(".");
		String extension = nameParts[nameParts.length - 1];

		if (!("schem".equals(extension) || "schematic".equals(extension))) {
			throw new IllegalArgumentException(String.format("File '%s' does not have extension '.schem' or '.schematic'", fileName));
		}
		return file;
	}

	public static TeamType teamTypeFromYml(String teamName) {
		try {
			return TeamType.valueOf(teamName.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(String.format("Plugin doesn't have team '%s'.", teamName));
		}
	}
}