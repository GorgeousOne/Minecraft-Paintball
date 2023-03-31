package me.gorgeousone.superpaintball.command.arena;

import me.gorgeousone.superpaintball.ConfigSettings;
import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.util.LocationUtil;
import me.gorgeousone.superpaintball.arena.PbArena;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.util.ConfigUtil;
import me.gorgeousone.superpaintball.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.Set;

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
			StringUtil.msg(sender, e.getMessage());
			return;
		}
		try {
			PbArena arena = arenaHandler.createArena(arenaName, schemFile, player.getLocation());
			StringUtil.msg(sender, "Created new arena %s at %s", arenaName, LocationUtil.humanBlockPos(arena.getSchemPos()));
		}catch (IllegalArgumentException e) {
			StringUtil.msg(sender, e.getMessage());
		}
	}
}
