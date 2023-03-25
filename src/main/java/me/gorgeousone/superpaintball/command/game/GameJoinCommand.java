package me.gorgeousone.superpaintball.command.game;

import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.game.PbLobby;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GameJoinCommand extends ArgCommand {

	private final PbLobbyHandler lobbyHandler;

	public GameJoinCommand(PbLobbyHandler lobbyHandler) {
		super("join");
		this.addArg(new Argument("lobby name", ArgType.STRING));
		this.addArg(new Argument("player", ArgType.STRING).setDefault("~"));
		this.setPlayerRequired(false);
	
		this.lobbyHandler = lobbyHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		String lobbyName = argValues.get(0).get();
		PbLobby lobby = lobbyHandler.getLobby(lobbyName);

		if (lobby == null) {
			StringUtil.msg(sender, "Lobby '%s' does not exits!", lobbyName);
			return;
		}
		String playerName = argValues.get(1).get();
		Player player;

		if (!sender.hasPermission("paintball.moderator") || playerName.equals("~")) {
			player = (Player) sender;
		} else {
			player = Bukkit.getPlayer(playerName);

			if (player == null) {
				StringUtil.msg(sender, "Player %s is not online!", playerName);
				return;
			}
		}
		try {
			lobby.joinPlayer(player);
		} catch (IllegalArgumentException e) {
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
