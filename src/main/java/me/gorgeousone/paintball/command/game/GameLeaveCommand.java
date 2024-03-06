package me.gorgeousone.paintball.command.game;

import me.gorgeousone.paintball.Message;
import me.gorgeousone.paintball.cmdframework.argument.ArgValue;
import me.gorgeousone.paintball.cmdframework.command.ArgCommand;
import me.gorgeousone.paintball.game.PbLobby;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Command to leave a paintball game and teleport back to the lobby.
 */
public class GameLeaveCommand extends ArgCommand {
	
	private final PbLobbyHandler lobbyHandler;
	
	public GameLeaveCommand(PbLobbyHandler lobbyHandler) {
		super("leave");
		this.lobbyHandler = lobbyHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		PbLobby lobby = lobbyHandler.getLobby(playerId);
		
		if (lobby == null) {
			Message.NOT_IN_LOBBY.send(sender);
			return;
		}
		lobby.removePlayer(player);
	}
}
