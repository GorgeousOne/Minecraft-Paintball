package me.gorgeousone.paintball.command.arena;

import me.gorgeousone.paintball.arena.PbArena;
import me.gorgeousone.paintball.arena.PbArenaHandler;
import me.gorgeousone.paintball.cmdframework.argument.ArgType;
import me.gorgeousone.paintball.cmdframework.argument.ArgValue;
import me.gorgeousone.paintball.cmdframework.argument.Argument;
import me.gorgeousone.paintball.cmdframework.command.ArgCommand;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import me.gorgeousone.paintball.util.LocationUtil;
import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OP command to move an arena to a new location (schematic and spawn points).
 */
public class ArenaMoveCommand extends ArgCommand {

	private final PbArenaHandler arenaHandler;
	
	public ArenaMoveCommand(PbArenaHandler arenaHandler) {
		super("move");
		this.addArg(new Argument("arena name", ArgType.STRING));
		
		this.arenaHandler = arenaHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		Player player = (Player) sender;
		String arenaName = argValues.get(0).get();
		PbArena arena = arenaHandler.getArena(arenaName);
		
		if (arena == null) {
			StringUtil.msg(sender, "Arena %s does not exits!", arenaName);
			return;
		}
		try {
			arena.moveTo(player.getLocation());
		} catch (IllegalArgumentException e) {
			sender.sendMessage(e.getMessage());
		}
		StringUtil.msg(sender, "Moved arena %s: to %s", arenaName, LocationUtil.humanBlockPos(arena.getSchemPos()));
	}

	@Override
	protected List<String> onTabComplete(CommandSender sender, String[] stringArgs) {
		if (stringArgs.length == 1) {
			return arenaHandler.getArenas().stream().map(PbArena::getName).collect(Collectors.toList());
		}
		return new LinkedList<>();
	}
}
