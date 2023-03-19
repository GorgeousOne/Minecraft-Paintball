package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.game.GameState;
import me.gorgeousone.superpaintball.game.PbLobby;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementListener implements Listener {

	private final PbLobbyHandler lobbyHandler;

	public MovementListener(PbLobbyHandler lobbyHandler) {
		this.lobbyHandler = lobbyHandler;
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		PbLobby lobby = lobbyHandler.getLobby(player.getUniqueId());

		if (lobby == null || lobby.getState() != GameState.COUNTING_DOWN) {
			return;
		}
		Location from = event.getFrom();
		Location to = event.getTo();
		to.setX(from.getX());
		to.setZ(from.getZ());
		event.setTo(to);
	}
}
