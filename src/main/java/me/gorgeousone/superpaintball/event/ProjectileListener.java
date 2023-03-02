package me.gorgeousone.superpaintball.event;

import me.gorgeousone.superpaintball.GameHandler;
import me.gorgeousone.superpaintball.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class ProjectileListener implements Listener {
	
	private final GameHandler gameHandler;
	
	public ProjectileListener(GameHandler gameHandler) {
		this.gameHandler = gameHandler;
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		Projectile projectile = event.getEntity();
		
		if (!(projectile.getShooter() instanceof Player)) {
			return;
		}
		Player player = (Player) projectile.getShooter();
		Team team = gameHandler.getTeam(player.getUniqueId());
		int bulletDmg = getBulletDmg(projectile);
		
		if (team == null || bulletDmg == 0) {
			return;
		}
//		projectile.remove();
		
		if (event.getHitBlock() != null) {
			team.paintBlock(event.getHitBlock());
			return;
		}
		if (!(event.getHitEntity() instanceof Player)) {
			return;
		}
		Player otherPlayer = (Player) event.getHitEntity();
		Team otherTeam = gameHandler.getTeam(otherPlayer.getUniqueId());
		
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
		Team team = gameHandler.getTeam(player.getUniqueId());
		
		if (team == null) {
			return;
		}
		ItemStack item = potion.getItem();
		PotionMeta meta = (PotionMeta) item.getItemMeta();
		
		if (meta.getBasePotionData().getType() != PotionType.AWKWARD) {
			return;
		}
		Bukkit.broadcastMessage("that dont do much yet");
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
		    gameHandler.getGame(player.getUniqueId()) != null) {
			event.setCancelled(true);
		}
	}
}
