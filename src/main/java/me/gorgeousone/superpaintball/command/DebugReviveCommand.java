package me.gorgeousone.superpaintball.command;

import me.gorgeousone.superpaintball.GameHandler;
import me.gorgeousone.superpaintball.cmdframework.command.BaseCommand;
import me.gorgeousone.superpaintball.team.PbTeam;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugReviveCommand extends BaseCommand {
	
	private final GameHandler gameHandler;
	
	public DebugReviveCommand(GameHandler gameHandler) {
		super("rev");
		this.gameHandler = gameHandler;
	}
	
	@Override
	protected void onCommand(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		PbTeam team = gameHandler.getTeam(player.getUniqueId());
		
		if (team != null) {
			team.revivePlayer(player.getUniqueId());
		}
	}
}
