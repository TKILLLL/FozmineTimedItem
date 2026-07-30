package org.phantam.fozminetimeditem.resolver;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for resolving items from various plugins.
 * Each plugin should have its own implementation.
 */
public interface ItemResolver {

    /**
     * Resolve an item from a definition string.
     *
     * @param definition The definition (e.g., "SWORD:IRON_SWORD" for MMOItems, "DIAMOND" for Vanilla)
     * @param amount Stack size
     * @return ItemStack or null if cannot resolve
     */
    @Nullable
    ItemStack resolve(String definition, int amount);

    /**
     * Get the source name this resolver handles (e.g., "MMOITEM", "VANILLA").
     */
    String getSource();

    /**
     * Check if this resolver is available (plugin is loaded and enabled).
     */
    boolean isAvailable();
}