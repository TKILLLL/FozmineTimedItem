package org.phantam.fozminetimeditem.resolver;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class ItemsAdderResolver implements ItemResolver {

    private static final String SOURCE = "ITEMSADDER";
    private final boolean available;

    public ItemsAdderResolver() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ItemsAdder");
        available = plugin != null && plugin.isEnabled();
    }

    @Override
    @Nullable
    public ItemStack resolve(String definition, int amount) {
        if (!available || definition == null || definition.isEmpty()) return null;

        try {
            // Hỗ trợ định dạng: NAMESPACE:ID hoặc chỉ ID (fallback)
            String namespace = null;
            String id = definition;

            // Kiểm tra nếu có dấu ":" -> tách namespace và id
            if (definition.contains(":")) {
                String[] parts = definition.split(":", 2);
                namespace = parts[0];
                id = parts[1];
            }

            // Thử lấy CustomStack với đầy đủ namespace:ID
            String fullId = namespace != null ? namespace + ":" + id : id;
            CustomStack customStack = CustomStack.getInstance(fullId);

            // Nếu không tìm thấy và có namespace, thử chỉ với id
            if (customStack == null && namespace != null) {
                customStack = CustomStack.getInstance(id);
            }

            if (customStack == null) return null;

            ItemStack item = customStack.getItemStack();
            if (item == null) return null;

            item = item.clone();
            item.setAmount(Math.min(amount, item.getMaxStackSize()));
            return item;
        } catch (Exception e) {
            return null;
        }
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