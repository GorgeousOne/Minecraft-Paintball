package me.gorgeousone.superpaintball.arena;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import me.gorgeousone.superpaintball.util.LocationUtil;
import me.gorgeousone.superpaintball.team.TeamType;
import me.gorgeousone.superpaintball.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PbArena {

	private final PbArenaHandler arenaHandler;
	private final String name;
	private File schemFile;
	private Location schemPos;
	private final Map<TeamType, List<Location>> teamSpawns;

	public PbArena(String name, File schemFile, Location schemPos, PbArenaHandler arenaHandler) {
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

	public Location getSchemPos() {
		return schemPos.clone();
	}

	public void addSpawn(TeamType teamType, Location spawnPos) {
		LocationUtil.cleanSpawn(spawnPos);
		teamSpawns.computeIfAbsent(teamType, v -> new ArrayList<>());
		teamSpawns.get(teamType).add(spawnPos);
		arenaHandler.saveArena(this);
	}

	public void reset() {
		ClipboardFormat format = ClipboardFormats.findByFile(schemFile);
		Clipboard clipboard;

		try (ClipboardReader reader = format.getReader(Files.newInputStream(schemFile.toPath()))) {
			clipboard = reader.read();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		World weWorld = BukkitAdapter.adapt(schemPos.getWorld());

		try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1)) {
			Operation operation = new ClipboardHolder(clipboard)
					.createPaste(editSession)
					.to(BlockVector3.at(schemPos.getX(), schemPos.getY(), schemPos.getZ()))
					.ignoreAirBlocks(true)
					.build();
			Operations.complete(operation);
		} catch (WorldEditException e) {
			throw new RuntimeException(e);
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

	public static PbArena fromYml(String name, ConfigurationSection parentSection, String schemFolder, PbArenaHandler arenaHandler) {
		ConfigurationSection section = parentSection.getConfigurationSection(name);
		PbArena arena;
		try {
			ConfigUtil.assertKeyExists(section, "schematic");
			ConfigUtil.assertKeyExists(section, "position");
			ConfigUtil.assertKeyExists(section, "spawns");
			File schemFile = ConfigUtil.schemFileFromYml(section.getString("schematic"), schemFolder);
			Location schemPos = ConfigUtil.blockPosFromYmlString(section.getString("position"));
			arena = new PbArena(name, schemFile, schemPos, arenaHandler);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(String.format("Could not load arena '%s': %s", name, e.getMessage()));
		}
		ConfigurationSection spawnsSection = section.getConfigurationSection("spawns");

		for (String teamName : spawnsSection.getKeys(false)) {
			TeamType teamType;
			List<String> spawnStrings;
			try {
				teamType = ConfigUtil.teamTypeFromYml(teamName);
				spawnStrings = spawnsSection.getStringList(teamName);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(String.format("Could not load arena '%s': %s", name, e.getMessage()));
			}
			try {
				for (String spawnString : spawnStrings) {
					Location spawn = ConfigUtil.spawnFromYmlString(spawnString, arena.getSchemPos().getWorld());
					arena.addSpawn(teamType, spawn);
				}
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(String.format("Could not load arena '%s' team '%s' spawns: %s", arena, teamName, e.getMessage()));
			}
		}
		Bukkit.getLogger().log(Level.INFO, String.format("'%s' loaded", name));
		return arena;
	}

	public void assertIsPlayable() {
		if (!schemFile.exists()) {
			throw new IllegalArgumentException(String.format("Schematic '%s' for arena %s does not exist.", schemFile.getName(), name));
		}
		for (TeamType teamType : TeamType.values()) {
			if (!teamSpawns.containsKey(teamType) || teamSpawns.get(teamType).isEmpty()) {
				throw new IllegalArgumentException(String.format(
						"Arena '%s' does not have any spawn points for team %s. /pb arena add-spawn %s", name, teamType.displayName, teamType.name().toLowerCase()));
			}
		}
	}
}
