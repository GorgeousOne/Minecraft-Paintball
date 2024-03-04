package me.gorgeousone.paintball.kit;

import me.gorgeousone.paintball.team.PbTeam;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Class for the sniper kit, which fires a hitscan bullet if sneak-scoped. With increasing damage if scoped for longer.
 */
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
