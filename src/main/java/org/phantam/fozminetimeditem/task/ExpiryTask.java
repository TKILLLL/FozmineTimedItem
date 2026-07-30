package org.phantam.fozminetimeditem.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.phantam.fozminetimeditem.ExpiryManager;

import java.util.List;

public class ExpiryTask extends BukkitRunnable {

    private final ExpiryManager expiryManager;
    private final int playersPerTick;
    private int currentIndex = 0;

    public ExpiryTask(ExpiryManager expiryManager, int playersPerTick) {
        this.expiryManager = expiryManager;
        this.playersPerTick = Math.max(1, playersPerTick);
    }

    @Override
    public void run() {
        List<Player> onlinePlayers = List.copyOf(Bukkit.getOnlinePlayers());
        if (onlinePlayers.isEmpty()) {
            return;
        }

        int size = onlinePlayers.size();
        int start = currentIndex % size;
        int checked = 0;

        while (checked < playersPerTick && checked < size) {
            Player player = onlinePlayers.get((start + checked) % size);
            checkPlayerInventory(player);
            checked++;
        }

        currentIndex = (start + checked) % size;
    }

    private void checkPlayerInventory(Player player) {
        for (int slot = 0; slot < 36; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (expiryManager.isExpired(item)) {
                expiryManager.handleExpiredItem(player, item, slot);
            }
        }

        for (int slot = 36; slot <= 40; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (expiryManager.isExpired(item)) {
                expiryManager.handleExpiredItem(player, item, slot);
            }
        }
    }
}