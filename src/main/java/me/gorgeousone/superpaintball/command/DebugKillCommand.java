package me.gorgeousone.superpaintball.command;

import me.gorgeousone.superpaintball.game.PbGame;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.cmdframework.command.BaseCommand;
import me.gorgeousone.superpaintball.team.PbTeam;
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
