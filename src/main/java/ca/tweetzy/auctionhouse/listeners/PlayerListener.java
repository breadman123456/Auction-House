package ca.tweetzy.auctionhouse.listeners;

import ca.tweetzy.auctionhouse.AuctionHouse;
import ca.tweetzy.auctionhouse.api.AuctionAPI;
import ca.tweetzy.auctionhouse.api.UpdateChecker;
import ca.tweetzy.auctionhouse.auction.AuctionPlayer;
import ca.tweetzy.auctionhouse.guis.GUIAuctionHouse;
import ca.tweetzy.auctionhouse.helpers.PlayerHelper;
import ca.tweetzy.auctionhouse.settings.Settings;
import ca.tweetzy.core.compatibility.ServerVersion;
import ca.tweetzy.core.compatibility.XMaterial;
import ca.tweetzy.core.utils.PlayerUtils;
import ca.tweetzy.core.utils.TextUtils;
import ca.tweetzy.core.utils.nms.NBTEditor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * The current file has been created by Kiran Hart
 * Date Created: February 10 2021
 * Time Created: 1:33 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class PlayerListeners implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		mAuction.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(mAuction.getInstance(), () -> {
			mAuction.getInstance().getAuctionPlayerManager().addPlayer(new AuctionPlayer(player));
		}, 20);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		mAuction.getInstance().getAuctionPlayerManager().removePlayer(player.getUniqueId());
		mAuction.getInstance().getAuctionPlayerManager().getCooldowns().remove(player.getUniqueId());
		mAuction.getInstance().getAuctionPlayerManager().getSellHolding().remove(player.getUniqueId());
	}

	@EventHandler
	public void onCraftWithBundle(PrepareItemCraftEvent event) {
		final ItemStack[] craftingItems = event.getInventory().getMatrix();
		for (ItemStack item : craftingItems) {
			if (item == null || item.getType() == XMaterial.AIR.parseMaterial()) continue;
			if (NBTEditor.contains(item, "AuctionBundleItem")) event.getInventory().setResult(XMaterial.AIR.parseItem());
		}
	}

	@EventHandler
	public void onAuctionChestClick(PlayerInteractEvent event) {
		final Player player = e.getPlayer();
		final Block block = e.getClickedBlock();

		if (block == null || block.getType() != XMaterial.CHEST.parseMaterial()) return;
		final Chest chest = (Chest) block.getState();

		final NamespacedKey key = new NamespacedKey(AuctionHouse.getInstance(), "AuctionMarkedChest");
		if (chest.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
			event.setUseInteractedBlock(Event.Result.DENY);
			event.setCancelled(true);

			if (mAuction.getInstance().getAuctionBanManager().checkAndHandleBan(player)) return;
			AuctionHouse.getInstance().getGuiManager().showGUI(player, new GUIAuctionHouse(mAuction.getInstance().getAuctionPlayerManager().getPlayer(player.getUniqueId())));
		}
	}

	@EventHandler
	public void onBundleClick(PlayerInteractEvent event) {
		Player player = e.getPlayer();
		ItemStack item = PlayerHelper.getHeldItem(player);

		if (item == null || (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)) return;
		if (item.getType() == XMaterial.AIR.parseMaterial()) return;
		if (!NBTEditor.contains(heldItem, "AuctionBundleItem")) return;
		event.setCancelled(true);

		List<ItemStack> items = new ArrayList<>();
		for (int i = 0; i < NBTEditor.getInt(heldItem, "AuctionBundleItem"); i++) items.add(mAuction.getInstance().deserializeItem(NBTEditor.getByteArray(heldItem, "AuctionBundleItem-" + i)));

		if (item.getAmount() >= 2) item.setAmount(heldItem.getAmount() - 1);
		else player.getInventory().setItemInMainHand(XMaterial.AIR.parseItem());

		PlayerUtils.giveItem(player, items);
	}

	@EventHandler
	public void onInventoryClick(PrepareAnvilEvent event) {
		ItemStack stack = event.getResult();
		if (stack == null) return;

		stack = NBTEditor.set(stack, "AUCTION_REPAIRED", "AuctionHouseRepaired");
		event.setResult(stack);
	}
}
