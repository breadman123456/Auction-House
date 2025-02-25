package ca.tweetzy.auctionhouse.guis.filter;

import ca.tweetzy.auctionhouse.auction.enums.AuctionItemCategory;
import ca.tweetzy.auctionhouse.guis.AbstractPlaceholderGui;
import ca.tweetzy.auctionhouse.helpers.ConfigurationItemHelper;
import ca.tweetzy.auctionhouse.settings.Settings;
import ca.tweetzy.core.utils.TextUtils;
import org.bukkit.entity.Player;

/**
 * The current file has been created by Kiran Hart
 * Date Created: June 22 2021
 * Time Created: 3:14 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class GUIFilterWhitelist extends AbstractPlaceholderGui {

	public GUIFilterWhitelist(Player player) {
		super(player);
		setTitle(TextUtils.formatText(Settings.GUI_FILTER_WHITELIST_TITLE.getString()));
		setRows(6);
		setAcceptsItems(false);
		setDefaultItem(Settings.GUI_FILTER_WHITELIST_BG_ITEM.getMaterial().parseItem());
		setUseLockedCells(true);
		draw();
	}

	private void draw() {
		setButton(2, 1, ConfigurationItemHelper.createConfigurationItem(Settings.GUI_FILTER_WHITELIST_ITEMS_BLOCKS_ITEM.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_BLOCKS_NAME.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_BLOCKS_LORE.getStringList(), null), e -> {
			e.manager.showGUI(e.player, new GUIFilterWhitelistList(e.player, AuctionItemCategory.BLOCKS));
		});

		setButton(2, 3, ConfigurationItemHelper.createConfigurationItem(Settings.GUI_FILTER_WHITELIST_ITEMS_FOOD_ITEM.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_FOOD_NAME.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_FOOD_LORE.getStringList(), null), e -> {
			e.manager.showGUI(e.player, new GUIFilterWhitelistList(e.player, AuctionItemCategory.FOOD));
		});

		setButton(2, 5, ConfigurationItemHelper.createConfigurationItem(Settings.GUI_FILTER_WHITELIST_ITEMS_ARMOR_ITEM.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_ARMOR_NAME.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_ARMOR_LORE.getStringList(), null), e -> {
			e.manager.showGUI(e.player, new GUIFilterWhitelistList(e.player, AuctionItemCategory.ARMOR));
		});

		setButton(2, 7, ConfigurationItemHelper.createConfigurationItem(Settings.GUI_FILTER_WHITELIST_ITEMS_TOOLS_ITEM.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_TOOLS_NAME.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_TOOLS_LORE.getStringList(), null), e -> {
			e.manager.showGUI(e.player, new GUIFilterWhitelistList(e.player, AuctionItemCategory.TOOLS));
		});

		// 2ND ROW STARTS

		setButton(3, 1, ConfigurationItemHelper.createConfigurationItem(Settings.GUI_FILTER_WHITELIST_ITEMS_SPAWNERS_ITEM.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_SPAWNERS_NAME.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_SPAWNERS_LORE.getStringList(), null), e -> {
			e.manager.showGUI(e.player, new GUIFilterWhitelistList(e.player, AuctionItemCategory.SPAWNERS));
		});

		setButton(3, 3, ConfigurationItemHelper.createConfigurationItem(Settings.GUI_FILTER_WHITELIST_ITEMS_ENCHANTS_ITEM.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_ENCHANTS_NAME.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_ENCHANTS_LORE.getStringList(), null), e -> {
			e.manager.showGUI(e.player, new GUIFilterWhitelistList(e.player, AuctionItemCategory.ENCHANTS));
		});

		setButton(3, 5, ConfigurationItemHelper.createConfigurationItem(Settings.GUI_FILTER_WHITELIST_ITEMS_WEAPONS_ITEM.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_WEAPONS_NAME.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_WEAPONS_LORE.getStringList(), null), e -> {
			e.manager.showGUI(e.player, new GUIFilterWhitelistList(e.player, AuctionItemCategory.WEAPONS));
		});

		setButton(3, 7, ConfigurationItemHelper.createConfigurationItem(Settings.GUI_FILTER_WHITELIST_ITEMS_MISC_ITEM.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_MISC_NAME.getString(), Settings.GUI_FILTER_WHITELIST_ITEMS_MISC_LORE.getStringList(), null), e -> {
			e.manager.showGUI(e.player, new GUIFilterWhitelistList(e.player, AuctionItemCategory.MISC));
		});
	}
}
