package org.phantam.fozminetimeditem.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.phantam.fozminetimeditem.ConfigManager;
import org.phantam.fozminetimeditem.ExpiryManager;
import org.phantam.fozminetimeditem.FozmineTimedItem;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GiveCommand implements CommandExecutor, TabCompleter {

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhd])");
    private static final List<String> AMOUNT_SUGGESTIONS = List.of("1", "5", "10", "64");
    private static final List<String> TIME_SUGGESTIONS = List.of("30s", "5m", "1h", "1d", "3600");

    private final FozmineTimedItem plugin;
    private final ExpiryManager expiryManager;
    private final ConfigManager configManager;

    public GiveCommand(FozmineTimedItem plugin) {
        this.plugin = plugin;
        this.expiryManager = plugin.getExpiryManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("fozminetimeditem.give")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "Usage: /ti give <player> <id> <amount> <time>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        String id = args[2];
        int amount = parseAmount(args[3]);
        if (amount < 1) {
            sender.sendMessage(ChatColor.RED + "Invalid amount. Must be at least 1.");
            return true;
        }

        long durationMs;
        try {
            durationMs = parseDuration(args[4]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Invalid time format. Use e.g. 30m, 2h, 1d.");
            return true;
        }

        durationMs = Math.min(durationMs, ExpiryManager.MAX_DURATION_MS);
        if (durationMs >= ExpiryManager.MAX_DURATION_MS) {
            sender.sendMessage(ChatColor.YELLOW + "Duration capped at 3650 days.");
        }

        ItemStack item = expiryManager.createTimedItem(id, amount, durationMs);
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "Invalid item ID or configuration.");
            return true;
        }

        giveItemToPlayer(target, item);
        sendSuccessMessage(sender, target, item, amount);

        return true;
    }

    private int parseAmount(String input) {
        try {
            return Math.max(1, Integer.parseInt(input));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private long parseDuration(String input) {
        if (input.matches("\\d+")) {
            return Long.parseLong(input) * 1000L;
        }
        Matcher matcher = TIME_PATTERN.matcher(input);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time format");
        }
        long value = Long.parseLong(matcher.group(1));
        char unit = matcher.group(2).toLowerCase().charAt(0);
        long multiplier = switch (unit) {
            case 's' -> 1000L;
            case 'm' -> 60L * 1000L;
            case 'h' -> 60L * 60L * 1000L;
            case 'd' -> 24L * 60L * 60L * 1000L;
            default -> throw new IllegalArgumentException("Unknown time unit");
        };
        return value * multiplier;
    }

    private void giveItemToPlayer(Player player, ItemStack item) {
        player.getInventory().addItem(item).values()
                .forEach(remaining -> player.getWorld().dropItem(player.getLocation(), remaining));
    }

    private void sendSuccessMessage(CommandSender sender, Player target, ItemStack item, int amount) {
        String displayName = expiryManager.getItemDisplayName(item);
        String message = configManager.getGiveMessage()
                .replace("%amount%", String.valueOf(amount))
                .replace("%item%", displayName)
                .replace("%player%", target.getName());
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 3) {
            return configManager.getItemMappings().keySet().stream()
                    .filter(id -> id.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 4) {
            return AMOUNT_SUGGESTIONS.stream()
                    .filter(s -> s.startsWith(args[3]))
                    .collect(Collectors.toList());
        }
        if (args.length == 5) {
            return TIME_SUGGESTIONS.stream()
                    .filter(s -> s.startsWith(args[4]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}