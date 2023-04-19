package me.gorgeousone.paintball.event;

import me.gorgeousone.paintball.game.PbGame;
import me.gorgeousone.paintball.game.PbLobbyHandler;
import me.gorgeousone.paintball.kit.PbKitHandler;
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
import java.util.UUID;

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
		Player shooter = (Player) projectile.getShooter();
		UUID playerId = shooter.getUniqueId();
		PbGame game = lobbyHandler.getGame(playerId);
		int bulletDmg = getBulletDmg(projectile);
		
		if (game == null || bulletDmg == 0) {
			return;
		}
		if (event.getHitBlock() != null) {
			game.getTeam(playerId).paintBlock(event.getHitBlock());
			return;
		}
		if (!(event.getHitEntity() instanceof Player)) {
			return;
		}
		Player target = (Player) event.getHitEntity();
		
		if (lobbyHandler.getGame(target.getUniqueId()) == game) {
			game.damagePlayer(target, shooter, bulletDmg);
		}
	}
	
	@EventHandler
	public void onPotionSplash(PotionSplashEvent event) {
		ThrownPotion potion = event.getPotion();
		
		if (!(potion.getShooter() instanceof Player)) {
			return;
		}
		Player player = (Player) potion.getShooter();
		PbGame game = lobbyHandler.getGame(player.getUniqueId());
		
		if (game == null || !PbKitHandler.getWaterBombs().isSimilar(potion.getItem())) {
			return;
		}
		for (Entity entity : getEffectedEntities(potion)) {
//		for (Entity entity : event.getAffectedEntities()) {
			if (entity instanceof Player) {
				game.healPlayer((Player) entity, player);
			} else if (entity instanceof ArmorStand) {
				game.revivePlayer((ArmorStand) entity, player);
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
