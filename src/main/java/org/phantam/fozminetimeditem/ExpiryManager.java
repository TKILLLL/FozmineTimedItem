package org.phantam.fozminetimeditem;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.phantam.fozminetimeditem.resolver.ItemResolverFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ExpiryManager {

    private static final long SECOND_MS = 1000L;
    private static final long MINUTE_MS = 60L * SECOND_MS;
    private static final long HOUR_MS = 60L * MINUTE_MS;
    private static final long DAY_MS = 24L * HOUR_MS;

    public static final long MAX_DURATION_MS = 3650L * DAY_MS;

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final NamespacedKey expiryKey;
    private final NamespacedKey customIdKey;

    public ExpiryManager(FozmineTimedItem plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.expiryKey = new NamespacedKey(plugin, "expiry");
        this.customIdKey = new NamespacedKey(plugin, "custom_id");
    }

    @Nullable
    public ItemStack createTimedItem(String customId, int amount, long durationMs) {
        ConfigManager.ItemMapping mapping = configManager.getItemMappings().get(customId);
        if (mapping == null) {
            plugin.getLogger().warning("No item mapping found for: " + customId);
            return null;
        }

        ItemStack base = ItemResolverFactory.resolve(mapping.itemDefinition, amount);
        if (base == null) {
            plugin.getLogger().warning("Failed to resolve item: " + mapping.itemDefinition);
            return null;
        }

        ItemStack item = base.clone();
        item.setAmount(amount);

        long expiryTime = System.currentTimeMillis() + durationMs;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(expiryKey, PersistentDataType.LONG, expiryTime);
        pdc.set(customIdKey, PersistentDataType.STRING, customId);

        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        lore.removeIf(line -> line.contains("⏳"));
        lore.add(formatExpiryLore(expiryTime));
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    public void updateLore(ItemStack item) {
        if (!hasTimedData(item)) return;

        long expiry = getExpiryTime(item);
        if (expiry < 0) return;

        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        lore.removeIf(line -> line.contains("⏳"));
        lore.add(formatExpiryLore(expiry));
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private String formatExpiryLore(long expiryTime) {
        long remaining = Math.max(0, expiryTime - System.currentTimeMillis());
        String time = formatTime(remaining);
        String pattern = configManager.getExpiryPeriodFormat();
        return ChatColor.translateAlternateColorCodes('&', pattern.replace("%value%", time));
    }

    private String formatTime(long millis) {
        long totalSec = millis / SECOND_MS;
        long hours = totalSec / 3600;
        long minutes = (totalSec % 3600) / 60;
        long seconds = totalSec % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public boolean isExpired(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(expiryKey, PersistentDataType.LONG)) return false;

        long expiry = pdc.get(expiryKey, PersistentDataType.LONG);
        return System.currentTimeMillis() >= expiry;
    }

    public boolean hasTimedData(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(expiryKey, PersistentDataType.LONG);
    }

    public long getExpiryTime(ItemStack item) {
        if (!hasTimedData(item)) return -1;
        return item.getItemMeta().getPersistentDataContainer().get(expiryKey, PersistentDataType.LONG);
    }

    @Nullable
    public String getCustomId(ItemStack item) {
        if (!hasTimedData(item)) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.get(customIdKey, PersistentDataType.STRING);
    }

    public void handleExpiredItem(Player player, ItemStack item, int slot) {
        if (!isExpired(item)) return;

        ItemStack replacement = buildReplacement(item);
        if (replacement != null && slot >= 0) {
            player.getInventory().setItem(slot, replacement);
            return;
        }

        if (slot >= 0) {
            player.getInventory().setItem(slot, null);
        }
    }

    @Nullable
    private ItemStack buildReplacement(ItemStack expiredItem) {
        if (!configManager.isReplaceEnabled()) return null;

        String customId = getCustomId(expiredItem);
        if (customId == null) return null;

        ConfigManager.ItemMapping mapping = configManager.getItemMappings().get(customId);
        String replaceKey = (mapping != null && mapping.expiredReplaceKey != null)
                ? mapping.expiredReplaceKey
                : customId;

        String replacementDef = configManager.getExpiredReplacements().get(replaceKey);
        if (replacementDef == null || replacementDef.isEmpty()) return null;

        return ItemResolverFactory.resolve(replacementDef, expiredItem.getAmount());
    }

    public String getItemDisplayName(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return "Air";

        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }

        String name = item.getType().name().toLowerCase().replace('_', ' ');
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public NamespacedKey getExpiryKey() {
        return expiryKey;
    }

    public NamespacedKey getCustomIdKey() {
        return customIdKey;
    }
}