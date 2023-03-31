package me.gorgeousone.paintball.equipment;

import me.gorgeousone.paintball.kit.KitType;
import me.gorgeousone.paintball.kit.PbKitHandler;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class IngameEquipment extends Equipment {

	public static final int GUN_SLOT = 0;
	public static final int WATER_BOMB_SLOT = 1;

	private final PbKitHandler kitHandler;

	public IngameEquipment(Consumer<SlotClickEvent> onShoot, Consumer<SlotClickEvent> onThrowWaterBomb, PbKitHandler kitHandler) {
		this.kitHandler = kitHandler;
		setItem(GUN_SLOT, KitType.RIFLE.getGun(), onShoot);
		setItem(WATER_BOMB_SLOT, PbKitHandler.getWaterBombs(), onThrowWaterBomb);
	}

	@Override
	public void equip(Player player) {
		//workaround .-. change gun item in equipment each time a player is being equipped
		items.put(GUN_SLOT, kitHandler.getKitType(player.getUniqueId()).getGun());
		super.equip(player);
	}
}
