package me.gorgeousone.superpaintball.kit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class ShotgunKit extends AbstractKit {
	
	private final JavaPlugin plugin;
	
	public ShotgunKit(JavaPlugin plugin) {
		super(KitType.SHOTGUN, 1, 8, 1.75f, .2f, 30, Sound.ENTITY_CHICKEN_EGG, .95f, .5f);
		this.plugin = plugin;
	}
	
	@Override
	protected void playGunshotSound(Player player, Collection<Player> others, float pitchLow, float pitchHigh) {
		super.playGunshotSound(player, others, pitchLow, pitchHigh);

		new BukkitRunnable() {
			@Override
			public void run() {
				ShotgunKit.super.playGunshotSound(player, others, pitchLow, pitchHigh + .05f);
			}
		}.runTaskLater(plugin, 1);
	}
}
