package me.gorgeousone.paintball.command;

import me.gorgeousone.paintball.PaintballPlugin;
import me.gorgeousone.paintball.cmdframework.command.BaseCommand;
import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.command.CommandSender;

/**
 * Command to reload the plugin's config values.
 */
public class ReloadCommand extends BaseCommand {
	
	private final PaintballPlugin plugin;
	
	public ReloadCommand(PaintballPlugin plugin) {
		super("reload");
		addAlias("rl");
		setPlayerRequired(false);
		setPermission("paintball.configure");

		this.plugin = plugin;
	}
	
	@Override
	protected void onCommand(CommandSender sender, String[] args) {
		plugin.reload();
		StringUtil.msg(sender, "Reloaded config values.");
	}
}
