package me.gorgeousone.superpaintball.command.arena;

import me.gorgeousone.superpaintball.game.GameUtil;
import me.gorgeousone.superpaintball.game.PbArena;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgType;
import me.gorgeousone.superpaintball.cmdframework.argument.ArgValue;
import me.gorgeousone.superpaintball.cmdframework.argument.Argument;
import me.gorgeousone.superpaintball.cmdframework.command.ArgCommand;
import me.gorgeousone.superpaintball.team.PbTeam;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Set;

public class ArenaCreateCommand extends ArgCommand {
	
	private final JavaPlugin plugin;
	private final PbLobbyHandler lobbyHandler;
	
	public ArenaCreateCommand(JavaPlugin plugin, PbLobbyHandler lobbyHandler) {
		super("create");
		this.addArg(new Argument("arena name", ArgType.STRING));
		this.addArg(new Argument("schematic name", ArgType.STRING));
		
		this.plugin = plugin;
		this.lobbyHandler = lobbyHandler;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		Player player = (Player) sender;
		PbTeam team = lobbyHandler.getTeam(player.getUniqueId());
		
		if (team != null) {
			team.knockoutPlayer(player);
		}
		String arenaName = argValues.get(0).get();
		
		if (lobbyHandler.containsArena(arenaName)) {
			sender.sendMessage("Nah bro we already have that name.");
			return;
		}
		String schemFileName = argValues.get(1).get();
		File schemFile = new File(plugin.getDataFolder() + File.separator + schemFileName);
		
		if (!schemFile.exists()) {
			sender.sendMessage("Could not find schematic named " + schemFileName + " in data folder.");
			return;
		}
		PbArena newArena = new PbArena(arenaName, schemFile, player.getLocation());
		lobbyHandler.registerArena(newArena);
		newArena.reset();
		sender.sendMessage("Created new arena '" + arenaName + "' at " + GameUtil.humanBlockPos(newArena.getSchemPos()));
	}
}
