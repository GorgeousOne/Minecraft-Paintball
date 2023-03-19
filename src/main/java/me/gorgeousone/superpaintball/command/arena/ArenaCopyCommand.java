package me.gorgeousone.superpaintball.command.arena;

import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.util.LocationUtil;
import me.gorgeousone.superpaintball.arena.PbArena;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

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
			sender.sendMessage(String.format("Arena '%s' does not exist!", oldName));
			return;
		}
		String newName = argValues.get(1).get();

		if (arenaHandler.containsArena(oldName)) {
			sender.sendMessage("Nah bro we already have that name.");
			return;
		}
		PbArena oldArena = arenaHandler.getArena(oldName);

		try {
			PbArena newArena = arenaHandler.createArena(oldArena, newName, player.getLocation());
			sender.sendMessage(String.format("Copied new arena '%s' to %s", newName, LocationUtil.humanBlockPos(newArena.getSchemPos())));
		}catch (IllegalArgumentException e) {
			sender.sendMessage(e.getMessage());
			return;
		}
	}
}
