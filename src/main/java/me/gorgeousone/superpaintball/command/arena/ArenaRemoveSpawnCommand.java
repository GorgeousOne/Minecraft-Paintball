package me.gorgeousone.superpaintball.command.arena;

import me.gorgeousone.superpaintball.arena.PbArena;
import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.team.TeamType;
import me.gorgeousone.superpaintball.util.LocationUtil;
import me.gorgeousone.superpaintball.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
			StringUtil.msg(sender, "Arena %s does not exist!", arenaName);
			return;
		}
		TeamType teamType;
		String teamName = argValues.get(1).get();

		try {
			teamType = TeamType.valueOf(teamName.toUpperCase());
		} catch (IllegalArgumentException e) {
			StringUtil.msg(sender, "Team %s does not exist!", teamName);
			return;
		}
		PbArena arena = arenaHandler.getArena(arenaName);
		List<Location> spawns = arena.getSpawns(teamType);
		
		if (spawns.isEmpty()) {
			StringUtil.msg(sender, "Team %s does not have spawn points in arena %s.", teamType.displayName, arenaName);
			return;
		}
		int spawnIndex = argValues.get(2).getInt();
		
		if (spawnIndex < 1 || spawnIndex > spawns.size()) {
			StringUtil.msg(sender, "Spawn no. %d for does not exist! Team %s has spawn points 1 to %d in arena %s.", spawnIndex, teamType.displayName, spawns.size(), arenaName);
			return;
		}
		Location spawnPos = spawns.remove(spawnIndex - 1);
		StringUtil.msg(sender, "Removed %d. spawn point of team %s in arena %s (was %s).", spawnIndex, teamType.displayName, arenaName, LocationUtil.humanBlockPos(spawnPos));
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
