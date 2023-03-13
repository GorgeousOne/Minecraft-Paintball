package me.gorgeousone.superpaintball.arena;

import me.gorgeousone.superpaintball.game.PbArena;
import me.gorgeousone.superpaintball.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PbArenaHandler {

	private final JavaPlugin plugin;
	private final Map<String, PbArena> arenas;
	private final YamlConfiguration backupConfig;

	public PbArenaHandler(JavaPlugin plugin) {
		this.plugin = plugin;
		this.backupConfig = ConfigUtil.loadConfig("arenas", plugin);
		this.arenas = new HashMap<>();
	}

	public boolean containsArena(String arenaName) {
		return arenas.containsKey(arenaName);
	}

	public void registerArena(PbArena arena) {
		if (arenas.containsKey(arena.getName())) {
			throw new IllegalArgumentException(String.format("Already registered arena with name '%s'", arena.getName()));
		}
		arenas.put(arena.getName(), arena);
		ConfigurationSection arenasSection = backupConfig.getConfigurationSection("arenas");
		arena.toYml(arenasSection);
		ConfigUtil.saveConfig(backupConfig, "arenas", plugin);
	}

	public PbArena getArena(String name) {
		return arenas.getOrDefault(name, null);
	}

	public Collection<PbArena> getArenas() {
		return arenas.values();
	}

	public void removeArena(String name) {
		arenas.remove(name);
	}

	public void saveArenas() {
		Logger logger = Bukkit.getLogger();
		logger.log(Level.INFO, "  Saving arenas:");

		if (!backupConfig.contains("arenas")) {
			backupConfig.createSection("arenas");
		}
		ConfigurationSection arenasSection = backupConfig.getConfigurationSection("arenas");
		arenas.values().forEach(l -> l.toYml(arenasSection));
		logger.log(Level.INFO, String.format("  Saved %d arenas", arenas.size()));
	}

	public void loadArenas(String schemFolder) {
		Logger logger = Bukkit.getLogger();
		logger.log(Level.INFO, "  Loading arenas:");

		ConfigUtil.assertKeyExists(backupConfig, "arenas");
		ConfigurationSection arenaSection = backupConfig.getConfigurationSection("arenas");

		for (String name : arenaSection.getKeys(false)) {
			try {
				PbArena arena = PbArena.fromYml(name, arenaSection, schemFolder);
				registerArena(arena);
			} catch (IllegalArgumentException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		}
		ConfigUtil.saveConfig(backupConfig, "arenas", plugin);
		logger.log(Level.INFO, String.format("  Loaded %d arenas", arenas.size()));
	}
}
