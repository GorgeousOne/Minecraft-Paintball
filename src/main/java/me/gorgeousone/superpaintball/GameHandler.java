package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.kit.AbstractKit;
import me.gorgeousone.superpaintball.kit.MachineGunKit;
import me.gorgeousone.superpaintball.kit.RifleKit;
import me.gorgeousone.superpaintball.kit.KitType;
import me.gorgeousone.superpaintball.kit.ShotgunKit;
import me.gorgeousone.superpaintball.kit.SniperKit;
import me.gorgeousone.superpaintball.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameHandler {
	
	private final JavaPlugin plugin;
	private final Map<KitType, AbstractKit> kits;
	private final Map<UUID, GameInstance> games;
	
	public GameHandler(JavaPlugin plugin) {
		this.plugin = plugin;
		this.kits = new HashMap<>();
		this.games = new HashMap<>();
		kits.put(KitType.RIFLE, new RifleKit());
		kits.put(KitType.SHOTGUN, new ShotgunKit(this.plugin));
		kits.put(KitType.MACHINE_GUN, new MachineGunKit());
		kits.put(KitType.SNIPER, new SniperKit());
	}
	
	public GameInstance createGame() {
		GameInstance game = new GameInstance();
		games.put(game.getId(), game);
		return game;
	}
	
	public GameInstance getGame(UUID playerId) {
		for (GameInstance game : games.values()) {
			if (game.hasPlayer(playerId)) {
				return game;
			}
		}
		return null;
	}
	
	public boolean hasPlayer(UUID playerId) {
		for (GameInstance game : games.values()) {
			if (game.hasPlayer(playerId)) {
				return true;
			}
		}
		return false;
	}
	
	public Team getTeam(UUID playerId) {
		GameInstance game = getGame(playerId);
		
		if (game != null) {
			return game.getTeam(playerId);
		}
		return null;
	}
	
	
	public AbstractKit getKit(KitType kitType) {
		return kits.get(kitType);
	}
	
}
