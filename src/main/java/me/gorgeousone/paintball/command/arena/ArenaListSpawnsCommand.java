package me.gorgeousone.paintball.command.arena;

import me.gorgeousone.paintball.Message;
import me.gorgeousone.paintball.arena.PbArena;
import me.gorgeousone.paintball.arena.PbArenaHandler;
import me.gorgeousone.paintball.cmdframework.argument.ArgType;
import me.gorgeousone.paintball.cmdframework.argument.ArgValue;
import me.gorgeousone.paintball.cmdframework.argument.Argument;
import me.gorgeousone.paintball.cmdframework.command.ArgCommand;
import me.gorgeousone.paintball.team.TeamType;
import me.gorgeousone.paintball.util.LocationUtil;
import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OP command to list all spawn points of an arena.
 */
public class ArenaListSpawnsCommand extends ArgCommand {
	
	private final PbArenaHandler arenaHandler;
	
	public ArenaListSpawnsCommand(PbArenaHandler arenaHandler) {
		super("list-spawns");
		this.addArg(new Argument("arena name", ArgType.STRING));
		
		this.arenaHandler = arenaHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		String arenaName = argValues.get(0).get();
		PbArena arena = arenaHandler.getArena(arenaName);
		
		if (arena == null) {
			Message.ARENA_MISSING.send(sender, arenaName);
			return;
		}
		StringUtil.msg(sender, "Spawn points of arena %s:", arenaName);

		for (TeamType teamType : TeamType.values()) {
			StringUtil.msgPlain(sender, teamType.displayName + ":");
			int i = 1;
			
			for (Location location : arena.getSpawns(teamType)) {
				StringUtil.msgPlain(sender, i + " %s", LocationUtil.humanBlockPos(location));
				++i;
			}
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
