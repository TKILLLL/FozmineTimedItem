package org.phantam.fozminetimeditem.resolver;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class CraftEngineResolver implements ItemResolver {

    private static final String SOURCE = "CRAFTENGINE";
    private final boolean available;

    public CraftEngineResolver() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("CraftEngine");
        available = plugin != null && plugin.isEnabled();
    }

    @Override
    @Nullable
    public ItemStack resolve(String definition, int amount) {
        if (!available || definition == null || definition.isEmpty()) return null;

        try {
            Key key = Key.of(definition);

            Item craftItem = Item.byId(key);
            if (craftItem == null || craftItem.isEmpty()) return null;

            Object platformItem = craftItem.platformItem();
            ItemStack result = null;
            if (platformItem instanceof ItemStack) {
                result = (ItemStack) platformItem;
            } else {
                Object mcItem = craftItem.minecraftItem();
                if (mcItem instanceof ItemStack) {
                    result = (ItemStack) mcItem;
                }
            }

            if (result == null) return null;

            result = result.clone();
            result.setAmount(Math.min(amount, result.getMaxStackSize()));
            return result;
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