package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.equipment.Equipment;
import me.gorgeousone.superpaintball.equipment.IngameEquipment;
import me.gorgeousone.superpaintball.equipment.SlotClickEvent;
import me.gorgeousone.superpaintball.game.PbLobby;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public class SkellyInteractListener implements Listener {
	
	private final PbLobbyHandler lobbyHandler;
	
	public SkellyInteractListener(PbLobbyHandler lobbyHandler) {
		this.lobbyHandler = lobbyHandler;
	}
	
	@EventHandler
	public void onEntityInteract(PlayerInteractAtEntityEvent event) {
		if (!(event.getRightClicked() instanceof ArmorStand)) {
			return;
		}
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		PbLobby lobby = lobbyHandler.getLobby(playerId);
		
		if (lobby == null) {
			return;
		}
		event.setCancelled(true);
		Equipment equip = lobby.getEquip();

		if (equip == null) {
			return;
		}
		PlayerInventory inv = player.getInventory();
		int slot = inv.getHeldItemSlot();

		//TODO i dont think this is good at all
		if (slot != IngameEquipment.WATER_BOMB_SLOT) {
			return;
		}
		SlotClickEvent clickEvent = equip.onClickSlot(player, slot);

		if (!clickEvent.isCancelled()) {
			throwPotion(inv, slot);
		}
	}

	private void throwPotion(PlayerInventory inv, int slot) {
		ItemStack heldItem = inv.getItem(slot);
		ThrownPotion waterBomb = inv.getHolder().launchProjectile(ThrownPotion.class);
		waterBomb.setItem(heldItem);
		heldItem.setAmount(heldItem.getAmount() - 1);
		inv.setItem(slot, heldItem);
	}
}
