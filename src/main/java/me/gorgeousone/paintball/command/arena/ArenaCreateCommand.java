package me.gorgeousone.paintball.command.arena;

import me.gorgeousone.paintball.ConfigSettings;
import me.gorgeousone.paintball.arena.PbArenaHandler;
import me.gorgeousone.paintball.util.LocationUtil;
import me.gorgeousone.paintball.arena.PbArena;
import me.gorgeousone.paintball.cmdframework.argument.ArgType;
import me.gorgeousone.paintball.cmdframework.argument.ArgValue;
import me.gorgeousone.paintball.cmdframework.argument.Argument;
import me.gorgeousone.paintball.cmdframework.command.ArgCommand;
import me.gorgeousone.paintball.util.ConfigUtil;
import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * OP command to create a new arena with a schematic file from FAWE or Worldedit schematics folder.
 */
public class ArenaCreateCommand extends ArgCommand {
	
	private final PbArenaHandler arenaHandler;

	public ArenaCreateCommand(PbArenaHandler arenaHandler) {
		super("create");
		this.addArg(new Argument("arena name", ArgType.STRING));
		this.addArg(new Argument("schematic name", ArgType.STRING));
		
		this.arenaHandler = arenaHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		Player player = (Player) sender;
		String arenaName = argValues.get(0).get();
		
		if (arenaHandler.containsArena(arenaName)) {
			StringUtil.msg(sender, "Arena %s already exists!", arenaName);
			return;
		}
		String schemFileName = argValues.get(1).get() + ".schem";
		File schemFile;

		try {
			schemFile = ConfigUtil.schemFileFromYml(schemFileName, ConfigSettings.SCHEM_FOLDER);
		}catch (IllegalArgumentException e) {
			sender.sendMessage(e.getMessage());
			return;
		}
		try {
			PbArena arena = arenaHandler.createArena(arenaName, schemFile, player.getLocation());
			StringUtil.msg(sender, "Created new arena %s at %s", arenaName, LocationUtil.humanBlockPos(arena.getSchemPos()));
		}catch (IllegalArgumentException e) {
			sender.sendMessage(e.getMessage());
		}
	}
}
