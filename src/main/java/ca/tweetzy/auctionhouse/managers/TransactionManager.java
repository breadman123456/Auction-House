package ca.tweetzy.auctionhouse.managers;

import ca.tweetzy.auctionhouse.AuctionHouse;
import ca.tweetzy.auctionhouse.transaction.Transaction;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The current file has been created by Kiran Hart
 * Date Created: March 22 2021
 * Time Created: 3:34 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class TransactionManager {

	private final ConcurrentHashMap<UUID, Transaction> transactions = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Player, UUID> prePurchaseHolding = new ConcurrentHashMap<>();

	public void addTransaction(Transaction transaction) {
		if (transaction == null) return;
		this.transactions.put(transaction.getId(), transaction);
	}

	public void removeTransaction(UUID uuid) {
		this.transactions.remove(uuid);
	}

	public Transaction getTransaction(UUID uuid) {
		return this.transactions.getOrDefault(uuid, null);
	}

	public ConcurrentHashMap<UUID, Transaction> getTransactions() {
		return this.transactions;
	}

	public int getTotalItemsBought(UUID buyer) {
		int total = 0;
		Iterator<Map.Entry<UUID, Transaction>> iterator = this.transactions.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, Transaction> entry = iterator.next();
			if (entry.getValue().getBuyer().equals(buyer)) total++;
		}
		return total;
	}

	public int getTotalItemsSold(UUID seller) {
		int total = 0;
		Iterator<Map.Entry<UUID, Transaction>> iterator = this.transactions.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, Transaction> entry = iterator.next();
			if (entry.getValue().getSeller().equals(seller)) total++;
		}
		return total;
	}

	public void addPrePurchase(Player player, UUID uuid) {
		this.prePurchaseHolding.put(player, uuid);
	}

	public void removeAllRelatedPlayers(UUID uuid) {
		this.prePurchaseHolding.keySet().removeIf(p -> this.prePurchaseHolding.get(p).equals(uuid));
	}

	public ConcurrentHashMap<Player, UUID> getPrePurchaseHolding() {
		return this.prePurchaseHolding;
	}

	public List<Player> getPrePurchasePlayers(UUID uuid) {
		return this.prePurchaseHolding.keySet().stream().filter(p -> this.prePurchaseHolding.get(p).equals(uuid)).collect(Collectors.toList());
	}

	public void loadTransactions() {
		mAuction.getInstance().getDataManager().getTransactions((error, results) -> {
			if (error == null) for (Transaction transaction : results) addTransaction(transaction);
		});
	}
}
