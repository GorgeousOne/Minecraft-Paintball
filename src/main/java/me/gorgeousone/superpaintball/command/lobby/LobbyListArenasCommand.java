package me.gorgeousone.superpaintball.command.lobby;

import me.gorgeousone.superpaintball.arena.PbArena;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.game.PbLobby;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.team.TeamType;
import me.gorgeousone.superpaintball.util.LocationUtil;
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
			sender.sendMessage(String.format("Lobby '%s' does not exits!", lobbyName));
			return;
		}
		sender.sendMessage(String.format("Arenas linked to lobby '%s':", lobbyName));
		
		for (PbArena arena : lobby.getArenas()) {
			sender.sendMessage("  " + arena.getName() + ":  " + LocationUtil.humanBlockPos(arena.getSchemPos()));
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
