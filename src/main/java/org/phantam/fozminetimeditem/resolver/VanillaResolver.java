package org.phantam.fozminetimeditem.resolver;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

public class VanillaResolver implements ItemResolver {

    private static final String SOURCE = "VANILLA";

    @Override
    @Nullable
    public ItemStack resolve(String definition, int amount) {
        if (definition == null || definition.isEmpty()) return null;

        String[] parts = definition.split(":", 3);
        String materialName = parts[0].toUpperCase();
        Material mat = Material.getMaterial(materialName);
        if (mat == null || !mat.isItem()) return null;

        ItemStack item = new ItemStack(mat);
        item.setAmount(Math.min(amount, mat.getMaxStackSize()));

        if (parts.length == 2 && !parts[1].isEmpty()) {
            try {
                int customModelData = Integer.parseInt(parts[1]);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setCustomModelData(customModelData);
                    item.setItemMeta(meta);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return item;
    }

    @Override
    public String getSource() {
        return SOURCE;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}