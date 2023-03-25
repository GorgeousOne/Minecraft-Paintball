package me.gorgeousone.superpaintball.command.game;

import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.game.PbLobby;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

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
			StringUtil.msg(sender, "You are not in a paintball game.");
			return;
		}
		lobby.removePlayer(player);
	}
}
