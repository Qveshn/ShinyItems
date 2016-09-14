/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 sipsi133
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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class ShinyItem implements ConfigurationSerializable {

    private Material item;
    private int durability;
    private boolean unbreakable;
    private int lightlevel;

    public ShinyItem(Material item, int lightlevel, int durability, boolean unbreakable) {
        this.item = item;
        this.lightlevel = lightlevel;
        this.durability = durability;
        this.unbreakable = unbreakable;
    }

    public Material getMaterial() {
        return item;
    }

    public int getLightLevel() {
        return lightlevel;
    }

    public int getDurability() {
        return durability;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> temp = new HashMap<>();
        temp.put("item", item.toString());
        temp.put("durability", durability);
        temp.put("unbreakable", unbreakable);
        temp.put("lightlevel", lightlevel);
        return temp;
    }

    public static ShinyItem deserialize(Map<String, Object> map) {
        return new ShinyItem(
                Material.valueOf((String) map.get("item")),
                (Integer) map.get("lightlevel"),
                (Integer) map.get("durability"),
                (Boolean) map.get("unbreakable"));
    }
}
