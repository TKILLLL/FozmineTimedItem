package org.phantam.fozminetimeditem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.phantam.fozminetimeditem.command.CommandRouter;
import org.phantam.fozminetimeditem.listener.ExpiryListener;
import org.phantam.fozminetimeditem.task.ExpiryTask;

import java.util.ArrayList;
import java.util.List;

public class FozmineTimedItem extends JavaPlugin {

    private ConfigManager configManager;
    private ExpiryManager expiryManager;
    private ExpiryTask expiryTask;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        expiryManager = new ExpiryManager(this, configManager);

        configManager.loadConfig();

        CommandRouter router = new CommandRouter(this);
        getCommand("ti").setExecutor(router);
        getCommand("ti").setTabCompleter(router);

        getServer().getPluginManager().registerEvents(new ExpiryListener(expiryManager), this);

        startExpiryTask();

        printStatus(true);
        printHookedPlugins();
    }

    @Override
    public void onDisable() {
        if (expiryTask != null) {
            expiryTask.cancel();
        }
        printStatus(false);
    }

    private void startExpiryTask() {
        int interval = configManager.getItemCheckInterval();
        int perTick = configManager.getPlayersPerTick();
        expiryTask = new ExpiryTask(expiryManager, perTick);
        expiryTask.runTaskTimer(this, 0L, interval);
    }

    public void reloadConfigAndRestartTask() {
        configManager.loadConfig();
        if (expiryTask != null) {
            expiryTask.cancel();
            startExpiryTask();
        }
        // Gửi thông báo reload thành công
        Bukkit.getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&',
                        "&#00ffcc[FozmineTimedItem] &aConfiguration reloaded successfully.")
        );
    }

    private void printStatus(boolean isEnable) {
        String version = getDescription().getVersion();
        String status = isEnable ? "&a&lENABLED" : "&c&lDISABLED";

        String message = String.join("\n",
                "&8&m----------------------------------------",
                "  &b&lFozmineTimedItem &7v" + version,
                "  &7Author: &bphantam",
                "  &7Status: " + status,
                "&8&m----------------------------------------"
        );

        Bukkit.getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&', message)
        );
    }

    private void printHookedPlugins() {
        List<String> softDepend = getDescription().getSoftDepend();
        List<String> hooked = new ArrayList<>();

        for (String pluginName : softDepend) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
            if (plugin != null && plugin.isEnabled()) {
                hooked.add(pluginName);
            }
        }

        if (!hooked.isEmpty()) {
            String msg = "&#00ffcc[FozmineTimedItem] &7Hooked successfully with: &b" + String.join("&7, &b", hooked);
            Bukkit.getConsoleSender().sendMessage(
                    ChatColor.translateAlternateColorCodes('&', msg)
            );
        } else {
            Bukkit.getConsoleSender().sendMessage(
                    ChatColor.translateAlternateColorCodes('&',
                            "&#ffaa00[FozmineTimedItem] &7No supported plugins found.")
            );
        }
    }

    // ---- Getters ----

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ExpiryManager getExpiryManager() {
        return expiryManager;
    }

    public ExpiryTask getExpiryTask() {
        return expiryTask;
    }
}