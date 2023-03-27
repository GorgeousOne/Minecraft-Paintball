package me.gorgeousone.superpaintball.game;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;

public class PbCountdown {
	
	private final JavaPlugin plugin;
	private int secondsLeft;
	private final Consumer<Integer> onTick;
	private final Runnable onTimeOut;
	private BukkitRunnable timer;
	private boolean isRunning;
	
	public PbCountdown(Consumer<Integer> onTick, Runnable onTimerOut, JavaPlugin plugin) {
		this.onTick = onTick;
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
				onTick.accept(secondsLeft);
				
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
	
	public Object getSecondsLeft() {
		return secondsLeft;
	}
}
