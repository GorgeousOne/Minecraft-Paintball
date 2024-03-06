package me.gorgeousone.paintball.command.arena;

import me.gorgeousone.paintball.Message;
import me.gorgeousone.paintball.arena.PbArena;
import me.gorgeousone.paintball.arena.PbArenaHandler;
import me.gorgeousone.paintball.cmdframework.argument.ArgType;
import me.gorgeousone.paintball.cmdframework.argument.ArgValue;
import me.gorgeousone.paintball.cmdframework.argument.Argument;
import me.gorgeousone.paintball.cmdframework.command.ArgCommand;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OP command to reset an arena for a new game.
 */
public class ArenaResetCommand extends ArgCommand {
	
	private final PbArenaHandler arenaHandler;
	
	public ArenaResetCommand(PbArenaHandler arenaHandler) {
		super("reset");
		this.addArg(new Argument("arena name", ArgType.STRING));
		this.setPlayerRequired(false);
		
		this.arenaHandler = arenaHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		String arenaName = argValues.get(0).get();
		
		if (!arenaHandler.containsArena(arenaName)) {
			Message.ARENA_MISSING.send(sender, arenaName);
			return;
		}
		PbArena arena = arenaHandler.getArena(arenaName);
		arena.resetSchem();
		Message.ARENA_RESET.send(sender, arenaName);
	}
	
	@Override
	protected List<String> onTabComplete(CommandSender sender, String[] stringArgs) {
		if (stringArgs.length == 1) {
			return arenaHandler.getArenas().stream().map(PbArena::getName).collect(Collectors.toList());
		}
		return new LinkedList<>();
	}
}
