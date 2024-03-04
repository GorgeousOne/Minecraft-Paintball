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
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OP command to remove a spawn point from an arena for a specific team.
 */
public class ArenaRemoveSpawnCommand extends ArgCommand {
	
	private final PbArenaHandler arenaHandler;
	
	public ArenaRemoveSpawnCommand(PbArenaHandler arenaHandler) {
		super("remove-spawn");
		this.addArg(new Argument("arena", ArgType.STRING));
		this.addArg(new Argument("team", ArgType.STRING));
		this.addArg(new Argument("spawn index", ArgType.INTEGER));
		
		this.arenaHandler = arenaHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		String arenaName = argValues.get(0).get();
		
		if (!arenaHandler.containsArena(arenaName)) {
			Message.LINE_02.send(sender, arenaName);
			return;
		}
		TeamType teamType;
		String teamName = argValues.get(1).get();
		
		try {
			teamType = TeamType.valueOf(teamName.toUpperCase());
		} catch (IllegalArgumentException e) {
			Message.LINE_03.send(sender, teamName);
			return;
		}
		PbArena arena = arenaHandler.getArena(arenaName);
		List<Location> spawns = arena.getSpawns(teamType);
		
		if (spawns.isEmpty()) {
			Message.LINE_13.send(sender, teamType.displayName, arenaName);
			return;
		}
		int spawnIndex = argValues.get(2).getInt();
		
		if (spawnIndex < 1 || spawnIndex > spawns.size()) {
			Message.LINE_14.send(sender, spawnIndex, teamType.displayName, spawns.size(), arenaName);
			return;
		}
		Location spawnPos = spawns.remove(spawnIndex - 1);
		Message.LINE_15.send(sender, spawnIndex, teamType.displayName, arenaName, LocationUtil.humanBlockPos(spawnPos));
	}
	
	@Override
	protected List<String> onTabComplete(CommandSender sender, String[] stringArgs) {
		switch (stringArgs.length) {
			case 1:
				return arenaHandler.getArenas().stream().map(PbArena::getName).collect(Collectors.toList());
			case 2:
				return Arrays.stream(TeamType.values()).map(t -> t.name().toLowerCase()).collect(Collectors.toList());
			case 3:
				return Arrays.asList("1", "2", "3");
		}
		return new LinkedList<>();
	}
}
