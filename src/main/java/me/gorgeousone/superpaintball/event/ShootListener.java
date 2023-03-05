package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.GameHandler;
import me.gorgeousone.superpaintball.GameState;
import me.gorgeousone.superpaintball.PbGame;
import me.gorgeousone.superpaintball.kit.KitType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ShootListener implements Listener {
	
	private final GameHandler gameHandler;
	
	public ShootListener(GameHandler gameHandler) {
		this.gameHandler = gameHandler;
	}
	
	@EventHandler
	public void onPlayerItemUse(PlayerInteractEvent event) {
		if (!isMainHand(event) || !isRightClick(event.getAction())) {
			return;
		}
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		PbGame game = gameHandler.getGame(playerId);
		
		if (game == null) {
			return;
		}
		ItemStack heldItem = getHeldItem(player);
		boolean canPlayerInteract = game.getState() == GameState.RUNNING && game.getTeam(playerId).getAlivePlayers().contains(playerId);
		
		if (!canPlayerInteract) {
			event.setCancelled(true);
			return;
		}
		KitType kitType = getKitType(heldItem.getType());
		
		if (kitType == null) {
			return;
		}
		game.launchShot(player, gameHandler.getKit(kitType));
	}
	
	KitType getKitType(Material mat) {
		for (KitType kitType : KitType.values()) {
			if (kitType.gunMaterial == mat) {
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
