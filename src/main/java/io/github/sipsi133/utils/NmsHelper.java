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
package io.github.sipsi133.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
}
