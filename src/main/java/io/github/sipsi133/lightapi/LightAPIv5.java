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
package io.github.sipsi133.lightapi;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import ru.beykerykt.minecraft.lightapi.common.IChunkData;
import ru.beykerykt.minecraft.lightapi.common.LightType;

import java.util.List;
import java.util.stream.Collectors;

public class LightAPIv5 implements LightAPI {

    private LightAPIv5() {
    }

    public static LightAPI createInstance() {
        try {
            Class.forName("ru.beykerykt.minecraft.lightapi.common.LightAPI");
        } catch (ClassNotFoundException e) {
            return null;
        }
        return new LightAPIv5();
    }

    @Override
    public String implementationVersion() {
        return JavaPlugin.getProvidingPlugin(ru.beykerykt.minecraft.lightapi.common.LightAPI.class)
                .getDescription().getVersion();
    }

    @Override
    public boolean createLight(Location location, int lightLevel) {
        try {
            return ru.beykerykt.minecraft.lightapi.common.LightAPI.createLight(
                    getWorldNotNull(location).getName(),
                    LightType.BLOCK,
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ(),
                    lightLevel);
        } catch (NullPointerException ignore) {
            return false;
        }
    }

    @Override
    public boolean deleteLight(Location location) {
        return ru.beykerykt.minecraft.lightapi.common.LightAPI.deleteLight(
                getWorldNotNull(location).getName(),
                LightType.BLOCK,
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }

    @Override
    public List<ChunkInfo> collectChunks(Location location, int lightLevel) {
        return ru.beykerykt.minecraft.lightapi.common.LightAPI.collectChunks(
                getWorldNotNull(location).getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                lightLevel
        ).stream().map(ChunkDataWrapper::new).collect(Collectors.toList());
    }

    @Override
    public void sendChunk(ChunkInfo chunk) {
        if (chunk instanceof ChunkDataWrapper) {
            ru.beykerykt.minecraft.lightapi.common.LightAPI.sendChanges(((ChunkDataWrapper) chunk).chunk);
        }
    }

    private World getWorldNotNull(Location location) {
        World world = location.getWorld();
        if (world == null) throw new IllegalArgumentException("Location has no world");
        return world;
    }

    private class ChunkDataWrapper implements ChunkInfo {

        final IChunkData chunk;

        ChunkDataWrapper(IChunkData chunk) {
            this.chunk = chunk;
        }
    }
}
