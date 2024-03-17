package me.gorgeousone.paintball.equipment;

import me.gorgeousone.paintball.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Class to create and manage hotbar items with custom click functionality for players.
 */
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
			ItemStack item = items.get(slot);
			ItemStack oldItem = inv.getItem(slot);
			
			//this is a stupid workaround so re-equipping after changing kit won't replace team queuing enchantment glow
			if (oldItem == null || oldItem.getType() != item.getType()) {
				inv.setItem(slot, items.get(slot).clone());
			}
		}
	}
	
	/**
	 * Updates item names in the player's inventory if the items are already in the inventory.
	 */
	public void updateNames(Player player) {
		PlayerInventory inv = player.getInventory();
		
		for (int slot : items.keySet()) {
			ItemStack newItem = items.get(slot);
			ItemStack oldItem = inv.getItem(slot);
			
			if (oldItem != null && oldItem.getType() == newItem.getType()) {
				ItemUtil.nameItem(oldItem, newItem.getItemMeta().getDisplayName());
				inv.setItem(slot, oldItem);
			}
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
