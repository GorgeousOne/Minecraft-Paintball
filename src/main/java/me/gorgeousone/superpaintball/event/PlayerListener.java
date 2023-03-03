package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.GameHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

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
		
		if (gameHandler.isPlaying(player.getUniqueId()) && event.getCause() != EntityDamageEvent.DamageCause.CUSTOM) {
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
}
