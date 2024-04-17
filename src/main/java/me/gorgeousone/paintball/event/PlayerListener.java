package me.gorgeousone.paintball.event;

import me.gorgeousone.paintball.game.GameState;
import me.gorgeousone.paintball.game.PbGame;
import me.gorgeousone.paintball.game.PbLobby;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import me.gorgeousone.paintball.kit.PbKitHandler;
import me.gorgeousone.paintball.team.PbTeam;
import me.gorgeousone.paintball.util.ItemUtil;
import me.gorgeousone.paintball.util.LocationUtil;
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

/**
 * Listener to cancel survival related events for players in lobbies and games.
 * Also handles players joining and leaving the server regarding inventory saving.
 */
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
		
		if (dmgCause != EntityDamageEvent.DamageCause.VOID) {
			return;
		}
		PbGame game = lobby.getGame();
		
		if (game.getState() == GameState.LOBBYING) {
			LocationUtil.tpTick(player, lobby.getJoinSpawn(), plugin);
		} else {
			PbTeam team = game.getTeam(player.getUniqueId());
			LocationUtil.tpTick(player, game.getPlayedArena().getSpawns(team.getType()).get(0), plugin);
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
		ItemUtil.loadPlayerBackup(event.getPlayer(), plugin, false);
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
