package org.phantam.fozminetimeditem.resolver;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for managing ItemResolver instances.
 * Automatically registers all available resolvers on class initialization.
 */
public class ItemResolverFactory {

    private static final Map<String, ItemResolver> RESOLVERS = new HashMap<>();

    static {
        registerResolver(new MMOItemsResolver());
        registerResolver(new VanillaResolver());
        registerResolver(new ItemsAdderResolver());
        registerResolver(new NexoResolver());
        registerResolver(new OraxenResolver());
        registerResolver(new CraftEngineResolver());
    }

    private ItemResolverFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Register a resolver. Only registers if the resolver is available.
     */
    private static void registerResolver(ItemResolver resolver) {
        if (resolver.isAvailable()) {
            RESOLVERS.put(resolver.getSource().toUpperCase(), resolver);
        }
    }

    /**
     * Get a resolver for a specific source.
     *
     * @param source The source name (e.g., "MMOITEM", "VANILLA")
     * @return The resolver, or null if none registered for this source
     */
    @Nullable
    public static ItemResolver getResolver(String source) {
        if (source == null) return null;
        return RESOLVERS.get(source.toUpperCase());
    }

    /**
     * Resolve an item from a full definition string.
     * Format: "SOURCE:TYPE:ID" or "SOURCE:ID"
     * Special case for ITEMSADDER: "ITEMSADDER:NAMESPACE:ID"
     *
     * @param fullDefinition The full definition (e.g., "MMOITEM:SWORD:IRON_SWORD" or "ITEMSADDER:MY_NAMESPACE:MY_ITEM")
     * @param amount Stack size
     * @return ItemStack or null if cannot resolve
     */
    @Nullable
    public static ItemStack resolve(String fullDefinition, int amount) {
        if (fullDefinition == null || fullDefinition.isEmpty()) {
            Bukkit.getLogger().warning("[FozmineTimedItem] Definition is null or empty");
            return null;
        }

        String[] parts = fullDefinition.split(":", 3);
        if (parts.length < 2) {
            Bukkit.getLogger().warning("[FozmineTimedItem] Invalid definition format (need at least 2 parts): " + fullDefinition);
            return null;
        }

        String source = parts[0].toUpperCase();
        String definition;

        if (source.equals("ITEMSADDER")) {
            if (parts.length == 3) {
                definition = parts[1] + ":" + parts[2];
            } else if (parts.length == 2) {
                definition = parts[1];
            } else {
                return null;
            }
        } else {
            definition = fullDefinition.substring(source.length() + 1);
        }

        if (definition == null || definition.isEmpty()) {
            Bukkit.getLogger().warning("[FozmineTimedItem] Definition part is empty for source: " + source);
            return null;
        }

        ItemResolver resolver = getResolver(source);
        if (resolver == null) {
            Bukkit.getLogger().warning("[FozmineTimedItem] No resolver for source: " + source);
            return null;
        }

        return resolver.resolve(definition, amount);
    }

    /**
     * Check if a full definition string is valid.
     */
    public static boolean isValidDefinition(String fullDefinition) {
        if (fullDefinition == null || fullDefinition.isEmpty()) return false;

        String[] parts = fullDefinition.split(":", 3);
        if (parts.length < 2) return false;

        String source = parts[0].toUpperCase();

        // Kiểm tra đặc biệt cho ITEMSADDER
        if (source.equals("ITEMSADDER")) {
            // Cần ít nhất 2 phần (ITEMSADDER:ID) hoặc 3 phần (ITEMSADDER:NAMESPACE:ID)
            if (parts.length < 2) return false;
            // Kiểm tra ID không rỗng
            String id = parts.length == 2 ? parts[1] : parts[1] + ":" + parts[2];
            if (id == null || id.isEmpty()) return false;
        }

        ItemResolver resolver = getResolver(source);
        return resolver != null && resolver.isAvailable();
    }

    /**
     * Get all registered sources.
     */
    public static java.util.Set<String> getRegisteredSources() {
        return RESOLVERS.keySet();
    }
}