package me.gorgeousone.superpaintball.command;

import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

public class KitCommand extends ArgCommand {
	
	
	public KitCommand() {
		super("kit");
		addArg(new Argument("kit name", ArgType.STRING, "rifle", "shotgun", "machinegun", "sniper"));
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
	
	}
}
