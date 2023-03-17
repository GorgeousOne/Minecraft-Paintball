package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.game.PbLobby;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class InventoryListener implements Listener {

	private final PbLobbyHandler lobbyHandler;
	private final PbKitHandler kitHandler;

	public InventoryListener(PbLobbyHandler lobbyHandler, PbKitHandler kitHandler) {
		this.lobbyHandler = lobbyHandler;
		this.kitHandler = kitHandler;
	}

	@EventHandler
	public void onInventoryEdit(InventoryClickEvent event) {
		HumanEntity entity = event.getWhoClicked();

		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		PbLobby lobby = lobbyHandler.getLobby(player.getUniqueId());

		if (lobby != null) {
			event.setCancelled(true);
		}
	}


	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		PbLobby lobby = lobbyHandler.getLobby(player.getUniqueId());

		if (lobby != null) {
			event.setCancelled(true);
		}
	}
}
