package me.gorgeousone.superpaintball.command.arena;

import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.game.GameUtil;
import me.gorgeousone.superpaintball.game.PbArena;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.team.PbTeam;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

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
			sender.sendMessage(String.format("No arena found with name '%s'.", arenaName));
			return;
		}
		PbArena arena = arenaHandler.getArena(arenaName);
		arena.reset();
		sender.sendMessage(String.format("Reset arena '%s' for a new game.", arenaName));
	}
}
