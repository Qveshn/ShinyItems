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

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

class LightLocation {

    private Location location;
    private Map<Player, Integer> playersLightLevels = new HashMap<>();
    private int previousLightLevel = 0;
    private int lightLevel = 0;
    private boolean isValid = true;
    private boolean isSuccessCreated = true;

    LightLocation(Location location) {
        this.location = location;
    }

    Location location() {
        return location;
    }

    int lightLevel() {
        if (!isValid) {
            lightLevel = playersLightLevels.values().stream().max(Comparator.naturalOrder()).orElse(0);
            isValid = true;
        }
        return lightLevel;
    }

    boolean isLightLevelChanged() {
        return previousLightLevel != lightLevel();
    }

    void markLightLevelUnchanged() {
        previousLightLevel = lightLevel();
    }

    void markLightLevelChanged() {
        previousLightLevel = -1;
    }

    void setCreatedStatus(boolean success) {
        isSuccessCreated = success;
    }

    boolean isSuccessCreated() {
        return isSuccessCreated;
    }

    void update(Player player, int lightLevel) {
        int oldLightLevel = playersLightLevels.getOrDefault(player, 0);
        if (oldLightLevel != lightLevel) {
            if (lightLevel > 0) {
                playersLightLevels.put(player, lightLevel);
            } else {
                playersLightLevels.remove(player);
            }
            isValid = false;
        }
    }
}
