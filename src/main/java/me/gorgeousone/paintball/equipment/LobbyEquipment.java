package me.gorgeousone.paintball.equipment;

import me.gorgeousone.paintball.Message;
import me.gorgeousone.paintball.kit.KitType;
import me.gorgeousone.paintball.kit.PbKitHandler;
import me.gorgeousone.paintball.team.TeamType;
import me.gorgeousone.paintball.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.function.Consumer;

/**
 * Class to manage hotbar items with custom click functions for players in the paintball lobby.
 */
public class LobbyEquipment extends Equipment {
	
	private static final String LOBBY_ITEM_NAME = ChatColor.WHITE + "%s" + ChatColor.GRAY + " (Right Click)";
	private final PbKitHandler kitHandler;
	
	private static final int MAP_SLOT = 4;
	private static final int KIT_SLOT = 5;
	private static final int LEAVE_SLOT = 8;
	
	public LobbyEquipment(
			Consumer<SlotClickEvent> onTeamSelect,
			Consumer<SlotClickEvent> onMapVote,
			Consumer<SlotClickEvent> onKitSelect,
			Consumer<SlotClickEvent> onLobbyLeave,
			PbKitHandler kitHandler) {
		this.kitHandler = kitHandler;
		int i = 0;
		
		for (TeamType teamType : TeamType.values()) {
			ItemStack teamItem = teamType.getJoinItem();
			ItemUtil.nameItem(teamItem, Message.LOBBY_ITEM_NAME.format(Message.TEAM + " " + teamType.displayName));
			
			setItem(i, teamItem, onTeamSelect);
			++i;
		}
		setItem(MAP_SLOT, ItemUtil.nameItem(new ItemStack(Material.BOOK), Message.LOBBY_ITEM_NAME.format(Message.VOTE_MAP)), onMapVote);
		setItem(KIT_SLOT, KitType.RIFLE.getGun(), onKitSelect);
		setItem(LEAVE_SLOT, ItemUtil.nameItem(new ItemStack(Material.CLOCK), Message.LOBBY_ITEM_NAME.format(Message.QUIT)), onLobbyLeave);
	}
	
	@Override
	public void equip(Player player) {
		super.equip(player);
		changeKit(player, kitHandler.getKitType(player.getUniqueId()));
	}
	
	public void changeKit(Player player, KitType newKit) {
		ItemStack kitItem = newKit.getGun();
		ItemUtil.nameItem(kitItem, Message.LOBBY_ITEM_NAME.format(Message.KIT + " " + newKit.gunName));
		
		PlayerInventory inv = player.getInventory();
		inv.setItem(KIT_SLOT, kitItem);
	}
}
