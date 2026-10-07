package dev.slava7777.buyer.menu.cache;

import dev.by1337.item.ItemModel;
import org.bukkit.Material;

import java.util.EnumMap;

public final class ModelCache {

    private static final EnumMap<Material, ItemModel> CACHE = new EnumMap<>(Material.class);

    private ModelCache() {
    }

    public static ItemModel getOrCache(Material material) {
        ItemModel cached = CACHE.get(material);
        if (cached != null) return cached;
        cached = ItemModel.ofMaterial(material.name());
        CACHE.put(material, cached);
        return cached;
    }

    public static void invalidate() {
        CACHE.clear();
    }
}
