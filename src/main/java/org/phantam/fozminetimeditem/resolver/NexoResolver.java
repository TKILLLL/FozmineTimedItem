package org.phantam.fozminetimeditem.resolver;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class NexoResolver implements ItemResolver {

    private static final String SOURCE = "NEXO";
    private final boolean available;

    public NexoResolver() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Nexo");
        available = plugin != null && plugin.isEnabled();
    }

    @Override
    @Nullable
    public ItemStack resolve(String definition, int amount) {
        if (!available || definition == null || definition.isEmpty()) return null;

        try {
            if (!NexoItems.exists(definition)) {
                return null;
            }

            ItemBuilder builder = NexoItems.itemFromId(definition);
            if (builder == null) {
                java.util.Optional<ItemBuilder> optional = NexoItems.optionalItemFromId(definition);
                if (optional.isEmpty()) return null;
                builder = optional.get();
            }

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