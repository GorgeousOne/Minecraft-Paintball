package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
		
		//idk i'm a bit uncreative rn
		if (!lobbyHandler.isPlaying(player.getUniqueId())) {
			return;
		}
		event.setCancelled(true);
		PlayerInventory inv = player.getInventory();
		ItemStack heldItem = inv.getItemInMainHand();
		
		if (!lobbyHandler.getWaterBombs().isSimilar(heldItem)) {
			return;
		}
		ThrownPotion waterBomb = player.launchProjectile(ThrownPotion.class);
		waterBomb.setItem(heldItem);
		heldItem.setAmount(heldItem.getAmount() - 1);
		inv.setItemInMainHand(heldItem);
	}
}
