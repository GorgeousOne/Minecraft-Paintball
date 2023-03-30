package me.gorgeousone.superpaintball.kit;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class RifleKit extends AbstractKit {
	
	public RifleKit() {
		super(KitType.RIFLE, 3, 1, 2.25f, 0.02f, 10, Sound.ENTITY_CHICKEN_EGG, 1.35f, 1.15f);
	}
	
	@Override
	public void prepPlayer(Player player) {
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, 0, false, false, false));
	}
}
