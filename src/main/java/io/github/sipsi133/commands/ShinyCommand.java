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
package io.github.sipsi133.commands;

import io.github.sipsi133.ShinyItems;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShinyCommand implements CommandExecutor {

    private ShinyItems plugin;

    public ShinyCommand(ShinyItems plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.isOp() || sender.hasPermission("shinyitems.reload")) {
                    plugin.reloadConfig();
                    sender.sendMessage("§aShinyItems configuration reloaded!");
                }
            } else if (args[0].equalsIgnoreCase("toggle")) {
                if (sender.isOp() || sender.hasPermission("shinyitems.toggle") && sender instanceof Player) {
                    plugin.Toggle((Player) sender);
                    if (!plugin.isToggledOn((Player) sender)) {
                        sender.sendMessage("§cShinyitems is now disabled for you.");
                    } else {
                        sender.sendMessage("§aShinyitems is now enabled for you.");
                    }
                }
            }
        }
        return true;
    }
}
