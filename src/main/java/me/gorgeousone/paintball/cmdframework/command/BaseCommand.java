package me.gorgeousone.paintball.cmdframework.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A command with no defined way of execution (not arguments or sub commands)
 */
public abstract class BaseCommand {
	
	public static final UUID CONSOLE_ID = UUID.randomUUID();
	
	private final String name;
	private final Set<String> aliases;
	private ParentCommand parent;
	private String permission;
	private boolean isPlayerRequired = true;
	
	public BaseCommand(String name) {
		this.name = name.toLowerCase();
		aliases = new HashSet<>();
		aliases.add(this.name);
	}
	
	public String getPermission() {
		return permission;
	}
	
	/**
	 * Sets a permission required to execute the command
	 *
	 * @param permission name of permission
	 */
	public void setPermission(String permission) {
		this.permission = permission;
	}
	
	public boolean passesPermission(CommandSender sender) {
		return permission == null || sender.hasPermission(permission);
	}
	
	public boolean isPlayerRequired() {
		return isPlayerRequired;
	}
	
	/**
	 * Sets whether this command can be executed over console/command block or not.
	 *
	 * @param playerRequired true if only players are allowed to execute this command
	 */
	public void setPlayerRequired(boolean playerRequired) {
		isPlayerRequired = playerRequired;
	}
	
	protected ParentCommand getParent() {
		return parent;
	}
	
	/**
	 * Sets a parent command for this command. This effects the string returned by {@link #getUsage()}
	 */
	public BaseCommand setParent(ParentCommand parent) {
		this.parent = parent;
		return this;
	}
	
	/**
	 * Executes the implemented functionality of this command if permissions and player requirements are met
	 *
	 * @param sender being that executed this command
	 * @param args   further arguments input behind the command name
	 */
	public void execute(CommandSender sender, String[] args) {
		if (isPlayerRequired() && !(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
			return;
		}
		if (!passesPermission(sender)) {
			sender.sendMessage(ChatColor.RED + "You do not have the permission for this command.");
			return;
		}
		onCommand(sender, args);
	}
	
	/**
	 * Executes the functionality of the command
	 *
	 * @param sender being that executed this command
	 * @param args   further arguments input behind the command name
	 */
	protected abstract void onCommand(CommandSender sender, String[] args);
	
	public String getName() {
		return name;
	}
	
	public void addAlias(String alias) {
		aliases.add(alias.toLowerCase());
	}
	
	public boolean matchesAlias(String alias) {
		return aliases.contains(alias);
	}
	
	/**
	 * Returns the pattern how to use this command
	 */
	public String getUsage() {
		if (parent != null) {
			return parent.getParentUsage() + " " + getName();
		}
		return ChatColor.RED + "/" + getName();
	}
	
	public void sendUsage(CommandSender sender) {
		sender.sendMessage(getUsage());
	}
	
	public List<String> getTabList(CommandSender sender, String[] arguments) {
		if (passesPermission(sender)) {
			return onTabComplete(sender, arguments);
		}
		return new LinkedList<>();
	}
	
	protected List<String> onTabComplete(CommandSender sender, String[] arguments) {
		return new LinkedList<>();
	}
	
	/**
	 * @return player's UUID if sender is a player, pre-generated UUID for console commands and command blocks
	 */
	protected UUID getSenderId(CommandSender sender) {
		return sender instanceof Player ? ((Player) sender).getUniqueId() : CONSOLE_ID;
	}
}
