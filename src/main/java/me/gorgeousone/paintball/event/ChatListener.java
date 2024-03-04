package me.gorgeousone.paintball.event;

import me.gorgeousone.paintball.ConfigSettings;
import me.gorgeousone.paintball.game.PbGame;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import me.gorgeousone.paintball.team.PbTeam;
import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

/**
 * Listener to format player prefixes in in-game chat for teams and dead players.
 */
public class ChatListener implements Listener {
	
	private final PbLobbyHandler lobbyHandler;
	
	public ChatListener(PbLobbyHandler lobbyHandler) {this.lobbyHandler = lobbyHandler;}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		PbGame game = lobbyHandler.getGame(playerId);
		
		if (game == null || !game.isRunning()) {
			return;
		}
		PbTeam team = game.getTeam(playerId);
		String format = team.isAlive(playerId) ? ConfigSettings.CHAT_PREFIX_ALIVE : ConfigSettings.CHAT_PREFIX_DEAD;
		format = StringUtil.replace(format, "player", "%1$s");
		format = StringUtil.replace(format, "team-color", team.getType().prefixColor.toString());
		event.setFormat(format + " %2$s");
	}
}
