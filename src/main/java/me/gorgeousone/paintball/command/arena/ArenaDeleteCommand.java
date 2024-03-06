package me.gorgeousone.paintball.command.arena;

import me.gorgeousone.paintball.Message;
import me.gorgeousone.paintball.arena.PbArena;
import me.gorgeousone.paintball.arena.PbArenaHandler;
import me.gorgeousone.paintball.cmdframework.argument.ArgType;
import me.gorgeousone.paintball.cmdframework.argument.ArgValue;
import me.gorgeousone.paintball.cmdframework.argument.Argument;
import me.gorgeousone.paintball.cmdframework.command.ArgCommand;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OP command to delete an existing arena.
 */
public class ArenaDeleteCommand extends ArgCommand {
	
	private final PbArenaHandler arenaHandler;
	private final PbLobbyHandler lobbyHandler;
	
	public ArenaDeleteCommand(PbArenaHandler arenaHandler, PbLobbyHandler lobbyHandler) {
		super("delete");
		this.addArg(new Argument("arena name", ArgType.STRING));
		
		this.arenaHandler = arenaHandler;
		this.lobbyHandler = lobbyHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		String arenaName = argValues.get(0).get();
		
		try {
			lobbyHandler.unlinkArena(arenaHandler.getArena(arenaName));
			arenaHandler.removeArena(arenaName);
			Message.ARENA_REMOVE.send(sender, arenaName);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(e.getMessage());
		}
	}
	
	@Override
	protected List<String> onTabComplete(CommandSender sender, String[] stringArgs) {
		if (stringArgs.length == 1) {
			return arenaHandler.getArenas().stream().map(PbArena::getName).collect(Collectors.toList());
		}
		return new LinkedList<>();
	}
}
