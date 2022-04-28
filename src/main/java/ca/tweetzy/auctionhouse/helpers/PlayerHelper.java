package ca.tweetzy.auctionhouse.helpers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The current file has been created by Kiran Hart
 * Date Created: January 12 2021
 * Time Created: 9:29 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class PlayerHelper {

	public static ItemStack getHeldItem(Player player) {
		return player.getInventory().getItemInMainHand();
	}
}
