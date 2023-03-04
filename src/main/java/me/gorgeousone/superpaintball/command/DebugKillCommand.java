package me.gorgeousone.superpaintball.command;

import me.gorgeousone.superpaintball.GameHandler;
import me.gorgeousone.superpaintball.cmdframework.command.BaseCommand;
import me.gorgeousone.superpaintball.team.PbTeam;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugKillCommand extends BaseCommand {
	
	private final GameHandler gameHandler;
	
	public DebugKillCommand(GameHandler gameHandler) {
		super("kill");
		this.gameHandler = gameHandler;
	}
	
	@Override
	protected void onCommand(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		PbTeam team = gameHandler.getTeam(player.getUniqueId());
		
		if (team != null) {
			team.knockoutPlayer(player);
		}
	}
}
