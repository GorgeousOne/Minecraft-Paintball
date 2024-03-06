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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LobbySetExitCommand extends ArgCommand {
	
	private final PbLobbyHandler lobbyHandler;
	
	public LobbySetExitCommand(PbLobbyHandler lobbyHandler) {
		super("set-exit");
		this.addArg(new Argument("lobby name", ArgType.STRING));
		
		this.lobbyHandler = lobbyHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		String lobbyName = argValues.get(0).get();
		PbLobby lobby = lobbyHandler.getLobby(lobbyName);
		
		if (lobby == null) {
			Message.LOBBY_MISSING.send(sender, lobbyName);
			return;
		}
		Player player = (Player) sender;
		lobby.setExitSpawn(player.getLocation());
		Message.LOBBY_EXIT_SET.send(sender, lobby.getName(), LocationUtil.humanBlockPos(lobby.getJoinSpawn()));
	}
	
	@Override
	protected List<String> onTabComplete(CommandSender sender, String[] stringArgs) {
		if (stringArgs.length == 1) {
			return lobbyHandler.getLobbies().stream().map(PbLobby::getName).collect(Collectors.toList());
		}
		return new LinkedList<>();
	}
}
