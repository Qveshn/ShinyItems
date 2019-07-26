/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 sipsi133
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
package io.github.sipsi133;

import io.github.sipsi133.commands.ShinyCommand;
import io.github.sipsi133.engine.LightEngine;
import io.github.sipsi133.engine.ShinyItem;
import io.github.sipsi133.engine.ShinyItemSelector;
import io.github.sipsi133.lightapi.LightAPI;
import io.github.sipsi133.lightapi.LightAPIv3;
import io.github.sipsi133.lightapi.LightAPIv5;
import io.github.sipsi133.utils.Debug;
import io.github.sipsi133.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public class ShinyItems extends JavaPlugin implements Listener {

    private static ShinyItems instance = null;
    private final Set<String> disabledPlayers = new HashSet<>();
    private final LightEngine lightEngine;
    private ShinyItemSelector itemSelector;
    private LightAPI lightAPI;

    public ShinyItems() {
        Debug.setPrefix(getName(), ChatColor.YELLOW, ChatColor.DARK_AQUA);

        lightAPI = LightAPIv5.createInstance();
        if (lightAPI == null) {
            lightAPI = LightAPIv3.createInstance();
        }

        ConfigurationSerialization.registerClass(ShinyItem.class);
        lightEngine = new LightEngine(this);
        instance = this;
    }

    @Override
    public void onEnable() {
        if (lightAPI == null) {
            getLogger().log(Level.SEVERE,
                    "LightAPI not found! Download LightAPI in order to use ShinyItems. Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().log(Level.INFO, String.format("Using LightAPI v%s", lightAPI.implementationVersion()));

        PluginCommand shinyitemsCommand = getCommand("shinyitems");
        if (shinyitemsCommand == null) {
            getLogger().log(Level.SEVERE, "No ShinyItems command!");
        } else {
            shinyitemsCommand.setExecutor(new ShinyCommand(this));
        }

        File file = new File(getDataFolder(), "toggled_players.yml");
        if (file.exists()) {
            YamlConfiguration players = YamlConfiguration.loadConfiguration(file);
            disabledPlayers.addAll(players.getStringList("Toggled"));
        }

        reloadConfig();
        lightEngine.start();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
        lightEngine.stop();
        File file = new File(getDataFolder(), "toggled_players.yml");
        try {
            Utils.mkdirs(file.getParentFile());
            YamlConfiguration players = new YamlConfiguration();
            players.set("Toggled", disabledPlayers);
            players.save(file);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save toggled players to file " + file.getName(), e);
            e.printStackTrace();
        }
    }

    @Override
    public void reloadConfig() {
        boolean isStarted = lightEngine.isStarted();
        HandlerList.unregisterAll((Plugin) this);
        if (isStarted) {
            lightEngine.stop();
        }

        super.reloadConfig();
        Debug.setEnable(getConfig().getBoolean("debug"));

        List<ShinyItem> lightSources;
        if (!new File(this.getDataFolder(), "config.yml").exists()) {
            getConfig().set("lightsources", lightSources = getValidLightSources(false));
            getConfig().options().copyDefaults(true);
            Utils.saveConfigWithHeader(this);
        } else {
            lightSources = getValidLightSources(true);
        }
        itemSelector = new ShinyItemSelector(lightSources, permsEnabled(), itemPermsEnabled());
        lightEngine.setTaskPeriod(getConfig().getInt("update-delay-ticks"));
        if (isStarted) {
            lightEngine.start();
            getServer().getPluginManager().registerEvents(this, this);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isToggledOn(Player p) {
        return !disabledPlayers.contains(p.getName());
    }

    private void Toggle(Player player, boolean toggleOn) {
        if (toggleOn) {
            disabledPlayers.add(player.getName());
        } else {
            disabledPlayers.remove(player.getName());
        }
    }

    public void Toggle(Player player) {
        Toggle(player, !isToggledOn(player));
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        lightEngine.remove(e.getPlayer());
    }

    private List<ShinyItem> getValidLightSources(boolean warnings) {
        List<ShinyItem> list = new ArrayList<>();
        List<?> lightSources = getConfig().getList("lightsources");
        if (lightSources == null) {
            getLogger().log(Level.SEVERE, "No lightsources in config.yml!");
        } else {
            for (Object s : lightSources) {
                if (s instanceof ShinyItem) {
                    ShinyItem item = (ShinyItem) s;
                    Material material = item.getMaterial();
                    if (material != null) {
                        String materialName;
                        String originalName;
                        if (warnings && !(materialName = material.toString())
                                .equalsIgnoreCase(originalName = item.getOriginalMaterialName())
                        ) {
                            getLogger().log(Level.WARNING,
                                    String.format("Material %s has been converted to %s", originalName, materialName));
                        }
                        list.add(item);
                    } else if (warnings) {
                        getLogger().log(Level.SEVERE,
                                String.format("Bad material name: %s", item.getOriginalMaterialName()));
                    }
                }
            }
        }
        return list;
    }

    private boolean permsEnabled() {
        return getConfig().getBoolean("enable-permissions");
    }

    private boolean itemPermsEnabled() {
        return getConfig().getBoolean("enable-item-specific-permissions");
    }

    public LightAPI getLightAPI() {
        return lightAPI;
    }

    public ShinyItemSelector getItemSelector() {
        return itemSelector;
    }

    @SuppressWarnings("unused")
    public LightEngine getLightEngine() {
        return lightEngine;
    }

    @SuppressWarnings("unused")
    public static ShinyItems getInstance() {
        return instance;
    }
}
