package me.gorgeousone.superpaintball.equipment;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Equipment {

	protected final Map<Integer, ItemStack> items;
	protected final Map<Integer, Consumer<SlotClickEvent>> onClicks;

	public Equipment() {
		this.items = new HashMap<>();
		this.onClicks = new HashMap<>();
	}

	public void equip(Player player) {
		PlayerInventory inv = player.getInventory();

		for (int slot : items.keySet()) {
			inv.setItem(slot, items.get(slot).clone());
		}
	}

	public void setItem(int slot, ItemStack item, Consumer<SlotClickEvent> onClick) {
		if (slot < 0 || slot > 8) {
			throw new IllegalArgumentException("Slot must be on hotbar between 0 and 8");
		}
		items.put(slot, item);
		onClicks.put(slot, onClick);
	}

	public SlotClickEvent onClickSlot(Player player, int slot) {

		if (!items.containsKey(slot)) {
			return null;
		}
		Consumer<SlotClickEvent> onClick = onClicks.get(slot);

		if (onClick != null) {
			SlotClickEvent event = new SlotClickEvent(player, slot, items.get(slot).clone());
			onClick.accept(event);
			return event;
		}
		return null;
	}
}
