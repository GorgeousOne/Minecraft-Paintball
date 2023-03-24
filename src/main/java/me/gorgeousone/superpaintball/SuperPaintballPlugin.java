package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.cmdframework.command.ParentCommand;
import me.gorgeousone.superpaintball.cmdframework.handler.CommandHandler;
import me.gorgeousone.superpaintball.command.*;
import me.gorgeousone.superpaintball.command.arena.*;
import me.gorgeousone.superpaintball.command.lobby.*;
import me.gorgeousone.superpaintball.event.*;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.kit.KitType;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.team.TeamType;
import me.gorgeousone.superpaintball.util.SoundUtil;
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

	@Override
	public void onEnable() {
		setupVersioning();
		reloadConfigSettings();
		PbKitHandler.setupKits(this);
		
		this.kitHandler = new PbKitHandler();
		this.arenaHandler = new PbArenaHandler(this);
		this.lobbyHandler = new PbLobbyHandler(this, kitHandler);

		loadBackup();
		registerCommands();
		registerListeners();
	}

	@Override
	public void onDisable() {
		lobbyHandler.closeLobbies();
	}

	private void setupVersioning() {
		VersionUtil.setup(this);
		BlockType.setup(VersionUtil.IS_LEGACY_SERVER);
		KitType.setup();
		TeamType.setup();
		SoundUtil.setup();
	}
	
	private void registerCommands() {
		ParentCommand pbCmd = new ParentCommand("paintball");
		pbCmd.setPlayerRequired(false);
		pbCmd.addAlias("pb");

		ParentCommand arenaCmd = new ParentCommand("arena");
		arenaCmd.setPlayerRequired(false);
		arenaCmd.addChild(new ArenaCreateCommand(arenaHandler));
		arenaCmd.addChild(new ArenaDeleteCommand(arenaHandler));
		arenaCmd.addChild(new ArenaCopyCommand(arenaHandler));
		arenaCmd.addChild(new ArenaAddSpawnCommand(arenaHandler));
		arenaCmd.addChild(new ArenaRemoveSpawnCommand(arenaHandler));
		arenaCmd.addChild(new ArenaListSpawnsCommand(arenaHandler));

		ParentCommand lobbyCmd = new ParentCommand("lobby");
		lobbyCmd.setPlayerRequired(false);
		lobbyCmd.addChild(new LobbyCreateCommand(lobbyHandler));
		lobbyCmd.addChild(new LobbyDeleteCommand(lobbyHandler));
		lobbyCmd.addChild(new LobbyLinkArenaCommand(lobbyHandler, arenaHandler));
		lobbyCmd.addChild(new LobbyUnlinkArenaCommand(lobbyHandler, arenaHandler));
		lobbyCmd.addChild(new LobbyListArenasCommand(lobbyHandler));

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
		manager.registerEvents(new PlayerListener(this, lobbyHandler, kitHandler), this);
		manager.registerEvents(new ItemUseListener(lobbyHandler), this);
		manager.registerEvents(new InventoryListener(lobbyHandler, kitHandler), this);
		manager.registerEvents(new MovementListener(lobbyHandler), this);

		manager.registerEvents(new ProjectileListener(lobbyHandler), this);
		manager.registerEvents(new SkellyInteractListener(lobbyHandler), this);
	}
	
	private void reloadConfigSettings() {
		reloadConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();
		ConfigSettings.loadSettings(getConfig());
	}
	
	private void loadBackup() {
		this.saveDefaultConfig();
		this.reloadConfig();

		try {
			arenaHandler.loadArenas(ConfigSettings.SCHEM_FOLDER);
		} catch (IllegalArgumentException e) {
			Bukkit.getLogger().log(Level.WARNING, e.getMessage());
		}
		try {
			lobbyHandler.loadLobbies(arenaHandler);
		} catch (IllegalArgumentException e) {
			Bukkit.getLogger().log(Level.WARNING, e.getMessage());
		}
	}
}