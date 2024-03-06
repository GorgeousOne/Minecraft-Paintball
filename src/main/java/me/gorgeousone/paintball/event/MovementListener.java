package me.gorgeousone.paintball.event;

import me.gorgeousone.paintball.game.GameState;
import me.gorgeousone.paintball.game.PbGame;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listener to keep players at spawn positions in the countdown phase of a game.
 */
public class MovementListener implements Listener {
	
	private final PbLobbyHandler lobbyHandler;
	
	public MovementListener(PbLobbyHandler lobbyHandler) {
		this.lobbyHandler = lobbyHandler;
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		PbGame game = lobbyHandler.getGame(player.getUniqueId());
		
		if (game == null || game.getState() != GameState.COUNTING_DOWN) {
			return;
		}
		Location from = event.getFrom();
		Location to = event.getTo();
		to.setX(from.getX());
		to.setZ(from.getZ());
		event.setTo(to);
	}
}
