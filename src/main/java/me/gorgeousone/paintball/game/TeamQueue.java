package me.gorgeousone.paintball.game;

import me.gorgeousone.paintball.Message;
import me.gorgeousone.paintball.equipment.SlotClickEvent;
import me.gorgeousone.paintball.team.PbTeam;
import me.gorgeousone.paintball.team.TeamType;
import me.gorgeousone.paintball.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class to keep track of players that queued for a team and to assign them to the teams when the game starts.
 */
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
			Message.LINE_36.send(player, newTeam.displayName);
		} else {
			Message.LINE_37.send(player, newTeam.displayName);
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
			player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f);
		}
	}
	
	public void assignTeams(Collection<UUID> players, Map<TeamType, PbTeam> teams) {
		int maxPlayerPerTeam = (int) Math.ceil(players.size() / 2f);
		List<UUID> unassignedPlayers = new LinkedList<>(players);
		List<UUID> queuedPlayers = new LinkedList<>(teamQueues.keySet());
		Collections.shuffle(queuedPlayers);
		
		for (UUID playerId : queuedPlayers) {
			PbTeam team = teams.get(teamQueues.get(playerId));
			
			if (team.size() < maxPlayerPerTeam) {
				team.addPlayer(Bukkit.getPlayer(playerId));
				unassignedPlayers.remove(playerId);
			}
		}
		List<PbTeam> shuffledTeams = new LinkedList<>(teams.values());
		Collections.shuffle(shuffledTeams);
		Collections.shuffle(unassignedPlayers);
		
		for (PbTeam team : shuffledTeams) {
			while (team.size() < maxPlayerPerTeam && unassignedPlayers.size() > 0) {
				team.addPlayer(Bukkit.getPlayer(unassignedPlayers.remove(0)));
			}
		}
		teamQueues.clear();
	}
	
	public void removePlayer(UUID playerId) {
		teamQueues.remove(playerId);
	}
}
