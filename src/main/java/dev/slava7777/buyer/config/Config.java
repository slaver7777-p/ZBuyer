package dev.slava7777.buyer.config;

import dev.by1337.core.util.io.ResourceUtil;
import dev.by1337.edsl.MessageManager;
import dev.by1337.yaml.YamlMap;
import dev.by1337.yaml.decoder.RecordYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;
import dev.slava7777.buyer.buyer.BuyerItem;
import dev.slava7777.buyer.buyer.BuyerMultiplier;
import dev.slava7777.buyer.buyer.auto.BuyerStatus;
import dev.slava7777.buyer.buyer.category.Categories;
import dev.slava7777.buyer.buyer.sort.SortingBoot;
import dev.slava7777.buyer.registry.BuyerRegistries;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;

public record Config(MessageManager eventCtx, Categories categories, List<String> sorting, DbConfig dbConfig,
                     List<BuyerItem> buyerItems, CommandsConf commands, BuyerMultiplier buyerMultiplier) {

    public static final YamlDecoder<Config> DECODER = RecordYamlDecoder.mapOf(
            Config::new,
            MessageManager.decoder("messages.yml", "lang.yml").fieldOf(),
            readFile("categories.yml").and(Categories.DECODER).fieldOf(null, new Categories(Map.of())),
            YamlDecoder.STRING.listOf().fieldOf("sorting"),
            readFile("database.yml").and(DbConfig.DECODER).fieldOf(),
            //readFile("buyer_items.yml").and(BuyerItem.DECODER.listOf()).fieldOf("buyer_items"), //Какого-то хуя не работает
            readFile("buyer_items.yml").map(map ->
                    map.get("buyer_items").decode(BuyerItem.CODEC.listOf()).getOrThrow()
            ).fieldOf(),
            CommandsConf.DECODER.fieldOf("commands"),
            BuyerMultiplier.DECODER.fieldOf("multiplier")
    );

    public void boot(BuyerRegistries registries) {
        categories.boot(registries);
        SortingBoot.boot(registries.sorting, sorting);
        for (final BuyerItem bi : buyerItems) {
            bi.bake();
            registries.buyerItems.register(String.valueOf(bi.id()), bi);
        }
        BuyerStatus.boot(registries);
    }

    private static YamlDecoder<YamlMap> readFile(String name) {
        return YamlDecoder.fromContext(Plugin.class)
                .map(pl -> ResourceUtil.saveIfNotExist(name, pl))
                .and(YamlDecoder.READ_YAML_FROM_FILE);
    }
}
