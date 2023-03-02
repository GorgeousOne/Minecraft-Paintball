package me.gorgeousone.superpaintball.kit;

import me.gorgeousone.superpaintball.team.Team;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SniperKit extends AbstractKit {
	
	public SniperKit() {
		super(KitType.SNIPER, 4, 0, 0, 0, 3.5f, Sound.ENTITY_ARROW_HIT, 1);
	}
	
	@Override
	public void launchShot(Player player, Team team) {
		//TODO complicated stuff
	}
}
