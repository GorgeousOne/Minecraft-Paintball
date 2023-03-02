package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.kit.AbstractKit;
import me.gorgeousone.superpaintball.kit.RifleKit;
import me.gorgeousone.superpaintball.kit.KitType;
import me.gorgeousone.superpaintball.kit.ShotgunKit;
import me.gorgeousone.superpaintball.team.Team;
import me.gorgeousone.superpaintball.team.TeamType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GameHandler {
	
	
	private final Map<KitType, AbstractKit> kits;
	private final Map<UUID, GameInstance> games;
	
	public GameHandler() {
		this.kits = new HashMap<>();
		this.games = new HashMap<>();
		kits.put(KitType.RIFLE, new RifleKit());
		kits.put(KitType.SHOTGUN, new ShotgunKit());
	}
	
	public GameInstance createGame() {
		GameInstance game = new GameInstance();
		games.put(game.getId(), game);
		return game;
	}
	
	public GameInstance getGame(Player player) {
		for (GameInstance game : games.values()) {
			if (game.hasPlayer(player)) {
				return game;
			}
		}
		return null;
	}
	
	public Team getTeam(Player player) {
		GameInstance game = getGame(player);
		
		if (game != null) {
			return game.getTeam(player);
		}
		return null;
	}
	
	public void launchShot(Player player, KitType kitType) {
		AbstractKit kit = kits.get(kitType);
		GameInstance game = getGame(player);
		kit.launchShot(player, game.getTeam(player));
	}
}
