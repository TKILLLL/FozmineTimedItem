package org.phantam.fozminetimeditem.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.phantam.fozminetimeditem.FozmineTimedItem;

public class ReloadCommand implements CommandExecutor {

    private static final String PERMISSION = "fozminetimeditem.reload";

    private final FozmineTimedItem plugin;

    public ReloadCommand(FozmineTimedItem plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        try {
            plugin.reloadConfigAndRestartTask();
            sender.sendMessage(ChatColor.GREEN + "Config reloaded successfully.");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Failed to reload config: " + e.getMessage());
        }

        return true;
    }
}