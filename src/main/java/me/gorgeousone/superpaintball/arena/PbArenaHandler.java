package me.gorgeousone.superpaintball.arena;

import me.gorgeousone.superpaintball.game.PbArena;
import me.gorgeousone.superpaintball.game.PbLobby;
import me.gorgeousone.superpaintball.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PbArenaHandler {

	private final Map<String, PbArena> arenas;

	public PbArenaHandler() {
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

	public void saveArenas(YamlConfiguration arenaConfig) {
		Logger logger = Bukkit.getLogger();
		logger.log(Level.INFO, "  Saving arenas:");

		if (!arenaConfig.contains("arenas")) {
			arenaConfig.createSection("arenas");
		}
		ConfigurationSection arenasSection = arenaConfig.getConfigurationSection("arenas");
		arenas.values().forEach(l -> l.toYml(arenasSection));
		logger.log(Level.INFO, String.format("  Saved %d arenas", arenas.size()));
	}

	public void loadArenas(YamlConfiguration arenaConfig, String dataFolder) {
		Logger logger = Bukkit.getLogger();
		logger.log(Level.INFO, "  Loading arenas:");

		ConfigUtil.assertKeyExists(arenaConfig, "arenas");
		ConfigurationSection arenaSection = arenaConfig.getConfigurationSection("arenas");

		for (String name : arenaSection.getKeys(false)) {
			try {
				PbArena arena = PbArena.fromYml(name, arenaSection, dataFolder);
				registerArena(arena);
			} catch (IllegalArgumentException e) {
				logger.log(Level.WARNING, e.getMessage());
			}
		}
		logger.log(Level.INFO, String.format("  Loaded %d arenas", arenas.size()));
	}
}
