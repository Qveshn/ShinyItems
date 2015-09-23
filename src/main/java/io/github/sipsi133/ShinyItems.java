/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 sipsi133
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
            lastLoc.get(e.getPlayer().getName()).getBlock().getState().update();
            lastLoc.remove(e.getPlayer().getName());
        }
        if (e.getPlayer().getInventory().getItemInHand() != null
                && isLightSource(e.getPlayer().getInventory().getItemInHand().getType())
        ) {
            if (e.getPlayer().hasPermission("shinyitems.use") || !permsEnabled()) {
                Location torchLoc = fakeTorchLoc(e.getPlayer(), e.getTo());
                e.getPlayer().sendBlockChange(
                        torchLoc,
                        getLightlevel(e.getPlayer().getInventory().getItemInHand().getType()),
                        (byte) 0);
                lastLoc.put(e.getPlayer().getName(), torchLoc);
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
            if (e.getPlayer().hasPermission("shinyitems.use") || !permsEnabled()) {
                Location torchLoc = fakeTorchLoc(e.getPlayer(), e.getPlayer().getLocation());
                e.getPlayer().sendBlockChange(
                        torchLoc,
                        getLightlevel(e.getPlayer().getInventory().getItemInHand().getType()),
                        (byte) 0);
                lastLoc.put(e.getPlayer().getName(), torchLoc);
            }
        }
    }

    public boolean isValid(Player p, Location loc) {
        return !loc.getBlock().getType().equals(Material.WOOD_DOOR)
                && !loc.getBlock().getType().equals(Material.WOODEN_DOOR)
                && !loc.getBlock().getType().equals(Material.DARK_OAK_DOOR)
                && !loc.getBlock().getType().equals(Material.BIRCH_DOOR)
                && !loc.getBlock().getType().equals(Material.SPRUCE_DOOR)
                && !loc.getBlock().getType().equals(Material.FENCE)
                && !loc.getBlock().getType().equals(Material.FENCE_GATE)
                && !loc.getBlock().getType().equals(Material.TRAP_DOOR)
                && !loc.getBlock().getType().equals(Material.WOOD_STAIRS)
                && !loc.getBlock().getType().equals(Material.BRICK_STAIRS)
                && !loc.getBlock().getType().equals(Material.DARK_OAK_STAIRS)
                && !loc.getBlock().getType().equals(Material.BIRCH_WOOD_STAIRS)
                && !loc.getBlock().getType().equals(Material.SPRUCE_WOOD_STAIRS)
                && !loc.getBlock().getType().equals(Material.WOOD_PLATE)
                && !loc.getBlock().getType().equals(Material.STONE_PLATE)
                && !loc.getBlock().getType().equals(Material.STONE_SLAB2)
                && !loc.getBlock().getType().equals(Material.WOOD_STEP)
                && !loc.getBlock().getType().equals(Material.ACACIA_DOOR)
                && !loc.getBlock().getType().equals(Material.ACACIA_FENCE)
                && !loc.getBlock().getType().equals(Material.ACACIA_FENCE_GATE)
                && !loc.getBlock().getType().equals(Material.ACACIA_STAIRS)
                && !loc.getBlock().getType().equals(Material.WEB)
                && !loc.getBlock().getType().equals(Material.DAYLIGHT_DETECTOR)
                && !loc.getBlock().getType().equals(Material.DAYLIGHT_DETECTOR_INVERTED)
                && !loc.getBlock().getType().equals(Material.DIODE)
                && !loc.getBlock().getType().equals(Material.DIODE_BLOCK_OFF)
                && !loc.getBlock().getType().equals(Material.DIODE_BLOCK_ON)
                && !loc.getBlock().getType().equals(Material.DOUBLE_STEP)
                && !loc.getBlock().getType().equals(Material.DOUBLE_PLANT)
                && !loc.getBlock().getType().equals(Material.DOUBLE_STONE_SLAB2)
                && !loc.getBlock().getType().equals(Material.COBBLESTONE_STAIRS)
                && !loc.getBlock().getType().equals(Material.COBBLE_WALL)
                && !loc.getBlock().getType().equals(Material.SPRUCE_FENCE)
                && !loc.getBlock().getType().equals(Material.SPRUCE_FENCE_GATE)
                && !loc.getBlock().getType().equals(Material.BIRCH_FENCE)
                && !loc.getBlock().getType().equals(Material.BIRCH_FENCE_GATE)
                && !loc.getBlock().getType().equals(Material.DARK_OAK_FENCE)
                && !loc.getBlock().getType().equals(Material.DARK_OAK_FENCE_GATE)
                && !loc.getBlock().getType().equals(Material.LADDER)
                && !loc.getBlock().getType().equals(Material.SNOW)
                && !loc.getBlock().getType().equals(Material.WATER)
                && !loc.getBlock().getType().equals(Material.WATER_LILY)
                && !loc.getBlock().getType().equals(Material.LAVA)
                && !loc.getBlock().getType().equals(Material.WEB)
                && !loc.getBlock().getType().equals(Material.NETHER_BRICK_STAIRS)
                && !loc.getBlock().getType().equals(Material.NETHER_FENCE)
                && !loc.getBlock().getType().equals(Material.NETHER_WARTS)
                && loc.getBlock().getType().equals(Material.AIR)
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
            return list;
        }
        return lightlevels;
    }

    public boolean permsEnabled() {
        return getConfig().getBoolean("enable-permissions");
    }

    public Material getLightlevel(Material mat) {
        if (isLightSource(mat)) {
            for (Map.Entry<Material, Material> entry : getLightlevels().entrySet()) {
                if (entry.getKey().equals(mat)) {
                    return entry.getValue();
                }
            }
        }
        return Material.TORCH;
    }
}
