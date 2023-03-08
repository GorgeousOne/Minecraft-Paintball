package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.arena.PbArenaHandler;
import me.gorgeousone.superpaintball.cmdframework.command.ParentCommand;
import me.gorgeousone.superpaintball.cmdframework.handler.CommandHandler;
import me.gorgeousone.superpaintball.command.*;
import me.gorgeousone.superpaintball.command.arena.*;
import me.gorgeousone.superpaintball.event.*;
import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.game.GameUtil;
import me.gorgeousone.superpaintball.game.PbLobby;
import me.gorgeousone.superpaintball.kit.KitType;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.team.TeamType;
import me.gorgeousone.superpaintball.util.ConfigUtil;
import me.gorgeousone.superpaintball.util.blocktype.BlockType;
import me.gorgeousone.superpaintball.util.version.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

public final class SuperPaintballPlugin extends JavaPlugin {
	
	private PbLobbyHandler lobbyHandler;
	private PbKitHandler kitHandler;
	private PbArenaHandler arenaHandler;

	@Override
	public void onEnable() {
		setupVersioning();
		this.kitHandler = new PbKitHandler();
		this.arenaHandler = new PbArenaHandler();
		this.lobbyHandler = new PbLobbyHandler(this, kitHandler);
		loadSaves();
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
		
		//IDK this is just creating kits? not actually version dependent
		PbKitHandler.setupKits(this);
	}
	
	@Override
	public void onDisable() {}
	
	private boolean randomize;
	private void setupTest() {
		PbLobby lobby = lobbyHandler.createLobby("lobby", new Location(Bukkit.getWorld("world"), 0, 128, 0));
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			lobby.joinPlayer(player, Math.random() >= .5 ? TeamType.EMBER : TeamType.ICE);
			randomize = !randomize;
		}
		lobby.start();
	}
	
	private void registerCommands() {
		ParentCommand pbCmd = new ParentCommand("paintball");
		pbCmd.addAlias("pb");
		
		ParentCommand arenaCmd = new ParentCommand("arena");
		arenaCmd.addChild(new ArenaCreateCommand(arenaHandler, getDataFolder().getAbsolutePath()));
		arenaCmd.addChild(new ArenaDeleteCommand(lobbyHandler));
		arenaCmd.addChild(new ArenaCopyCommand(lobbyHandler));
		arenaCmd.addChild(new ArenaAddSpawnCommand(lobbyHandler));
		
		pbCmd.addChild(arenaCmd);
		pbCmd.addChild(new KitCommand());
		pbCmd.addChild(new DebugKillCommand(lobbyHandler));
		pbCmd.addChild(new DebugReviveCommand(lobbyHandler));
		
		CommandHandler cmdHandler = new CommandHandler(this);
		cmdHandler.registerCommand(pbCmd);
	}
	
	private void registerListeners() {
		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(new PlayerListener(lobbyHandler), this);
		manager.registerEvents(new ShootListener(lobbyHandler), this);
		manager.registerEvents(new ProjectileListener(lobbyHandler), this);
		manager.registerEvents(new SkellyInteractListener(lobbyHandler), this);
	}
	
	private void loadSaves() {
		YamlConfiguration arenaConfig = ConfigUtil.loadConfig("arenas", this);
		YamlConfiguration lobbyConfig = ConfigUtil.loadConfig("lobbies", this);
		
		try {
			arenaConfig.save("arenas");
			lobbyConfig.save("lobbies");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try {
			arenaHandler.loadArenas(arenaConfig, this.getDataFolder().toString());
		} catch (Exception e) {
			Bukkit.getLogger().log(Level.WARNING, e.getMessage());
		}
		try {
			lobbyHandler.loadLobbies(lobbyConfig);
		} catch (Exception e) {
			Bukkit.getLogger().log(Level.WARNING, e.getMessage());
		}
	}
}