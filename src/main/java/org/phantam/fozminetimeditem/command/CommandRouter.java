package org.phantam.fozminetimeditem.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.phantam.fozminetimeditem.FozmineTimedItem;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandRouter implements CommandExecutor, TabCompleter {

    private static final List<String> SUB_COMMANDS = List.of("give", "reload");

    private final GiveCommand giveCommand;
    private final ReloadCommand reloadCommand;

    public CommandRouter(FozmineTimedItem plugin) {
        this.giveCommand = new GiveCommand(plugin);
        this.reloadCommand = new ReloadCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            showHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "give":
                return giveCommand.onCommand(sender, command, label, args);
            case "reload":
                return reloadCommand.onCommand(sender, command, label, args);
            default:
                showHelp(sender);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterSubCommands(args[0]);
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("give")) {
            return giveCommand.onTabComplete(sender, command, alias, args);
        }
        return List.of();
    }

    private List<String> filterSubCommands(String input) {
        return SUB_COMMANDS.stream()
                .filter(cmd -> cmd.startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.LIGHT_PURPLE + "FozmineTimedItem | phantam");
        sender.sendMessage(ChatColor.WHITE + "/ti reload" + ChatColor.GRAY + " - Reloads the plugin");
        sender.sendMessage(ChatColor.WHITE + "/ti give <player> <id> <amount> <time>" + ChatColor.GRAY + " - Gives timed items");
    }
}