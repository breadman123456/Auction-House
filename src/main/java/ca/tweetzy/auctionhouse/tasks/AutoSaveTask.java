package ca.tweetzy.auctionhouse.tasks;

import ca.tweetzy.auctionhouse.AuctionHouse;
import ca.tweetzy.auctionhouse.settings.Settings;
import ca.tweetzy.core.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * The current file has been created by Kiran Hart
 * Date Created: April 15 2021
 * Time Created: 2:28 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class AutoSaveTask extends BukkitRunnable {

	private static AutoSaveTask instance;

	public static AutoSaveTask startTask() {
		if (instance == null) {
			instance = new AutoSaveTask();
			instance.runTaskTimerAsynchronously(AuctionHouse.getInstance(), 20 * 5, (long) 20 * Settings.AUTO_SAVE_EVERY.getInt());
		}
		return instance;
	}

	public void run() {
		mAuction.getInstance().getDataManager().updateItems(AuctionHouse.getInstance().getAuctionItemManager().getItems().values(), null);
		mAuction.getInstance().getDataManager().updateStats(AuctionHouse.getInstance().getAuctionStatManager().getStats(), null);
		mAuction.getInstance().getFilterManager().saveFilterWhitelist(true);
	}
}
