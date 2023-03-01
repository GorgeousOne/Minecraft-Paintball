package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.GameHandler;
import me.gorgeousone.superpaintball.kit.KitType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ClickListener implements Listener {
	
	private final GameHandler gameHandler;
	
	public ClickListener(GameHandler gameHandler) {
		this.gameHandler = gameHandler;
	}
	
	@EventHandler
	public void onPlayerFireGun(PlayerInteractEvent event) {
		if (!isMainHand(event) || !isRightClick(event.getAction())) {
			return;
		}
		Player player = event.getPlayer();
		ItemStack heldItem = getHeldItem(player);
		KitType kitType = getKitType(heldItem.getType());
		
		if (kitType == null) {
			return;
		}
		gameHandler.launchShot(player, kitType);
	}
	
	KitType getKitType(Material mat) {
		for (KitType kitType : KitType.values()) {
			if (kitType.gunItem == mat) {
				return kitType;
			}
		}
		return null;
	}
	
	private boolean isMainHand(PlayerInteractEvent event) {
		try {
			return event.getHand() == EquipmentSlot.HAND;
		} catch (NoSuchMethodError legacyError) {
			return true;
		}
	}
	
	private boolean isRightClick(Action action) {
		return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
	}
	
	
	private ItemStack getHeldItem(Player player) {
		try {
			return player.getInventory().getItemInMainHand();
		} catch (NoSuchMethodError legacyError) {
			return player.getItemInHand();
		}
	}
}
