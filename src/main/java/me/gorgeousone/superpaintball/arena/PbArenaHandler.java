package me.gorgeousone.superpaintball.arena;

import me.gorgeousone.superpaintball.game.PbArena;
import me.gorgeousone.superpaintball.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

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
			throw new IllegalArgumentException("Already registered arena with name " + arena.getName());
		}
		arenas.put(arena.getName(), arena);
	}

	public void loadArenas(YamlConfiguration arenaConfig, String dataFolder) {
		ConfigUtil.assertKeyExists(arenaConfig, "arenas");
		ConfigurationSection arenas = arenaConfig.getConfigurationSection("arenas");

		for (String name : arenas.getKeys(false)) {
			PbArena arena = PbArena.fromYml(name, arenas, dataFolder);
			registerArena(arena);
		}
	}
}
