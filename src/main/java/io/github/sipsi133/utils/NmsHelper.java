/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Qveshn
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
package io.github.sipsi133.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class NmsHelper {

    private static Function<String, Material> matchLegacyMaterial = (materialName) -> {
        // Material.matchMaterial(materialName, true)
        try {
            Material.class.getMethod("matchMaterial", String.class, boolean.class);
            return (matchLegacyMaterial = (x) -> Material.matchMaterial(x, true)).apply(materialName);
        } catch (NoSuchMethodException ignore) {
        }
        // null
        return (matchLegacyMaterial = (x) -> null).apply(materialName);
    };

    public static Material matchLegacyMaterial(String materialName) {
        return matchLegacyMaterial.apply(materialName);
    }

    public static Material matchMaterial(String... materialNames) {
        for (String materialName : materialNames) {
            Material material = Material.matchMaterial(materialName);
            if (material != null) {
                return material;
            }
        }
        for (String materialName : materialNames) {
            Material material = matchLegacyMaterial(materialName);
            if (material != null && material != Material.AIR) {
                return material;
            }
        }
        return null;
    }

    private static Function<PlayerInventory, ItemStack> getItemInPlayerMainHand = (inventory) -> {
        // inventory.getItemInMainHand()
        try {
            PlayerInventory.class.getMethod("getItemInMainHand");
            return (getItemInPlayerMainHand = PlayerInventory::getItemInMainHand).apply(inventory);
        } catch (NoSuchMethodException ignore) {
        }
        // inventory.getItemInHand()
        return (getItemInPlayerMainHand = PlayerInventory::getItemInHand).apply(inventory);
    };

    public static ItemStack getItemInPlayerMainHand(PlayerInventory inventory) {
        ItemStack itemStack = getItemInPlayerMainHand.apply(inventory);
        return itemStack != null ? itemStack : new ItemStack(Material.AIR);
    }

    private static Function<PlayerInventory, ItemStack> getItemInPlayerOffHand = (inventory) -> {
        // inventory.getItemInOffHand()
        try {
            PlayerInventory.class.getMethod("getItemInOffHand");
            return (getItemInPlayerOffHand = PlayerInventory::getItemInOffHand).apply(inventory);
        } catch (NoSuchMethodException ignore) {
        }
        // null
        return (getItemInPlayerOffHand = (x) -> null).apply(inventory);
    };

    public static ItemStack getItemInPlayerOffHand(PlayerInventory inventory) {
        ItemStack itemStack = getItemInPlayerOffHand.apply(inventory);
        return itemStack != null ? itemStack : new ItemStack(Material.AIR);
    }

    private static String nmsPrefix;

    private static String nmsPrefix() {
        if (nmsPrefix == null) {
            nmsPrefix = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        }
        return nmsPrefix;
    }

    private static String nmsPaddedPrefix;

    private static String nmsPaddedPrefix(String prefix) {
        return Utils.leftPad(prefix, "\\d+", '0', 8);
    }

    private static String nmsPaddedPrefix() {
        if (nmsPaddedPrefix == null) {
            nmsPaddedPrefix = nmsPaddedPrefix(nmsPrefix());
        }
        return nmsPaddedPrefix;
    }

    public static int nmsCompareTo(String nmsVersion) {
        return nmsPaddedPrefix().compareTo(nmsPaddedPrefix(nmsVersion));
    }

    private static Class<?> classCraftItemStack;
    private static Method methodAsNMSCopy;
    private static Method methodGetTag;
    private static Method methodGetTagBoolean;

    private static Method methodSpigot;
    private static Method methodSpigotIsUnbreakable;

    @SuppressWarnings({"JavaReflectionMemberAccess", "ConstantConditions"})
    private static Function<ItemStack, Boolean> isItemUnbreakable = (itemStack) -> {
        // itemStack.getItemMeta().isUnbreakable()
        try {
            ItemMeta.class.getMethod("isUnbreakable");
            Debug.print("Handler isItemUnbreakable = %s", "itemStack.getItemMeta().isUnbreakable()");
            return (isItemUnbreakable = (x) -> x.getItemMeta().isUnbreakable()).apply(itemStack);
        } catch (NoSuchMethodException ignore) {
        }
        // itemStack.getItemMeta().spigot().isUnbreakable
        try {
            methodSpigot = ItemMeta.class.getMethod("spigot");
            Class<?> classItemMetaSpigot = Class.forName("org.bukkit.inventory.meta.ItemMeta.Spigot");
            methodSpigotIsUnbreakable = classItemMetaSpigot.getMethod("isUnbreakable");
            Debug.print("Handler isItemUnbreakable = %s", "itemStack.getItemMeta().spigot().isUnbreakable");
            return (isItemUnbreakable = (x) -> {
                try {
                    Object spigot = methodSpigot.invoke(ItemMeta.class, x.getItemMeta());
                    return (boolean) methodSpigotIsUnbreakable.invoke(spigot);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }).apply(itemStack);
        } catch (ClassNotFoundException | NoSuchMethodException ignore) {
        }
        // CraftItemStack.asNMSCopy(itemStack).getTag().getBoolean("Unbreakable")
        try {
            classCraftItemStack = Class.forName(
                    String.format("org.bukkit.craftbukkit.%s.inventory.CraftItemStack", nmsPrefix()));
            methodAsNMSCopy = classCraftItemStack.getMethod("asNMSCopy", ItemStack.class);
            Class<?> classItemStack = Class.forName(
                    String.format("net.minecraft.server.%s.ItemStack", nmsPrefix()));
            methodGetTag = classItemStack.getMethod("getTag");
            Class<?> classNBTTagCompound = Class.forName(
                    String.format("net.minecraft.server.%s.NBTTagCompound", nmsPrefix()));
            methodGetTagBoolean = classNBTTagCompound.getMethod("getBoolean", String.class);
            Debug.print("Handler isItemUnbreakable = %s",
                    "CraftItemStack.asNMSCopy(itemStack).getTag().getBoolean(\"Unbreakable\")");
            return (isItemUnbreakable = (x) -> {
                try {
                    Object stack = methodAsNMSCopy.invoke(classCraftItemStack, x);
                    Object tag = methodGetTag.invoke(stack);
                    return tag != null && (boolean) methodGetTagBoolean.invoke(tag, "Unbreakable");
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }).apply(itemStack);
        } catch (ClassNotFoundException | NoSuchMethodException ignore) {
        }
        // false
        Debug.print("Handler isItemUnbreakable = %s", "false");
        return (isItemUnbreakable = (x) -> false).apply(itemStack);
    };

    public static boolean isItemUnbreakable(ItemStack itemStack) {
        return itemStack.hasItemMeta() ? isItemUnbreakable.apply(itemStack) : false;
    }

    private static Function<Block, Boolean> isWaterlogged = (block) -> {
        // ((Waterlogged) blockData).isWaterlogged()
        try {
            Class.forName("org.bukkit.block.data.Waterlogged");
            return (isWaterlogged = (x) -> {
                BlockData blockData = x.getBlockData();
                return blockData instanceof Waterlogged && ((Waterlogged) blockData).isWaterlogged();
            }).apply(block);
        } catch (ClassNotFoundException ignore) {
        }
        // false
        return (isWaterlogged = (x) -> false).apply(block);
    };

    public static boolean isWaterlogged(Block block) {
        return isWaterlogged.apply(block);
    }

    private static Function<Block, Integer> getBlockLevel = (block) -> {
        // ((Levelled) blockData).getLevel()
        try {
            Class.forName("org.bukkit.block.data.Levelled");
            return (getBlockLevel = (x) -> {
                BlockData blockData = x.getBlockData();
                return blockData instanceof Levelled ? ((Levelled) blockData).getLevel() : 0;
            }).apply(block);
        } catch (ClassNotFoundException ignore) {
        }
        // block.getData()
        return (getBlockLevel = (x) -> (int) x.getData()).apply(block);
    };

    public static int getBlockLevel(Block block) {
        return getBlockLevel.apply(block);
    }

    private static Function<Double, Boolean> isValidY = (Y) -> (isValidY = nmsCompareTo("v1_14_R1") < 0
            ? (y) -> y >= 0 && y < 256
            : (y) -> y >= 0 - 16 && y < 256 + 16).apply(Y);

    @SuppressWarnings("WeakerAccess")
    public static boolean isValidY(double y) {
        return isValidY.apply(y);
    }
}
