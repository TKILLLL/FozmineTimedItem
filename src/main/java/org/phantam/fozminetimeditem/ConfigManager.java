package org.phantam.fozminetimeditem;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.phantam.fozminetimeditem.resolver.ItemResolverFactory;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    private final Map<String, ItemMapping> itemMappings = new HashMap<>();
    private final Map<String, String> expiredReplacements = new HashMap<>();

    private boolean replaceEnabled = true;
    private String expiryPeriodFormat;
    private String expiredItemRemoved;
    private String giveMessage;
    private final Map<String, String> unitFormat = new HashMap<>();
    private int itemCheckInterval = 5;
    private String dateFormat;
    private int playersPerTick = 5;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        loadExpiredReplacements();
        loadItemMappings();
        loadGeneralSettings();
    }

    private void loadExpiredReplacements() {
        replaceEnabled = config.getBoolean("expired-item-replace.enable", true);
        expiredReplacements.clear();

        if (!config.contains("expired-item-replace")) return;

        for (String key : config.getConfigurationSection("expired-item-replace").getKeys(false)) {
            if (key.equals("enable")) continue;
            String value = config.getString("expired-item-replace." + key);
            if (value != null && !value.isEmpty()) {
                expiredReplacements.put(key, value);
            }
        }
    }

    private void loadItemMappings() {
        itemMappings.clear();

        if (!config.contains("item-mappings")) return;

        for (String key : config.getConfigurationSection("item-mappings").getKeys(false)) {
            String itemDef = extractItemDefinition(key);
            if (itemDef == null || itemDef.isEmpty()) {
                plugin.getLogger().warning("Skipped mapping for key '" + key + "' – missing item definition.");
                continue;
            }

            if (!ItemResolverFactory.isValidDefinition(itemDef)) {
                plugin.getLogger().warning("Invalid item definition for key '" + key + "': " + itemDef);
                continue;
            }

            String expiredKey = config.getString("item-mappings." + key + ".expired");
            itemMappings.put(key, new ItemMapping(itemDef, expiredKey));
            plugin.getLogger().info("Loaded mapping: " + key + " -> " + itemDef);
        }
    }

    private String extractItemDefinition(String key) {
        if (config.contains("item-mappings." + key + ".item")) {
            return config.getString("item-mappings." + key + ".item");
        }

        if (config.contains("item-mappings." + key + ".type") && config.contains("item-mappings." + key + ".id")) {
            String type = config.getString("item-mappings." + key + ".type");
            String id = config.getString("item-mappings." + key + ".id");
            if (type != null && id != null) {
                return "MMOITEM:" + type + ":" + id;
            }
        }

        Object raw = config.get("item-mappings." + key);
        return raw instanceof String ? (String) raw : null;
    }

    private void loadGeneralSettings() {
        expiryPeriodFormat = config.getString("expiry-period-format", "&e⏳ &fHết hạn sau: &7%value%");
        expiredItemRemoved = config.getString("expired-item-removed", "&cĐã xóa %amount% %item% vật phẩm hết hạn");
        giveMessage = config.getString("give-message", "&aĐã gửi &e%amount% &f%item% &atới &e%player%");
        dateFormat = config.getString("date-format", "HH:mm:ss");
        itemCheckInterval = Math.max(1, config.getInt("item-check-interval", 5));
        playersPerTick = Math.max(1, config.getInt("players-per-tick", 5));

        loadUnitFormats();
    }

    private void loadUnitFormats() {
        unitFormat.clear();

        if (config.contains("unit-format")) {
            for (String key : config.getConfigurationSection("unit-format").getKeys(false)) {
                unitFormat.put(key, config.getString("unit-format." + key));
            }
        }

        unitFormat.putIfAbsent("second", "%d s");
        unitFormat.putIfAbsent("minute", "%d m");
        unitFormat.putIfAbsent("hour", "%d h");
        unitFormat.putIfAbsent("day", "%d d");
        unitFormat.putIfAbsent("seconds", "%d s");
        unitFormat.putIfAbsent("minutes", "%d m");
        unitFormat.putIfAbsent("hours", "%d h");
        unitFormat.putIfAbsent("days", "%d d");
    }

    // ---- Getters ----

    public Map<String, ItemMapping> getItemMappings() {
        return itemMappings;
    }

    public Map<String, String> getExpiredReplacements() {
        return expiredReplacements;
    }

    public boolean isReplaceEnabled() {
        return replaceEnabled;
    }

    public String getExpiryPeriodFormat() {
        return expiryPeriodFormat;
    }

    public String getExpiredItemRemoved() {
        return expiredItemRemoved;
    }

    public String getGiveMessage() {
        return giveMessage;
    }

    public Map<String, String> getUnitFormat() {
        return unitFormat;
    }

    public int getItemCheckInterval() {
        return itemCheckInterval;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public int getPlayersPerTick() {
        return playersPerTick;
    }

    // ---- Inner class ----

    public static class ItemMapping {
        public final String itemDefinition;
        public final String expiredReplaceKey;

        public ItemMapping(String itemDefinition, String expiredReplaceKey) {
            this.itemDefinition = itemDefinition;
            this.expiredReplaceKey = expiredReplaceKey;
        }
    }
}