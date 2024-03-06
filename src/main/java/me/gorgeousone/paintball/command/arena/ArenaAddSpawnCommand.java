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
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OP command to add a spawn point to an arena for a specific team.
 */
public class ArenaAddSpawnCommand extends ArgCommand {
	
	private final PbArenaHandler arenaHandler;
	
	public ArenaAddSpawnCommand(PbArenaHandler arenaHandler) {
		super("add-spawn");
		this.addArg(new Argument("arena", ArgType.STRING));
		this.addArg(new Argument("team", ArgType.STRING));
		
		this.arenaHandler = arenaHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		Player player = (Player) sender;
		String arenaName = argValues.get(0).get();
		
		if (!arenaHandler.containsArena(arenaName)) {
			Message.ARENA_MISSING.send(sender, arenaName);
			return;
		}
		TeamType teamType;
		String teamName = argValues.get(1).get();
		
		try {
			teamType = TeamType.valueOf(teamName.toUpperCase());
		} catch (IllegalArgumentException e) {
			Message.TEAM_MISSING.send(sender, teamName);
			return;
		}
		PbArena arena = arenaHandler.getArena(arenaName);
		Location spawnPos = player.getLocation();
		arena.addSpawn(teamType, spawnPos);
		Message.ARENA_ADD_SPAWN.send(sender, LocationUtil.humanBlockPos(spawnPos), arenaName, teamType.displayName);
	}
	
	@Override
	protected List<String> onTabComplete(CommandSender sender, String[] stringArgs) {
		switch (stringArgs.length) {
			case 1:
				return arenaHandler.getArenas().stream().map(PbArena::getName).collect(Collectors.toList());
			case 2:
				return Arrays.stream(TeamType.values()).map(t -> t.name().toLowerCase()).collect(Collectors.toList());
		}
		return new LinkedList<>();
	}
}
