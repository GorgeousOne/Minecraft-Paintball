package me.gorgeousone.superpaintball.command.arena;

import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.arena.PbArena;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
			sender.sendMessage(String.format("Arena '%s' does not exist!", arenaName));
			return;
		}
		PbArena arena = arenaHandler.getArena(arenaName);
		arena.reset();
		sender.sendMessage(String.format("Reset arena '%s' for a new game.", arenaName));
	}

	@Override
	public List<String> getTabList(String[] stringArgs) {
		if (stringArgs.length == 1) {
			return arenaHandler.getArenas().stream().map(PbArena::getName).collect(Collectors.toList());
		}
		return new LinkedList<>();
	}
}
