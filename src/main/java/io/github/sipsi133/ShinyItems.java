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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.chunks.ChunkInfo;

import ru.beykerykt.lightapi.server.ServerModInfo;
import ru.beykerykt.lightapi.server.ServerModManager;
import ru.beykerykt.lightapi.server.nms.craftbukkit.CraftBukkit_v1_10_R1;
import ru.beykerykt.lightapi.server.nms.craftbukkit.CraftBukkit_v1_8_R3;
import ru.beykerykt.lightapi.server.nms.craftbukkit.CraftBukkit_v1_9_R1;
import ru.beykerykt.lightapi.server.nms.craftbukkit.CraftBukkit_v1_9_R2;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ShinyItems extends JavaPlugin implements Listener {

    private Map<String, Location> lastLoc = new HashMap<>();
    public List<String> disabledPlayers = new ArrayList<>();
    public List<ShinyItem> shinyItemList = new ArrayList<>();
    public static ShinyItems instance = null;
    public static boolean is19version = true;
    private boolean lightApiEnabled = true;

    public boolean isLightAPI() {
        return lightApiEnabled;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    public boolean isToggledOn(Player p) {
        return !disabledPlayers.contains(p.getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onEnable() {
        Plugin lightapi = getServer().getPluginManager().getPlugin("LightAPI");
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        if (lightapi != null) {
            String tmp = version.split("_")[1];
            double last = Double.valueOf(tmp);
            if (last < 9) {
                is19version = false;
            }
            if (!is19version) {
                getLogger().log(Level.INFO, "You are still using lower than 1.9 version.");
            }
            if (version.contains("1_11")) {
                getLogger().log(Level.INFO, "Adding v_1_11_R1 NMS Implementation to LightAPI...");
                try {
                    Field f = ServerModManager.class.getDeclaredField("supportImpl");
                    f.setAccessible(true);
                    Map<?, ?> map = (Map) f.get(null);
                    Map<?, ?> clone = new HashMap<>(map);
                    if (clone.containsKey("Spigot")) {
                        clone.remove("Spigot");
                    }
                    if (clone.containsKey("CraftBukkit")) {
                        clone.remove("CraftBukkit");
                    }
                    f.set(null, clone);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                ServerModInfo spigot = new ServerModInfo("Spigot");
                spigot.getVersions().put("v1_8_R3", CraftBukkit_v1_8_R3.class);
                spigot.getVersions().put("v1_9_R1", CraftBukkit_v1_9_R1.class);
                spigot.getVersions().put("v1_9_R2", CraftBukkit_v1_9_R2.class);
                spigot.getVersions().put("v1_10_R1", CraftBukkit_v1_10_R1.class);
                spigot.getVersions().put("v1_11_R1", CraftBukkit_v1_11_R1.class);
                ServerModManager.registerServerMod(spigot);
                ServerModInfo cb = new ServerModInfo("CraftBukkit");
                cb.getVersions().put("v1_8_R3", CraftBukkit_v1_8_R3.class);
                cb.getVersions().put("v1_9_R1", CraftBukkit_v1_9_R1.class);
                cb.getVersions().put("v1_9_R2", CraftBukkit_v1_9_R2.class);
                cb.getVersions().put("v1_10_R1", CraftBukkit_v1_10_R1.class);
                cb.getVersions().put("v1_11_R1", CraftBukkit_v1_11_R1.class);
                ServerModManager.registerServerMod(cb);
                ServerModManager.init();
                getLogger().log(Level.INFO, "Injection successfull!");
            }
            getLogger().log(Level.INFO, "Enabled ShinyItems!");
        } else {
            getLogger().log(Level.SEVERE,
                    "LightAPI not found! Download LightAPI in order to use ShinyItems. Disabling...");
            getServer().getPluginManager().disablePlugin(this);
        }
        ConfigurationSerialization.registerClass(ShinyItem.class);
        reloadConfig();
        getConfig().addDefault("enable-permissions", false);
        getConfig().addDefault("enable-item-specific-permissions", false);
        getConfig().addDefault("distance-before-new-lightsource", 1);
        getConfig().addDefault("lightsources", Arrays.asList(
                new ShinyItem(Material.REDSTONE_TORCH_ON, 7, -1, false),
                new ShinyItem(Material.REDSTONE_TORCH_OFF, 7, -1, false),
                new ShinyItem(Material.GLOWSTONE, 14, -1, false),
                new ShinyItem(Material.TORCH, 14, -1, false),
                new ShinyItem(Material.LAVA, 14, -1, false),
                new ShinyItem(Material.LAVA_BUCKET, 14, -1, false)
        ));
        getConfig().options().copyDefaults(true);
        getConfig().options().copyHeader(false);
        saveConfig();
        File file = new File(getDataFolder(), "toggled_players.yml");
        if (file.exists()) {
            YamlConfiguration players = YamlConfiguration.loadConfiguration(file);
            disabledPlayers.addAll((List<String>) players.getList("Toggled"));
        }
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("shinyitems").setExecutor(new Commands(this));
        instance = this;
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    handleRemove(p);
                    if (!isToggledOn(p)) {
                        return;
                    }
                    handleCreate(p);
                }
                ++i;
            }
        }, 100L, 10L);
    }

    public void handleRemove(Player p) {
        if (lastLoc.containsKey(p.getName())) {
            instance.deleteLight(p, false);
            if (!is19version) {
                if (!instance.isLightSource(instance.getItemInHand(p))) {
                    instance.update(p);
                    lastLoc.remove(p.getName());
                }
            } else {
                if (!instance.isLightSource(instance.getItemInMainHand(p))
                        && !instance.isLightSource(instance.getItemInOffHand(p))
                ) {
                    instance.update(p);
                    lastLoc.remove(p.getName());
                }
            }
        }
    }

    public void handleCreate(Player p) {
        if (!is19version) {
            if (instance.isLightSource(instance.getItemInHand(p))) {
                if (permsEnabled()) {
                    if (!p.hasPermission("shinyitems.use")) {
                        return;
                    }
                    if (itemPermsEnabled()) {
                        if (!p.hasPermission("shinyitems.use."
                                + instance.getItemInHand(p).getType().toString().toLowerCase())
                        ) {
                            return;
                        }
                    }
                }
                lastLoc.put(p.getName(), p.getLocation());
                instance.createLight(p.getLocation(), p, true, false);
                instance.update(p);
            }
        } else {
            if (instance.isLightSource(instance.getItemInMainHand(p))
                    || instance.isLightSource(instance.getItemInOffHand(p))
            ) {
                if (permsEnabled()) {
                    if (!p.hasPermission("shinyitems.use")) {
                        return;
                    }
                    if (itemPermsEnabled()) {
                        if (!p.hasPermission("shinyitems.use."
                                + instance.getItemInHand(p).getType().toString().toLowerCase())
                        ) {
                            return;
                        }
                    }
                }
                lastLoc.put(p.getName(), p.getLocation());
                instance.createLight(p.getLocation(), p, true, instance.isLightSource(instance.getItemInOffHand(p)));
                instance.update(p);
            }
        }
    }

    public ItemStack getItemInHand(Player p) {
        if (p.getItemInHand() == null) {
            return new ItemStack(Material.AIR);
        }
        return p.getItemInHand();
    }

    public ItemStack getItemInMainHand(Player p) {
        if (p.getInventory().getItemInMainHand() == null) {
            return new ItemStack(Material.AIR);
        }
        return p.getInventory().getItemInMainHand();
    }

    public ItemStack getItemInOffHand(Player p) {
        if (p.getInventory().getItemInOffHand() == null) {
            return new ItemStack(Material.AIR);
        }
        return p.getInventory().getItemInOffHand();
    }

    @Override
    public void onDisable() {
        for (Map.Entry<String, Location> entry : lastLoc.entrySet()) {
            if (isLightAPI()) {
                LightAPI.deleteLight(entry.getValue(), false);
            } else {
                entry.getValue().getBlock().getState().update();
            }
        }
        try {
            File file = new File(getDataFolder(), "toggled_players.yml");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            YamlConfiguration players = YamlConfiguration.loadConfiguration(file);
            players.set("Toggled", disabledPlayers);
            players.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ShinyItems getInstance() {
        return instance;
    }

    public void fixConfig() {
        List<String> ls = getConfig().getStringList("lightsources");
        List<String> newls = new ArrayList<>();
        for (String s : ls) {
            if (!isInteger(s.split("=")[1])) {
                String str = s.split("=")[0];
                int light = 0;
                switch (str.toUpperCase()) {
                    case "REDSTONE_TORCH_OFF":
                    case "REDSTONE_TORCH_ON":
                        light = 7;
                        break;
                    default:
                        light = 14;
                        break;
                }
                newls.add(str + "=" + light);
            } else {
                newls.add(s);
            }
        }
        getConfig().set("lightsources", newls);
        saveConfig();
        reloadConfig();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        handleRemove(e.getPlayer());
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

    public List<ShinyItem> getLightSources() {
        if (shinyItemList.isEmpty()) {
            List<ShinyItem> list = new ArrayList<>();
            for (Object s : getConfig().getList("lightsources")) {
                list.add((ShinyItem) s);
            }
            shinyItemList = list;
            return shinyItemList;
        }
        return shinyItemList;
    }

    public boolean isLightSource(ItemStack mat) {
        for (ShinyItem si : getLightSources()) {
            if (!si.getMaterial().equals(mat.getType()))
                continue;
            if (si.getDurability() != -1 && si.getDurability() != mat.getDurability())
                continue;
            if (si.isUnbreakable() && !mat.hasItemMeta())
                continue;
            if (si.isUnbreakable() != mat.getItemMeta().spigot().isUnbreakable())
                continue;
            return true;
        }
        return false;
    }

    public void createLight(Location torchLoc, Player p, boolean checkToggle, boolean offHand) {
        if (is19version) {
            LightAPI.createLight(
                    torchLoc.getWorld(),
                    torchLoc.getBlockX(),
                    torchLoc.getBlockY(),
                    torchLoc.getBlockZ(),
                    !offHand
                            ? getLightlevel(p.getInventory().getItemInMainHand().getType())
                            : getLightlevel(p.getInventory().getItemInOffHand().getType()),
                    true);
        } else {
            LightAPI.createLight(
                    torchLoc.getWorld(),
                    torchLoc.getBlockX(),
                    torchLoc.getBlockY(),
                    torchLoc.getBlockZ(),
                    getLightlevel(p.getInventory().getItemInHand().getType()),
                    true);
        }
    }

    public void update(Player p) {
        Location loc = lastLoc.get(p.getName());
        for (ChunkInfo info :
                LightAPI.collectChunks(
                        loc.getWorld(),
                        loc.getBlockX(),
                        loc.getBlockY(),
                        loc.getBlockZ())
        ) {
            if (info != null) {
                LightAPI.updateChunk(info);
            }
        }
    }

    public void deleteLight(Player p, boolean checkToggle) {
        Location loc = lastLoc.get(p.getName());
        LightAPI.deleteLight(
                loc.getWorld(),
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ(),
                true);
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

    public int getLightlevel(Material mat) {
        for (ShinyItem si : getLightSources()) {
            if (si.getMaterial().equals(mat)) {
                return si.getLightLevel();
            }
        }
        return 14;
    }
}
