package me.gorgeousone.superpaintball.team;

import me.gorgeousone.superpaintball.GameInstance;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class Team {
	
	private static final Random rng = new Random();
	
	private final TeamType teamType;
	private final GameInstance game;
	private final Set<UUID> players;
	private final Set<UUID> remainingPlayers;
	
	public Team(TeamType teamType, GameInstance game) {
		this.teamType = teamType;
		this.game = game;
		this.players = new HashSet<>();
		this.remainingPlayers = new HashSet<>();
	}
	
	
	public TeamType getType() {
		return teamType;
	}
	
	public GameInstance getGame() {
		return game;
	}
	
	public Set<UUID> getPlayers() {
		return new HashSet<>(players);
	}
	
	public Set<UUID> getRemainingPlayers() {
		return new HashSet<>(remainingPlayers);
	}
	
	public void addPlayer(Player player) {
		UUID playerId = player.getUniqueId();
		//if game started, throw
		players.add(playerId);
		remainingPlayers.add(playerId);
	}
	
	public void removePlayer(Player player) {}
	
	public void killPlayer(Player player) {}
	
	public void revivePlayer(Player player) {}
	
	public boolean hasPlayer(Player player) {
		return players.contains(player.getUniqueId());
	}
	
	public void paintBlock(Block shotBlock) {
		paintBlot(shotBlock, 5, 1);
		//spawn fancy particle stuff
		//make break sound
	}
	
	public void damagePlayer(Player player, int bulletDmg) {
		UUID playerId = player.getUniqueId();
		
		if (!remainingPlayers.contains(playerId)) {
			return;
		}
		Bukkit.broadcastMessage(String.format("hit %s for %o dmg", player.getName(), bulletDmg));
	}
	
	//TODO make this nicer patterns :(
	private void paintBlot(Block block, int blockCount, int range) {
		if (isTerracotta(block)) {
			teamType.blockColor.updateBlock(block, false);
		}
		List<Block> neighbors = getNeighbors(block, range);
		
		for (int i = 0; i < blockCount - 1; ++i) {
			if (neighbors.isEmpty()) {
				break;
			}
			int rndIdx = rng.nextInt(neighbors.size());
			Block neighbor = neighbors.get(rndIdx);
			teamType.blockColor.updateBlock(neighbor, false);
			neighbors.remove(rndIdx);
		}
	}
	
	private List<Block> getNeighbors(Block block, int range) {
		List<Block> terracotta = new LinkedList<>();
		
		for (int dz = -range; dz <= range; ++dz) {
			for (int dy = -range; dy <= range; ++dy) {
				for (int dx = -range; dx <= range; ++dx) {
					Block neighbor = block.getRelative(dx, dy, dz);
					
					if (isTerracotta(neighbor)) {
						terracotta.add(neighbor);
					}
				}
			}
		}
		terracotta.remove(block);
		return terracotta;
	}
	
	private boolean isTerracotta(Block block) {
		String matName = block.getType().name();
		return matName.contains("STAINED_CLAY") || matName.contains("TERRACOTTA");
	}
}
