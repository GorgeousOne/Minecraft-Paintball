package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.game.MapVoting;
import me.gorgeousone.superpaintball.game.PbLobby;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class InventoryListener implements Listener {

	private final PbLobbyHandler lobbyHandler;
	private final PbKitHandler kitHandler;

	public InventoryListener(PbLobbyHandler lobbyHandler, PbKitHandler kitHandler) {
		this.lobbyHandler = lobbyHandler;
		this.kitHandler = kitHandler;
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();

		if (lobbyHandler.isPlaying(player.getUniqueId())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();

		if (lobbyHandler.isPlaying(player.getUniqueId())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryEdit(InventoryClickEvent event) {
		HumanEntity entity = event.getWhoClicked();

		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		UUID playerId = player.getUniqueId();
		PbLobby lobby = lobbyHandler.getLobby(playerId);

		if (lobby == null) {
			return;
		}
		event.setCancelled(true);
		String title = event.getView().getTitle();
		Inventory inv = event.getInventory();
		int clickedSlot = event.getSlot();
		
		switch (title) {
			case PbKitHandler.KIT_SELECT_UI_TITLE:
				boolean changedKit = kitHandler.onSelectKit(player, inv, clickedSlot);
				
				if (changedKit) {
					lobby.getEquip().equip(player);
				}
				break;
			case MapVoting.MAP_VOTE_UI_TITLE:
				lobby.addMapVote(player, inv, clickedSlot);
				break;
		}
	}
}
