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

import io.github.sipsi133.ShinyItems;
import io.github.sipsi133.lightapi.ChunkInfo;
import io.github.sipsi133.lightapi.LightAPI;
import io.github.sipsi133.utils.Debug;
import io.github.sipsi133.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class LightEngine {

    private final ShinyItems plugin;

    @SuppressWarnings("FieldCanBeLocal")
    private static boolean VERBOSE = false;

    private Map<Location, LightLocation> lightedLocations = new HashMap<>();
    private Map<Player, Location> playersLastLocations = new HashMap<>();
    private Map<Location, LightLocation> lightedLocationsToApply = new HashMap<>();

    private int taskId = -1;
    private int taskPeriod = 5;
    private Runnable taskTimer = () -> {
        for (Player player : Bukkit.getOnlinePlayers()) {
            update(player);
        }
        markAllChanged();
        flush();
    };

    public LightEngine(ShinyItems plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unused")
    public boolean isStarted() {
        return taskId != -1;
    }

    public void start() {
        if (taskId == -1) {
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, taskTimer, taskPeriod, taskPeriod);
        }
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
            removeAll();
            markAllChanged();
            flush();
            if (lightedLocations.size() != 0) {
                plugin.getLogger().log(Level.SEVERE, String.format(
                        "There are still %d lighted locations in buffer after stopping the engine",
                        lightedLocations.size()));
            }
        }
    }

    @SuppressWarnings("unused") // for future implementation
    public int getTaskPeriod() {
        return taskPeriod;
    }

    public void setTaskPeriod(int ticks) {
        taskPeriod = ticks;
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, taskTimer, taskPeriod, taskPeriod);
        }
    }

    /**
     * Update or delete light level at player location.
     * Old player lighted location will be removed automaticaly.
     * Use flush() to apply changes.
     *
     * @param player      The player.
     * @param deleteLight Force to delete light instead of update.
     */
    private void update(Player player, boolean deleteLight) {
        Location oldLocation = playersLastLocations.get(player);
        Location newLocation = deleteLight ? null : Utils.toValidBlockLocation(player.getEyeLocation());
        int newLightLevel = newLocation == null || !plugin.isToggledOn(player)
                ? 0 : plugin.getItemSelector().getLightLevel(player);

        if (oldLocation != null && !oldLocation.equals(newLocation)) {
            LightLocation lightedLocation = lightedLocations.get(oldLocation);
            if (lightedLocation != null) {
                lightedLocation.update(player, 0);
                lightedLocationsToApply.put(oldLocation, lightedLocation);
            }
        }
        if (newLocation != null) {
            LightLocation lightedLocation = lightedLocations.get(newLocation);
            if (lightedLocation != null || newLightLevel > 0) {
                if (lightedLocation == null) {
                    lightedLocations.put(newLocation, lightedLocation = new LightLocation(newLocation));
                }
                lightedLocation.update(player, newLightLevel);
                lightedLocationsToApply.put(newLocation, lightedLocation);
            }
        }

        if (newLightLevel > 0) {
            playersLastLocations.put(player, newLocation);
        } else {
            playersLastLocations.remove(player);
        }
    }

    /**
     * Update light level at player location.
     * Old player lighted location will be removed automaticaly if needed.
     * Useful on player move event or item changed in hand.
     * Use flush() to apply changes.
     *
     * @param player The player/
     */
    private void update(Player player) {
        update(player, false);
    }

    /**
     * Remove player old lighted location if it exists.
     * Useful on player quit event.
     * Use flush() to apply changes.
     *
     * @param player The player
     */
    public void remove(Player player) {
        update(player, true);
    }

    /**
     * Restore light level at current location if it was deleted due to server block updates.
     * Use flush() to apply changes.
     *
     * @param location The location to check and restore light level if it was deleted.
     */
    @SuppressWarnings("unused") // for future implementation
    private void update(Location location) {
        location = Utils.toValidBlockLocation(location);
        if (location != null) {
            LightLocation lightedLocation = lightedLocations.get(location);
            if (lightedLocation != null) {
                lightedLocationsToApply.put(location, lightedLocation);
            }
        }
    }

    /**
     * Remove all old players lighted locations.
     * Useful on plugin disable event.
     * Use flush() to apply changes.
     */
    private void removeAll() {
        for (Player player : playersLastLocations.keySet().toArray(new Player[0])) {
            remove(player);
        }
    }

    /**
     * Mark all current players locations as changed. This will force ro update lights during flush method.
     * This method should be call if no events are intercepted when Server delete lights during block update.
     * Use flush() to apply changes.
     */
    private void markAllChanged() {
        for (LightLocation lightedLocation : lightedLocations.values()) {
            lightedLocation.markLightLevelChanged();
            lightedLocationsToApply.put(lightedLocation.location(), lightedLocation);
        }
    }

    /**
     * Apply all pending changes to the server and send packets to players.
     */
    private void flush() {
        LightAPI lightAPI = plugin.getLightAPI();
        boolean first = true;
        // update blocks light level and collect affected chunks
        Set<ChunkInfo> chunksToUpdate = new HashSet<>();
        int deletedCount = 0;
        int createdCount = 0;
        for (LightLocation lightedLocation : lightedLocationsToApply.values()) {
            if (lightedLocation.isLightLevelChanged()) {
                int lightLevel = lightedLocation.lightLevel();
                Location location = lightedLocation.location();
                if (VERBOSE && first) Debug.print("", first = false);
                if (VERBOSE)
                    Debug.print("%-15s %-15s %-2d %s", "step 1", locstr(location), lightLevel, lights(location));

                int oldLightLevel = getLightLevel(location);
                if (lightLevel > 0) {
                    if (!lightedLocation.isSuccessCreated()) {
                        if (VERBOSE) Debug.print("skip deleting due previous not success created");
                        continue;
                    }
                    if (lightLevel >= oldLightLevel) {
                        if (VERBOSE) Debug.print("skip deleting due lightLevel >= blockLightLevel (will be created)");
                        continue;
                    }
                }
                deletedCount++;
                lightAPI.deleteLight(location);
                int newLightLevel = getLightLevel(location);
                if (lightLevel == 0) {
                    lightedLocations.remove(lightedLocation.location());
                    lightedLocation.markLightLevelUnchanged();
                }
                chunksToUpdate.addAll(lightAPI.collectChunks(location, oldLightLevel));
                if (VERBOSE && newLightLevel != 0)
                    Debug.print("%-15s %-15s %-2d %s%s", "deleted", locstr(location), lightLevel, lights(location),
                            " !ERROR!");
            }
        }
        for (LightLocation lightedLocation : lightedLocationsToApply.values()) {
            if (lightedLocation.isLightLevelChanged()) {
                lightedLocation.markLightLevelUnchanged();
                int lightLevel = lightedLocation.lightLevel();
                Location location = lightedLocation.location();
                int oldLightLevel = getLightLevel(location);
                if (VERBOSE)
                    Debug.print("%-15s %-15s %-2d %s", "step 2", locstr(location), lightLevel, lights(location));
                if (lightLevel > oldLightLevel) {
                    createdCount++;
                    try {
                        lightAPI.createLight(location, lightLevel);
                    } catch (Exception e) {
                        if (VERBOSE) {
                            Debug.print("Error while creating light: %s", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    int newLightLevel = getLightLevel(location);
                    lightedLocation.setCreatedStatus(newLightLevel == lightLevel);
                    chunksToUpdate.addAll(lightAPI.collectChunks(location, lightLevel));
                    if (VERBOSE && !lightedLocation.isSuccessCreated())
                        Debug.print("%-15s %-15s %-2d %s%s", "created", locstr(location), lightLevel, lights(location),
                                lightedLocation.isSuccessCreated() ? "" : " !NOT SUCCESS!");
                } else {
                    if (VERBOSE) Debug.print("skip creating due lightLevel <= oldLightLevel");
                    lightedLocation.setCreatedStatus(lightLevel == oldLightLevel);
                }
            }
        }
        // Send packets to players.
        for (ChunkInfo chunk : chunksToUpdate) {
            lightAPI.sendChunk(chunk);
        }
        if (Debug.isEnabled() && (chunksToUpdate.size() > 0 || deletedCount > 0 || createdCount > 0)
        ) {
            Debug.print("deleted = %d. created = %d, chunks = %d",
                    deletedCount,
                    createdCount,
                    chunksToUpdate.size());
        }
        lightedLocationsToApply.clear();
    }

    @SuppressWarnings("unused") // for diagnostics
    private boolean hasLighterNeighbour(Location location, int lightLevel) {
        location = location.clone();
        return getLightLevel(location.add(-1, 0, 0)) > lightLevel
                || getLightLevel(location.add(2, 0, 0)) > lightLevel
                || getLightLevel(location.add(-1, -1, 0)) > lightLevel
                || getLightLevel(location.add(0, 2, 0)) > lightLevel
                || getLightLevel(location.add(0, -1, -1)) > lightLevel
                || getLightLevel(location.add(0, 0, 2)) > lightLevel;
    }

    private String locstr(Location location) {
        return String.format("<%d,%d,%d>", location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private String lights(Location location) {
        location = location.clone();
        return String.format("<%-2d -x:%-2d +x:%-2d -y:%-2d +y:%-2d -z:%-2d +z:%-2d>",
                getLightLevel(location),
                getLightLevel(location.add(-1, 0, 0)),
                getLightLevel(location.add(2, 0, 0)),
                getLightLevel(location.add(-1, -1, 0)),
                getLightLevel(location.add(0, 2, 0)),
                getLightLevel(location.add(0, -1, -1)),
                getLightLevel(location.add(0, 0, 2)));
    }

    private static int getLightLevel(Location location) {
        return location.getBlock().getLightFromBlocks();
    }
}
