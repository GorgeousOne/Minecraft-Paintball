package me.gorgeousone.superpaintball.game;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import me.gorgeousone.superpaintball.team.TeamType;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PbArena {
	
	private static final BlockFace[] CARDINAL_FACES = {
			BlockFace.NORTH,
			BlockFace.EAST,
			BlockFace.SOUTH,
			BlockFace.WEST};
	
	private final String name;
	private File schemFile;
	private Location schemPos;
	private final Map<TeamType, List<Location>> teamSpawns;
	
	public PbArena(String name, File schemFile, Location schemPos) {
		this(name, schemFile, schemPos, new HashMap<>());
	}
	
	public PbArena(String name, File schemFile, Location schemPos, Map<TeamType, List<Location>> teamSpawns) {
		this.name = name;
		this.schemFile = schemFile;
		this.schemPos = schemPos;
		this.teamSpawns = teamSpawns;
		
		schemPos.setX(schemPos.getBlockX());
		schemPos.setY(schemPos.getBlockY());
		schemPos.setZ(schemPos.getBlockZ());
	}
	
	public void addSpawn(TeamType teamType, Location spawnPos) {
		spawnPos.setDirection(yawToFace(spawnPos.getYaw()).getDirection());
		spawnPos.setX(spawnPos.getBlockX() + .5);
		spawnPos.setY(spawnPos.getBlockY());
		spawnPos.setZ(spawnPos.getBlockZ() + .5);

		teamSpawns.computeIfAbsent(teamType, v -> new ArrayList<>());
		teamSpawns.get(teamType).add(spawnPos);
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
					.ignoreAirBlocks(false)
					.build();
			Operations.complete(operation);
		} catch (WorldEditException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static BlockFace yawToFace(float yaw) {
		return CARDINAL_FACES[Math.round(yaw / 90f) & 0x3].getOppositeFace();
	}
	
	public String getName() {
		return name;
	}
	
	public Location getSchemPos() {
		return schemPos.clone();
	}
}
