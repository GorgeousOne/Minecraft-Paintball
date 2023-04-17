package me.gorgeousone.paintball.cmdframework.handler;

import me.gorgeousone.paintball.cmdframework.command.BaseCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.LinkedList;
import java.util.List;

/**
 * Tab completes any command registered in the command handler
 */
public class CommandCompleter implements TabCompleter {
	
	private final CommandHandler cmdHandler;
	
	public CommandCompleter(CommandHandler cmdHandler) {
		this.cmdHandler = cmdHandler;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		for (BaseCommand command : cmdHandler.getCommands()) {
			if (!command.matchesAlias(cmd.getName())) {
				continue;
			}
			List<String> tabList = new LinkedList<>();
			for (String tab : command.getTabList(sender, args)) {
				if (tab.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
					tabList.add(tab);
				}
			}
			return tabList;
		}
		return null;
	}
}