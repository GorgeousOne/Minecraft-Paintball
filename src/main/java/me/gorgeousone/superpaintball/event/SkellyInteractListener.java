package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.GameHandler;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SkellyInteractListener implements Listener {
	
	private final GameHandler gameHandler;
	
	public SkellyInteractListener(GameHandler gameHandler) {
		this.gameHandler = gameHandler;
	}
	
	@EventHandler
	public void onEntityInteract(PlayerInteractAtEntityEvent event) {
		if (!(event.getRightClicked() instanceof ArmorStand)) {
			return;
		}
		Player player = event.getPlayer();
		
		//idk i'm a bit uncreative rn
		if (!gameHandler.isPlaying(player.getUniqueId())) {
			return;
		}
		event.setCancelled(true);
		PlayerInventory inv = player.getInventory();
		ItemStack heldItem = inv.getItemInMainHand();
		
		if (!gameHandler.getWaterBombs().isSimilar(heldItem)) {
			return;
		}
		ThrownPotion waterBomb = player.launchProjectile(ThrownPotion.class);
		waterBomb.setItem(heldItem);
		heldItem.setAmount(heldItem.getAmount() - 1);
		inv.setItemInMainHand(heldItem);
	}
}
