package me.gorgeousone.paintball.game;

import me.gorgeousone.paintball.kit.KitType;
import me.gorgeousone.paintball.util.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class to keep track of the stats of a game and save them to the player's stats file after the game is over.
 */
public class GameStats {
	
	private final HashMap<UUID, KitType> playerGuns;
	private final HashMap<UUID, Integer> shotsFired;
	private final HashMap<UUID, Integer> bulletHits;
	private final HashMap<UUID, Integer> kills;
	private final HashMap<UUID, Integer> deaths;
	private final HashMap<UUID, Integer> revives;
	private final HashMap<UUID, Boolean> isWin;
	
	public GameStats() {
		playerGuns = new HashMap<>();
		shotsFired = new HashMap<>();
		bulletHits = new HashMap<>();
		kills = new HashMap<>();
		deaths = new HashMap<>();
		revives = new HashMap<>();
		isWin = new HashMap<>();
	}
	
	public void addPlayer(UUID playerId, KitType gunType) {
		playerGuns.put(playerId, gunType);
		shotsFired.put(playerId, 0);
		bulletHits.put(playerId, 0);
		kills.put(playerId, 0);
		deaths.put(playerId, 0);
		revives.put(playerId, 0);
		isWin.put(playerId, false);
	}
	
	public void addGunShot(UUID shooterId) {
		assert playerGuns.containsKey(shooterId);
		shotsFired.merge(shooterId, 1, Integer::sum);
	}
	
	public void addBulletHit(UUID shooterId) {
		assert playerGuns.containsKey(shooterId);
		bulletHits.merge(shooterId, 1, Integer::sum);
	}
	
	public void addKill(UUID shooterId, UUID victimId) {
		assert playerGuns.containsKey(shooterId);
		kills.merge(shooterId, 1, Integer::sum);
		deaths.merge(victimId, 1, Integer::sum);
	}
	
	public void addRevive(UUID playerId) {
		assert playerGuns.containsKey(playerId);
		revives.merge(playerId, 1, Integer::sum);
	}
	
	public void setWin(UUID playerId) {
		assert playerGuns.containsKey(playerId);
		isWin.put(playerId, true);
	}
	
	public void save(JavaPlugin plugin) {
		for (UUID playerId : playerGuns.keySet()) {
			updatePlayerStats(playerId, plugin);
		}
	}
	
	private void updatePlayerStats(UUID playerId, JavaPlugin plugin) {
		String filePath = "player_stats/" + Bukkit.getOfflinePlayer(playerId).getName() + playerId;
		YamlConfiguration statsConfig = ConfigUtil.loadConfig(filePath, "player_stats", plugin);
		Map<String, Integer> statsToAdd = new LinkedHashMap<>();
		
		String gunKey = "gun-stats." + playerGuns.get(playerId).name().toLowerCase().replace("_", "-");
		statsToAdd.put("games-played", 1);
		statsToAdd.put("games-won", isWin.get(playerId) ? 1 : 0);
		statsToAdd.put("kills", kills.get(playerId));
		statsToAdd.put("deaths", deaths.get(playerId));
		statsToAdd.put("revives", revives.get(playerId));
		statsToAdd.put(gunKey + ".times-used", 1);
		statsToAdd.put(gunKey + ".shots-fired", shotsFired.get(playerId));
		statsToAdd.put(gunKey + ".bullet-hits", bulletHits.get(playerId));
		
		for (String key : statsToAdd.keySet()) {
			statsConfig.set(key, statsConfig.getInt(key) + statsToAdd.get(key));
		}
		ConfigUtil.saveConfig(statsConfig, filePath, plugin);
	}
}
