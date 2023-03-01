package me.gorgeousone.superpaintball.team;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;

public enum TeamType {

	NETHER("Nether", Snowball.class, Color.RED, ChatColor.RED),
	FROST("Frost", EnderPearl.class, Color.AQUA, ChatColor.AQUA);
	
	public final String displayName;
	public final Class<? extends Projectile> projectileType;
	public final Color garmentColor;
	public final ChatColor prefixColor;
	
	TeamType(String displayName, Class<? extends Projectile> projectileType, Color garmentColor, ChatColor prefixColor) {
		this.displayName = displayName;
		this.projectileType = projectileType;
		this.garmentColor = garmentColor;
		this.prefixColor = prefixColor;
	}
}
