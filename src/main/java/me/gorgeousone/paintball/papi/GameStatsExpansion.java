package me.gorgeousone.paintball.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
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
			case "kd":
				int deaths = stats.getInt("deaths");
				if (deaths == 0) {
					return "0.00";
				}
				float kd = 1f * stats.getInt("kills") / deaths;
				return String.format("%.2f", kd);
			case "revives":
				return stats.getInt("revives");
			default:
				KitType kitType;

				if (key.startsWith("rifle")) {
					kitType = KitType.RIFLE;
				} else if (key.startsWith("shotgun")) {
					kitType = KitType.SHOTGUN;
				} else if (key.startsWith("machine_gun")) {
					kitType = KitType.MACHINE_GUN;
				} else {
					return null;
				}
				String gunKey = kitType.name().toLowerCase().replace("_", "-");
				String statKey = key.substring(gunKey.length());

				switch (statKey) {
					case "_times_used":
						return stats.getInt("gun-stats." + gunKey + ".times-used");
					case "_shots_fired":
						return stats.getInt("gun-stats." + gunKey + ".shots-fired");
					case "_bullet_hits":
						stats.getInt("gun-stats." + gunKey + ".bullet-hits");
					case "_accuracy":
						int shotsFired = stats.getInt("gun-stats." + gunKey + ".shots-fired");
						if (shotsFired == 0) {
							return 0;
						}
						float accuracy = 100f * stats.getInt("gun-stats." + gunKey + ".bullet-hits") / shotsFired;
						return String.format("%.1f", accuracy) + "%";
					default:
						return null;
				}
		}
	}
}
