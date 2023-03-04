package me.gorgeousone.superpaintball.kit;

import me.gorgeousone.superpaintball.team.PbTeam;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SniperKit extends AbstractKit {
	
	public SniperKit() {
		super(KitType.SNIPER, 4, 0, 0, 0, 60, Sound.ENTITY_ARROW_HIT, 1);
	}
	
	@Override
	public long launchShot(Player player, PbTeam team) {
		//TODO complicated stuff
		return fireRate;
	}
}
