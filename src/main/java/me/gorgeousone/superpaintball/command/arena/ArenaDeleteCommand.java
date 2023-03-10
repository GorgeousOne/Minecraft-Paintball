package me.gorgeousone.superpaintball.command.arena;

import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

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
			sender.sendMessage(String.format("Removed new arena '%s'.", arenaName));
		} catch (Exception e) {
			sender.sendMessage(e.getMessage());
		}
	}
}
