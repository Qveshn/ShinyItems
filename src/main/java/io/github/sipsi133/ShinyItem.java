/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 sipsi133
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
package io.github.sipsi133;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;

public class ShinyItem implements ConfigurationSerializable {

    private Material item;
    private Integer durability;
    private Boolean unbreakable;
    private int lightlevel;
    private List<Enchantment> enchantments = new ArrayList<>();

    public ShinyItem(Material item, int lightlevel, Integer durability, Boolean unbreakable,
                     List<Enchantment> enchantments
    ) {
        this.item = item;
        this.lightlevel = lightlevel;
        this.durability = durability;
        this.unbreakable = unbreakable;
        this.enchantments = enchantments;
    }

    public ShinyItem(Material item, int lightlevel, Integer durability, Boolean unbreakable) {
        this(item, lightlevel, durability, unbreakable, new ArrayList<Enchantment>());
    }

    public ShinyItem(Material item, int lightlevel, Integer durability) {
        this(item, lightlevel, durability, null, new ArrayList<Enchantment>());
    }

    public ShinyItem(Material item, int lightlevel, Integer durability, List<Enchantment> e) {
        this(item, lightlevel, durability, null, e);
    }

    public ShinyItem(Material item, int lightlevel, Boolean unbreakable) {
        this(item, lightlevel, null, unbreakable, new ArrayList<Enchantment>());
    }

    public ShinyItem(Material item, int lightlevel, Boolean unbreakable, List<Enchantment> e) {
        this(item, lightlevel, null, unbreakable, e);
    }

    public Material getMaterial() {
        return item;
    }

    public int getLightLevel() {
        return lightlevel;
    }

    public Integer getDurability() {
        return durability;
    }

    public Boolean isUnbreakable() {
        return unbreakable;
    }

    public List<Enchantment> getEnchantments() {
        return enchantments;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> temp = new HashMap<>();
        temp.put("item", item.toString());
        temp.put("lightlevel", lightlevel);
        temp.put("durability", durability);
        temp.put("unbreakable", unbreakable);
        List<String> tmp = new ArrayList<>();
        for (Enchantment e : enchantments) {
            tmp.add(e.getName());
        }
        temp.put("enchants", tmp);
        return temp;
    }

    @SuppressWarnings("unchecked")
    public static ShinyItem deserialize(Map<String, Object> map) {
        Material mat = map.containsKey("item") ? Material.valueOf((String) map.get("item")) : null;
        int lightlevel = (Integer) map.get("lightlevel");
        Integer durability = map.containsKey("durability")
                ? Integer.valueOf((int) map.get("durability"))
                : -1;
        Boolean unbreakable = map.containsKey("unbreakable")
                ? Boolean.valueOf((boolean) map.get("unbreakable"))
                : false;
        List<Enchantment> enchants = new ArrayList<>();
        if (map.containsKey("enchants")) {
            for (String s : (List<String>) map.get("enchants")) {
                enchants.add(Enchantment.getByName(s));
            }
        }
        return new ShinyItem(mat, lightlevel, durability, unbreakable, enchants);
    }
}
