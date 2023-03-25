package me.gorgeousone.superpaintball.command.arena;

import me.gorgeousone.superpaintball.arena.PbArena;
import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.util.StringUtil;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ArenaDeleteCommand extends ArgCommand {

	private final PbArenaHandler arenaHandler;
	
	public ArenaDeleteCommand(PbArenaHandler arenaHandler) {
		super("delete");
		this.addArg(new Argument("arena name", ArgType.STRING));
		
		this.arenaHandler = arenaHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		String arenaName = argValues.get(0).get();

		try {
			arenaHandler.removeArena(arenaName);
			StringUtil.msg(sender, "Removed new arena '%s'.", arenaName);
		} catch (IllegalArgumentException e) {
			StringUtil.msg(sender, e.getMessage());
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
