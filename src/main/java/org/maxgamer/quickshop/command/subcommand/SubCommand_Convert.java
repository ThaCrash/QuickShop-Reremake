/*
 * This file is a part of project QuickShop, the name is SubCommand_Convert.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.command.subcommand;

import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.database.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;


public class SubCommand_Convert implements CommandProcesser {
    private final QuickShop plugin;
    private volatile boolean running;

    public SubCommand_Convert(QuickShop plugin) {
        this.plugin = plugin;
    }

    /**
     * Accept the onCommand, it will call when have Command Event cmdArg not contains
     * CommandContainer's prefix. E.g: Register the CommandContainer with Prefix: unlimited
     * Permission: quickshop.unlimited
     *
     * <p>When player type /qs unlimited 123 cmdArg's content is 123
     *
     * @param sender       Sender
     * @param commandLabel The command prefix /qs is qs
     * @param cmdArg       Args
     */
    @SneakyThrows
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof Player) {
            sender.sendMessage(ChatColor.RED + "Danger command, please execute in console.");
            return;
        }
        if (cmdArg.length == 0) {
            sender.sendMessage(ChatColor.RED + "Please select you want convert to: mysql or sqlite");
            return;
        }
        if (running) {
            sender.sendMessage(ChatColor.RED + "Convert command is running,Please wait until it finished!");
            return;
        }
        if (cmdArg[0].equalsIgnoreCase("mysql")) {
            if (plugin.getDatabaseManager().getDatabase() instanceof MySQLCore) {
                sender.sendMessage(ChatColor.RED + "Please switch to SQLite before converting to MySQL.");
                return;
            }
            ConfigurationSection dbCfg = plugin.getConfig().getConfigurationSection("database");
            String user = dbCfg.getString("user");
            String pass = dbCfg.getString("password");
            String host = dbCfg.getString("host");
            String port = dbCfg.getString("port");
            String databaseStr = dbCfg.getString("database");
            boolean useSSL = dbCfg.getBoolean("usessl");
            running = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        AbstractDatabaseCore dbCore = new MySQLCore(plugin, Objects.requireNonNull(host, "MySQL host can't be null"), Objects.requireNonNull(user, "MySQL username can't be null"), Objects.requireNonNull(pass, "MySQL password can't be null"), Objects.requireNonNull(databaseStr, "MySQL database name can't be null"), Objects.requireNonNull(port, "MySQL port can't be null"), useSSL);
                        DatabaseManager databaseManager = new DatabaseManager(QuickShop.getInstance(), dbCore);
                        sender.sendMessage(ChatColor.GREEN + "Converting...");
                        transferShops(new DatabaseHelper(plugin, databaseManager), sender);
                        databaseManager.unInit();
                        sender.sendMessage(ChatColor.GREEN + "All done, please edit config.yml to mysql to apply changes.");
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.RED + "Error when converting database, Please check your console.");
                        plugin.getServer().getLogger().log(Level.SEVERE, "Error when converting database", e);
                    } finally {
                        running = false;
                    }
                }
            }.runTaskAsynchronously(plugin);
        } else if (cmdArg[0].equalsIgnoreCase("sqlite")) {
            if (plugin.getDatabaseManager().getDatabase() instanceof SQLiteCore) {
                sender.sendMessage(ChatColor.GREEN + "Please switch to MySQL before converting to SQLite.");
                return;
            }
            running = true;
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        AbstractDatabaseCore core = new SQLiteCore(plugin, new File(plugin.getDataFolder(), "shops.db"));
                        DatabaseManager databaseManager = new DatabaseManager(QuickShop.getInstance(), core);
                        sender.sendMessage(ChatColor.GREEN + "Converting...");
                        transferShops(new DatabaseHelper(plugin, databaseManager), sender);
                        databaseManager.unInit();
                        sender.sendMessage(ChatColor.GREEN + "All done, please edit config.yml to sqlite to apply changes.");
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.RED + "Error when converting database, Please check your console.");
                        plugin.getServer().getLogger().log(Level.SEVERE, "Error when converting database", e);
                    } finally {
                        running = false;
                    }
                }
            }.runTaskAsynchronously(plugin);

        } else {
            sender.sendMessage(ChatColor.RED + "Wrong type! Only can be mysql or sqlite");
        }
    }

    private void transferShops(@NotNull DatabaseHelper helper, @NotNull CommandSender sender) {
        plugin.getShopManager().getAllShops().forEach(shop -> {
            helper.removeShop(shop);
            helper.createShop(shop, null, (ignored) -> sender.sendMessage("Failed to convert shop " + shop));
        });
    }

    /**
     * Accept the onTabComplete, it will call when have Tab Event cmdArg not contains
     * CommandContainer's prefix. E.g: Register the CommandContainer with Prefix: unlimited
     * Permission: quickshop.unlimited
     *
     * <p>When player type /qs unlimited 123 cmdArg's content is 123
     *
     * @param sender       Sender
     * @param commandLabel The command prefix /qs is qs
     * @param cmdArg       Args
     * @return The result for tab-complete lists
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 2) {
            List<String> str = new ArrayList<>();
            str.add("sqlite");
            str.add("mysql");
            return str;
        }
        return Collections.emptyList();
    }
}
