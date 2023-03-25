package me.gorgeousone.superpaintball.game;

import me.gorgeousone.superpaintball.equipment.SlotClickEvent;
import me.gorgeousone.superpaintball.team.PbTeam;
import me.gorgeousone.superpaintball.team.TeamType;
import me.gorgeousone.superpaintball.util.ItemUtil;
import me.gorgeousone.superpaintball.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TeamQueue {
	
	private final Map<UUID, TeamType> teamQueues;
	
	public TeamQueue() {
		this.teamQueues = new HashMap<>();
	}
	
	public void onQueueForTeam(SlotClickEvent event) {
		Player player = event.getPlayer();
		UUID playerId = player.getUniqueId();
		
		int slot = event.getClickedSlot();
		TeamType newTeam = TeamType.values()[slot];
		TeamType queuedTeam = teamQueues.getOrDefault(playerId, null);
		
		if (newTeam == queuedTeam) { //I hope it's not possible to queue for team null :D
			StringUtil.msg(player, "You un-queued from team %s.", newTeam.displayName);
		} else {
			StringUtil.msg(player, "You queued for team %s.", newTeam.displayName);
			
		}
		changeQueuedTeam(player, queuedTeam, newTeam);
	}
	
	private void changeQueuedTeam(Player player, TeamType oldTeam, TeamType newTeam) {
		UUID playerId = player.getUniqueId();
		Inventory inv = player.getInventory();
		
		if (oldTeam != null) {
			ItemUtil.removeMagicGlow(inv.getItem(oldTeam.ordinal()));
			teamQueues.remove(playerId, oldTeam);
		}
		if (oldTeam != newTeam) {
			teamQueues.put(playerId, newTeam);
			ItemUtil.addMagicGlow(inv.getItem(newTeam.ordinal()));
		}
	}
	
	public void assignTeams(Collection<UUID> players, Map<TeamType, PbTeam> teams) {
		List<UUID> unassignedPlayers = new LinkedList<>(players);
		List<UUID> queuedPlayers = new LinkedList<>(teamQueues.keySet());
		Collections.shuffle(queuedPlayers);
		int maxPlayerPerTeam = (int) Math.ceil(players.size() / 2f);
		
		for (UUID playerId : queuedPlayers) {
			PbTeam team = teams.get(teamQueues.get(playerId));
			
			if (team.size() < maxPlayerPerTeam) {
				team.addPlayer(Bukkit.getPlayer(playerId));
				unassignedPlayers.remove(playerId);
			}
		}
		Collections.shuffle(unassignedPlayers);
		
		for (UUID playerId : unassignedPlayers) {
			for (PbTeam team : teams.values()) {
				if (team.size() < maxPlayerPerTeam) {
					team.addPlayer(Bukkit.getPlayer(playerId));
					break;
				}
			}
		}
		teamQueues.clear();
	}
}
