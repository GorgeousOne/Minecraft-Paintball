package me.gorgeousone.superpaintball;

import me.gorgeousone.superpaintball.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;

public class GameBoard {

	private final Scoreboard board;
	private final Objective objective;
	private final Map<Integer, String> lines;

	public GameBoard(int size) {
		this.board = Bukkit.getScoreboardManager().getNewScoreboard();
		this.objective = board.registerNewObjective("paintball", "dummy");
		this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.lines = new HashMap<>();

		for (int lineIdx = 1; lineIdx <= size; ++lineIdx) {
			String blank = StringUtil.pad(lineIdx);
			Score line = objective.getScore(blank);
			line.setScore(lineIdx);
			lines.put(lineIdx, blank);
		}
	}
	
	public void setTitle(String text) {
		objective.setDisplayName(text);
	}

	public void addPlayer(Player player) {
		player.setScoreboard(board);
	}

	public void removePlayer(Player player) {
		player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	public Team createTeam(String name, String prefix) {
		Team team = board.registerNewTeam(name);
		team.setPrefix(prefix);
		return team;
	}

	public void setLine(int lineIdx, String text) {
		if (lineIdx < 1 || lineIdx > lines.size()) {
			throw new IllegalArgumentException("Line index must be between 0 and " + lines.size());
		}
		String oldText = lines.get(lineIdx);
		board.resetScores(oldText);
		Score line = objective.getScore(text);
		line.setScore(lineIdx);
		lines.put(lineIdx, text);
	}
}
