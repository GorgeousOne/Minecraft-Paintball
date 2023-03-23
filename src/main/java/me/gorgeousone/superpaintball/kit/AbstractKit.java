package me.gorgeousone.superpaintball.kit;

import me.gorgeousone.superpaintball.team.PbTeam;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public abstract class AbstractKit {
	
	protected final KitType kitType;
	protected final int bulletDmg;
	protected final int bulletCount;
	protected final float bulletSpeed;
	protected final float bulletSpread;
	protected final long fireRate;
	protected final Sound gunshotSound;
	protected final float gunshotPitchHigh;
	protected final float gunshotPitchLow;
	protected final Random rnd = new Random();

	private final Map<UUID, Long> shootCooldowns;

	protected AbstractKit(KitType kitType,
	                      int bulletDmg,
	                      int bulletCount,
	                      float bulletSpeed,
	                      float bulletSpread,
	                      long fireRate,
	                      Sound gunshotSound,
	                      float gunshotPitchHigh, float gunshotPitchLow) {
		this.kitType = kitType;
		this.bulletDmg = bulletDmg;
		this.bulletCount = bulletCount;
		this.bulletSpeed = bulletSpeed;
		this.bulletSpread = bulletSpread;
		this.fireRate = fireRate;
		this.gunshotSound = gunshotSound;
		this.gunshotPitchHigh = gunshotPitchHigh;
		this.gunshotPitchLow = gunshotPitchLow;
		this.shootCooldowns = new HashMap<>();
	}
	
	public void launchShot(Player player, PbTeam team, Collection<Player> coplayers) {
		UUID playerId = player.getUniqueId();

		if (shootCooldowns.getOrDefault(playerId, 0L) > System.currentTimeMillis()) {
			return;
		}
		Vector facing = player.getLocation().getDirection();
		
		for (int i = 0; i < bulletCount; ++i) {
			Projectile bullet = player.launchProjectile(team.getType().projectileType);
			bullet.setShooter(player);
			bullet.setVelocity(createVelocity(facing, bulletSpeed, bulletSpread));
			bullet.setCustomName("" + bulletDmg);
		}
		playGunshotSound(player, coplayers, gunshotPitchLow, gunshotPitchHigh);

		if (fireRate > 0) {
			shootCooldowns.put(playerId, System.currentTimeMillis() + fireRate * 50);
		}
	}
	
	//TODO make own gunshots sound high pitched
	//TODO lower pitch? seems loud
	protected void playGunshotSound(Player player, Collection<Player> coplayers, float pitchLow, float pitchHigh) {
		Location location = player.getEyeLocation();

		for (Player other : coplayers) {
			if (other == player) {
				other.playSound(location, gunshotSound, .5f, pitchHigh);
			} else {
				other.playSound(location, gunshotSound, .5f, pitchLow);
			}
		}
	}
	
	public Vector createVelocity(Vector facing, float speed, float spread) {
		Vector velocity = facing.clone();
		velocity.add(new Vector(
				(rnd.nextFloat() - .5) * spread,
				(rnd.nextFloat() - .5) * spread,
				(rnd.nextFloat() - .5) * spread));
		return velocity.multiply(speed);
	}

	public void removePlayer(UUID playerId) {
		shootCooldowns.remove(playerId);
	}
}
