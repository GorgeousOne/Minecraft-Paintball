package me.gorgeousone.paintball.event;

import me.gorgeousone.paintball.equipment.Equipment;
import me.gorgeousone.paintball.equipment.SlotClickEvent;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import me.gorgeousone.paintball.game.PbLobby;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.UUID;

public class ItemUseListener implements Listener {
	
	private final PbLobbyHandler lobbyHandler;

	public ItemUseListener(PbLobbyHandler lobbyHandler) {
		this.lobbyHandler = lobbyHandler;
	}
	
	@EventHandler
	public void onPlayerItemUse(PlayerInteractEvent event) {
		if (!isMainHand(event) || !isRightClick(event.getAction())) {
			return;
		}
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		PbLobby lobby = lobbyHandler.getLobby(playerId);
		
		if (lobby == null) {
			return;
		}
		Equipment equip = lobby.getEquip();

		if (equip == null) {
			event.setCancelled(true);
			return;
		}
		int slot = player.getInventory().getHeldItemSlot();
		SlotClickEvent clickEvent = lobby.getEquip().onClickSlot(player, slot);

		if (clickEvent != null && clickEvent.isCancelled()) {
			event.setCancelled(true);
		}
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
}
