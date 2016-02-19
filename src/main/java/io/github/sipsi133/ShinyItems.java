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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShinyItems extends JavaPlugin implements Listener {

    private Map<String, Location> lastLoc = new HashMap<String, Location>();
    private List<Material> lightsources = new ArrayList<Material>();
    private Map<Material, Material> lightlevels = new HashMap<Material, Material>();
    public static ShinyItems instance = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        getConfig().addDefault("enable-permissions", false);
        getConfig().addDefault("enable-item-specific-permissions", false);
        getConfig().addDefault("distance-before-new-lightsource", 1);
        getConfig().addDefault("lightsources", Arrays.asList(
                "REDSTONE_TORCH_ON=REDSTONE_TORCH_ON",
                "REDSTONE_TORCH_OFF=REDSTONE_TORCH_ON",
                "GLOWSTONE=TORCH",
                "TORCH=TORCH",
                "LAVA=TORCH",
                "LAVA_BUCKET=TORCH"
        ));
        getConfig().options().copyDefaults(true);
        getConfig().options().copyHeader(false);
        saveConfig();
        reloadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        instance = this;
    }

    public static ShinyItems getInstance() {
        return instance;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo().getBlockX() == e.getFrom().getBlockX()
                && e.getTo().getBlockY() == e.getFrom().getBlockY()
                && e.getTo().getBlockZ() == e.getFrom().getBlockZ()
        ) {
            return;
        }
        if (lastLoc.containsKey(e.getPlayer().getName())) {
            if (getDistanceToPrevious() == -1) {
                lastLoc.get(e.getPlayer().getName()).getBlock().getState().update();
                lastLoc.remove(e.getPlayer().getName());
            } else {
                if (e.getPlayer().getLocation().distance(lastLoc.get(e.getPlayer().getName()))
                        < (double) getDistanceToPrevious()
                ) {
                    return;
                }
                lastLoc.get(e.getPlayer().getName()).getBlock().getState().update();
                lastLoc.remove(e.getPlayer().getName());
            }
        }
        if (e.getPlayer().getInventory().getItemInHand() != null
                && isLightSource(e.getPlayer().getInventory().getItemInHand().getType())
        ) {
            if (e.getTo().getBlock().getLightLevel() > 10) {
                return;
            }
            if (e.getPlayer().getLocation().add(0, -1, 0).getBlock().isLiquid()
                    || e.getPlayer().getLocation().getBlock().isLiquid()
            ) {
                return;
            }
            if (e.getPlayer().hasPermission("shinyitems.use") || !permsEnabled()) {
                if (e.getPlayer().hasPermission("shinyitems.use."
                        + e.getPlayer().getInventory().getItemInHand().getType().name().toLowerCase())
                        || !itemPermsEnabled()
                ) {
                    Location torchLoc = fakeTorchLoc(e.getPlayer(), e.getTo());
                    e.getPlayer().sendBlockChange(
                            torchLoc,
                            getLightlevel(e.getPlayer().getInventory().getItemInHand().getType()),
                            (byte) 0);
                    lastLoc.put(e.getPlayer().getName(), torchLoc);
                }
            }
        }
    }

    @EventHandler
    public void onHeldItem(PlayerItemHeldEvent e) {
        if (lastLoc.containsKey(e.getPlayer().getName())) {
            lastLoc.get(e.getPlayer().getName()).getBlock().getState().update();
            lastLoc.remove(e.getPlayer().getName());
        }
        if (e.getPlayer().getInventory().getItem(e.getNewSlot()) != null
                && isLightSource(e.getPlayer().getInventory().getItem(e.getNewSlot()).getType())
        ) {
            if (e.getPlayer().getLocation().getBlock().getLightLevel() > 10) {
                return;
            }
            if (e.getPlayer().getLocation().add(0, -1, 0).getBlock().isLiquid()
                    || e.getPlayer().getLocation().getBlock().isLiquid()
            ) {
                return;
            }
            if (e.getPlayer().hasPermission("shinyitems.use") || !permsEnabled()) {
                if (e.getPlayer().hasPermission("shinyitems.use."
                        + e.getPlayer().getInventory().getItemInHand().getType().name().toLowerCase())
                        || !itemPermsEnabled()
                ) {
                    Location torchLoc = fakeTorchLoc(e.getPlayer(), e.getPlayer().getLocation());
                    e.getPlayer().sendBlockChange(
                            torchLoc,
                            getLightlevel(e.getPlayer().getInventory().getItemInHand().getType()),
                            (byte) 0);
                    lastLoc.put(e.getPlayer().getName(), torchLoc);
                }
            }
        }
    }

    public boolean isValid(Player p, Location loc) {
        Material type = loc.getBlock().getType();
        return !type.equals(Material.WOOD_DOOR)
                && !type.equals(Material.WOODEN_DOOR)
                && !type.equals(Material.DARK_OAK_DOOR)
                && !type.equals(Material.BIRCH_DOOR)
                && !type.equals(Material.SPRUCE_DOOR)
                && !type.equals(Material.FENCE)
                && !type.equals(Material.FENCE_GATE)
                && !type.equals(Material.TRAP_DOOR)
                && !type.equals(Material.WOOD_STAIRS)
                && !type.equals(Material.BRICK_STAIRS)
                && !type.equals(Material.DARK_OAK_STAIRS)
                && !type.equals(Material.BIRCH_WOOD_STAIRS)
                && !type.equals(Material.SPRUCE_WOOD_STAIRS)
                && !type.equals(Material.WOOD_PLATE)
                && !type.equals(Material.STONE_PLATE)
                && !type.equals(Material.STONE_SLAB2)
                && !type.equals(Material.WOOD_STEP)
                && !type.equals(Material.ACACIA_DOOR)
                && !type.equals(Material.ACACIA_FENCE)
                && !type.equals(Material.ACACIA_FENCE_GATE)
                && !type.equals(Material.ACACIA_STAIRS)
                && !type.equals(Material.WEB)
                && !type.equals(Material.DAYLIGHT_DETECTOR)
                && !type.equals(Material.DAYLIGHT_DETECTOR_INVERTED)
                && !type.equals(Material.DIODE)
                && !type.equals(Material.DIODE_BLOCK_OFF)
                && !type.equals(Material.DIODE_BLOCK_ON)
                && !type.equals(Material.DOUBLE_STEP)
                && !type.equals(Material.DOUBLE_PLANT)
                && !type.equals(Material.DOUBLE_STONE_SLAB2)
                && !type.equals(Material.COBBLESTONE_STAIRS)
                && !type.equals(Material.COBBLE_WALL)
                && !type.equals(Material.SPRUCE_FENCE)
                && !type.equals(Material.SPRUCE_FENCE_GATE)
                && !type.equals(Material.BIRCH_FENCE)
                && !type.equals(Material.BIRCH_FENCE_GATE)
                && !type.equals(Material.DARK_OAK_FENCE)
                && !type.equals(Material.DARK_OAK_FENCE_GATE)
                && !type.equals(Material.LADDER)
                && !type.equals(Material.SNOW)
                && !type.equals(Material.WATER)
                && !type.equals(Material.WATER_LILY)
                && !type.equals(Material.LAVA)
                && !type.equals(Material.WEB)
                && !type.equals(Material.NETHER_BRICK_STAIRS)
                && !type.equals(Material.NETHER_FENCE)
                && !type.equals(Material.NETHER_WARTS)
                && type.equals(Material.AIR)
                &&
                (p.getEyeLocation().getBlockY() >= loc.getBlockY()
                        || loc.clone().add(0, -1, 0).getBlock().getType().equals(Material.AIR));
    }

    public Location fakeTorchLoc(Player p, Location to) {
        Location loc = to.clone().add(0, 3, 0);
        if (isValid(p, loc)) {
            return loc;
        }
        loc = to.clone().add(0, 2, 0);
        if (isValid(p, loc)) {
            return loc;
        }
        loc = to;
        if (isValid(p, loc)) {
            return loc;
        }
        loc = to.clone().add(0, 1, 0);
        if (isValid(p, loc)) {
            return loc;
        }
        loc = to.clone().add(0, 2, 0);
        return loc;
    }

    public List<Material> getLightSources() {
        if (lightsources.isEmpty()) {
            List<Material> list = new ArrayList<Material>();
            for (String s : getConfig().getStringList("lightsources")) {
                if (!s.contains("=")) {
                    list.add(Material.getMaterial(s));
                } else {
                    list.add(Material.getMaterial(s.split("=")[0]));
                }
            }
            lightsources.addAll(list);
            return list;
        }
        return lightsources;
    }

    public boolean isLightSource(Material mat) {
        return getLightSources().contains(mat);
    }

    public Map<Material, Material> getLightlevels() {
        if (lightlevels.isEmpty()) {
            Map<Material, Material> list = new HashMap<Material, Material>();
            for (String s : getConfig().getStringList("lightsources")) {
                Material m;
                if (!s.contains("=")) {
                    m = Material.getMaterial(s);
                    list.put(m, Material.TORCH);
                } else {
                    m = Material.getMaterial(s.split("=")[1]);
                    if (m.equals(Material.TORCH) || m.equals(Material.REDSTONE_TORCH_ON)
                            || m.equals(Material.REDSTONE_TORCH_OFF)
                    ) {
                        if (m.equals(Material.REDSTONE_TORCH_OFF)) {
                            list.put(Material.getMaterial(s.split("=")[0]), Material.REDSTONE_TORCH_ON);
                        } else {
                            list.put(Material.getMaterial(s.split("=")[0]), Material.getMaterial(s.split("=")[1]));
                        }
                    } else {
                        list.put(Material.getMaterial(s.split("=")[0]), Material.TORCH);
                    }
                }
            }
            lightlevels.putAll(list);
            return list;
        }
        return lightlevels;
    }

    public boolean permsEnabled() {
        return getConfig().getBoolean("enable-permissions");
    }

    public boolean itemPermsEnabled() {
        return getConfig().getBoolean("enable-item-specific-permissions");
    }

    public int getDistanceToPrevious() {
        return getConfig().getInt("distance-before-new-lightsource");
    }

    public Material getLightlevel(Material mat) {
        if (isLightSource(mat) && getLightlevels().containsKey(mat)) {
            return getLightlevels().get(mat);
        }
        return Material.TORCH;
    }
}
