package me.gorgeousone.superpaintball.command;

import me.gorgeousone.superpaintball.SuperPaintballPlugin;
import me.gorgeousone.superpaintball.cmdframework.command.BaseCommand;
import me.gorgeousone.superpaintball.team.PbTeam;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand extends BaseCommand {
	
	private final SuperPaintballPlugin plugin;
	
	public ReloadCommand(SuperPaintballPlugin plugin) {
		super("reload");
		addAlias("rl");
		setPlayerRequired(false);
		
		this.plugin = plugin;
	}
	
	@Override
	protected void onCommand(CommandSender sender, String[] args) {
		plugin.reload();
		sender.sendMessage("Reloaded config values.");
	}
}
