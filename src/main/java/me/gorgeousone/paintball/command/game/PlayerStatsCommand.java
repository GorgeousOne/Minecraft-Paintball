package me.gorgeousone.paintball.command.game;

import me.gorgeousone.paintball.cmdframework.argument.ArgType;
import me.gorgeousone.paintball.cmdframework.argument.ArgValue;
import me.gorgeousone.paintball.cmdframework.argument.Argument;
import me.gorgeousone.paintball.cmdframework.command.ArgCommand;
import me.gorgeousone.paintball.kit.KitType;
import me.gorgeousone.paintball.util.ConfigUtil;
import me.gorgeousone.paintball.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlayerStatsCommand extends ArgCommand {
	
	private final JavaPlugin plugin;
	
	public PlayerStatsCommand(JavaPlugin plugin) {
		super("stats");
		this.addArg(new Argument("player", ArgType.STRING).setDefault("~"));
		setPlayerRequired(false);
		
		this.plugin = plugin;
	}
	
	@Override
	protected void executeArgs(CommandSender sender, List<ArgValue> argValues, Set<String> usedFlags) {
		String playerName = argValues.get(0).get();
		OfflinePlayer player;
		
		if (playerName.equals("~")) {
			if (!(sender instanceof Player)) {
				StringUtil.msg(sender, "Please enter a player name.");
				return;
			}
			player = (Player) sender;
		} else {
			player = Bukkit.getOfflinePlayer(playerName);
			
			if (player == null) {
				StringUtil.msg(sender, "Player %s never played paintball.", playerName);
				return;
			}
		}
		File backupFile = ConfigUtil.matchFirstFile(player.getUniqueId().toString(), "player_stats", plugin);
		
		if (backupFile == null) {
			StringUtil.msg(sender, "Player %s never played paintball.", playerName);
			return;
		}
		listStats(sender, player.getName(), YamlConfiguration.loadConfiguration(backupFile));
	}
	
	private void listStats(CommandSender sender, String playerName, YamlConfiguration statsConfig) {
		String gamesData = "§7┌───§e Stats for §f§l%s: §7────\n" +
		                   "§7│ games played: §f%d\n" +
		                   "§7│ Win/Loss: §f%d/%d\n" +
		                   "§7│ K/D: §f%d/%d %.2f\n" +
		                   "§7│ revives: §f%d\n" +
		                   "§7│\n";
		String gunData = "§7│ §e%s\n" +
		                 "§7│ ├ times used: §f%d\n" +
		                 "§7│ ├ shots fired: §f%d\n" +
		                 "§7│ └ accuracy: §f%.1f%%\n";
		
		int gamesPlayed = statsConfig.getInt("games-played");
		int gamesWon = statsConfig.getInt("games-won");
		int kills = statsConfig.getInt("kills");
		int deaths = statsConfig.getInt("deaths");
		int revives = statsConfig.getInt("revives");
		float kdRatio = deaths == 0 ? kills : 1f * kills/deaths;
		sender.sendMessage(String.format(gamesData, playerName, gamesPlayed, gamesWon, gamesPlayed - gamesWon, kills, deaths, kdRatio, revives));
		
		for (KitType kitType : KitType.values()) {
			String gunKey = "gun-stats." + kitType.name().toLowerCase().replace("_", "-");
			
			String shortGunName = kitType.gunName.substring(12);
			int timesUsed = statsConfig.getInt(gunKey + ".times-used");
			int shotsFired = statsConfig.getInt(gunKey + ".shots-fired");
			float accuracy = shotsFired == 0 ? 0f : 100f * statsConfig.getInt(gunKey + ".bullet-hits") / shotsFired;
			sender.sendMessage(String.format(gunData, shortGunName, timesUsed, shotsFired, accuracy));
		}
	}
	
	@Override
	protected List<String> onTabComplete(CommandSender sender, String[] stringArgs) {
		return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
	}
}
