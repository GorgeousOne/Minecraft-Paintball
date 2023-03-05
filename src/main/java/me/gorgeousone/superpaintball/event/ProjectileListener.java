package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.game.PbLobbyHandler;
import me.gorgeousone.superpaintball.team.PbTeam;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Collection;

public class ProjectileListener implements Listener {
	
	private final PbLobbyHandler lobbyHandler;
	
	public ProjectileListener(PbLobbyHandler lobbyHandler) {
		this.lobbyHandler = lobbyHandler;
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile projectile = event.getEntity();
		
		if (!(projectile.getShooter() instanceof Player)) {
			return;
		}
		Player player = (Player) projectile.getShooter();
		PbTeam team = lobbyHandler.getTeam(player.getUniqueId());
		int bulletDmg = getBulletDmg(projectile);
		
		if (team == null || bulletDmg == 0) {
			return;
		}
		//does this prevent teleporting? probably no
		projectile.remove();
		
		if (event.getHitBlock() != null) {
			team.paintBlock(event.getHitBlock());
			return;
		}
		if (!(event.getHitEntity() instanceof Player)) {
			return;
		}
		Player otherPlayer = (Player) event.getHitEntity();
		PbTeam otherTeam = lobbyHandler.getTeam(otherPlayer.getUniqueId());
		
		if (otherTeam == null) {
			return;
		}
		if (otherTeam.getType() != team.getType()) {
			otherTeam.damagePlayer(otherPlayer, player, bulletDmg);
		}
	}
	
	@EventHandler
	public void onPotionSplash(PotionSplashEvent event) {
		ThrownPotion potion = event.getPotion();
		
		if (!(potion.getShooter() instanceof Player)) {
			return;
		}
		Player player = (Player) potion.getShooter();
		PbTeam team = lobbyHandler.getTeam(player.getUniqueId());
		
		if (team == null) {
			return;
		}
		if (!lobbyHandler.getWaterBombs().isSimilar(potion.getItem())) {
			return;
		}
		healPlayers(getEffectedEntities(potion), team);
	}
	
	private void healPlayers(Collection<Entity> entities, PbTeam team) {
		for (Entity entity : entities) {
			if (entity instanceof Player) {
				Player player = (Player) entity;
				PbTeam playerTeam = lobbyHandler.getTeam(player.getUniqueId());
				
				if (team == playerTeam) {
					team.healPlayer(player);
				}
			} else if (entity instanceof ArmorStand) {
				ArmorStand skelly = (ArmorStand) entity;
				PbTeam skellyTeam = lobbyHandler.getTeam(skelly);
				
				if (team == skellyTeam) {
					skellyTeam.revivePlayer(skelly);
				}
			}
		}
	}
	
	int getBulletDmg(Projectile bullet) {
		String bulletName = bullet.getCustomName();
		
		if (bulletName == null) {
			return 0;
		}
		try {
			return Integer.parseInt(bulletName);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	@EventHandler()
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		
		if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL &&
		    lobbyHandler.getLobby(player.getUniqueId()) != null) {
			event.setCancelled(true);
		}
	}
	
	private Collection<Entity> getEffectedEntities(ThrownPotion potion) {
		Location pos = potion.getLocation();
		Collection<Entity> entities = pos.getWorld().getNearbyEntities(pos, 4, 2, 4);
		entities.remove(potion);
		return entities;
	}
	
}
