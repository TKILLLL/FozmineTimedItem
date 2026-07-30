package org.phantam.fozminetimeditem.resolver;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class MMOItemsResolver implements ItemResolver {

    private static final String SOURCE = "MMOITEM";
    private final boolean available;

    public MMOItemsResolver() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MMOItems");
        available = plugin != null && plugin.isEnabled();
    }

    @Override
    @Nullable
    public ItemStack resolve(String definition, int amount) {
        if (!available || definition == null || definition.isEmpty()) {
            if (!available) {
                Bukkit.getLogger().warning("[FozmineTimedItem] MMOItems not available");
            }
            return null;
        }

        String[] parts = definition.split(":", 2);
        if (parts.length < 2) {
            Bukkit.getLogger().warning("[FozmineTimedItem] MMOItems definition invalid: " + definition);
            return null;
        }

        String typeName = parts[0];
        String id = parts[1];

        try {
            Bukkit.getLogger().info("[FozmineTimedItem] Looking for MMOItem: type=" + typeName + ", id=" + id);
            Type type = Type.get(typeName);
            if (type == null) {
                Bukkit.getLogger().warning("[FozmineTimedItem] MMOItems Type not found: " + typeName);
                Bukkit.getLogger().warning("[FozmineTimedItem] Available types: " + getAvailableTypes());
                return null;
            }

            if (MMOItems.plugin == null) {
                Bukkit.getLogger().severe("[FozmineTimedItem] MMOItems.plugin is null!");
                return null;
            }

            ItemStack item = MMOItems.plugin.getItem(type, id);
            if (item == null) {
                Bukkit.getLogger().warning("[FozmineTimedItem] MMOItems item not found: type=" + typeName + ", id=" + id);
                return null;
            }

            item = item.clone();
            item.setAmount(Math.min(amount, item.getMaxStackSize()));
            return item;
        } catch (Exception e) {
            Bukkit.getLogger().warning("[FozmineTimedItem] Exception in MMOItemsResolver: " + e.getMessage());
            return null;
        }
    }

    // Thêm helper để log các type có sẵn (tùy chọn)
    private String getAvailableTypes() {
        // Nếu bạn có cách lấy danh sách type, hãy thêm vào. Không bắt buộc.
        return "SWORD, BOW, AXE, PICKAXE, SHOVEL, HOE, ARMOR, MATERIAL, ...";
    }

    @Override
    public String getSource() {
        return SOURCE;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }
}