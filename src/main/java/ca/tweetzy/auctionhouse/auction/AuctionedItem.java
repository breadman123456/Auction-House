package ca.tweetzy.auctionhouse.auction;

import ca.tweetzy.auctionhouse.AuctionHouse;
import ca.tweetzy.auctionhouse.api.AuctionAPI;
import ca.tweetzy.auctionhouse.auction.enums.AuctionItemCategory;
import ca.tweetzy.auctionhouse.auction.enums.AuctionStackType;
import ca.tweetzy.auctionhouse.settings.Settings;
import ca.tweetzy.core.compatibility.ServerVersion;
import ca.tweetzy.core.utils.TextUtils;
import ca.tweetzy.core.utils.nms.NBTEditor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The current file has been created by Kiran Hart
 * Date Created: July 29 2021
 * Time Created: 6:58 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */

@Getter
@Setter
public class AuctionedItem {

	private UUID id;
	private UUID owner;
	private UUID highestBidder;

	private String ownerName;
	private String highestBidderName;
	private AuctionItemCategory category;

	private ItemStack item;
	private double basePrice;
	private double bidStartingPrice;
	private double bidIncrementPrice;
	private double currentPrice;

	private boolean isBidItem;
	private boolean expired;
	private long expiresAt;

	private String listedWorld = null;
	private boolean infinite = false;

	public AuctionedItem() {
	}

	public AuctionedItem(
			@NonNull UUID id,
			@NonNull UUID owner,
			@NonNull UUID highestBidder,
			@NonNull String ownerName,
			@NonNull String highestBidderName,
			@NonNull AuctionItemCategory category,
			@NonNull ItemStack item,
			double basePrice,
			double bidStartingPrice,
			double bidIncrementPrice,
			double currentPrice,
			boolean isBidItem,
			boolean expired,
			long expiresAt
	) {
		this.id = id;
		this.owner = owner;
		this.highestBidder = highestBidder;
		this.ownerName = ownerName;
		this.highestBidderName = highestBidderName;
		this.category = category;
		this.item = item;
		this.basePrice = basePrice;
		this.bidStartingPrice = bidStartingPrice;
		this.bidIncrementPrice = bidIncrementPrice;
		this.currentPrice = currentPrice;
		this.isBidItem = isBidItem;
		this.expired = expired;
		this.expiresAt = expiresAt;
	}

	public ItemStack getBidStack() {
		ItemStack itemStack = this.item.clone();
		itemStack.setAmount(Math.max(this.item.getAmount(), 1));
		ItemMeta meta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
		List<String> lore = (meta.hasLore()) ? meta.getLore() : new ArrayList<>();
		lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_DETAILS_HEADER.getStringList()));
		lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_DETAILS_SELLER.getStringList().stream().map(s -> s.replace("%seller%", this.ownerName)).collect(Collectors.toList())));
		lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_DETAILS_CURRENT_PRICE.getStringList().stream().map(s -> s.replace("%currentprice%", Settings.USE_SHORT_NUMBERS_ON_ITEMS.getBoolean() ? AuctionAPI.getInstance().getFriendlyNumber(this.currentPrice) : AuctionAPI.getInstance().formatNumber(this.currentPrice))).collect(Collectors.toList())));
		lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_DETAILS_HIGHEST_BIDDER.getStringList().stream().map(s -> s.replace("%highestbidder%", this.highestBidder.equals(this.owner) ? AuctionHouse.getInstance().getLocale().getMessage("auction.nobids").getMessage() : this.highestBidderName)).collect(Collectors.toList())));

		if (this.infinite) {
			lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_DETAILS_INFINITE.getStringList()));
		} else {
			long[] times = AuctionAPI.getInstance().getRemainingTimeValues((this.expiresAt - System.currentTimeMillis()) / 1000);
			lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_DETAILS_TIME_LEFT.getStringList().stream().map(s -> s
					.replace("%remaining_days%", String.valueOf(times[0]))
					.replace("%remaining_hours%", String.valueOf(times[1]))
					.replace("%remaining_minutes%", String.valueOf(times[2]))
					.replace("%remaining_seconds%", String.valueOf(times[3]))
			).collect(Collectors.toList())));
		}

		lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_PURCHASE_CONTROL_FOOTER.getStringList()));

		meta.setLore(lore);
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	public ItemStack getDisplayStack(AuctionStackType type) {
		ItemStack itemStack = this.item.clone();
		itemStack.setAmount(Math.max(this.item.getAmount(), 1));
		ItemMeta meta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemStack.getType());
		List<String> lore = (meta.hasLore()) ? meta.getLore() : new ArrayList<>();

		lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_DETAILS_HEADER.getStringList()));
		lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_DETAILS_SELLER.getStringList().stream().map(s -> s.replace("%seller%", this.ownerName)).collect(Collectors.toList())));

		if (this.basePrice != -1) {
			lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_DETAILS_BUY_NOW.getStringList().stream().filter(s -> this.isBidItem ? s.length() != 0 : s.length() >= 0).map(s -> s.replace("%buynowprice%", Settings.USE_SHORT_NUMBERS_ON_ITEMS.getBoolean() ? AuctionAPI.getInstance().getFriendlyNumber(this.basePrice) : AuctionAPI.getInstance().formatNumber(this.basePrice))).collect(Collectors.toList())));
		}

		if (this.isBidItem) {
			lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_DETAILS_CURRENT_PRICE.getStringList().stream().map(s -> s.replace("%currentprice%", Settings.USE_SHORT_NUMBERS_ON_ITEMS.getBoolean() ? AuctionAPI.getInstance().getFriendlyNumber(this.currentPrice) : AuctionAPI.getInstance().formatNumber(this.currentPrice))).collect(Collectors.toList())));
			if (!Settings.FORCE_CUSTOM_BID_AMOUNT.getBoolean()) {
				lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_DETAILS_BID_INCREMENT.getStringList().stream().map(s -> s.replace("%bidincrement%", Settings.USE_SHORT_NUMBERS_ON_ITEMS.getBoolean() ? AuctionAPI.getInstance().getFriendlyNumber(this.bidIncrementPrice) : AuctionAPI.getInstance().formatNumber(this.bidIncrementPrice))).collect(Collectors.toList())));
			}
			lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_DETAILS_HIGHEST_BIDDER.getStringList().stream().map(s -> s.replace("%highestbidder%", this.highestBidder.equals(this.owner) ? AuctionHouse.getInstance().getLocale().getMessage("auction.nobids").getMessage() : this.highestBidderName)).collect(Collectors.toList())));
		}

		if (this.infinite) {
			lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_DETAILS_INFINITE.getStringList()));
		} else {
			long[] times = AuctionAPI.getInstance().getRemainingTimeValues((this.expiresAt - System.currentTimeMillis()) / 1000);
			lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_DETAILS_TIME_LEFT.getStringList().stream().map(s -> s
					.replace("%remaining_days%", String.valueOf(times[0]))
					.replace("%remaining_hours%", String.valueOf(times[1]))
					.replace("%remaining_minutes%", String.valueOf(times[2]))
					.replace("%remaining_seconds%", String.valueOf(times[3]))
			).collect(Collectors.toList())));
		}

		lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_PURCHASE_CONTROL_HEADER.getStringList()));

		if (type == AuctionStackType.MAIN_AUCTION_HOUSE) {
			if (this.isBidItem) {
				if (this.basePrice != -1) {
					lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_PURCHASE_CONTROLS_BID_ON.getStringList()));
				} else {
					lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_PURCHASE_CONTROLS_BID_ON_NO_BUY_NOW.getStringList()));
				}
			} else {
				lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_PURCHASE_CONTROLS_BID_OFF.getStringList()));
			}

			if (NBTEditor.contains(itemStack, "AuctionBundleItem") || (ServerVersion.isServerVersionAtLeast(ServerVersion.V1_11) && itemStack.getType().name().contains("SHULKER_BOX"))) {
				lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_PURCHASE_CONTROLS_INSPECTION.getStringList()));
			}
		} else {
			lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_PURCHASE_CONTROLS_CANCEL_ITEM.getStringList()));
			if (Settings.ALLOW_PLAYERS_TO_ACCEPT_BID.getBoolean() && this.bidStartingPrice >= 1 || this.bidIncrementPrice >= 1) {
				if (!this.owner.equals(this.highestBidder)) {
					lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_PURCHASE_CONTROLS_ACCEPT_BID.getStringList()));
				}
			}
		}

		lore.addAll(TextUtils.formatText(Settings.AUCTION_STACK_PURCHASE_CONTROL_FOOTER.getStringList()));

		meta.setLore(lore);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
}
