package me.gorgeousone.superpaintball.command.arena;

import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.team.PbTeam;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public class ArenaResetCommand extends ArgCommand {
	
	private final PbLobbyHandler lobbyHandler;
	
	public ArenaResetCommand(PbLobbyHandler lobbyHandler) {
		super("reset");
		this.addArg(new Argument("arena name", ArgType.STRING));
		
		this.lobbyHandler = lobbyHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		Player player = (Player) sender;
		PbTeam team = lobbyHandler.getTeam(player.getUniqueId());
		
		if (team != null) {
			team.knockoutPlayer(player);
		}
	}
}
