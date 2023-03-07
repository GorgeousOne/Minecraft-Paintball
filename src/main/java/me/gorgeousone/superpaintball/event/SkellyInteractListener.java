package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.game.GameState;
import me.gorgeousone.superpaintball.game.PbLobby;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
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
		boolean canPlayerInteract = lobby.getState() == GameState.RUNNING && lobby.getTeam(playerId).getAlivePlayers().contains(playerId);
		
		if (!canPlayerInteract) {
			return;
		}
		PlayerInventory inv = player.getInventory();
		ItemStack heldItem = inv.getItemInMainHand();
		
		if (!PbKitHandler.getWaterBombs().isSimilar(heldItem)) {
			return;
		}
		ThrownPotion waterBomb = player.launchProjectile(ThrownPotion.class);
		waterBomb.setItem(heldItem);
		heldItem.setAmount(heldItem.getAmount() - 1);
		inv.setItemInMainHand(heldItem);
	}
}
