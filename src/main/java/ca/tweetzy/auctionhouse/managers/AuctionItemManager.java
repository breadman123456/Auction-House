package ca.tweetzy.auctionhouse.managers;

import ca.tweetzy.auctionhouse.AuctionHouse;
import ca.tweetzy.auctionhouse.auction.AuctionedItem;
import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The current file has been created by Kiran Hart
 * Date Created: February 02 2021
 * Time Created: 6:27 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */

public class AuctionItemManager {

	@Getter private final ConcurrentHashMap<UUID, AuctionedItem> items = new ConcurrentHashMap<>();
	@Getter private final ConcurrentHashMap<UUID, AuctionedItem> garbageBin = new ConcurrentHashMap<>();
	@Getter private final ConcurrentHashMap<UUID, AuctionedItem> deletedItems = new ConcurrentHashMap<>();


	public void start() {
		mAuction.getInstance().getDataManager().getItems((error, results) -> {
			if (error == null) for (AuctionedItem item : results) addAuctionItem(item);
		});
	}

	public void end() {
		mAuction.getInstance().getDataManager().updateItems(this.items.values(), null);
	}

	public void addAuctionItem(@NonNull AuctionedItem auctionedItem) {
		this.items.put(auctionedItem.getId(), auctionedItem);
	}

	public void sendToGarbage(@NonNull AuctionedItem auctionedItem) {
		this.garbageBin.put(auctionedItem.getId(), auctionedItem);
	}

	public AuctionedItem getItem(@NonNull UUID id) {
		return this.items.getOrDefault(id, null);
	}
}
