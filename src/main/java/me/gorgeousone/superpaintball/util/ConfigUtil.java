package me.gorgeousone.superpaintball.util;

import me.gorgeousone.superpaintball.game.GameUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
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
		assertContainsKeys(dataMap, "world", "x", "y", "z");
		
		World world = Bukkit.getWorld(dataMap.get("world"));
		int x = Integer.parseInt(dataMap.get("x"));
		int y = Integer.parseInt(dataMap.get("y"));
		int z = Integer.parseInt(dataMap.get("z"));
		return new Location(world, x, y, z);
	}
	
	public static Location spawnFromYmlString(String ymlBlockPos) {
		Map<String, String> dataMap = getDataMapFromString(ymlBlockPos);
		assertContainsKeys(dataMap, "world", "x", "y", "z", "facing");
		
		World world = Bukkit.getWorld(dataMap.get("world"));
		double x = Integer.parseInt(dataMap.get("x")) + .5;
		double y = Integer.parseInt(dataMap.get("y")) + .5;
		double z = Integer.parseInt(dataMap.get("z")) + .5;
		BlockFace facing = BlockFace.valueOf(dataMap.get("facing"));
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
	
	private static void assertContainsKeys(Map<?, ?> map, Object... keys) {
		for (Object key : keys) {
			if (!map.containsKey(key)) {
				throw new IllegalArgumentException(String.format("Missing key '%s'.", key.toString()));
			}
		}
	}
}