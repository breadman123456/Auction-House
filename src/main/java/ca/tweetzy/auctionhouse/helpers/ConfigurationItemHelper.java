package ca.tweetzy.auctionhouse.helpers;

import ca.tweetzy.core.compatibility.XMaterial;
import ca.tweetzy.core.utils.TextUtils;
import ca.tweetzy.core.utils.nms.NBTEditor;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The current file has been created by Kiran Hart
 * Date Created: February 10 2021
 * Time Created: 1:03 p.m.
 * Usage of any code found within this class is prohibited unless given explicit permission otherwise
 */
public class ConfigurationItemHelper {

	public static ItemStack createConfigurationItem(ItemStack stack, String title, List<String> lore, HashMap<String, Object> replacements, String... nbtData) {
		ItemMeta meta = stack.getItemMeta();
		assert meta != null;
		meta.setDisplayName(TextUtils.formatText(title));

		if (replacements != null) {
			for (String key : replacements.keySet()) if (title.contains(key)) title = title.replace(key, String.valueOf(replacements.get(key)));
			for (int i = 0; i < lore.size(); i++) {
				for (String key : replacements.keySet()) {
					if (lore.get(i).contains(key)) lore.set(i, lore.get(i).replace(key, String.valueOf(replacements.get(key))));
				}
			}
		}

		meta.setDisplayName(TextUtils.formatText(title));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS);
		meta.setLore(lore.stream().map(TextUtils::formatText).collect(Collectors.toList()));
		stack.setItemMeta(meta);
		if (nbtData != null) for (String nbt : nbtData) stack = NBTEditor.set(stack, nbt.split(";")[1], nbt.split(";")[0]);
		return stack;
	}

	public static ItemStack createConfigurationItem(String item, String title, List<String> lore, HashMap<String, Object> replacements) {
		return createConfigurationItem(Objects.requireNonNull(XMaterial.matchXMaterial(item).get().parseItem()), title, lore, replacements);
	}
}
