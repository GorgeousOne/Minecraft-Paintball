package me.gorgeousone.paintball.util;

import me.gorgeousone.paintball.util.version.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Locale;

public abstract class SoundUtil {

	public static Sound COUNTDOWN_SOUND;
	public static Sound GAME_START_SOUND;
	public static Sound LOBBY_JOIN_SOUND;
	public static Sound LOBBY_LEAVE_SOUND;

	public static Sound RIFLE_SHOOT_SOUND;
	public static Sound SHOTGUN_SHOOT_SOUND;
	public static Sound MACHINE_GUN_SHOOT_SOUND;
	public static Sound WATER_BOMB_THROW_SOUND;
	public static Sound WATER_BOMB_EXPLODE_SOUND;

	public static Sound BULLET_HIT_SOUND;
	public static Sound PAINT_PLAYER_SOUND;
	public static Sound REVIVE_PLAYER_SOUND;

	public static final Sound DEFEAT_SOUND = resolveSound("ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP");
	public static final Sound RELOAD_SOUND = resolveSound("ENTITY_EXPERIENCE_ORB_PICKUP", "ORB_PICKUP");
	public static final Sound ENEMY_HIT_SOUND = resolveSound("ENTITY_ARROW_HIT_PLAYER", "SUCCESSFUL_HIT");
	public static final Sound TEAM_WIN_SOUND = resolveSound("ENTITY_PLAYER_LEVELUP", "LEVEL_UP");

	public static void setup() {
		if (VersionUtil.IS_LEGACY_SERVER) {
			COUNTDOWN_SOUND = resolveSound("NOTE_PLING", "NOTE_PLING");
			GAME_START_SOUND = resolveSound("NOTE_BASS", "NOTE_BASS");
			LOBBY_JOIN_SOUND = resolveSound("ITEM_PICKUP", "ITEM_PICKUP");
			LOBBY_LEAVE_SOUND = resolveSound("ITEM_PICKUP", "ITEM_PICKUP");

			RIFLE_SHOOT_SOUND = resolveSound("CHICKEN_EGG_POP", "CHICKEN_EGG_POP");
			SHOTGUN_SHOOT_SOUND = resolveSound("ZOMBIE_WOOD", "ZOMBIE_WOOD");
			MACHINE_GUN_SHOOT_SOUND = resolveSound("ITEM_PICKUP", "ITEM_PICKUP");
			WATER_BOMB_THROW_SOUND = resolveSound("SHOOT_ARROW", "SHOOT_ARROW");
			WATER_BOMB_EXPLODE_SOUND = resolveSound("SPLASH", "SPLASH");

			BULLET_HIT_SOUND = resolveSound("SLIME_WALK", "SLIME_WALK");
			PAINT_PLAYER_SOUND = resolveSound("SPLASH2", "SPLASH2");
			REVIVE_PLAYER_SOUND = resolveSound("SWIM", "SWIM");
		} else {
			COUNTDOWN_SOUND = resolveSound("BLOCK_NOTE_BLOCK_PLING", "BLOCK_NOTE_PLING");
			GAME_START_SOUND = resolveSound("BLOCK_NOTE_BLOCK_BASS", "BLOCK_NOTE_BASS");
			LOBBY_JOIN_SOUND = resolveSound("ENTITY_ITEM_PICKUP", "ENTITY_ITEM_PICKUP");
			LOBBY_LEAVE_SOUND = resolveSound("ENTITY_ITEM_PICKUP", "ENTITY_ITEM_PICKUP");

			RIFLE_SHOOT_SOUND = resolveSound("ENTITY_CHICKEN_EGG", "ENTITY_CHICKEN_EGG");
			SHOTGUN_SHOOT_SOUND = resolveSound("ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR", "ENTITY_ZOMBIE_ATTACK_DOOR_WOOD");
			MACHINE_GUN_SHOOT_SOUND = resolveSound("ENTITY_ITEM_PICKUP", "ENTITY_ITEM_PICKUP");
			WATER_BOMB_THROW_SOUND = resolveSound("ENTITY_ARROW_SHOOT", "ENTITY_ARROW_SHOOT");
			WATER_BOMB_EXPLODE_SOUND = resolveSound("ENTITY_PLAYER_SPLASH", "ENTITY_BOBBER_SPLASH");

			BULLET_HIT_SOUND = resolveSound("BLOCK_SLIME_BLOCK_STEP", "BLOCK_SLIME_STEP");
			PAINT_PLAYER_SOUND = resolveSound("ENTITY_PLAYER_SPLASH_HIGH_SPEED", "ENTITY_PLAYER_SPLASH");
			REVIVE_PLAYER_SOUND = resolveSound("ENTITY_PLAYER_SWIM", "ENTITY_PLAYER_SWIM");
		}
	}

	private static Sound resolveSound(String... candidates) {
		for (String name : candidates) {
			if (name == null || name.isEmpty()) {
				continue;
			}
			try {
				return Sound.valueOf(name);
			} catch (IllegalArgumentException ignored) {
			}
			try {
				NamespacedKey key = NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT).replace('_', '.'));
				Sound sound = Registry.SOUNDS.get(key);
				if (sound != null) {
					return sound;
				}
			} catch (Throwable ignored) {
			}
		}
		Bukkit.getLogger().warning("[MinecraftPaintball] Could not resolve sound: " + String.join(", ", candidates));
		return null;
	}

	public static void playSound(Sound sound, Location location, float volume, float pitch) {
		if (sound != null && location != null && location.getWorld() != null) {
			location.getWorld().playSound(location, sound, volume, pitch);
		}
	}

	public static void playSound(Sound sound, Player player, float volume, float pitch) {
		if (sound != null && player != null) {
			player.playSound(player.getLocation(), sound, volume, pitch);
		}
	}
}
