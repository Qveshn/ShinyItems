/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 sipsi133
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
import io.github.sipsi133.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.enchantments.Enchantment;

import java.util.*;
import java.util.stream.Collectors;

@SerializableAs("-=[ShinyItem]=-")
public class ShinyItem implements ConfigurationSerializable {

    private final static String ANY_VALUE = "Any";

    private String originalMaterialName;
    private final Material item;
    private final Interval<Integer> amount;
    private final Interval<Integer> durability;
    private final Boolean unbreakable;
    private final List<Enchantment> enchantments;
    private final Interval<Integer> lightLevel;

    @SuppressWarnings("unused")
    public ShinyItem(
            Material item,
            Interval<Integer> lightLevel
    ) {
        this(item, lightLevel, null);
    }

    @SuppressWarnings("WeakerAccess")
    public ShinyItem(
            Material item,
            Interval<Integer> lightLevel,
            Interval<Integer> amount
    ) {
        this(item, lightLevel, amount, null);
    }

    @SuppressWarnings("WeakerAccess")
    public ShinyItem(
            Material item,
            Interval<Integer> lightLevel,
            Interval<Integer> amount,
            Interval<Integer> durability
    ) {
        this(item, lightLevel, amount, durability, null);
    }

    @SuppressWarnings("WeakerAccess")
    public ShinyItem(
            Material item,
            Interval<Integer> lightLevel,
            Interval<Integer> amount,
            Interval<Integer> durability,
            Boolean unbreakable
    ) {
        this(item, lightLevel, amount, durability, unbreakable, null);
    }

    @SuppressWarnings("WeakerAccess")
    public ShinyItem(
            Material item,
            Interval<Integer> lightLevel,
            Interval<Integer> amount,
            Interval<Integer> durability,
            Boolean unbreakable,
            List<Enchantment> enchantments
    ) {
        this.item = item;
        originalMaterialName = item.toString();
        this.lightLevel = lightLevel == null ? new Interval<>(14, 14) : lightLevel;
        this.amount = amount == null || amount.max() < 0 ? null : amount.getSorted();
        this.durability = durability == null || durability.max() < 0 ? null : durability.getSorted();
        this.unbreakable = unbreakable;
        this.enchantments = enchantments;
    }

    // Constructor to create ShinyItem during deserialization
    private ShinyItem(
            String materialNames,
            Interval<Integer> lightLevel,
            Interval<Integer> amount,
            Interval<Integer> durability,
            Boolean unbreakable,
            List<Enchantment> enchantments
    ) {
        Material material = null;
        String materialName = null;
        for (String name : materialNames.split(",")) {
            materialName = name.trim();
            material = Material.matchMaterial(materialName);
            if (material != null) {
                break;
            }
        }
        if (material == null) {
            for (String name : materialNames.split(",")) {
                materialName = name.trim();
                material = Utils.nullIf(NmsHelper.matchLegacyMaterial(materialName), Material.AIR);
                if (material != null) {
                    break;
                }
            }
        }
        this.item = material;
        this.originalMaterialName = material != null ? materialName : materialNames;
        this.lightLevel = lightLevel == null ? new Interval<>(14, 14) : lightLevel;
        this.amount = amount == null || amount.max() < 0 ? null : amount.getSorted();
        this.durability = durability == null || durability.max() < 0 ? null : durability.getSorted();
        this.unbreakable = unbreakable;
        this.enchantments = enchantments;
    }

    public Material getMaterial() {
        return item;
    }

    public String getOriginalMaterialName() {
        return originalMaterialName;
    }

    @Deprecated
    public int getLightLevel() {
        return lightLevel.first;
    }

    @SuppressWarnings("WeakerAccess")
    public int getAirLightLevel() {
        return lightLevel.first;
    }

    @SuppressWarnings("WeakerAccess")
    public int getWaterLightLevel() {
        return lightLevel.second;
    }

    @SuppressWarnings("WeakerAccess")
    public Interval<Integer> getDurability() {
        return durability;
    }

    @SuppressWarnings("WeakerAccess")
    public Interval<Integer> getAmount() {
        return amount;
    }

    @SuppressWarnings("WeakerAccess")
    public Boolean getUnbreakable() {
        return unbreakable;
    }

    @SuppressWarnings("WeakerAccess")
    public List<Enchantment> getEnchantments() {
        return enchantments;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> temp = new LinkedHashMap<>();
        temp.put("item", item.toString());
        temp.put("lightlevel", serialize(lightLevel));
        temp.put("amount", serialize(amount));
        temp.put("durability", serialize(durability));
        temp.put("unbreakable", serialize(unbreakable));
        temp.put("enchantments", serialize(enchantments));
        return temp;
    }

    private Object serialize(Interval<Integer> value) {
        return value == null ? ANY_VALUE : value.first.equals(value.second) ? value.first
                : String.format("%d, %d", value.first, value.second);
    }

    private Object serialize(Boolean value) {
        return value == null ? ANY_VALUE : value;
    }

    private Object serialize(Collection<Enchantment> value) {
        return value == null ? ANY_VALUE : value.stream().map(Enchantment::getName).collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    public static ShinyItem deserialize(Map<String, Object> map) {
        String materialName = (String) map.get("item");
        Interval<Integer> lightlevel = parseInterval(map.get("lightlevel"), new Interval<>(0));
        Interval<Integer> amount = parseInterval(map.get("amount"));
        Interval<Integer> durability = parseInterval(map.get("durability"));
        Boolean unbreakable = parseBoolean(map.get("unbreakable"));
        List<Enchantment> enchantments = parseEnchantments(map.get("enchantments"));
        return new ShinyItem(materialName, lightlevel, amount, durability, unbreakable, enchantments);
    }

    @SuppressWarnings("unused") // for future
    private static int[] parseIntegers(Object value, int defaultValue) {
        if (value == null || (value instanceof String && ANY_VALUE.equalsIgnoreCase((String) value))) {
            return new int[]{defaultValue};
        }
        if (value instanceof Number) {
            return new int[]{(int) value};
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).stream().mapToInt(x -> Integer.parseInt(x.toString().trim())).toArray();
        }
        return Arrays.stream(value.toString().split(",")).mapToInt(x -> Integer.parseInt(x.trim())).toArray();
    }

    private static Interval<Integer> parseInterval(Object value) {
        return parseInterval(value, null);
    }

    private static Interval<Integer> parseInterval(Object value, Interval<Integer> defaultValue) {
        if (value == null || (value instanceof String && ANY_VALUE.equalsIgnoreCase((String) value))) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return new Interval<>((int) value, (int) value);
        }
        int[] values;
        if (value instanceof Collection) {
            values = ((Collection<?>) value).stream()
                    .mapToInt(x -> Integer.parseInt(x.toString().trim()))
                    .toArray();
        } else {
            values = Arrays.stream(value.toString().split(",")).mapToInt(x -> Integer.parseInt(x.trim())).toArray();
        }
        return new Interval<>(values[0], values.length > 1 ? values[1] : values[0]);
    }

    private static Boolean parseBoolean(Object value) {
        return parseBoolean(value, null);
    }

    @SuppressWarnings("SameParameterValue")
    private static Boolean parseBoolean(Object value, Boolean defaultValue) {
        if (value == null || (value instanceof String && ANY_VALUE.equalsIgnoreCase((String) value))) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString().trim());
    }

    private static List<Enchantment> parseEnchantments(Object value) {
        return parseEnchantments(value, null);
    }

    @SuppressWarnings({"unchecked", "SameParameterValue"})
    private static List<Enchantment> parseEnchantments(Object value, List<Enchantment> defaultValue) {
        if (value == null || (value instanceof String && ANY_VALUE.equalsIgnoreCase((String) value))
                || !(value instanceof List)
        ) {
            return defaultValue;
        }
        List<Enchantment> enchantments = new ArrayList<>();
        for (String enchantment : (List<String>) value) {
            enchantments.add(Enchantment.getByName(enchantment));
        }
        return enchantments;
    }
}
