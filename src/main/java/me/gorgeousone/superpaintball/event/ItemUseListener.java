package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.game.PbLobby;
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

public class ItemUseListener implements Listener {
	
	private final PbLobbyHandler lobbyHandler;
	private final PbKitHandler kitHandler;
	
	public ItemUseListener(PbLobbyHandler lobbyHandler, PbKitHandler kitHandler) {
		this.lobbyHandler = lobbyHandler;
		this.kitHandler = kitHandler;
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
		ItemStack heldItem = getHeldItem(player);
		System.out.println(lobby.getState());
		switch (lobby.getState()) {
			case LOBBYING:
				onLobbyItemInteract(player, heldItem, event, lobby);
				break;
			case RUNNING:
				onArenaItemInteract(player, heldItem, event, lobby);
				break;
			case COUNTING_DOWN:
			case OVER:
				event.setCancelled(true);
				break;
		}
	}

	private void onLobbyItemInteract(Player player, ItemStack item, PlayerInteractEvent event, PbLobby lobby) {
		KitType kitType = getKitType(item.getType());

		if (kitType != null) {
			PbKitHandler.openKitSelector(player);
		}
	}

	private void onArenaItemInteract(Player player, ItemStack item, PlayerInteractEvent event, PbLobby lobby) {
		UUID playerId = player.getUniqueId();

		if (!lobby.getTeam(playerId).getAlivePlayers().contains(playerId)) {
			event.setCancelled(true);
			return;
		}
		KitType kitType = getKitType(item.getType());

		if (kitType == null) {
			return;
		}
		lobby.launchShot(player, PbKitHandler.getKit(kitType));
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
