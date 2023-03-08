package me.gorgeousone.superpaintball.game;

import me.gorgeousone.superpaintball.util.version.VersionUtil;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;

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
	
	public static void setup() {
		RELOAD_SOUND = Sound.valueOf(VersionUtil.IS_LEGACY_SERVER ? "BLOCK_NOTE_HAT" : "BLOCK_NOTE_BLOCK_HAT");
	}
	
	public static String humanBlockPos(Location location) {
		return String.format("x:%o, y:%o, z:%o", location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
}
