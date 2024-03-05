package me.gorgeousone.paintball.command.arena;

import me.gorgeousone.paintball.Message;
import me.gorgeousone.paintball.arena.PbArena;
import me.gorgeousone.paintball.arena.PbArenaHandler;
import me.gorgeousone.paintball.cmdframework.argument.ArgType;
import me.gorgeousone.paintball.cmdframework.argument.ArgValue;
import me.gorgeousone.paintball.cmdframework.argument.Argument;
import me.gorgeousone.paintball.cmdframework.command.ArgCommand;
import me.gorgeousone.paintball.util.LocationUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OP command to copy and paste an existing arena to a new location.
 */
public class ArenaCopyCommand extends ArgCommand {
	
	private final PbArenaHandler arenaHandler;
	
	public ArenaCopyCommand(PbArenaHandler arenaHandler) {
		super("copy");
		this.addArg(new Argument("arena name", ArgType.STRING));
		this.addArg(new Argument("new arena", ArgType.STRING));
		
		this.arenaHandler = arenaHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		Player player = (Player) sender;
		String oldName = argValues.get(0).get();
		
		if (!arenaHandler.containsArena(oldName)) {
			Message.ARENA_MISSING.send(sender, oldName);
			return;
		}
		String newName = argValues.get(1).get();
		
		if (arenaHandler.containsArena(newName)) {
			Message.ARENA_EXISTS.send(sender, newName);
			return;
		}
		PbArena oldArena = arenaHandler.getArena(oldName);
		
		try {
			PbArena newArena = arenaHandler.createArena(oldArena, newName, player.getLocation());
			Message.ARENA_COPY.send(sender, newName, LocationUtil.humanBlockPos(newArena.getSchemPos()));
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
