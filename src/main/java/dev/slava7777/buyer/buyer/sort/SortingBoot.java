package dev.slava7777.buyer.buyer.sort;

import dev.slava7777.buyer.buyer.BuyerItem;
import dev.slava7777.buyer.registry.BuyerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortingBoot {
    private static final Map<String, Sorting> map = new HashMap<>();
    private static final Sorting CHEAPER = make("cheaper", Comparator.comparingDouble(v -> v.money));
    private static final Sorting EXPENSIVE = make("expensive", CHEAPER.comparator().reversed());
    private static final Logger log = LoggerFactory.getLogger(SortingBoot.class);

    public static void boot(BuyerRegistry<Sorting> registry, List<String> usedSort) {
        for (final String s : usedSort) {
            final var sorting = map.get(s);
            if (sorting == null){
                log.error("Unknown sorting {}", s);
                continue;
            }
            registry.register(sorting.id(), sorting);
        }
        if (registry.size() == 0) {
            registry.register(EXPENSIVE.id(), EXPENSIVE);
        }
    }

    private static Sorting make(String key, Comparator<BuyerItem> comparator) {
        final Comparator<BuyerItem> stable = comparator.thenComparingInt(BuyerItem::id);
        final Sorting s = new Sorting(key, stable);
        map.put(key, s);
        return s;
    }
}