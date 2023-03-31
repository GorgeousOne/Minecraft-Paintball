package me.gorgeousone.paintball.team;

import me.gorgeousone.paintball.util.StringUtil;
import me.gorgeousone.paintball.util.blocktype.BlockType;
import me.gorgeousone.paintball.util.version.VersionUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;

public enum TeamType {
	
	EMBER("Ember", EnderPearl.class, Color.RED, ChatColor.RED, Particle.ITEM_CRACK, new ItemStack(Material.REDSTONE_BLOCK)),
	ICE("Ice", Snowball.class, Color.AQUA, ChatColor.AQUA, Particle.SNOWBALL, null);
	
	public static final Color DEATH_COLOR = Color.PURPLE;

	public final String displayName;
	public final Class<? extends Projectile> projectileType;
	public final Color armorColor;
	public final ChatColor prefixColor;
	public BlockType blockColor;
	public final Particle blockParticle;
	public final Object particleExtra;
	private ItemStack joinItem;

	TeamType(String displayName,
	         Class<? extends Projectile> projectileType,
	         Color garmentColor,
	         ChatColor prefixColor,
	         Particle blockParticle, Object particleExtra) {
		this.particleExtra = particleExtra;
		this.displayName = "" + prefixColor + ChatColor.BOLD + displayName + StringUtil.MSG_COLOR;
		this.projectileType = projectileType;
		this.armorColor = garmentColor;
		this.prefixColor = prefixColor;
		this.blockParticle = blockParticle;
	}

	public ItemStack getJoinItem() {
		return joinItem.clone();
	}
	
	/**
	 * call setup after BlockType is setup for server version (legacy)
	 */
	public static void setup() {
		EMBER.blockColor = BlockType.get("minecraft:red_terracotta", "stained_clay:14");
		ICE.blockColor = BlockType.get("minecraft:light_blue_terracotta", "stained_clay:3");

		EMBER.joinItem = createWool("RED_WOOL", (short) 14);
		ICE.joinItem = createWool("LIGHT_BLUE_WOOL", (short) 3);
	}

	private static ItemStack createWool(String newName, short magicVal) {
		return VersionUtil.IS_LEGACY_SERVER ?
			new ItemStack(Material.valueOf("WOOL"), 1, magicVal) :
			new ItemStack(Material.valueOf(newName));
	}
}
