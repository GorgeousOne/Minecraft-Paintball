package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.game.GameUtil;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.game.PbLobby;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerListener implements Listener {
	
	private final PbLobbyHandler lobbyHandler;
	
	public PlayerListener(PbLobbyHandler lobbyHandler) {
		this.lobbyHandler = lobbyHandler;
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getEntity();
		EntityDamageEvent.DamageCause dmgCause = event.getCause();
		
		if (!lobbyHandler.isPlaying(player.getUniqueId())) {
			return;
		}
		boolean isBelowWorldMin = player.getLocation().getY() < GameUtil.getWorldMinY(player.getWorld());

		if (!(dmgCause == EntityDamageEvent.DamageCause.CUSTOM ||
				dmgCause == EntityDamageEvent.DamageCause.VOID && isBelowWorldMin)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerHeal(EntityRegainHealthEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getEntity();
		
		if (lobbyHandler.isPlaying(player.getUniqueId())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerHunger(FoodLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getEntity();
		
		if (lobbyHandler.isPlaying(player.getUniqueId())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		PbLobby lobby = lobbyHandler.getLobby(playerId);
		
		if (lobby != null) {
			lobby.removePlayer(player);
		}
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
