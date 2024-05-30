package me.gorgeousone.paintball.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.gorgeousone.paintball.Message;
import me.gorgeousone.paintball.kit.KitType;
import me.gorgeousone.paintball.util.ConfigUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class GameStatsExpansion extends PlaceholderExpansion {

	private final JavaPlugin plugin;

	public GameStatsExpansion(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public @NotNull String getIdentifier() {
		return "paintball";
	}

	@Override
	public @NotNull String getAuthor() {
		return "Gorgeousone";
	}

	@Override
	public @NotNull String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
		File backupFile = ConfigUtil.matchFirstFile(player.getUniqueId().toString(), "player_stats", plugin);

		if (backupFile == null) {
			return "0";
		}
		YamlConfiguration stats = YamlConfiguration.loadConfiguration(backupFile);
		return String.valueOf(getStat(stats, params));
	}

	private Object getStat(YamlConfiguration stats, String key) {
		switch (key) {
			case "games_played":
				return stats.getInt("games-played");
			case "games_won":
				return stats.getInt("games-won");
			case "games_lost":
				return stats.getInt("games-played") - stats.getInt("games-won");
			case "kills":
				return stats.getInt("kills");
			case "deaths":
				return stats.getInt("deaths");
			case "kd_ratio":
				int deaths = stats.getInt("deaths");
				return deaths == 0 ? stats.getInt("kills") : 1f * stats.getInt("kills") / deaths;
			case "revives":
				return stats.getInt("revives");
			default:
				if (key.startsWith("gun_")) {
					KitType kitType = KitType.valueOf(key.substring(4).toUpperCase().replace("-", "_"));
					String gunKey = "gun-stats." + kitType.name().toLowerCase().replace("_", "-");

					return switch (key.substring(4)) {
						case "times_used" -> stats.getInt(gunKey + ".times-used");
						case "shots_fired" -> stats.getInt(gunKey + ".shots-fired");
						case "accuracy" -> {
							int shotsFired = stats.getInt(gunKey + ".shots-fired");
							yield shotsFired == 0 ? 0 : 100f * stats.getInt(gunKey + ".bullet-hits") / shotsFired;
						}
						default -> null;
					};
				}
				return null;
		}
	}
}
