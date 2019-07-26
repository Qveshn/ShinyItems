/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Qveshn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.sipsi133.engine;

import io.github.sipsi133.utils.NmsHelper;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShinyItemSelector {

    private final Map<Material, List<ShinyItem>> mapToItem = new HashMap<>();
    private final boolean permsEnabled;
    private final boolean itemPermsEnabled;

    public ShinyItemSelector(List<ShinyItem> items, boolean permsEnabled, boolean itemPermsEnabled) {
        for (ShinyItem item : items) {
            List<ShinyItem> materialItems = mapToItem.computeIfAbsent(item.getMaterial(), k -> new ArrayList<>());
            materialItems.add(item);
        }
        this.permsEnabled = permsEnabled;
        this.itemPermsEnabled = itemPermsEnabled;
    }

    @SuppressWarnings("WeakerAccess")
    public int getLightLevel(Player player) {
        PlayerInventory inventory = player.getInventory();
        int level = 0;
        Boolean isLiquid = null;

        ShinyItem item = getLightlevelForPlayer(player, NmsHelper.getItemInPlayerMainHand(inventory));
        if (item != null) {
            int air = item.getAirLightLevel();
            int water = item.getWaterLightLevel();
            level = air == water || !(isLiquid = isInWater(player)) ? air : water;
        }

        if (level < 15) {
            item = getLightlevelForPlayer(player, NmsHelper.getItemInPlayerOffHand(inventory));
            if (item != null) {
                int air = item.getAirLightLevel();
                int water = item.getWaterLightLevel();
                if (level < air && level < water) {
                    level = air == water || ((isLiquid == null || !isLiquid) && !isInWater(player)) ? air : water;
                }
            }
        }
        return Math.min(level, 15);
    }

    private boolean isInWater(Player player) {
        return player.getEyeLocation().getBlock().isLiquid();
    }

    private ShinyItem getLightlevelForPlayer(Player player, ItemStack itemStack) {
        if (itemStack != null) {
            if (!permsEnabled || player.hasPermission("shinyitems.use")) {
                if (!itemPermsEnabled
                        || player.hasPermission("shinyitems.use." + itemStack.getType().toString().toLowerCase())
                ) {
                    return get(itemStack);
                }
            }
        }
        return null;
    }

    private ShinyItem get(ItemStack itemStack) {
        List<ShinyItem> materialItems = mapToItem.get(itemStack.getType());
        if (materialItems != null) {
            for (ShinyItem item : materialItems) {

                Interval<Integer> amount = item.getAmount();
                if (amount != null && !amount.isInside(itemStack.getAmount())) {
                    continue;
                }

                Interval<Integer> durability = item.getDurability();
                if (durability != null && !durability.isInside((int) itemStack.getDurability())) {
                    continue;
                }

                Boolean unbreakable = item.getUnbreakable();
                if (unbreakable != null && !unbreakable.equals(NmsHelper.isItemUnbreakable(itemStack))) {
                    continue;
                }

                List<Enchantment> enchantments = item.getEnchantments();
                if (enchantments != null) {
                    if (!enchantments.isEmpty()) {
                        boolean isEqual = true;
                        for (Enchantment enchantment : enchantments) {
                            if (!itemStack.containsEnchantment(enchantment)) {
                                isEqual = false;
                                break;
                            }
                        }
                        if (!isEqual) {
                            continue;
                        }
                    } else {
                        if (!itemStack.getEnchantments().isEmpty()) {
                            continue;
                        }
                    }
                }
                return item;
            }
        }
        return null;
    }
}
