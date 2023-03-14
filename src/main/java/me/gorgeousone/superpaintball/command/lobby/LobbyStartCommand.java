package me.gorgeousone.superpaintball.command.lobby;

import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.game.PbLobby;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.team.TeamType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class LobbyStartCommand extends ArgCommand {

	private final PbLobbyHandler lobbyHandler;

	public LobbyStartCommand(PbLobbyHandler lobbyHandler) {
		super("start");
		this.addArg(new Argument("lobby name", ArgType.STRING));
		
		this.lobbyHandler = lobbyHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		String lobbyName = argValues.get(0).get();
		PbLobby lobby = lobbyHandler.getLobby(lobbyName);

		if (lobby == null) {
			sender.sendMessage(String.format("Lobby '%s' does not exits!", lobbyName));
			return;
		}
		try {
			lobby.start();
		} catch (Exception e) {
			sender.sendMessage(e.getMessage());
		}
	}
}
