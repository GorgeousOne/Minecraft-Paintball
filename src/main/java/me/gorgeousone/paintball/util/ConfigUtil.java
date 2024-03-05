package me.gorgeousone.paintball.util;

import me.gorgeousone.paintball.team.TeamType;
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
		return loadConfig(configName, configName, plugin);
	}
	
	public static YamlConfiguration loadConfig(String configPath, String defaultName, JavaPlugin plugin) {
		File configFile = new File(plugin.getDataFolder() + "/" + configPath + ".yml");
		YamlConfiguration defConfig = loadDefaultConfig(defaultName, plugin);
		
		if (!configFile.exists()) {
			try {
				defConfig.save(configFile);
				return defConfig;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		config.setDefaults(defConfig);
		config.options().copyDefaults(true);
		saveConfig(config, configPath, plugin);
		return config;
	}
	
	public static YamlConfiguration loadDefaultConfig(String configName, JavaPlugin plugin) {
		InputStream defConfigStream = plugin.getResource(configName + ".yml");
		return YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
	}
	
	public static void saveConfig(YamlConfiguration config, String configName, JavaPlugin plugin) {
		try {
			config.save(plugin.getDataFolder() + "/" + configName + ".yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static File matchFirstFile(String partialName, String folderPath, JavaPlugin plugin) {
		File folder = new File(plugin.getDataFolder() + "/" + folderPath);
		
		if (!folder.exists()) {
			return null;
		}
		File[] matches = folder.listFiles((dir, name) -> name.contains(partialName));
		
		if (matches != null && matches.length > 0) {
			return matches[0];
		}
		return null;
	}
	
	public static String blockPosToYmlString(Location blockPos) {
		return String.format("world=%s,x=%d,y=%d,z=%d", blockPos.getWorld().getName(), blockPos.getBlockX(), blockPos.getBlockY(), blockPos.getBlockZ());
	}
	
	public static Location blockPosFromYmlString(String ymlBlockPos) {
		Map<String, String> dataMap = getDataMapFromString(ymlBlockPos);
		assertKeysExist(dataMap, "world", "x", "y", "z");
		
		try {
			World world = parseWorld(dataMap, "world");
			int x = parseInt(dataMap, "x");
			int y = parseInt(dataMap, "y");
			int z = parseInt(dataMap, "z");
			return new Location(world, x, y, z);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(StringUtil.format("Could not load position %s: %s", ymlBlockPos, e.getMessage()));
		}
	}
	
	public static String spawnToYmlString(Location spawn, boolean saveWorld) {
		if (saveWorld) {
			return String.format("world=%s,x=%d,y=%d,z=%d,facing=%s", spawn.getWorld().getName(), spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ(), LocationUtil.yawToFace(spawn.getYaw()).name().toLowerCase());
		} else {
			return String.format("x=%d,y=%d,z=%d,facing=%s", spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ(), LocationUtil.yawToFace(spawn.getYaw()).name().toLowerCase());
		}
	}
	
	public static Location spawnFromYmlString(String ymlBlockPos) {
		return spawnFromYmlString(ymlBlockPos, null);
	}
	
	public static Location spawnFromYmlString(String ymlBlockPos, World world) {
		Map<String, String> dataMap = getDataMapFromString(ymlBlockPos);
		
		try {
			if (world == null) {
				assertKeysExist(dataMap, "world");
				world = parseWorld(dataMap, "world");
			}
			assertKeysExist(dataMap, "x", "y", "z", "facing");
			double x = parseInt(dataMap, "x") + .5;
			double y = parseInt(dataMap, "y") + .5;
			double z = parseInt(dataMap, "z") + .5;
			BlockFace facing = parseBlockFace(dataMap, "facing");
			return new Location(world, x, y, z).setDirection(LocationUtil.faceToDirection(facing));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(String.format("Could not load spawn %s: %s", ymlBlockPos, e.getMessage()));
		}
	}
	
	private static Map<String, String> getDataMapFromString(String s) {
		return getDataMapFromString(s, ',', '=');
	}
	
	private static Map<String, String> getDataMapFromString(String s, char itemSep, char keyValSep) {
		Map<String, String> dataMap = new HashMap<>();
		
		if (s == null) {
			return dataMap;
		}
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
			throw new IllegalArgumentException(String.format("Missing or incomplete value for %s.", key));
		}
	}
	
	private static void assertKeysExist(Map<?, ?> map, Object... keys) {
		for (Object key : keys) {
			if (!map.containsKey(key)) {
				throw new IllegalArgumentException(String.format("Missing or incomplete value for %s.", key.toString()));
			}
		}
	}
	
	private static World parseWorld(Map<String, String> dataMap, String key) {
		String value = dataMap.get(key);
		World world = Bukkit.getWorld(value);
		
		if (world != null) {
			return world;
		}
		throw new IllegalArgumentException(String.format("Could not find %s with name %s.", key, value));
	}
	
	private static int parseInt(Map<String, String> dataMap, String key) {
		String value = dataMap.get(key);
		
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(String.format("Could not read integer %s for %s.", value, key));
		}
	}
	
	private static BlockFace parseBlockFace(Map<String, String> dataMap, String key) {
		String value = dataMap.get(key);
		
		try {
			return BlockFace.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(String.format("Could not read %s as %s.", value, key));
		}
	}
	
	public static File schemFileFromYml(String fileName, String schemFolder) {
		File file = new File(schemFolder + "/" + fileName);
		
		if (!file.exists()) {
			throw new IllegalArgumentException(StringUtil.format("Schematic %s does not exist.", fileName));
		}
		return file;
	}
	
	public static TeamType teamTypeFromYml(String teamName) {
		try {
			return TeamType.valueOf(teamName.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(StringUtil.format("Plugin doesn't have team %s.", teamName));
		}
	}
}