package me.gorgeousone.paintball.kit;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Class for the rifle kit, which shoots a single high-range and accurate bullet.
 */
public class RifleKit extends AbstractKit {
	
	public RifleKit() {
		super(KitType.RIFLE, 3, 1, 1.75f, 0.02f, 10, Sound.ENTITY_CHICKEN_EGG, 1.35f, 1.15f);
	}
	
	@Override
	public void prepPlayer(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, 0, false, false, false));
	}
}
