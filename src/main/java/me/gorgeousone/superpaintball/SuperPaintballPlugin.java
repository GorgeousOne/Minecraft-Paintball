package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.cmdframework.command.ParentCommand;
import me.gorgeousone.superpaintball.cmdframework.handler.CommandHandler;
import me.gorgeousone.superpaintball.command.DebugKillCommand;
import me.gorgeousone.superpaintball.command.KitCommand;
import me.gorgeousone.superpaintball.event.PlayerListener;
import me.gorgeousone.superpaintball.event.ProjectileListener;
import me.gorgeousone.superpaintball.event.ShootListener;
import me.gorgeousone.superpaintball.event.SkellyInteractListener;
import me.gorgeousone.superpaintball.kit.KitType;
import me.gorgeousone.superpaintball.team.TeamType;
import me.gorgeousone.superpaintball.util.blocktype.BlockType;
import me.gorgeousone.superpaintball.util.version.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SuperPaintballPlugin extends JavaPlugin {
	
	private ParentCommand paintballCmd;
	private GameHandler gameHandler;
	
	@Override
	public void onEnable() {
		setupVersioning();
		
		this.gameHandler = new GameHandler(this);
		registerCommands();
		registerListeners();
		setupTest();
	}
	
	private void setupVersioning() {
		VersionUtil.setup(this);
		BlockType.setup(VersionUtil.IS_LEGACY_SERVER);
		KitType.setup();
		TeamType.setup();
	}
	
	@Override
	public void onDisable() {}
	
	private boolean randomize;
	private void setupTest() {
		PbGame game = gameHandler.createGame();
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			game.addPlayer(player.getUniqueId(), Math.random() >= .5 ? TeamType.NETHER : TeamType.FROST);
			randomize = !randomize;
		}
		game.start();
	}
	
	private void registerCommands() {
		paintballCmd = new ParentCommand("paintball");
		paintballCmd.addAlias("pb");
		paintballCmd.addChild(new KitCommand());
		paintballCmd.addChild(new DebugKillCommand(gameHandler));
		
		CommandHandler cmdHandler = new CommandHandler(this);
		cmdHandler.registerCommand(paintballCmd);
	}
	
	void registerListeners() {
		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(new PlayerListener(gameHandler), this);
		manager.registerEvents(new ShootListener(gameHandler), this);
		manager.registerEvents(new ProjectileListener(gameHandler), this);
		manager.registerEvents(new SkellyInteractListener(gameHandler), this);
	}
}