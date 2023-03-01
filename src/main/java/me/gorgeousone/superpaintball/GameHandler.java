package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.kit.AbstractKit;
import me.gorgeousone.superpaintball.kit.RifleKit;
import me.gorgeousone.superpaintball.kit.KitType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GameHandler {
	
	private final Map<KitType, AbstractKit> kits;
	
	public GameHandler() {
		this.kits = new HashMap<>();
		kits.put(KitType.RIFLE, new RifleKit());
	}
	
	
	public void launchShot(Player player, KitType kitType) {
		AbstractKit kit = kits.get(kitType);
		kit.launchShot(player);
	}
}
