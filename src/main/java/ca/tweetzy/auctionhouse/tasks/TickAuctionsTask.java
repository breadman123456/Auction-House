package ca.tweetzy.auctionhouse.tasks;

import ca.tweetzy.auctionhouse.AuctionHouse;
import ca.tweetzy.auctionhouse.api.AuctionAPI;
import ca.tweetzy.auctionhouse.api.events.AuctionEndEvent;
import ca.tweetzy.auctionhouse.auction.AuctionStat;
import ca.tweetzy.auctionhouse.auction.AuctionedItem;
import ca.tweetzy.auctionhouse.auction.enums.AuctionSaleType;
import ca.tweetzy.auctionhouse.settings.Settings;
import ca.tweetzy.core.hooks.EconomyManager;
import ca.tweetzy.core.utils.PlayerUtils;
import ca.tweetzy.core.utils.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The current file has been created by Kiran Hart
 * Date Created: February 18 2021
 * Time Created: 8:47 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class TickAuctionsTask extends BukkitRunnable {

	private static TickAuctionsTask instance;
	private static long clock;

	public static TickAuctionsTask startTask() {
		if (instance == null) {
			clock = 0L;
			instance = new TickAuctionsTask();
			instance.runTaskTimerAsynchronously(mAuction.getInstance(), 0, (long) 20 * 1);
		}
		return instance;
	}

	@Override
	public void run() {
		clock += Settings.TICK_UPDATE_TIME.getInt();

		Set<Map.Entry<UUID, AuctionedItem>> entrySet = mAuction.getInstance().getAuctionItemManager().getItems().entrySet();
		Iterator<Map.Entry<UUID, AuctionedItem>> auctionItemIterator = entrySet.iterator();


		while (auctionItemIterator.hasNext()) {
			Map.Entry<UUID, AuctionedItem> entry = auctionItemIterator.next();
			AuctionedItem auctionItem = entry.getValue();
			ItemStack itemStack = auctionItem.getItem();

			if (mAuction.getInstance().getAuctionItemManager().getGarbageBin().containsKey(auctionItem.getId())) {
				mAuction.getInstance().getAuctionItemManager().getGarbageBin().remove(auctionItem.getId());
				mAuction.getInstance().getAuctionItemManager().getDeletedItems().put(auctionItem.getId(), auctionItem);
				auctionItemIterator.remove();
				continue;
			}

			if (!mAuction.getInstance().getAuctionItemManager().getDeletedItems().keySet().isEmpty()) {
				if (clock % 60 == 0) {
					mAuction.getInstance().getDataManager().deleteItems(AuctionHouse.getInstance().getAuctionItemManager().getDeletedItems().values().stream().map(AuctionedItem::getId).collect(Collectors.toList()));
					mAuction.getInstance().getAuctionItemManager().getDeletedItems().clear();
				} else {
					if (mAuction.getInstance().getAuctionItemManager().getDeletedItems().size() >= Settings.GARBAGE_DELETION_MAX_ITEMS.getInt()) {
						mAuction.getInstance().getDataManager().deleteItemsAsync(AuctionHouse.getInstance().getAuctionItemManager().getDeletedItems().values().stream().map(AuctionedItem::getId).collect(Collectors.toList()));
						mAuction.getInstance().getAuctionItemManager().getDeletedItems().clear();
					}
				}
			}

			if (auctionItem.isInfinite()) continue;
			long timeRemaining = (auctionItem.getExpiresAt() - System.currentTimeMillis()) / 1000;

			if (!auctionItem.isExpired()) {
				if (Settings.BROADCAST_AUCTION_ENDING.getBoolean()) {
					if (timeRemaining <= Settings.BROADCAST_AUCTION_ENDING_AT_TIME.getInt() && timeRemaining % 10 == 0 && timeRemaining != 0) {
						Bukkit.getOnlinePlayers().forEach(player -> AuctionHouse.getInstance().getLocale().getMessage("auction.broadcast.ending")
								.processPlaceholder("item", AuctionAPI.getInstance().getItemName(itemStack))
								.processPlaceholder("seconds", timeRemaining)
								.sendPrefixedMessage(player));
					}
				}
			}

			if (timeRemaining <= 0 && !auctionItem.isExpired()) {
				if (auctionItem.getHighestBidder().equals(auctionItem.getOwner())) {
					auctionItem.setExpired(true);
					if (auctionItem.isExpired()) AuctionHouse.getInstance().getAuctionStatManager().insertOrUpdate(Bukkit.getOfflinePlayer(auctionItem.getOwner()), new AuctionStat<>(0, 0, 1, 0D, 0D));
					continue;
				}

				OfflinePlayer auctionWinner = Bukkit.getOfflinePlayer(auctionItem.getHighestBidder());

				double finalPrice = auctionItem.getCurrentPrice();
				double tax = Settings.TAX_ENABLED.getBoolean() ? (Settings.TAX_SALES_TAX_AUCTION_WON_PERCENTAGE.getDouble() / 100) * auctionItem.getCurrentPrice() : 0D;

				if (!EconomyManager.hasBalance(auctionWinner, Settings.TAX_CHARGE_SALES_TAX_TO_BUYER.getBoolean() ? finalPrice + tax : finalPrice)) {
					auctionItem.setExpired(true);
					continue;
				}

				AuctionEndEvent auctionEndEvent = new AuctionEndEvent(Bukkit.getOfflinePlayer(auctionItem.getOwner()), auctionWinner, auctionItem, AuctionSaleType.USED_BIDDING_SYSTEM);
				AuctionHouse.getInstance().getServer().getPluginManager().callEvent(auctionEndEvent);
				if (auctionEndEvent.isCancelled()) continue;

				AuctionAPI.getInstance().withdrawBalance(auctionWinner, Settings.TAX_CHARGE_SALES_TAX_TO_BUYER.getBoolean() ? finalPrice + tax : finalPrice);
				AuctionAPI.getInstance().depositBalance(Bukkit.getOfflinePlayer(auctionItem.getOwner()), Settings.TAX_CHARGE_SALES_TAX_TO_BUYER.getBoolean() ? finalPrice : finalPrice - tax);

				if (Bukkit.getOfflinePlayer(auctionItem.getOwner()).isOnline()) {
					mAuction.getInstance().getLocale().getMessage("auction.itemsold").processPlaceholder("item", AuctionAPI.getInstance().getItemName(itemStack)).processPlaceholder("amount", itemStack.getAmount()).processPlaceholder("price", AuctionAPI.getInstance().formatNumber(Settings.TAX_CHARGE_SALES_TAX_TO_BUYER.getBoolean() ? finalPrice : finalPrice - tax)).processPlaceholder("buyer_name", Bukkit.getOfflinePlayer(auctionItem.getHighestBidder()).getName()).processPlaceholder("buyer_displayname", AuctionAPI.getInstance().getDisplayName(Bukkit.getOfflinePlayer(auctionItem.getHighestBidder()))).sendPrefixedMessage(Bukkit.getOfflinePlayer(auctionItem.getOwner()).getPlayer());
					mAuction.getInstance().getLocale().getMessage("pricing.moneyadd").processPlaceholder("player_balance", AuctionAPI.getInstance().formatNumber(EconomyManager.getBalance(Bukkit.getOfflinePlayer(auctionItem.getOwner())))).processPlaceholder("price", AuctionAPI.getInstance().formatNumber(Settings.TAX_CHARGE_SALES_TAX_TO_BUYER.getBoolean() ? finalPrice : finalPrice - tax)).sendPrefixedMessage(Bukkit.getOfflinePlayer(auctionItem.getOwner()).getPlayer());
				}

				if (auctionWinner.isOnline()) {
					assert auctionWinner.getPlayer() != null;
					mAuction.getInstance().getLocale().getMessage("auction.bidwon").processPlaceholder("item", AuctionAPI.getInstance().getItemName(itemStack)).processPlaceholder("amount", itemStack.getAmount()).processPlaceholder("price", AuctionAPI.getInstance().formatNumber(Settings.TAX_CHARGE_SALES_TAX_TO_BUYER.getBoolean() ? finalPrice + tax : finalPrice)).sendPrefixedMessage(auctionWinner.getPlayer());
					mAuction.getInstance().getLocale().getMessage("pricing.moneyremove").processPlaceholder("player_balance", AuctionAPI.getInstance().formatNumber(EconomyManager.getBalance(auctionWinner.getPlayer()))).processPlaceholder("price", AuctionAPI.getInstance().formatNumber(Settings.TAX_CHARGE_SALES_TAX_TO_BUYER.getBoolean() ? finalPrice + tax : finalPrice)).sendPrefixedMessage(auctionWinner.getPlayer());

					if (Settings.ALLOW_PURCHASE_IF_INVENTORY_FULL.getBoolean()) {
						if (Settings.SYNCHRONIZE_ITEM_ADD.getBoolean())
							AuctionHouse.newChain().sync(() -> PlayerUtils.giveItem(auctionWinner.getPlayer(), itemStack)).execute();
						else
							PlayerUtils.giveItem(auctionWinner.getPlayer(), itemStack);

						AuctionHouse.getInstance().getAuctionItemManager().sendToGarbage(auctionItem);
						continue;
					}

					if (auctionWinner.getPlayer().getInventory().firstEmpty() != -1) {
						AuctionHouse.newChain().sync(() -> PlayerUtils.giveItem(auctionWinner.getPlayer(), itemStack)).execute();
						mAuction.getInstance().getAuctionItemManager().sendToGarbage(auctionItem);
						continue;
					}
				}

				auctionItem.setOwner(auctionWinner.getUniqueId());
				auctionItem.setHighestBidder(auctionWinner.getUniqueId());
				auctionItem.setExpired(true);
			}
		}
	}
}
