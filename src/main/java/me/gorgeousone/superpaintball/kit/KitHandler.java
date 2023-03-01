package me.gorgeousone.superpaintball.kit;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class KitHandler {
	
	private final Map<UUID, KitType> kitSelection;
	
	public KitHandler(Map<UUID, KitType> kitSelection) {
		this.kitSelection = kitSelection;
	}
	
	public void setKit(Player player, KitType kitType) {
		UUID playerId = player.getUniqueId();
		kitSelection.put(playerId, kitType);
	}
	
	public KitType getKitType(Player player) {
		UUID playerId = player.getUniqueId();
		kitSelection.putIfAbsent(playerId, KitType.RIFLE);
		return kitSelection.get(playerId);
	}
}
