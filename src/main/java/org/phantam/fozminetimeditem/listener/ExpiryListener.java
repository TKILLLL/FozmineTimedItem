package org.phantam.fozminetimeditem.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.phantam.fozminetimeditem.ExpiryManager;

public class ExpiryListener implements Listener {

    private final ExpiryManager expiryManager;

    public ExpiryListener(ExpiryManager expiryManager) {
        this.expiryManager = expiryManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack current = event.getCurrentItem();
        if (current != null && expiryManager.hasTimedData(current)) {
            handleItemInteraction(player, current, event.getSlot());
        }

        ItemStack cursor = event.getCursor();
        if (cursor != null && expiryManager.hasTimedData(cursor)) {
            handleCursorItem(player, cursor, event);
        }

        checkAndRestoreInventoryItems(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !expiryManager.hasTimedData(item)) return;

        handleItemInteraction(player, item, player.getInventory().getHeldItemSlot());

        if (expiryManager.isExpired(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        if (item != null && expiryManager.hasTimedData(item)) {
            handleItemInteraction(player, item, event.getNewSlot());
        }
    }

    private void handleItemInteraction(Player player, ItemStack item, int slot) {
        if (!expiryManager.hasTimedData(item)) return;

        expiryManager.updateLore(item);
        restoreExpiryDataIfMissing(item);

        if (expiryManager.isExpired(item)) {
            expiryManager.handleExpiredItem(player, item, slot);
        }
    }

    private void handleCursorItem(Player player, ItemStack cursor, InventoryClickEvent event) {
        if (!expiryManager.hasTimedData(cursor)) return;

        expiryManager.updateLore(cursor);
        restoreExpiryDataIfMissing(cursor);

        if (expiryManager.isExpired(cursor)) {
            event.setCursor(null);
            expiryManager.handleExpiredItem(player, cursor, -1);
        }
    }

    private void checkAndRestoreInventoryItems(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && expiryManager.hasTimedData(item) && hasExpiryDataMissing(item)) {
                restoreExpiryData(item);
                player.getInventory().setItem(i, item);
            }
        }
    }

    private boolean hasExpiryDataMissing(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return !pdc.has(expiryManager.getExpiryKey(), PersistentDataType.LONG);
    }

    private void restoreExpiryDataIfMissing(ItemStack item) {
        if (hasExpiryDataMissing(item)) {
            restoreExpiryData(item);
        }
    }

    private void restoreExpiryData(ItemStack item) {
        // Placeholder: data cannot be recovered if completely lost.
        // This method exists to provide a hook if future implementation can restore data.
    }
}