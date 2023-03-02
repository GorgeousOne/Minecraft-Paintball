package me.gorgeousone.superpaintball.kit;

import me.gorgeousone.superpaintball.team.Team;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import java.util.Random;

public abstract class AbstractKit {
	
	protected final KitType kitType;
	protected final int bulletDmg;
	protected final int bulletCount;
	protected final float bulletSpeed;
	protected final float bulletSpread;
	protected final float fireRate;
	protected final Sound gunshotSound;
	protected final float gunshotPitch;
	protected final Random rnd = new Random();
	
	protected AbstractKit(KitType kitType, int bulletDmg, int bulletCount, float bulletSpeed, float bulletSpread, float fireRate,
	                      Sound gunshotSound, float gunshotPitch) {
		this.kitType = kitType;
		this.bulletDmg = bulletDmg;
		this.bulletCount = bulletCount;
		this.bulletSpeed = bulletSpeed;
		this.bulletSpread = bulletSpread;
		this.fireRate = fireRate;
		this.gunshotSound = gunshotSound;
		this.gunshotPitch = gunshotPitch;
	}
	
	public void launchShot(Player player, Team team) {
		Vector facing = player.getLocation().getDirection();
		
		for (int i = 0; i < bulletCount; ++i) {
			Projectile bullet = player.launchProjectile(team.getType().projectileType);
			bullet.setShooter(player);
			bullet.setVelocity(createVelocity(facing, bulletSpeed, bulletSpread));
			bullet.setCustomName("" + bulletDmg);
		}
		playGunshotSound(player.getEyeLocation());
	}
	
	//TODO make own gunshots sound high pitched
	//TODO lower pitch? seems loud
	protected void playGunshotSound(Location location) {
		location.getWorld().playSound(location, gunshotSound, .5f, gunshotPitch);
	}
	
	public Vector createVelocity(Vector facing, float speed, float spread) {
		Vector velocity = facing.clone();
		velocity.add(new Vector(
				rnd.nextFloat() * spread,
				rnd.nextFloat() * spread,
				rnd.nextFloat() * spread));
		return velocity.multiply(speed);
	}
}
