package dev.slava7777.buyer.buyer.category;

import dev.by1337.yaml.decoder.RecordYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;
import dev.slava7777.buyer.registry.BuyerRegistries;
import dev.slava7777.buyer.util.CyclicListIterator;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class Categories {

    public static final YamlDecoder<Categories> DECODER = RecordYamlDecoder.mapOf(
            Categories::new,
            YamlDecoder.mapOf(YamlDecoder.STRING, Category.DECODER)
                    .fieldOf("categories")
    );

    private final Map<String, Category> categories;
    private final List<Category> list;

    public Categories(Map<String, Category> categories) {
        if (categories.isEmpty()) {
            categories = Map.of("mob-loots", new Category("mob-loots", "Лут с мобов"));
        }
        this.categories = categories;
        list = List.copyOf(categories.values());
    }

    public void boot(BuyerRegistries registries) {
        for (final Category category : list) {
            registries.categories.register(category.getId(), category);
        }
    }

    public @Nullable Category getById(String id) {
        return categories.get(id);
    }

    public CyclicListIterator<Category> cycle() {
        return new CyclicListIterator<>(list);
    }

    public List<Category> list() {
        return list;
    }
}