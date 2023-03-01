package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.cmdframework.command.ParentCommand;
import me.gorgeousone.superpaintball.command.KitCommand;
import me.gorgeousone.superpaintball.event.ClickListener;
import me.gorgeousone.superpaintball.team.TeamType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SuperPaintballPlugin extends JavaPlugin {
	
	private ParentCommand paintballCmd;
	private GameHandler gameHandler;
	
	@Override
	public void onEnable() {
		this.gameHandler = new GameHandler();
		registerCommands();
		registerListeners();
	}
	
	@Override
	public void onDisable() {}
	
	private void setupTest() {
		GameInstance game = gameHandler.createGame();
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			game.addPlayer(player, TeamType.FROST);
		}
	}
	
	private void registerCommands() {
		paintballCmd = new ParentCommand("paintball");
		paintballCmd.addAlias("pb");
		
		paintballCmd.addChild(new KitCommand());
	}
	
	void registerListeners() {
		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(new ClickListener(gameHandler), this);
	}
}