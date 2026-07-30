package org.phantam.fozminetimeditem.resolver;

import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class OraxenResolver implements ItemResolver {

    private static final String SOURCE = "ORAXEN";
    private final boolean available;

    public OraxenResolver() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Oraxen");
        available = plugin != null && plugin.isEnabled();
    }

    @Override
    @Nullable
    public ItemStack resolve(String definition, int amount) {
        if (!available || definition == null || definition.isEmpty()) return null;

        try {
            ItemBuilder builder = OraxenItems.getItemById(definition);
            if (builder == null) return null;

            ItemStack item = builder.build();
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