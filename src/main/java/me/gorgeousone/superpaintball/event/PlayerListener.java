package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.game.GameState;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.util.BackupUtil;
import me.gorgeousone.superpaintball.util.LocationUtil;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.game.PbLobby;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PlayerListener implements Listener {

	private final JavaPlugin plugin;
	private final PbLobbyHandler lobbyHandler;
	private final PbKitHandler kitHandler;

	public PlayerListener(JavaPlugin plugin, PbLobbyHandler lobbyHandler, PbKitHandler kitHandler) {
		this.plugin = plugin;
		this.lobbyHandler = lobbyHandler;
		this.kitHandler = kitHandler;
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getEntity();
		EntityDamageEvent.DamageCause dmgCause = event.getCause();
		PbLobby lobby = lobbyHandler.getLobby(player.getUniqueId());
		
		if (lobby == null || dmgCause == EntityDamageEvent.DamageCause.CUSTOM) {
			return;
		}
		event.setCancelled(true);
		
		if (dmgCause == EntityDamageEvent.DamageCause.VOID && lobby.getGame().getState() == GameState.LOBBYING) {
			player.teleport(lobby.getJoinSpawn());
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
	public void onPlayerExpGain(PlayerExpChangeEvent event) {
		Player player = event.getPlayer();

		if (lobbyHandler.isPlaying(player.getUniqueId())) {
			event.setAmount(0);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		//TODO check if file searching is expensive
		BackupUtil.loadBackup(event.getPlayer(), plugin);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		PbLobby lobby = lobbyHandler.getLobby(playerId);
		kitHandler.removePlayer(playerId);

		if (lobby != null) {
			lobby.removePlayer(player);
		}
	}
}
