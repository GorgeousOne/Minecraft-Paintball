package me.gorgeousone.paintball.command.lobby;

import me.gorgeousone.paintball.cmdframework.command.BaseCommand;
import me.gorgeousone.paintball.game.PbLobby;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import me.gorgeousone.paintball.util.LocationUtil;
import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.command.CommandSender;

public class ListLobbiesCommand extends BaseCommand {
	
	private final PbLobbyHandler lobbyHandler;
	
	public ListLobbiesCommand(PbLobbyHandler lobbyHandler) {
		super("lobbies");
		setPlayerRequired(false);
		
		this.lobbyHandler = lobbyHandler;
	}
	
	@Override
	protected void onCommand(CommandSender sender, String[] args) {
		StringUtil.msg(sender, "List of lobbys:");
		
		for (PbLobby lobby : lobbyHandler.getLobbies()) {
			StringUtil.msgPlain(sender, "%s " + LocationUtil.humanBlockPos(lobby.getJoinSpawn()), lobby.getName());
		}
	}
}
