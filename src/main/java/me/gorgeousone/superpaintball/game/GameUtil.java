package me.gorgeousone.superpaintball.game;

import me.gorgeousone.superpaintball.util.version.VersionUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class GameUtil {
	
	public static Sound RELOAD_SOUND;
	
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

	public static void setup() {
		RELOAD_SOUND = Sound.valueOf(VersionUtil.IS_LEGACY_SERVER ? "BLOCK_NOTE_HAT" : "BLOCK_NOTE_BLOCK_HAT");
	}

	public static Location cleanSpawn(Location spawn) {
		Vector direction = faceToDirection(yawToFace(spawn.getYaw()));
		spawn.setDirection(direction);
		spawn.setX(spawn.getBlockX() + .5);
		spawn.setY(spawn.getBlockY());
		spawn.setZ(spawn.getBlockZ() + .5);
		return spawn;
	}

	public static String humanBlockPos(Location location) {
		return String.format("x:%d, y:%d, z:%d", location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
}
