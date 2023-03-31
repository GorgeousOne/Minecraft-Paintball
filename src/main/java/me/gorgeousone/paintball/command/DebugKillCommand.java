package me.gorgeousone.paintball.command;

import me.gorgeousone.paintball.game.PbGame;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import me.gorgeousone.paintball.cmdframework.command.BaseCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugKillCommand extends BaseCommand {
	
	private final PbLobbyHandler lobbyHandler;
	
	public DebugKillCommand(PbLobbyHandler lobbyHandler) {
		super("kill");
		setPermission("paintball.debug");

		this.lobbyHandler = lobbyHandler;
	}
	
	@Override
	protected void onCommand(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		PbGame game = lobbyHandler.getGame(player.getUniqueId());
		
		if (game != null) {
			game.damagePlayer(player, player, 9001);
		}
	}
}
