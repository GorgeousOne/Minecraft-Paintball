package me.gorgeousone.superpaintball.command.arena;

import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.game.GameUtil;
import me.gorgeousone.superpaintball.game.PbArena;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
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
	
	private final String dataFolder;
	private final PbArenaHandler arenaHandler;
	
	public ArenaCreateCommand(PbArenaHandler arenaHandler, String dataFolder) {
		super("create");
		this.addArg(new Argument("arena name", ArgType.STRING));
		this.addArg(new Argument("schematic name", ArgType.STRING));
		
		this.dataFolder = dataFolder;
		this.arenaHandler = arenaHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		Player player = (Player) sender;
		String arenaName = argValues.get(0).get();
		
		if (arenaHandler.containsArena(arenaName)) {
			sender.sendMessage("Nah bro we already have that name.");
			return;
		}
		String schemFileName = argValues.get(1).get();
		File schemFile;

		try {
			schemFile = ConfigUtil.schemFileFromYml(schemFileName, dataFolder);
		}catch (IllegalArgumentException e) {
			sender.sendMessage(e.getMessage());
			return;
		}
		PbArena newArena = new PbArena(arenaName, schemFile, player.getLocation());
		arenaHandler.registerArena(newArena);
		newArena.reset();
		sender.sendMessage(String.format("Created new arena '%s' at %s", arenaName, GameUtil.humanBlockPos(newArena.getSchemPos())));
	}
}
