package me.gorgeousone.superpaintball.equipment;

import me.gorgeousone.superpaintball.kit.KitType;
import me.gorgeousone.superpaintball.kit.PbKitHandler;
import me.gorgeousone.superpaintball.team.TeamType;
import me.gorgeousone.superpaintball.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.function.Consumer;

public class LobbyEquipment extends Equipment {

	private static final String KIT_ITEM_NAME = ChatColor.WHITE + "Kit %s" + ChatColor.GRAY + " (Right Click)";
	private static final String TEAM_ITEM_NAME = ChatColor.WHITE + "Team %s" + ChatColor.GRAY + " (Right Click)";
	private final PbKitHandler kitHandler;

	private static final int KIT_SLOT = 8;

	public LobbyEquipment(
			Consumer<SlotClickEvent> onTeamSelect,
			Consumer<SlotClickEvent> onKitSelect,
			PbKitHandler kitHandler) {
		this.kitHandler = kitHandler;
		int i = 0;

		for (TeamType teamType : TeamType.values()) {
			ItemStack teamItem = teamType.getJoinItem();
			ItemUtil.setItemName(teamItem, String.format(TEAM_ITEM_NAME, teamType.displayName));

			setItem(i, teamItem, onTeamSelect);
			++i;
		}
		setItem(KIT_SLOT, KitType.RIFLE.getGun(), onKitSelect);
	}

	@Override
	public void equip(Player player) {
		super.equip(player);
		changeKit(player, kitHandler.getKitType(player.getUniqueId()));
	}

	public void changeKit(Player player, KitType newKit) {
		ItemStack kitItem = newKit.getGun();
		ItemUtil.setItemName(kitItem, String.format(KIT_ITEM_NAME, newKit.gunName));

		PlayerInventory inv = player.getInventory();
		inv.setItem(KIT_SLOT, kitItem);
	}
}