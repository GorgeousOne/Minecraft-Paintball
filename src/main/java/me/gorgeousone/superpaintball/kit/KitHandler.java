package me.gorgeousone.superpaintball.kit;

import java.util.Map;
import java.util.UUID;

public class KitHandler {
	
	private final Map<UUID, KitType> kitSelection;
	
	public KitHandler(Map<UUID, KitType> kitSelection) {
		this.kitSelection = kitSelection;
	}
	
	public void setKit(UUID playerId, KitType kitType) {
		kitSelection.put(playerId, kitType);
	}
	
	public KitType getKitType(UUID playerId) {
		kitSelection.putIfAbsent(playerId, KitType.RIFLE);
		return kitSelection.get(playerId);
	}
}
