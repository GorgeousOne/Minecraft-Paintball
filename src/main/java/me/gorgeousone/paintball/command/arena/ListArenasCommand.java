package me.gorgeousone.paintball.command.arena;

import me.gorgeousone.paintball.arena.PbArena;
import me.gorgeousone.paintball.arena.PbArenaHandler;
import me.gorgeousone.paintball.cmdframework.command.BaseCommand;
import me.gorgeousone.paintball.util.LocationUtil;
import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.command.CommandSender;

/**
 * OP command to list all arenas and their schematic positions.
 */
public class ListArenasCommand extends BaseCommand {
	
	private final PbArenaHandler arenaHandler;
	
	public ListArenasCommand(PbArenaHandler arenaHandler) {
		super("arenas");
		setPlayerRequired(false);
		
		this.arenaHandler = arenaHandler;
	}
	
	@Override
	protected void onCommand(CommandSender sender, String[] args) {
		StringUtil.msg(sender, "List of arenas:");
		
		for (PbArena arena : arenaHandler.getArenas()) {
			StringUtil.msgPlain(sender, "%s " + LocationUtil.humanBlockPos(arena.getSchemPos()), arena.getName());
		}
	}
}
