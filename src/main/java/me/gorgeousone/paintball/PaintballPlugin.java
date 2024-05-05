package me.gorgeousone.paintball;

import me.gorgeousone.paintball.arena.PbArenaHandler;
import me.gorgeousone.paintball.cmdframework.command.ParentCommand;
import me.gorgeousone.paintball.cmdframework.handler.CommandHandler;
import me.gorgeousone.paintball.command.debug.DebugKillCommand;
import me.gorgeousone.paintball.command.debug.DebugReviveCommand;
import me.gorgeousone.paintball.command.ReloadCommand;
import me.gorgeousone.paintball.command.arena.ArenaAddSpawnCommand;
import me.gorgeousone.paintball.command.arena.ArenaCopyCommand;
import me.gorgeousone.paintball.command.arena.ArenaCreateCommand;
import me.gorgeousone.paintball.command.arena.ArenaDeleteCommand;
import me.gorgeousone.paintball.command.arena.ArenaListSpawnsCommand;
import me.gorgeousone.paintball.command.arena.ArenaMoveCommand;
import me.gorgeousone.paintball.command.arena.ArenaRemoveSpawnCommand;
import me.gorgeousone.paintball.command.arena.ArenaResetCommand;
import me.gorgeousone.paintball.command.arena.ListArenasCommand;
import me.gorgeousone.paintball.command.game.GameJoinCommand;
import me.gorgeousone.paintball.command.game.GameLeaveCommand;
import me.gorgeousone.paintball.command.game.GameStartCommand;
import me.gorgeousone.paintball.command.game.PlayerStatsCommand;
import me.gorgeousone.paintball.command.lobby.ListLobbiesCommand;
import me.gorgeousone.paintball.command.lobby.LobbyCreateCommand;
import me.gorgeousone.paintball.command.lobby.LobbyDeleteCommand;
import me.gorgeousone.paintball.command.lobby.LobbyLinkArenaCommand;
import me.gorgeousone.paintball.command.lobby.LobbyListArenasCommand;
import me.gorgeousone.paintball.command.lobby.LobbySetExitCommand;
import me.gorgeousone.paintball.command.lobby.LobbySetSpawnCommand;
import me.gorgeousone.paintball.command.lobby.LobbyUnlinkArenaCommand;
import me.gorgeousone.paintball.event.ChatListener;
import me.gorgeousone.paintball.event.InventoryListener;
import me.gorgeousone.paintball.event.ItemUseListener;
import me.gorgeousone.paintball.event.MovementListener;
import me.gorgeousone.paintball.event.PlayerListener;
import me.gorgeousone.paintball.event.ProjectileListener;
import me.gorgeousone.paintball.event.SkellyInteractListener;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import me.gorgeousone.paintball.kit.KitType;
import me.gorgeousone.paintball.kit.PbKitHandler;
import me.gorgeousone.paintball.team.TeamType;
import me.gorgeousone.paintball.util.ConfigUtil;
import me.gorgeousone.paintball.util.LocationUtil;
import me.gorgeousone.paintball.util.SoundUtil;
import me.gorgeousone.paintball.util.blocktype.BlockType;
import me.gorgeousone.paintball.util.version.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main class to register commands, listeners, load config settings and trigger loading/saving of arenas and lobbies.
 */
public final class PaintballPlugin extends JavaPlugin {
	
	private PbLobbyHandler lobbyHandler;
	private PbKitHandler kitHandler;
	private PbArenaHandler arenaHandler;
	
	@Override
	public void onEnable() {
		setupVersion();
		LocationUtil.createTpMarker(this);

		PbKitHandler.createKits(this);
		this.kitHandler = new PbKitHandler();
		this.arenaHandler = new PbArenaHandler(this);
		this.lobbyHandler = new PbLobbyHandler(this, kitHandler);

		reload();
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
		loadLanguage();
		
		kitHandler.updateConfigKitVals();
		lobbyHandler.updateLobbyUis();
		KitType.updateLanguage();
		KitType.updateItems();
		TeamType.updateItems();
	}
	
	/**
	 * Configure materials and sounds depending on legacy or aquatic MC version
	 */
	private void setupVersion() {
		VersionUtil.setup(this);
		BlockType.setup(VersionUtil.IS_LEGACY_SERVER);
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
		
		manager.registerEvents(new ProjectileListener(this, lobbyHandler), this);
		manager.registerEvents(new SkellyInteractListener(lobbyHandler), this);
	}
	
	private void loadConfigSettings() {
		reloadConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();
		ConfigSettings.loadSettings(getConfig());
	}
	
	private void loadLanguage() {
		Message.loadLanguage(ConfigUtil.loadConfig("language", this));
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