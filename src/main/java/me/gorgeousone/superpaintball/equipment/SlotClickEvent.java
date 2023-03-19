package me.gorgeousone.superpaintball.equipment;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SlotClickEvent {

	private final Player player;
	private final int clickedSlot;
	private final ItemStack clickedItem;
	private boolean isCancelled;

	public SlotClickEvent(Player player, int clickedSlot, ItemStack clickedItem) {
		this.player = player;
		this.clickedSlot = clickedSlot;
		this.clickedItem = clickedItem;
	}

	public Player getPlayer() {
		return player;
	}

	public int getClickedSlot() {
		return clickedSlot;
	}

	public ItemStack getClickedItem() {
		return clickedItem;
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	public void setCancelled(boolean cancelled) {
		isCancelled = cancelled;
	}
}
