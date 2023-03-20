package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.cmdframework.command.ParentCommand;
import me.gorgeousone.superpaintball.cmdframework.handler.CommandHandler;
import me.gorgeousone.superpaintball.command.*;
import me.gorgeousone.superpaintball.command.arena.*;
import me.gorgeousone.superpaintball.command.lobby.*;
import me.gorgeousone.superpaintball.event.*;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.util.LocationUtil;
import me.gorgeousone.superpaintball.kit.KitType;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.team.TeamType;
import me.gorgeousone.superpaintball.util.blocktype.BlockType;
import me.gorgeousone.superpaintball.util.version.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class SuperPaintballPlugin extends JavaPlugin {
	
	private PbLobbyHandler lobbyHandler;
	private PbKitHandler kitHandler;
	private PbArenaHandler arenaHandler;
	private String schemFolder;

	@Override
	public void onEnable() {
		setupVersioning();
		this.kitHandler = new PbKitHandler();

		this.arenaHandler = new PbArenaHandler(this);
		this.lobbyHandler = new PbLobbyHandler(this, kitHandler);

		loadBackup();
		registerCommands();
		registerListeners();
	}

	@Override
	public void onDisable() {
//		saveBackup();
	}

	private void setupVersioning() {
		VersionUtil.setup(this);
		BlockType.setup(VersionUtil.IS_LEGACY_SERVER);
		KitType.setup();
		TeamType.setup();
		LocationUtil.setup();
		
		//IDK this is just creating kits? not actually version dependent
		PbKitHandler.setupKits(this);
	}
	
	private void registerCommands() {
		ParentCommand pbCmd = new ParentCommand("paintball");
		pbCmd.addAlias("pb");

		ParentCommand arenaCmd = new ParentCommand("arena");
		arenaCmd.addChild(new ArenaCreateCommand(arenaHandler, schemFolder));
		arenaCmd.addChild(new ArenaDeleteCommand(arenaHandler));
		arenaCmd.addChild(new ArenaCopyCommand(arenaHandler));
		arenaCmd.addChild(new ArenaAddSpawnCommand(arenaHandler));

		ParentCommand lobbyCmd = new ParentCommand("lobby");
		lobbyCmd.addChild(new LobbyCreateCommand(lobbyHandler));
		lobbyCmd.addChild(new LobbyDeleteCommand(lobbyHandler));
		lobbyCmd.addChild(new LobbyLinkArenaCommand(lobbyHandler, arenaHandler));
		lobbyCmd.addChild(new LobbyUnlinkArenaCommand(lobbyHandler, arenaHandler));

		pbCmd.addChild(arenaCmd);
		pbCmd.addChild(lobbyCmd);
		pbCmd.addChild(new LobbyJoinCommand(lobbyHandler));
		pbCmd.addChild(new LobbyStartCommand(lobbyHandler));
		pbCmd.addChild(new LobbyLeaveCommand(lobbyHandler));

		pbCmd.addChild(new DebugKillCommand(lobbyHandler));
		pbCmd.addChild(new DebugReviveCommand(lobbyHandler));

		CommandHandler cmdHandler = new CommandHandler(this);
		cmdHandler.registerCommand(pbCmd);
	}
	
	private void registerListeners() {
		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(new PlayerListener(this, lobbyHandler), this);
		manager.registerEvents(new ItemUseListener(lobbyHandler), this);
		manager.registerEvents(new InventoryListener(lobbyHandler, kitHandler), this);
		manager.registerEvents(new MovementListener(lobbyHandler), this);

		manager.registerEvents(new ProjectileListener(lobbyHandler), this);
		manager.registerEvents(new SkellyInteractListener(lobbyHandler), this);
	}
	
	private void loadBackup() {
		this.saveDefaultConfig();
		this.reloadConfig();
		schemFolder = getConfig().getString("schematics-folder");

		try {
			arenaHandler.loadArenas(schemFolder);
		} catch (IllegalArgumentException e) {
			Bukkit.getLogger().log(Level.WARNING, e.getMessage());
		}
		try {
			lobbyHandler.loadLobbies(arenaHandler);
		} catch (IllegalArgumentException e) {
			Bukkit.getLogger().log(Level.WARNING, e.getMessage());
		}
	}

	public void saveBackup() {
		arenaHandler.saveArenas();
		lobbyHandler.saveLobbies();
	}
}