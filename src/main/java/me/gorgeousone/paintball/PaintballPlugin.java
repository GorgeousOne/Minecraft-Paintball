package me.gorgeousone.paintball;

import me.gorgeousone.paintball.arena.PbArenaHandler;
import me.gorgeousone.paintball.cmdframework.command.ParentCommand;
import me.gorgeousone.paintball.cmdframework.handler.CommandHandler;
import me.gorgeousone.paintball.command.*;
import me.gorgeousone.paintball.command.arena.*;
import me.gorgeousone.paintball.command.game.*;
import me.gorgeousone.paintball.command.lobby.*;
import me.gorgeousone.paintball.event.*;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import me.gorgeousone.paintball.kit.KitType;
import me.gorgeousone.paintball.kit.PbKitHandler;
import me.gorgeousone.paintball.team.TeamType;
import me.gorgeousone.paintball.util.SoundUtil;
import me.gorgeousone.paintball.util.blocktype.BlockType;
import me.gorgeousone.paintball.util.version.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class PaintballPlugin extends JavaPlugin {
	
	private PbLobbyHandler lobbyHandler;
	private PbKitHandler kitHandler;
	private PbArenaHandler arenaHandler;

	@Override
	public void onEnable() {
		setupVersioning();
		PbKitHandler.setupKits(this);
		this.kitHandler = new PbKitHandler();

		loadConfigSettings();
		
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
	
	public void reload() {
		loadConfigSettings();
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
		arenaCmd.setPermission("paintball.configure");
		arenaCmd.setPlayerRequired(false);
		
		arenaCmd.addChild(new ArenaCreateCommand(arenaHandler));
		arenaCmd.addChild(new ArenaDeleteCommand(arenaHandler, lobbyHandler));
		arenaCmd.addChild(new ArenaCopyCommand(arenaHandler));
		arenaCmd.addChild(new ArenaAddSpawnCommand(arenaHandler));
		arenaCmd.addChild(new ArenaMoveCommand(arenaHandler));
		arenaCmd.addChild(new ArenaRemoveSpawnCommand(arenaHandler));
		arenaCmd.addChild(new ArenaListSpawnsCommand(arenaHandler));
		arenaCmd.addChild(new ArenaResetCommand(arenaHandler));
		
		ParentCommand lobbyCmd = new ParentCommand("lobby");
		lobbyCmd.setPermission("paintball.configure");
		lobbyCmd.setPlayerRequired(false);
		
		lobbyCmd.addChild(new LobbyCreateCommand(lobbyHandler));
		lobbyCmd.addChild(new LobbyDeleteCommand(lobbyHandler));
		lobbyCmd.addChild(new LobbySetSpawnCommand(lobbyHandler));
		lobbyCmd.addChild(new LobbySetExitCommand(lobbyHandler));
		lobbyCmd.addChild(new LobbyLinkArenaCommand(lobbyHandler, arenaHandler));
		lobbyCmd.addChild(new LobbyUnlinkArenaCommand(lobbyHandler, arenaHandler));
		lobbyCmd.addChild(new LobbyListArenasCommand(lobbyHandler));
		
		ParentCommand listCmd = new ParentCommand("list");
		lobbyCmd.setPermission("paintball.configure");
		lobbyCmd.setPlayerRequired(false);
		
		listCmd.addChild(new ListLobbiesCommand(lobbyHandler));
		listCmd.addChild(new ListArenasCommand(arenaHandler));
		
		pbCmd.addChild(arenaCmd);
		pbCmd.addChild(lobbyCmd);
		pbCmd.addChild(listCmd);
		pbCmd.addChild(new GameJoinCommand(lobbyHandler));
		pbCmd.addChild(new GameStartCommand(lobbyHandler));
		pbCmd.addChild(new GameLeaveCommand(lobbyHandler));
		pbCmd.addChild(new PlayerStatsCommand(this));
		pbCmd.addChild(new ReloadCommand(this));

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
		manager.registerEvents(new ChatListener(lobbyHandler), this);

		manager.registerEvents(new ProjectileListener(lobbyHandler), this);
		manager.registerEvents(new SkellyInteractListener(lobbyHandler), this);
	}
	
	private void loadConfigSettings() {
		reloadConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();
		ConfigSettings.loadSettings(getConfig());
		kitHandler.reloadKits();
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