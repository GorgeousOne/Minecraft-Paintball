package me.gorgeousone.paintball.command.lobby;

import me.gorgeousone.paintball.Message;
import me.gorgeousone.paintball.arena.PbArena;
import me.gorgeousone.paintball.cmdframework.argument.ArgType;
import me.gorgeousone.paintball.cmdframework.argument.ArgValue;
import me.gorgeousone.paintball.cmdframework.argument.Argument;
import me.gorgeousone.paintball.cmdframework.command.ArgCommand;
import me.gorgeousone.paintball.game.PbLobby;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import me.gorgeousone.paintball.util.LocationUtil;
import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LobbyListArenasCommand extends ArgCommand {
	
	private final PbLobbyHandler lobbyHandler;
	
	public LobbyListArenasCommand(PbLobbyHandler lobbyHandler) {
		super("list-arenas");
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
		StringUtil.msg(sender, "Arenas linked to lobby %s:", lobbyName);
		
		for (PbArena arena : lobby.getArenas()) {
			StringUtil.msgPlain(sender, "  " + arena.getName() + ":  " + LocationUtil.humanBlockPos(arena.getSchemPos()));
		}
	}
	
	@Override
	protected List<String> onTabComplete(CommandSender sender, String[] stringArgs) {
		if (stringArgs.length == 1) {
			return lobbyHandler.getLobbies().stream().map(PbLobby::getName).collect(Collectors.toList());
		}
		return new LinkedList<>();
	}
}
