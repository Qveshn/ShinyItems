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
package io.github.sipsi133.engine;

import io.github.sipsi133.utils.NmsHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * WaterHeightMethod is an enumeration of possible algorithms for calculating the height of water.
 * Helps determine the height below which Minecraft begins an underwater rendering style.
 * <p>
 * There are 8 levels that water can have. Values are from 0 (max) to 7 (min).
 * Minecraft renders water as block with 9 layers (1/9 block height per layer).
 * The first layer (height = 1.00000) is always air until there is no water in the block above.
 * The second layer (height = 0.88889) corresponds to level 0.
 * The third layer (height = 0.77778) corresponds to level 1 etc.
 * The ninth layer (height = 0.11111) corresponds to level 7.
 * <p>
 * Minecraft 1.14 and above switches the rendering to an underwater style at the height of the water layer.
 * This is WaterHeightMethod.MODERN enumeration value. Heights values:
 * 1.00000 - there is also water at one block upper
 * 0.88889 - water level = 0
 * 0.77778 - water level = 1
 * 0.66667 - water level = 2
 * 0.55556 - water level = 3
 * 0.44444 - water level = 4
 * 0.33333 - water level = 5
 * 0.22222 - water level = 6
 * 0.11111 - water level = 7
 * 0.00000 - there is no water in this block
 * <p>
 * Minecraft below 1.14 switches the rendering to an underwater style at the height of one layer above (+0,11111).
 * This is WaterHeightMethod.OLD enumeration value. Heights values:
 * 1.00000 - water level = 0
 * 0.88889 - water level = 1
 * 0.77778 - water level = 2
 * 0.66667 - water level = 3
 * 0.55556 - water level = 4
 * 0.44444 - water level = 5
 * 0.33333 - water level = 6
 * 0.22222 - water level = 7
 * 0.00000 - there is no water in this block
 * <p>
 * WaterHeightMethod.OLD enumeration value indicates the use MODERN or OLD according to the Minecraft version.
 */
public enum WaterHeightMethod {
    /**
     * Uses MODERN or OLD according to the Minecraft version.
     */
    AUTO,

    /**
     * Uses the algorithm of Minecraft below 1.14.
     */
    OLD(false),

    /**
     * Uses the algorithm of Minecraft 1.14 and above (more accurately).
     */
    MODERN(true);

    private final boolean useModernMethod;

    WaterHeightMethod() {
        this(NmsHelper.nmsCompareTo("v1_14_R1") >= 0);
    }

    WaterHeightMethod(boolean useModernMethod) {
        this.useModernMethod = useModernMethod;
    }

    // Materials that always contain water
    private static Set<Material> waterMaterials = Stream.of(
            "WATER",
            "STATIONARY_WATER",
            "KELP",
            "KELP_PLANT",
            "SEAGRASS",
            "TALL_SEAGRASS",
            "BUBBLE_COLUMN"
    ).map(NmsHelper::matchMaterial).filter(Objects::nonNull).collect(Collectors.toSet());

    /**
     * Checks if it is a water-material (a material that always contains water).
     *
     * @param material The material to check.
     * @return True if the material always contains water, otherwise false.
     */
    public static boolean isWaterBlock(Material material) {
        return waterMaterials.contains(material);
    }

    /**
     * Checks if it is a water-block (a block in which there is always water).
     *
     * @param block The block to check.
     * @return True if there is always water in this block, otherwise false.
     */
    public static boolean isWaterBlock(Block block) {
        return isWaterBlock(block.getType());
    }

    /**
     * Checks if the block contains water.
     *
     * @param block The block to check.
     * @return True if the block contains water, otherwise false.
     */
    public static boolean hasWater(Block block) {
        return isWaterBlock(block.getType()) || NmsHelper.isWaterlogged(block);
    }

    /**
     * Returns the height of water in the block from 0 to 1.
     * This method uses the algorithm of Minecraft 1.14 and higher.
     *
     * @param block The block to find water height.
     * @return The height of water in this block from 0 to 1.
     */
    private static double getWaterHeightModernMethod(Block block) {
        if (isWaterBlock(block)) {
            int waterLevel = NmsHelper.getBlockLevel(block);
            return waterLevel > 7 ? 1 : waterLevel > 0 ? (8 - waterLevel) / 9d
                    : hasWater(block.getLocation().add(0, 1, 0).getBlock()) ? 1 : 8 / 9d;
        }
        if (NmsHelper.isWaterlogged(block)) return hasWater(block.getLocation().add(0, 1, 0).getBlock()) ? 1 : 8 / 9d;
        return 0;
    }

    /**
     * Returns the height of water in the block from 0 to 1.
     * This method uses the algorithm of Minecraft below 1.14.
     *
     * @param block The block to find water height.
     * @return The height of water in this block from 0 to 1.
     */
    private static double getWaterHeightOldMethod(Block block) {
        // eight level water
        if (isWaterBlock(block)) {
            int waterLevel = NmsHelper.getBlockLevel(block);
            return waterLevel > 7 ? 1 : (9 - waterLevel) / 9d;
        }
        if (NmsHelper.isWaterlogged(block)) return 1;
        return 0;
    }

    /**
     * Checks id the location is under water.
     *
     * @param location The location to check.
     * @return True if the location is under water, otherwise false.
     */
    public boolean isInWater(Location location) {
        double waterHeight = useModernMethod
                ? getWaterHeightModernMethod(location.getBlock())
                : getWaterHeightOldMethod(location.getBlock());
        if (waterHeight == 0) {
            return false;
        } else {
            double y = location.getY();
            return y - Math.floor(y) < waterHeight;
        }
    }
}
