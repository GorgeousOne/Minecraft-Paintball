package me.gorgeousone.paintball.util;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

public class SchemUtil {
	
	private static final String BACKUPS_FOLDER = "/backups/arenas/";
	
	public static void pasteSchem(File file, Location origin) throws IOException {
		Clipboard clipboard = loadClipboard(file);
		pasteClipboard(clipboard, origin, true);
	}
	
	public static void pasteSchemWithBackup(File file, Location origin, String backupName, JavaPlugin plugin) throws IOException {
		Clipboard clipboard = loadClipboard(file);
		World weWorld = BukkitAdapter.adapt(origin.getWorld());
		BlockVector3 originVec = BlockVector3.at(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
		BlockVector3 minPoint = originVec.add(clipboard.getMinimumPoint().subtract(clipboard.getOrigin()));
		
		File backupFile = new File(plugin.getDataFolder() + BACKUPS_FOLDER + backupName + ".schem");
		Clipboard backup = copyCuboid(weWorld, minPoint, clipboard.getDimensions());
		backup.setOrigin(originVec);
		
		saveClipboard(backup, backupFile);
		pasteClipboard(clipboard, origin, true);
	}
	
	public static void resetSchemFromBackup(String backupName, Location origin, JavaPlugin plugin) throws IOException {
		File backupFile = new File(plugin.getDataFolder() + BACKUPS_FOLDER + backupName + ".schem");
		Clipboard backup = loadClipboard(backupFile);
		pasteClipboard(backup, origin, false);
		backupFile.delete();
	}
	
	private static Clipboard loadClipboard(File file) throws IOException {
		ClipboardFormat format = ClipboardFormats.findByFile(file);
		ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()));
		return reader.read();
	}
	
	private static BlockArrayClipboard copyCuboid(World weWorld, BlockVector3 origin, BlockVector3 dimensions) {
		CuboidRegion region = new CuboidRegion(origin, origin.add(dimensions));
		BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
		EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1);
		ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
		
		try {
			Operations.complete(forwardExtentCopy);
		} catch (WorldEditException e) {
			throw new RuntimeException(e);
		}
		return clipboard;
	}
	
	private static void saveClipboard(Clipboard clipboard, File file) {
		File parent = file.getParentFile();
		
		if (!parent.exists()) {
			parent.mkdirs();
		}
		try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(Files.newOutputStream(file.toPath()))) {
			writer.write(clipboard);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void pasteClipboard(Clipboard clipboard, Location origin, boolean ignoreAir) {
		World weWorld = BukkitAdapter.adapt(origin.getWorld());
		
		try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(weWorld, -1)) {
			Operation operation = new ClipboardHolder(clipboard)
					.createPaste(editSession)
					.to(BlockVector3.at(origin.getX(), origin.getY(), origin.getZ()))
					.ignoreAirBlocks(ignoreAir)
					.build();
			Operations.complete(operation);
		} catch (WorldEditException e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
