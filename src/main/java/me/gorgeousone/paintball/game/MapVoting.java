package me.gorgeousone.paintball.game;

import me.gorgeousone.paintball.arena.PbArena;
import me.gorgeousone.paintball.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class MapVoting {
	
	public static final String MAP_VOTE_UI_TITLE = "Vote a map";
	
	private final Map<UUID, PbArena> votes;
	private final Random rng = new Random();
	
	public MapVoting() {
		this.votes = new HashMap<>();
	}
	
	public void toggleVote(UUID playerId, PbArena arena) {
		if (votes.getOrDefault(playerId, null) == arena) {
			votes.remove(playerId);
		} else {
			votes.put(playerId, arena);
		}
	}
	
	public PbArena getVote(UUID playerId) {
		return votes.getOrDefault(playerId, null);
	}
	
	public PbArena pickArena(List<PbArena> arenas) {
		if (votes.isEmpty()) {
			return arenas.get(rng.nextInt(arenas.size()));
		}
		Map<PbArena, Integer> voteCounts = new HashMap<>();
		votes.values().forEach(a -> voteCounts.put(a, voteCounts.getOrDefault(a, 0) + 1));
		int maxVotes = Collections.max(voteCounts.values());
		List<PbArena> tiedArenas = new LinkedList<>();
		
		for (PbArena arena : voteCounts.keySet()) {
			if (voteCounts.get(arena) == maxVotes) {
				tiedArenas.add(arena);
			}
		}
		votes.clear();
		return tiedArenas.get(rng.nextInt(tiedArenas.size()));
	}
	
	public static void openMapVoteUI(Player player, List<PbArena> arenas, int oldArenaIdx) {
		int rows = Math.max(1, (int) Math.ceil(arenas.size() / 9f));
		Inventory mapVoter = Bukkit.createInventory(null, rows * 9, MAP_VOTE_UI_TITLE);
		int slot = 0;
		
		for (PbArena map : arenas) {
			ItemStack mapVoteItem = new ItemStack(Material.WHITE_TERRACOTTA);
			ItemUtil.nameItem(mapVoteItem, ChatColor.WHITE + map.getSpacedName());
			mapVoter.setItem(slot, mapVoteItem);
			++slot;
		}
		if (oldArenaIdx != -1) {
			ItemUtil.addMagicGlow(mapVoter.getItem(oldArenaIdx));
		}
		player.openInventory(mapVoter);
	}
	
	public static void toggleMapVote(Inventory mapVoter, int newArenaIdx, int oldArenaIdx) {
		if (oldArenaIdx != -1) {
			ItemUtil.removeMagicGlow(mapVoter.getItem(oldArenaIdx));
		}
		if (newArenaIdx != oldArenaIdx) {
			ItemUtil.addMagicGlow(mapVoter.getItem(newArenaIdx));
		}
	}
}
