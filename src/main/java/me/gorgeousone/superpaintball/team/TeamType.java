package me.gorgeousone.superpaintball.team;

import me.gorgeousone.superpaintball.util.blocktype.BlockType;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;

public enum TeamType {
	
	EMBER("Ember", EnderPearl.class, Color.RED, ChatColor.RED),
	ICE("Ice", Snowball.class, Color.AQUA, ChatColor.AQUA);
	
	public static final Color DEATH_COLOR = Color.PURPLE;
	public final String displayName;
	public final Class<? extends Projectile> projectileType;
	public final Color armorColor;
	public final ChatColor prefixColor;
	public BlockType blockColor;
	
	TeamType(String displayName,
	         Class<? extends Projectile> projectileType,
	         Color garmentColor,
	         ChatColor prefixColor) {
		this.displayName = displayName;
		this.projectileType = projectileType;
		this.armorColor = garmentColor;
		this.prefixColor = prefixColor;
	}
	
	/**
	 * call setup after BlockType is setup for server version (legacy)
	 */
	public static void setup() {
		EMBER.blockColor = BlockType.get("minecraft:light_blue_terracotta", "stained_clay:14");
		ICE.blockColor = BlockType.get("minecraft:red_terracotta", "stained_clay:3");
	}
}
