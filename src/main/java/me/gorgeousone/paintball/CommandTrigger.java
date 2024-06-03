package me.gorgeousone.paintball;

import me.gorgeousone.paintball.team.PbTeam;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handler to for triggering commands specified in the config.yml at game end
 */
public class CommandTrigger {

	private static final String GAME_END_COMMANDS_KEY = "command-triggers.game-end-commands";
	private static final String PLAYER_WIN_COMMANDS_KEY = "command-triggers.player-win-commands";
	private static final String PLAYER_LOSE_COMMANDS_KEY = "command-triggers.player-lose-commands";

	private final CommandSender console;
	private List<String> gameEndCommands;
	private List<Message> playerWinCommands;
	private List<Message> playerLoseCommands;

	public CommandTrigger() {
		gameEndCommands = new ArrayList<>();
		playerWinCommands = new ArrayList<>();
		playerLoseCommands = new ArrayList<>();
		console = Bukkit.getConsoleSender();
	}

	public void loadCommands(FileConfiguration config) {
		gameEndCommands = (List<String>) config.getList(GAME_END_COMMANDS_KEY, new ArrayList<>());
		playerWinCommands = createMessages((List<String>) config.getList(PLAYER_WIN_COMMANDS_KEY, new ArrayList<>()));
		playerLoseCommands = createMessages((List<String>) config.getList(PLAYER_LOSE_COMMANDS_KEY, new ArrayList<>()));
	}

	/**
	 * Creates messages that can be formatted to insert player name anywhere a %player% appears
	 */
	private List<Message> createMessages(List<String> strings) {
		return strings.stream()
				.map(s -> new Message(s, "player"))
				.collect(Collectors.toList());
	}

	public void triggerGameEndCommands() {
		for (String command : gameEndCommands) {
			Bukkit.dispatchCommand(console, command);
		}
	}

	public void triggerPlayerWinCommands(PbTeam winners) {
		List<String> playerNames = getPlayerNames(winners.getPlayers());

		for (Message command : playerWinCommands) {
			for (String playerName : playerNames) {
				Bukkit.dispatchCommand(console, command.format(playerName));
			}
		}
	}

	public void triggerPlayerLoseCommands(PbTeam losers) {
		List<String> playerNames = getPlayerNames(losers.getPlayers());

		for (Message command : playerLoseCommands) {
			for (String playerName : playerNames) {
				Bukkit.dispatchCommand(console, command.format(playerName));
			}
		}
	}

	private List<String> getPlayerNames(Collection<UUID> playerIds) {
		return playerIds.stream()
				.map(Bukkit::getPlayer)
				.filter(Objects::nonNull)
				.map(Player::getName)
				.collect(Collectors.toList());
	}
}
