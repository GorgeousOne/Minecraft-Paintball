package me.gorgeousone.paintball.command.lobby;

import me.gorgeousone.paintball.Message;
import me.gorgeousone.paintball.cmdframework.argument.ArgType;
import me.gorgeousone.paintball.cmdframework.argument.ArgValue;
import me.gorgeousone.paintball.cmdframework.argument.Argument;
import me.gorgeousone.paintball.cmdframework.command.ArgCommand;
import me.gorgeousone.paintball.game.PbLobby;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import me.gorgeousone.paintball.util.LocationUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class LobbyCreateCommand extends ArgCommand {
	
	private final PbLobbyHandler lobbyHandler;
	
	public LobbyCreateCommand(PbLobbyHandler lobbyHandler) {
		super("create");
		this.addArg(new Argument("lobby name", ArgType.STRING));
		
		this.lobbyHandler = lobbyHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		Player player = (Player) sender;
		String lobbyName = argValues.get(0).get();
		try {
			PbLobby lobby = lobbyHandler.createLobby(lobbyName, player.getLocation());
			Message.LINE_25.send(sender, lobbyName, LocationUtil.humanBlockPos(lobby.getJoinSpawn()));
		} catch (Exception e) {
			sender.sendMessage(e.getMessage());
		}
	}
}
