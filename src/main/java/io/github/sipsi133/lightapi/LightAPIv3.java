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

import io.github.sipsi133.utils.Debug;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.stream.Collectors;

public class LightAPIv3 implements LightAPI {

    private boolean useCollectLightLevel = false;

    private LightAPIv3() {
        try {
            ru.beykerykt.lightapi.LightAPI.class.getMethod("collectChunks", Location.class, int.class);
            useCollectLightLevel = true;
        } catch (NoSuchMethodException ignore) {
        }
        Debug.print("Сollecting chunks using light level %s.", useCollectLightLevel ? "available" : "disable");
    }

    public static LightAPI createInstance() {
        try {
            Class.forName("ru.beykerykt.lightapi.LightAPI");
        } catch (ClassNotFoundException e) {
            return null;
        }
        return new LightAPIv3();
    }

    @Override
    public String implementationVersion() {
        return JavaPlugin.getProvidingPlugin(ru.beykerykt.lightapi.LightAPI.class)
                .getDescription().getVersion();
    }

    @Override
    public boolean createLight(Location location, int lightLevel) {
        return ru.beykerykt.lightapi.LightAPI.createLight(location, lightLevel, false);
    }

    @Override
    public boolean deleteLight(Location location) {
        return ru.beykerykt.lightapi.LightAPI.deleteLight(location, false);
    }

    @Override
    public List<ChunkInfo> collectChunks(Location location, int lightLevel) {
        return (useCollectLightLevel
                ? ru.beykerykt.lightapi.LightAPI.collectChunks(location, lightLevel)
                : ru.beykerykt.lightapi.LightAPI.collectChunks(location)
        ).stream().map(ChunkSectionInfo::new).collect(Collectors.toList());
    }

    @Override
    public void sendChunk(ChunkInfo chunk) {
        if (chunk instanceof ChunkSectionInfo) {
            ru.beykerykt.lightapi.LightAPI.updateChunk(((ChunkSectionInfo) chunk).chunk);
        }
    }

    private class ChunkSectionInfo implements ChunkInfo {

        final ru.beykerykt.lightapi.chunks.ChunkInfo chunk;
        final int y;

        ChunkSectionInfo(ru.beykerykt.lightapi.chunks.ChunkInfo chunk) {
            this.chunk = chunk;
            y = chunk.getChunkYHeight() & (~15);
        }


        @Override
        public int hashCode() {
            return 31 * super.hashCode() + y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (!(obj instanceof ChunkSectionInfo)) return false;
            ChunkSectionInfo other = (ChunkSectionInfo) obj;
            if (chunk == null) return other.chunk == null;
            return chunk.equals(other.chunk) && y == other.y;
        }
    }
}
