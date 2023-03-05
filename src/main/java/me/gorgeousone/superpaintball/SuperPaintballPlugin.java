package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.cmdframework.command.ParentCommand;
import me.gorgeousone.superpaintball.cmdframework.handler.CommandHandler;
import me.gorgeousone.superpaintball.command.*;
import me.gorgeousone.superpaintball.command.arena.*;
import me.gorgeousone.superpaintball.event.*;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.game.GameUtil;
import me.gorgeousone.superpaintball.game.PbLobby;
import me.gorgeousone.superpaintball.kit.KitType;
import me.gorgeousone.superpaintball.team.TeamType;
import me.gorgeousone.superpaintball.util.blocktype.BlockType;
import me.gorgeousone.superpaintball.util.version.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SuperPaintballPlugin extends JavaPlugin {
	
	private PbLobbyHandler lobbyHandler;
	
	@Override
	public void onEnable() {
		setupVersioning();
		
		this.lobbyHandler = new PbLobbyHandler(this);
		registerCommands();
		registerListeners();
		setupTest();
	}
	
	private void setupVersioning() {
		VersionUtil.setup(this);
		BlockType.setup(VersionUtil.IS_LEGACY_SERVER);
		KitType.setup();
		TeamType.setup();
		GameUtil.setup();
	}
	
	@Override
	public void onDisable() {}
	
	private boolean randomize;
	private void setupTest() {
		PbLobby lobby = lobbyHandler.createGame();
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			lobby.addPlayer(player.getUniqueId(), Math.random() >= .5 ? TeamType.EMBER : TeamType.ICE);
			randomize = !randomize;
		}
		lobby.start();
	}
	
	private void registerCommands() {
		ParentCommand pbCmd = new ParentCommand("paintball");
		pbCmd.addAlias("pb");
		pbCmd.addChild(new KitCommand());
		pbCmd.addChild(new DebugKillCommand(lobbyHandler));
		pbCmd.addChild(new DebugReviveCommand(lobbyHandler));
		
		ParentCommand arenaCmd = new ParentCommand("arena");
		arenaCmd.addChild(new ArenaCreateCommand(lobbyHandler));
		arenaCmd.addChild(new ArenaDeleteCommand(lobbyHandler));
		arenaCmd.addChild(new ArenaCopyCommand(lobbyHandler));
		arenaCmd.addChild(new ArenaAddSpawnCommand(lobbyHandler));
		
		CommandHandler cmdHandler = new CommandHandler(this);
		cmdHandler.registerCommand(pbCmd);
	}
	
	void registerListeners() {
		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(new PlayerListener(lobbyHandler), this);
		manager.registerEvents(new ShootListener(lobbyHandler), this);
		manager.registerEvents(new ProjectileListener(lobbyHandler), this);
		manager.registerEvents(new SkellyInteractListener(lobbyHandler), this);
	}
}