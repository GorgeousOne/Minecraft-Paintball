package me.gorgeousone.superpaintball.kit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import java.util.Random;

public abstract class AbstractKit {
	
	protected final KitType kitType;
	protected final Random rnd;
	
	protected AbstractKit(KitType kitType) {
		this.kitType = kitType;
		this.rnd = new Random();
	}
	
	public void launchShot(Player player) {
		Location eyeLoc = player.getEyeLocation();
		Vector facing = player.getLocation().getDirection();
		World world = player.getWorld();
		
		for (int i = 0; i < kitType.bulletCount; ++i) {
			Projectile bullet = world.spawn(eyeLoc, Snowball.class);
			bullet.setShooter(player);
			bullet.setVelocity(createVelocity(facing, kitType.bulletSpeed, kitType.bulletSpread));
		}
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
