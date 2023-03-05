package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.GameHandler;
import me.gorgeousone.superpaintball.PbGame;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerListener implements Listener {
	
	private final GameHandler gameHandler;
	
	public PlayerListener(GameHandler gameHandler) {
		this.gameHandler = gameHandler;
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getEntity();
		EntityDamageEvent.DamageCause dmgCause = event.getCause();
		
		if (gameHandler.isPlaying(player.getUniqueId()) &&
		    !(event.getCause() == EntityDamageEvent.DamageCause.CUSTOM ||
		      event.getCause() == EntityDamageEvent.DamageCause.VOID)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerHunger(FoodLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getEntity();
		
		if (gameHandler.isPlaying(player.getUniqueId())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		PbGame game = gameHandler.getGame(playerId);
		
		if (game != null) {
			game.removePlayer(playerId);
		}
	}
	
	@EventHandler
	public void onInventoryEdit(InventoryClickEvent event) {
		HumanEntity entity = event.getWhoClicked();
		
		if (!(entity instanceof Player)) {
			return;
		}
		Player player = (Player) entity;
		PbGame game = gameHandler.getGame(player.getUniqueId());
		
		if (game != null) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		PbGame game = gameHandler.getGame(player.getUniqueId());
		
		if (game != null) {
			event.setCancelled(true);
		}
	}
}
