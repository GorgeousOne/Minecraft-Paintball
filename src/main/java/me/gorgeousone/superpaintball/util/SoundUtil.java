package me.gorgeousone.superpaintball.util;

import me.gorgeousone.superpaintball.util.version.VersionUtil;
import org.bukkit.Sound;

public class SoundUtil {
	public static Sound RELOAD_SOUND;
	public static Sound GAME_START_SOUND;
	public static Sound COUNTDOWN_SOUND;
	
	public static void setup() {
		RELOAD_SOUND = Sound.valueOf(VersionUtil.IS_LEGACY_SERVER ? "BLOCK_NOTE_HAT" : "BLOCK_NOTE_BLOCK_HAT");
		GAME_START_SOUND = Sound.valueOf(VersionUtil.IS_LEGACY_SERVER ? "BLOCK_NOTE_HARP" : "BLOCK_NOTE_BLOCK_HARP");
		COUNTDOWN_SOUND = Sound.valueOf(VersionUtil.IS_LEGACY_SERVER ? "BLOCK_NOTE_PLING" : "BLOCK_NOTE_BLOCK_PLING");
	}
}
