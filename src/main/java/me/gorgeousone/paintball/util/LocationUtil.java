package me.gorgeousone.paintball.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class LocationUtil {
	
	private static final BlockFace[] CARDINAL_FACES = {
			BlockFace.NORTH,
			BlockFace.EAST,
			BlockFace.SOUTH,
			BlockFace.WEST};
	
	public static BlockFace yawToFace(float yaw) {
		return CARDINAL_FACES[Math.round(yaw / 90f) & 0x3].getOppositeFace();
	}
	
	public static Vector faceToDirection(BlockFace face) {
		return new Vector(face.getModX(), face.getModY(), face.getModZ());
	}
	
	/**
	 * Returns location centered to the middle of the block and facing rounded to the nearest cardinal direction.
	 *
	 * @param spawn
	 * @return
	 */
	public static Location cleanSpawn(Location spawn) {
		Vector direction = faceToDirection(yawToFace(spawn.getYaw()));
		spawn.setDirection(direction);
		spawn.setX(spawn.getBlockX() + .5);
		spawn.setY(spawn.getBlockY());
		spawn.setZ(spawn.getBlockZ() + .5);
		return spawn;
	}
	
	/**
	 * Executes a teleportation 1 tick later to prevent "Player moved too quickly" warnings.
	 */
	public static void tpTick(Player player, Location location, JavaPlugin plugin) {
		new BukkitRunnable() {
			@Override
			public void run() {
				player.teleport(location);
			}
		}.runTaskLater(plugin, 1);
	}
	
	public static String humanBlockPos(Location location) {
		return String.format("x:%d, y:%d, z:%d", location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
	
	public static int getWorldMinY(World world) {
		try {
			return world.getMinHeight();
		} catch (NoSuchMethodError e) {
			return 0;
		}
	}
}
