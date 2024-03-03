package me.gorgeousone.paintball.arena;

import me.gorgeousone.paintball.util.LocationUtil;
import me.gorgeousone.paintball.team.TeamType;
import me.gorgeousone.paintball.util.ConfigUtil;
import me.gorgeousone.paintball.util.SchemUtil;
import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * A class to store and manage the settings of a paintball arena.
 * Like the schematic file, the spawn points for the teams and the position of the schematic in the world.
 */
public class PbArena {

	private final JavaPlugin plugin;
	private final PbArenaHandler arenaHandler;
	private final String name;
	private File schemFile;
	private Location schemPos;
	private final Map<TeamType, List<Location>> teamSpawns;

	public PbArena(String name, File schemFile, Location schemPos, JavaPlugin plugin, PbArenaHandler arenaHandler) {
		this.plugin = plugin;
		this.arenaHandler = arenaHandler;
		this.name = name;
		this.schemFile = schemFile;
		this.schemPos = LocationUtil.cleanSpawn(schemPos);
		this.teamSpawns = new HashMap<>();

		schemPos.setX(schemPos.getBlockX());
		schemPos.setY(schemPos.getBlockY());
		schemPos.setZ(schemPos.getBlockZ());

		for (TeamType teamType : TeamType.values()) {
			teamSpawns.put(teamType, new LinkedList<>());
		}
	}

	public PbArena(PbArena other, String name, Location schemPos) {
		this.plugin = other.plugin;
		this.arenaHandler = other.arenaHandler;
		this.name = name;
		this.schemFile = other.schemFile;
		this.schemPos = LocationUtil.cleanSpawn(schemPos);
		this.teamSpawns = new HashMap<>();
		
		Location dist = this.schemPos.clone().subtract(other.getSchemPos());
		
		for (TeamType teamType : other.teamSpawns.keySet()) {
			List<Location> spawns = new ArrayList<>();
			
			for (Location spawn : other.teamSpawns.get(teamType)) {
				Location spawnCopy = spawn.clone().add(dist);
				spawnCopy.setWorld(this.schemPos.getWorld());
				spawns.add(spawnCopy);
			}
			teamSpawns.put(teamType, spawns);
		}
	}

	public String getName() {
		return name;
	}
	
	public String getSpacedName() {
		return name.replace('_', ' ');
	}
	
	public Location getSchemPos() {
		return schemPos.clone();
	}
	
	public void addSpawn(TeamType teamType, Location spawnPos) {
		LocationUtil.cleanSpawn(spawnPos);
		teamSpawns.computeIfAbsent(teamType, v -> new ArrayList<>());
		teamSpawns.get(teamType).add(spawnPos);
		arenaHandler.saveArena(this);
	}
	
	public void setupSchem() {
		try {
			SchemUtil.pasteSchemWithBackup(schemFile, schemPos, name, plugin);
		} catch (IOException e) {
			throw new IllegalArgumentException(StringUtil.format("Could not find the schematic of arena %s.", name));
		}
	}
	
	public void resetSchem() {
		try {
			SchemUtil.pasteSchem(schemFile, schemPos);
		} catch (IOException e) {
			throw new IllegalArgumentException(StringUtil.format("Could not find the schematic of arena %s.", name));
		}
	}
	
	public void moveTo(Location pos) {
		removeSchem();
		Location newSchemPos = LocationUtil.cleanSpawn(pos);
		Location dist = newSchemPos.clone().subtract(schemPos);
		schemPos = newSchemPos;
		
		for (TeamType teamType : teamSpawns.keySet()) {
			for (Location spawn : teamSpawns.get(teamType)) {
				spawn.add(dist);
				spawn.setWorld(schemPos.getWorld());
			}
		}
		setupSchem();
	}
	
	public void removeSchem() {
		try {
			SchemUtil.resetSchemFromBackup(name, schemPos, plugin);
		} catch (IOException e) {
			throw new IllegalArgumentException(StringUtil.format("The find the backup schematic to remove arena %s. But removed arena.", name));
		}
	}
	
	public boolean hasSpawns(TeamType teamType) {
		return teamSpawns.containsKey(teamType);
	}
	
	public List<Location> getSpawns(TeamType type) {
		return teamSpawns.get(type);
	}
	
	public void toYml(ConfigurationSection parentSection) {
		ConfigurationSection section = parentSection.createSection(name);
		section.set("schematic", schemFile.getName());
		section.set("position", ConfigUtil.blockPosToYmlString(schemPos));
		ConfigurationSection spawnsSection = section.createSection("spawns");

		for (TeamType teamType : teamSpawns.keySet()) {
			List<String> spawnStrings = new ArrayList<>();

			for (Location spawn : teamSpawns.get(teamType)) {
				spawnStrings.add(ConfigUtil.spawnToYmlString(spawn, false));
			}
			spawnsSection.set(teamType.name().toLowerCase(), spawnStrings);
		}
	}
	
	public static PbArena fromYml(String name, ConfigurationSection parentSection, String schemFolder, JavaPlugin plugin, PbArenaHandler arenaHandler) {
		ConfigurationSection section = parentSection.getConfigurationSection(name);
		PbArena arena;
		try {
			ConfigUtil.assertKeyExists(section, "schematic");
			ConfigUtil.assertKeyExists(section, "position");
			ConfigUtil.assertKeyExists(section, "spawns");
			File schemFile = ConfigUtil.schemFileFromYml(section.getString("schematic"), schemFolder);
			Location schemPos = ConfigUtil.blockPosFromYmlString(section.getString("position"));
			arena = new PbArena(name, schemFile, schemPos, plugin, arenaHandler);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(String.format("Could not load arena %s: %s", name, e.getMessage()));
		}
		ConfigurationSection spawnsSection = section.getConfigurationSection("spawns");

		for (String teamName : spawnsSection.getKeys(false)) {
			TeamType teamType;
			List<String> spawnStrings;
			try {
				teamType = ConfigUtil.teamTypeFromYml(teamName);
				spawnStrings = spawnsSection.getStringList(teamName);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(String.format("Could not load arena %s: %s", name, e.getMessage()));
			}
			try {
				for (String spawnString : spawnStrings) {
					Location spawn = ConfigUtil.spawnFromYmlString(spawnString, arena.getSchemPos().getWorld());
					arena.addSpawn(teamType, spawn);
				}
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(String.format("Could not load arena %s team %s spawns: %s", arena, teamName, e.getMessage()));
			}
		}
		Bukkit.getLogger().log(Level.INFO, String.format("%s loaded", name));
		return arena;
	}
	
	public void assertIsPlayable() {
		if (!schemFile.exists()) {
			throw new IllegalArgumentException(StringUtil.format("Schematic %s for arena %s does not exist.", schemFile.getName(), name));
		}
		for (TeamType teamType : TeamType.values()) {
			if (!teamSpawns.containsKey(teamType) || teamSpawns.get(teamType).isEmpty()) {
				throw new IllegalArgumentException(StringUtil.format(
						"Arena %s does not have any spawn points for team %s. /pb arena add-spawn %s", name, teamType.displayName, teamType.name().toLowerCase()));
			}
		}
	}
}
