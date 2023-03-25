package me.gorgeousone.superpaintball.command;

import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.cmdframework.command.BaseCommand;
import me.gorgeousone.superpaintball.team.PbTeam;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugReviveCommand extends BaseCommand {
	
	private final PbLobbyHandler lobbyHandler;
	
	public DebugReviveCommand(PbLobbyHandler lobbyHandler) {
		super("rev");
		setPermission("paintball.debug");
		
		this.lobbyHandler = lobbyHandler;
	}
	
	@Override
	protected void onCommand(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		PbTeam team = lobbyHandler.getTeam(player.getUniqueId());
		
		if (team != null) {
			team.revivePlayer(player.getUniqueId());
		}
	}
}
