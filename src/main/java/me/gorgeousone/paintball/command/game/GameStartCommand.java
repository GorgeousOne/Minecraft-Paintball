package me.gorgeousone.paintball.command.game;

import me.gorgeousone.paintball.cmdframework.argument.ArgType;
import me.gorgeousone.paintball.cmdframework.argument.ArgValue;
import me.gorgeousone.paintball.cmdframework.argument.Argument;
import me.gorgeousone.paintball.cmdframework.command.ArgCommand;
import me.gorgeousone.paintball.game.PbLobby;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GameStartCommand extends ArgCommand {

	private final PbLobbyHandler lobbyHandler;

	public GameStartCommand(PbLobbyHandler lobbyHandler) {
		super("start");
		this.addArg(new Argument("lobby name", ArgType.STRING).setDefault("~"));
		setPermission("paintball.moderator");
		
		this.lobbyHandler = lobbyHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		String lobbyName = argValues.get(0).get();
		PbLobby lobby;

		if (lobbyName.equals("~")) {
			Player player = (Player) sender;
			lobby = lobbyHandler.getLobby(player.getUniqueId());

			if (lobby == null) {
				StringUtil.msg(sender, "You are not in a lobby right now. Join a lobby or specify which lobby to start.");
				return;
			}
		} else {
			lobby = lobbyHandler.getLobby(lobbyName);

			if (lobby == null) {
				StringUtil.msg(sender, "Lobby %s does not exits!", lobbyName);
				return;
			}
		}
		try {
			lobby.startGame();
		} catch (IllegalStateException e) {
			StringUtil.msg(sender, e.getMessage());
		}
	}

	@Override
	protected List<String> onTabComplete(CommandSender sender, String[] stringArgs) {
		if (stringArgs.length == 1) {
			return lobbyHandler.getLobbies().stream().map(PbLobby::getName).collect(Collectors.toList());
		}
		return new LinkedList<>();
	}
}
