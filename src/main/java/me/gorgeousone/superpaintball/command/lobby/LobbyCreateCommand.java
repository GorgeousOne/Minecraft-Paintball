package me.gorgeousone.superpaintball.command.lobby;

import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.util.LocationUtil;
import me.gorgeousone.superpaintball.game.PbLobby;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.util.StringUtil;
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
			StringUtil.msg(sender, "Created new lobby '%s' at %s", lobbyName, LocationUtil.humanBlockPos(lobby.getJoinSpawn()));
		} catch (Exception e) {
			StringUtil.msg(sender, e.getMessage());
		}
	}
}
