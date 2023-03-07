package me.gorgeousone.superpaintball.game;

import me.gorgeousone.superpaintball.util.version.VersionUtil;
import org.bukkit.Location;
import org.bukkit.Sound;

public class GameUtil {
	
	public static Sound RELOAD_SOUND;
	
	public static void setup() {
		RELOAD_SOUND = Sound.valueOf(VersionUtil.IS_LEGACY_SERVER ? "BLOCK_NOTE_HAT" : "BLOCK_NOTE_BLOCK_HAT");
	}
	
	public static String humanBlockPos(Location location) {
		return String.format("X:%o, Y:%o, Z:%o", location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
}
