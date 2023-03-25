package me.gorgeousone.superpaintball.command.lobby;

import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.arena.PbArena;
import me.gorgeousone.superpaintball.game.PbLobby;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LobbyLinkArenaCommand extends ArgCommand {

	private final PbLobbyHandler lobbyHandler;
	private final PbArenaHandler arenaHandler;

	public LobbyLinkArenaCommand(PbLobbyHandler lobbyHandler, PbArenaHandler arenaHandler) {
		super("link");
		this.addArg(new Argument("lobby name", ArgType.STRING));
		this.addArg(new Argument("arena names...", ArgType.STRING));

		this.lobbyHandler = lobbyHandler;
		this.arenaHandler = arenaHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		String lobbyName = argValues.get(0).get();
		PbLobby lobby = lobbyHandler.getLobby(lobbyName);

		if (lobby == null) {
			sender.sendMessage(String.format("Lobby '%s' does not exits!", lobbyName));
			return;
		}
		for (int i = 1; i < argValues.size(); ++i) {
			String arenaName = argValues.get(i).get();
			PbArena arena = arenaHandler.getArena(arenaName);

			if (arena == null) {
				sender.sendMessage(String.format("Arena '%s' does not exits!", arenaName));
				continue;
			}
			try {
				lobbyHandler.linkArena(lobby, arena);
				sender.sendMessage(String.format("Arena '%s' is now playable in lobby '%s'.", arenaName, lobbyName));
			} catch (IllegalArgumentException e) {
				sender.sendMessage(e.getMessage());
			}
		}
	}

	@Override
	protected List<String> onTabComplete(CommandSender sender, String[] stringArgs) {
		if (stringArgs.length == 1) {
			return lobbyHandler.getLobbies().stream().map(PbLobby::getName).collect(Collectors.toList());
		} else if (stringArgs.length == 2) {
			return arenaHandler.getArenas().stream().map(PbArena::getName).collect(Collectors.toList());
		}
		return new LinkedList<>();
	}
}
