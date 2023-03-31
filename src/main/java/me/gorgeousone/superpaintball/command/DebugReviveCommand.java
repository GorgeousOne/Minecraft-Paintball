package me.gorgeousone.superpaintball.command;

import me.gorgeousone.superpaintball.game.PbGame;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.cmdframework.command.BaseCommand;
import me.gorgeousone.superpaintball.team.PbTeam;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DebugReviveCommand extends BaseCommand {
	
	private final PbLobbyHandler lobbyHandler;
	
	public DebugReviveCommand(PbLobbyHandler lobbyHandler) {
		super("rev");
		setPermission("paintball.debug");
		
		this.lobbyHandler = lobbyHandler;
	}
	
	@Override
	protected void onCommand(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		UUID playerId = player.getUniqueId();
		PbGame game = lobbyHandler.getGame(player.getUniqueId());
		
		if (game != null) {
			game.revivePlayer((ArmorStand) Bukkit.getEntity(game.getTeam(playerId).getReviveSkellyId(playerId)), player);
		}
	}
}
