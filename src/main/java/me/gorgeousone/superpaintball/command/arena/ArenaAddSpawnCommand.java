package me.gorgeousone.superpaintball.command.arena;

import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.arena.PbArena;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.util.LocationUtil;
import me.gorgeousone.superpaintball.team.TeamType;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

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
			sender.sendMessage(String.format("Arena '%s' does not exist!", arenaName));
			return;
		}
		TeamType teamType;
		String teamName = argValues.get(1).get();

		try {
			teamType = TeamType.valueOf(teamName.toUpperCase());
		} catch (IllegalStateException e) {
			sender.sendMessage(String.format("Team '%s' does not exist!", teamName));
			return;
		}
		PbArena arena = arenaHandler.getArena(arenaName);
		Location spawnPos = player.getLocation();
		arena.addSpawn(teamType, spawnPos);
		sender.sendMessage(String.format("Added spawn %s in arena '%s' for team %s.", LocationUtil.humanBlockPos(spawnPos), arenaName, teamType.displayName));
	}
}
