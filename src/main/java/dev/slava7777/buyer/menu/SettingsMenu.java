package dev.slava7777.buyer.menu;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.menu.AbstractMenu;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.bmenu.slot.SlotContent;
import dev.by1337.bmenu.slot.SlotFactory;
import dev.by1337.cmd.Command;
import dev.by1337.cmd.CommandMsgError;
import dev.by1337.cmd.argument.ArgumentString;
import dev.by1337.item.ItemModel;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.plc.Placeholders;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.RecordYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.slava7777.buyer.DBuyer;
import dev.slava7777.buyer.buyer.BuyerItem;
import dev.slava7777.buyer.buyer.auto.BuyerStatus;
import dev.slava7777.buyer.buyer.category.Category;
import dev.slava7777.buyer.buyer.sort.Sorting;
import dev.slava7777.buyer.db.BuyerUser;
import dev.slava7777.buyer.registry.BuyerRegistries;
import dev.slava7777.buyer.registry.BuyerRegistry;
import dev.slava7777.buyer.util.CyclicListIterator;
import dev.slava7777.buyer.util.EconomyUtil;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SettingsMenu extends AbstractMenu {

    private static final PlaceholderResolver<SettingsMenu> PLACEHOLDERS = Placeholders.<SettingsMenu>create()
            .withContext("current_category_id", m -> m.category.getId())
            .withContext("current_page", m -> m.currentPage + 1)
            .withContext("max_page", m -> m.maxPage + 1)
            .withContext("has_next_page", v -> v.hasNextPage)
            .withContext("all_sorting", m -> {
                final StringBuilder sb = new StringBuilder();
                final var cfg = m.cfg.sortingLine;
                if (cfg == null) return "has no sorting_line in config!";
                final var selected = m.sorting;
                for (final var sort : m.sortingIterator.list()) {
                    if (sort == selected) {
                        sb.append(cfg.if_selected.replace("{id}", sort.id()));
                    } else {
                        sb.append(cfg.if_not_selected.replace("{id}", sort.id()));
                    }
                    sb.append("\n");
                }
                if (sb.isEmpty()) return "";
                sb.setLength(sb.length() - 1);
                return sb.toString();
            })
            .withContext("all_autobuy_modes", m -> {
                final StringBuilder sb = new StringBuilder();
                final var cfg = m.cfg.autobuyLine;
                if (cfg == null) return "has no autobuy_line in config!";
                final var selected = m.user.getBuyerStatus();
                for (final BuyerStatus mode : m.buyerStatusIterator.list()) {
                    if (mode == selected) {
                        sb.append(cfg.if_selected.replace("{id}", mode.getId()));
                    } else {
                        sb.append(cfg.if_not_selected.replace("{id}", mode.getId()));
                    }
                    sb.append("\n");
                }
                if (sb.isEmpty()) return "";
                sb.setLength(sb.length() - 1);
                return sb.toString();
            })
            ;


    protected int currentPage = 0;
    protected int maxPage = 0;
    private boolean hasNextPage;
    private final int itemsPerPage;

    private static Command<ExecuteContext> COMMANDS;

    private final BuyerUser user;
    private final BuyerRegistry<Category> categoryRegistry;
    private final BuyerRegistry<Sorting> sortingRegistry;
    private final BuyerRegistry<BuyerItem> itemRegistry;
    private final BuyerRegistry<BuyerStatus> buyerStatusRegistry;

    private final CyclicListIterator<BuyerStatus> buyerStatusIterator;
    private final CyclicListIterator<Category> categoryIterator;
    private Category category;
    private final CyclicListIterator<Sorting> sortingIterator;
    private Sorting sorting;
    private List<BuyerItem> currentItems = new ArrayList<>();
    private final SettingMenuConfig cfg;

    private final Int2IntMap materialIndex = new Int2IntLinkedOpenHashMap();
    private long tickCounter = 0;

    public SettingsMenu(SettingMenuConfig config, Player viewer, Menu prevMenu) {
        super(config, viewer, prevMenu);
        cfg = config;
        user = DBuyer.plugin().userDao.getOrCreate(viewer.getUniqueId().toString());

        final BuyerRegistries registries = DBuyer.plugin().registries;
        this.categoryRegistry = registries.categories;
        this.sortingRegistry = registries.sorting;
        this.itemRegistry = registries.buyerItems;
        this.buyerStatusRegistry = registries.buyerStatuses;

        this.buyerStatusIterator = buyerStatusRegistry.cycle();
        buyerStatusIterator.setPosOf(user.getBuyerStatus());

        this.categoryIterator = categoryRegistry.cycle();
        this.category = categoryIterator.current();
        this.sortingIterator = sortingRegistry.cycle();
        this.sorting = sortingIterator.current();

        this.itemsPerPage = cfg.slots.length;
        updateItems();

        addPlaceholderResolver(PLACEHOLDERS.bindCtx(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (++tickCounter % cfg.tick_update == 0) {
            for (final int slot : cfg.slots) {
                final SlotContent content = findItemInSlot(slot);
                if (content == null) continue;
                final Object payload = content.getPayload();
                if (!(payload instanceof BuyerItem item)) continue;
                if (!item.isGrouped()) continue;

                int idx = materialIndex.getOrDefault(slot, 0);
                idx = (idx + 1) % item.materials.size();
                materialIndex.put(slot, idx);

                final ItemModel model = ItemModel.ofMaterial(item.materials.get(idx));

                final boolean enabled = user.hasAutoBuyItem(String.valueOf(item.id));

                final SlotContent newSlot = cfg.itemDisplay.build(model, PlaceholderResolver.of("price", EconomyUtil.df.format(item.money))
                        .append("points", EconomyUtil.df.format(item.points))
                        .append("total_price", EconomyUtil.df.format(user.getMultiplier() * item.money))
                        .append("status", enabled ? "<lang:dbuyer.autobuy.enable.status>" : "<lang:dbuyer.autobuy.disable.status>")
                        .bindCtx(this));
                newSlot.setPayload(item);
                setItem(newSlot, slot);
            }
        }
    }

    @Override
    public void refresh() {
        super.refresh();
    }

    @Override
    protected void generate() {
        final int start = currentPage * itemsPerPage;
        for (int i = 0; i < itemsPerPage && start + i < currentItems.size(); i++) {
            final BuyerItem item = currentItems.get(start + i);
            final ItemModel model = ItemModel.ofMaterial(item.materials.getFirst());

            final boolean enabled = user.hasAutoBuyItem(String.valueOf(item.id));

            final SlotContent slot = cfg.itemDisplay.build(model, PlaceholderResolver.of("price", EconomyUtil.df.format(item.money))
                    .append("points", EconomyUtil.df.format(item.points))
                    .append("total_price", EconomyUtil.df.format(user.getMultiplier() * item.money))
                    .append("status", enabled ? "<lang:dbuyer.autobuy.enable.status>" : "<lang:dbuyer.autobuy.disable.status>")
                    .bindCtx(this));
            slot.setPayload(item);
            setItem(slot, cfg.slots[i]);

            if (item.isGrouped()) {
                materialIndex.put(cfg.slots[i], 0);
            }
        }
    }

    private void updateItems() {
        final List<BuyerItem> items = new ArrayList<>();
        for (final BuyerItem bi : itemRegistry.values()) {
            if (bi.category.equals(category.getId())) {
                items.add(bi);
            }
        }
        items.sort(sorting.comparator());
        currentItems = items;
        maxPage = Math.max(0, (currentItems.size() - 1) / itemsPerPage);
        if (currentPage > maxPage) currentPage = maxPage;
        hasNextPage = currentPage < maxPage;
    }

    private void enableAllInCategory() {
        final String catId = category.getId();
        for (final BuyerItem item : itemRegistry.values()) {
            if (item.category.equals(catId)) {
                user.enableAutoBuyItem(String.valueOf(item.id));
            }
        }
        DBuyer.plugin().userDao.save(user);
        refresh();
    }

    private void disableAllInCategory() {
        final String catId = category.getId();
        for (BuyerItem item : itemRegistry.values()) {
            if (item.category.equals(catId)) {
                user.disableAutoBuyItem(String.valueOf(item.id));
            }
        }
        DBuyer.plugin().userDao.save(user);
        refresh();
    }

    public void selectCategory(Category category) {
        this.category = category;
        updateItems();
        refresh();
    }

    public void nextSorting() {
        sorting = sortingIterator.next();
        updateItems();
        refresh();
    }

    public void previousSorting() {
        sorting = sortingIterator.previous();
        updateItems();
        refresh();
    }

    public void nextStatus() {
        user.setBuyerStatus(buyerStatusIterator.next());
        DBuyer.plugin().userDao.save(user);
        refresh();
    }

    public void previousStatus() {
        user.setBuyerStatus(buyerStatusIterator.previous());
        DBuyer.plugin().userDao.save(user);
        refresh();
    }

    static void bootCommands(Command<ExecuteContext> base) {
        COMMANDS = base
                .sub(new Command<ExecuteContext>("[select_category]")
                        .alias("[SELECT_CATEGORY]")
                        .executor(new ArgumentString<>("categoryId"), (ctx, category) -> {
                            if (category == null) throw new CommandMsgError("use: [select_category] <id>");
                            if (ctx.menu instanceof SettingsMenu s) {
                                for (final Category cat : s.categoryIterator.list()) {
                                    if (cat.getId().equalsIgnoreCase(category)) {
                                        s.selectCategory(cat);
                                    }
                                }
                            }
                        }))
                .sub(new Command<ExecuteContext>("[next_sorting]")
                        .alias("[NEXT_SORTING]")
                        .executor(ctx -> {
                            if (ctx.menu instanceof SettingsMenu s) s.nextSorting();
                        }))
                .sub(new Command<ExecuteContext>("[previous_sorting]")
                        .alias("[PREVIOUS_SORTING]")
                        .executor(ctx -> {
                            if (ctx.menu instanceof SettingsMenu s) s.previousSorting();
                        }))
                .sub(new Command<ExecuteContext>("[next_buyer_mode]")
                        .alias("[NEXT_BUYER_MODE]")
                        .executor(ctx -> {
                            if (ctx.menu instanceof SettingsMenu s) s.nextStatus();
                        }))
                .sub(new Command<ExecuteContext>("[previous_buyer_mode]")
                        .alias("[PREVIOUS_BUYER_MODE]")
                        .executor(ctx -> {
                            if (ctx.menu instanceof SettingsMenu s) s.previousStatus();
                        }))
                .sub(new Command<ExecuteContext>("[toggle_autobuy]")
                        .alias("[TOGGLE_AUTOBUY]")
                        .executor(ctx -> {
                            if (ctx.menu instanceof SettingsMenu s) {
                                Object payload = s.lastClickedItemPayload();
                                if (payload instanceof BuyerItem item) {
                                    s.user.toggleAutoBuyItem(String.valueOf(item.id));
                                    DBuyer.plugin().userDao.save(s.user);
                                    s.refresh();
                                }
                            }
                        }))
                .sub(new Command<ExecuteContext>("[next_page]")
                        .alias("[NEXT_PAGE]")
                        .executor(ctx -> {
                            if (ctx.menu instanceof SettingsMenu s && s.hasNextPage) {
                                s.currentPage++;
                                s.refresh();
                            }
                        }))
                .sub(new Command<ExecuteContext>("[previous_page]")
                        .alias("[PREVIOUS_PAGE]")
                        .executor(ctx -> {
                            if (ctx.menu instanceof SettingsMenu s && s.currentPage > 0) {
                                s.currentPage--;
                                s.refresh();
                            }
                        }))
                .sub(new Command<ExecuteContext>("[enable_all]")
                        .alias("[ENABLE_ALL]")
                        .executor(ctx -> {
                            if (ctx.menu instanceof SettingsMenu s) {
                                s.enableAllInCategory();
                            }
                        }))
                .sub(new Command<ExecuteContext>("[disable_all]")
                        .alias("DISABLE_ALL")
                        .executor(ctx -> {
                            if (ctx.menu instanceof SettingsMenu s) {
                                s.disableAllInCategory();
                            }
                        }))
        ;
    }

    @Override
    public Command<ExecuteContext> getCommands() {
        return COMMANDS;
    }

    public static class SettingMenuConfig extends HomeMenu.HomeMenuConfig {
        public static final YamlCodec<SettingMenuConfig> CODEC = new PipelineYamlCodecBuilder<>(SettingMenuConfig::new)
                .and(HomeMenu.HomeMenuConfig.RAW_CODEC)
                .field(SlotFactory.CODEC, "buyer-item", v -> v.itemDisplay, (c, v) -> c.itemDisplay = v)
                .field(DataLine.CODEC, "autobuy_line", v -> v.autobuyLine, (c, v) -> c.autobuyLine = v)
                .field(DataLine.CODEC, "sorting_line", v -> v.sortingLine, (c, v) -> c.sortingLine = v)
                .build();

        public SlotFactory itemDisplay;
        public DataLine autobuyLine;
        public DataLine sortingLine;

        @Override
        public Menu create(Player viewer, @Nullable Menu previousMenu) {
            return new SettingsMenu(this, viewer, previousMenu);
        }

        public record DataLine(String if_selected, String if_not_selected) {
            public static final YamlCodec<DataLine> CODEC = RecordYamlCodecBuilder.mapOf(
                    DataLine::new,
                    YamlCodec.STRING.fieldOf("if_selected", v -> v.if_selected),
                    YamlCodec.STRING.fieldOf("else", v -> v.if_not_selected)
            );
        }
    }
}
