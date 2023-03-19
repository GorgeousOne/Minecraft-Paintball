package me.gorgeousone.superpaintball.team;

import me.gorgeousone.superpaintball.kit.KitType;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TeamUtil {
	
	public static final ItemStack[] DEATH_ARMOR_SET = createColoredArmoSet(TeamType.DEATH_COLOR);
	public static final PotionEffect KNOCKOUT_BLINDNESS = new PotionEffect(PotionEffectType.BLINDNESS, 30, 4);
	public static final int HEARTS_PER_DMG_POINT = 5;
	public static final int DMG_POINTS = 4;
	
	public static ItemStack[] createColoredArmoSet(Color color) {
		ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
		ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
		ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS);
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
		
		LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
		meta.setColor(color);
		
		helmet.setItemMeta(meta);
		chest.setItemMeta(meta);
		legs.setItemMeta(meta);
		boots.setItemMeta(meta);
		return new ItemStack[]{boots, legs, chest, helmet};
	}
	
	public static ArmorStand createSkelly(ItemStack[] deathArmorSet, Player player, TeamType teamType, KitType kitType) {
		Location deathLoc = player.getLocation();
		String name = "" +
				ChatColor.BOLD + ChatColor.MAGIC + "XX" + ChatColor.RESET +
				ChatColor.BOLD + teamType.prefixColor + player.getName() + ChatColor.RESET +
				ChatColor.BOLD + ChatColor.MAGIC + "XX";
		
//		deathLoc.setX((int) deathLoc.getX() + .5);
//		deathLoc.setY((int) deathLoc.getY() + .5);
//		deathLoc.setZ((int) deathLoc.getZ() + .5);
		
		ArmorStand stand = (ArmorStand) player.getWorld().spawnEntity(deathLoc, EntityType.ARMOR_STAND);
		stand.getEquipment().setArmorContents(deathArmorSet);
		stand.setCustomNameVisible(true);
		stand.setCustomName(name);
		stand.setInvulnerable(true);
		stand.setArms(true);
		stand.setBasePlate(false);
		
		try {
			stand.getEquipment().setItem(EquipmentSlot.HAND, kitType.getGun());
		} catch (NoSuchMethodError e) {
			stand.setItemInHand(kitType.getGun());
		}
		return stand;
	}
}
