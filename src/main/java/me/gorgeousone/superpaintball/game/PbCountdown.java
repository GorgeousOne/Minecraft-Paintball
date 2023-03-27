package me.gorgeousone.superpaintball.game;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;
import java.util.function.Consumer;

public class PbCountdown {
	
	private static final Set<Integer> ANNOUNCEMENTS = Set.of(300, 240, 180, 120, 60, 30, 20, 10, 3, 2, 1);
	private final JavaPlugin plugin;
	private int secondsLeft;
	private final Consumer<Integer> onAnnounceTime;
	private final Runnable onTimeOut;
	private BukkitRunnable timer;
	private boolean isRunning;
	
	public PbCountdown(Consumer<Integer> onAnnounceTime, Runnable onTimerOut, JavaPlugin plugin) {
		this.onAnnounceTime = onAnnounceTime;
		this.onTimeOut = onTimerOut;
		this.plugin = plugin;
	}
	
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public void start(int seconds) {
		if (isRunning()) {
			return;
		}
		isRunning = true;
		secondsLeft = seconds;
		
		timer = new BukkitRunnable() {
			@Override
			public void run() {
				if (!isRunning) {
					return;
				}
				if (ANNOUNCEMENTS.contains(secondsLeft)) {
					onAnnounceTime.accept(secondsLeft);
				}
				if (secondsLeft <= 0) {
					this.cancel();
					isRunning = false;
					onTimeOut.run();
				}
				secondsLeft -= 1;
			}
		};
		timer.runTaskTimer(plugin, 0, 20);
	}
	
	public void cancel() {
		if (isRunning) {
			isRunning = false;
			timer.cancel();
		}
	}
}
