package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.GameHandler;
import me.gorgeousone.superpaintball.GameInstance;
import me.gorgeousone.superpaintball.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BulletHitListener implements Listener {
	
	private final GameHandler gameHandler;
	
	public BulletHitListener(GameHandler gameHandler) {
		this.gameHandler = gameHandler;
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile bullet = event.getEntity();
		
		if (!(bullet.getShooter() instanceof Player)) {
			return;
		}
		Player player = (Player) bullet.getShooter();
		Team team = gameHandler.getTeam(player);
		int bulletDmg = getBulletDmg(bullet);

		if (team == null || bulletDmg == 0) {
			return;
		}
		bullet.remove();
		
		if (event.getHitBlock() != null) {
			team.paintBlock(event.getHitBlock());
			return;
		}
		if (!(event.getHitEntity() instanceof Player)) {
			return;
		}
		Player otherPlayer = (Player) event.getHitEntity();
		Team otherTeam = gameHandler.getTeam(otherPlayer);
		
		if (otherTeam == null) {
			return;
		}
		if (otherTeam.getType() != team.getType()) {
			otherTeam.damagePlayer(otherPlayer, bulletDmg);
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
		
		if (gameHandler.getGame(player) != null) {
			event.setCancelled(true);
		}
	}
}
