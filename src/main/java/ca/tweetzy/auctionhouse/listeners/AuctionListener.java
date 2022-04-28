package ca.tweetzy.auctionhouse.listeners;

import ca.tweetzy.auctionhouse.AuctionHouse;
import ca.tweetzy.auctionhouse.api.AuctionAPI;
import ca.tweetzy.auctionhouse.api.events.AuctionAdminEvent;
import ca.tweetzy.auctionhouse.api.events.AuctionBidEvent;
import ca.tweetzy.auctionhouse.api.events.AuctionEndEvent;
import ca.tweetzy.auctionhouse.api.events.AuctionStartEvent;
import ca.tweetzy.auctionhouse.auction.AuctionStat;
import ca.tweetzy.auctionhouse.auction.enums.AuctionSaleType;
import ca.tweetzy.auctionhouse.settings.Settings;
import ca.tweetzy.auctionhouse.transaction.Transaction;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * The current file has been created by Kiran Hart
 * Date Created: February 27 2021
 * Time Created: 4:49 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class AuctionListeners implements Listener {

	@EventHandler
	public void onAuctionStart(AuctionStartEvent event) {
		AuctionHouse.getInstance().getAuctionStatManager().insertOrUpdate(event.getSeller(), new AuctionStat<>(1, 0, 0, 0D, 0D));
	}

	@EventHandler
	public void onAuctionEnd(AuctionEndEvent event) {
		AuctionHouse.getInstance().getAuctionStatManager().insertOrUpdate(event.getOriginalOwner(), new AuctionStat<>(0, 1, 0, event.getSaleType() == AuctionSaleType.USED_BIDDING_SYSTEM ? event.getAuctionItem().getCurrentPrice() : event.getAuctionItem().getBasePrice(), 0D));
		AuctionHouse.getInstance().getAuctionStatManager().insertOrUpdate(e.getBuyer(), new AuctionStat<>(0, 0, 0, 0D, event.getSaleType() == AuctionSaleType.USED_BIDDING_SYSTEM ? event.getAuctionItem().getCurrentPrice() : event.getAuctionItem().getBasePrice()));

		AuctionHouse.newChain().async(() -> {
			AuctionHouse.getInstance().getDataManager().insertTransactionAsync(new Transaction(UUID.randomUUID(), event.getOriginalOwner().getUniqueId(), event.getBuyer().getUniqueId(), event.getAuctionItem().getOwnerName(), event.getBuyer().getName(), System.currentTimeMillis(), event.getAuctionItem().getItem(), event.getSaleType(), event.getAuctionItem().getCurrentPrice()), (error, transaction) -> {
				if (error == null) AuctionHouse.getInstance().getTransactionManager().addTransaction(transaction);
			});
		}).execute();
	}

	@EventHandler
	public void onAdminAction(AuctionAdminEvent event) {
		AuctionHouse.getInstance().getDataManager().insertLogAsync(event.getAuctionAdminLog());
	}
}
