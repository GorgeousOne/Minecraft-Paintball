package me.gorgeousone.paintball.cmdframework.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A command that can have other commands as arguments/children.
 */
public class ParentCommand extends BaseCommand {
	
	private final Set<BaseCommand> children;
	
	public ParentCommand(String name) {
		super(name);
		children = new HashSet<>();
	}
	
	public Set<BaseCommand> getChildren() {
		return children;
	}
	
	/**
	 * Adds a sub command to this command that can be accessed as the first argument behind this command's name
	 */
	public void addChild(BaseCommand child) {
		children.add(child.setParent(this));
	}
	
	public String getParentUsage() {
		if (getParent() != null) {
			return getParent().getParentUsage() + " " + getName();
		}
		return ChatColor.RED + "/" + getName();
	}
	
	@Override
	public String getUsage() {
		return super.getUsage() + " <>";
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sendUsage(sender);
			return;
		}
		for (BaseCommand child : children) {
			if (child.matchesAlias(args[0])) {
				child.execute(sender, Arrays.copyOfRange(args, 1, args.length));
				return;
			}
		}
		sendUsage(sender);
	}
	
	@Override
	protected List<String> onTabComplete(CommandSender sender, String[] arguments) {
		List<String> tabList = new LinkedList<>();

		if (arguments.length == 1) {
			for (BaseCommand child : getChildren()) {
				if (child.passesPermission(sender)) {
					tabList.add(child.getName());
				}
			}
			return tabList;
		}
		for (BaseCommand child : getChildren()) {
			if (child.matchesAlias(arguments[0])) {
				return child.getTabList(sender, Arrays.copyOfRange(arguments, 1, arguments.length));
			}
		}
		return tabList;
	}
}
