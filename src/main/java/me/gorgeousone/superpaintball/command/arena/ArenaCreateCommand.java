package me.gorgeousone.superpaintball.command.arena;

import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.util.LocationUtil;
import me.gorgeousone.superpaintball.arena.PbArena;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.util.ConfigUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.Set;

public class ArenaCreateCommand extends ArgCommand {
	
	private final PbArenaHandler arenaHandler;
	private final String schemFolder;

	public ArenaCreateCommand(PbArenaHandler arenaHandler, String schemFolder) {
		super("create");
		this.addArg(new Argument("arena name", ArgType.STRING));
		this.addArg(new Argument("schematic name", ArgType.STRING));
		
		this.arenaHandler = arenaHandler;
		this.schemFolder = schemFolder;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		Player player = (Player) sender;
		String arenaName = argValues.get(0).get();
		
		if (arenaHandler.containsArena(arenaName)) {
			sender.sendMessage("Arena '%s' already exists!");
			return;
		}
		String schemFileName = argValues.get(1).get();
		File schemFile;

		try {
			schemFile = ConfigUtil.schemFileFromYml(schemFileName, schemFolder);
		}catch (IllegalArgumentException e) {
			sender.sendMessage(e.getMessage());
			return;
		}
		try {
			PbArena arena = arenaHandler.createArena(arenaName, schemFile, player.getLocation());
			sender.sendMessage(String.format("Created new arena '%s' at %s", arenaName, LocationUtil.humanBlockPos(arena.getSchemPos())));
		}catch (IllegalArgumentException e) {
			sender.sendMessage(e.getMessage());
			return;
		}
	}
}
