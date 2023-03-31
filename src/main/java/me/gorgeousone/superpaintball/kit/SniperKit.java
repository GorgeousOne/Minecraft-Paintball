package me.gorgeousone.superpaintball.kit;

import me.gorgeousone.superpaintball.team.PbTeam;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;

public class SniperKit extends AbstractKit {
	
	public SniperKit() {
		super(KitType.RIFLE, 4, 0, 0, 0, 60, Sound.ENTITY_ARROW_HIT, 1, .5f);
	}

	@Override
	public boolean launchShot(Player player, PbTeam team, Collection<Player> gamePlayers) {
		//TODO complicated stuff
		return false;
	}
}
