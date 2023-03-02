package me.gorgeousone.superpaintball.kit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ShotgunKit extends AbstractKit {
	
	private final JavaPlugin plugin;
	
	public ShotgunKit(JavaPlugin plugin) {
		super(KitType.SHOTGUN, 1, 8, 1.5f, .15f, 30, Sound.ENTITY_CHICKEN_EGG, .95f);
		this.plugin = plugin;
	}
	
	@Override
	protected void playGunshotSound(Location location) {
		location.getWorld().playSound(location, gunshotSound, .5f, gunshotPitch);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				location.getWorld().playSound(location, gunshotSound, .5f, gunshotPitch + .05f);
			}
		}.runTaskLater(plugin, 1);
	}
}
