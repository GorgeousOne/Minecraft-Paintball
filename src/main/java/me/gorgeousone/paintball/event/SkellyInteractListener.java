package me.gorgeousone.paintball.event;

import me.gorgeousone.paintball.equipment.Equipment;
import me.gorgeousone.paintball.equipment.IngameEquipment;
import me.gorgeousone.paintball.equipment.SlotClickEvent;
import me.gorgeousone.paintball.game.PbLobby;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

/**
 * Listener to manually throw water bombs when right-clicking a skelly.
 * Because splash potions only get thrown when right-clicking the air and that's unintuitive.
 */
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
