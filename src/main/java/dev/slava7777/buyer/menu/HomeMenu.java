package dev.slava7777.buyer.menu;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.factory.MenuCodecs;
import dev.by1337.bmenu.loader.MenuConfig;
import dev.by1337.bmenu.menu.AbstractMenu;
import dev.by1337.bmenu.menu.Menu;
import dev.by1337.cmd.Command;
import dev.by1337.edsl.context.EventContext;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.plc.Placeholders;
import dev.by1337.yaml.codec.PipelineYamlCodecBuilder;
import dev.by1337.yaml.codec.YamlCodec;
import dev.slava7777.buyer.DBuyer;
import dev.slava7777.buyer.buyer.BuyerItem;
import dev.slava7777.buyer.db.BuyerUser;
import dev.slava7777.buyer.registry.BuyerRegistry;
import dev.slava7777.buyer.util.EconomyUtil;
import dev.slava7777.buyer.util.InvUtil;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class HomeMenu extends AbstractMenu {

    private static final PlaceholderResolver<HomeMenu> PLACEHOLDERS = Placeholders.<HomeMenu>create()
            .withContext("money_price", m -> EconomyUtil.df.format(m.currentSellMoney))
            .withContext("points_price", m -> EconomyUtil.df.format(m.currentSellPoints))
            .withContext("multiplier", m -> EconomyUtil.df.format(m.user.getMultiplier()))
            .withContext("user_points", m -> EconomyUtil.df.format(m.user.getPoints()))
            ;

    private static Command<ExecuteContext> COMMANDS;

    private final HomeMenuConfig cfg;

    private final BuyerUser user;
    private final BuyerRegistry<BuyerItem> itemRegistry;

    private double currentSellMoney = 0;
    private double currentSellPoints = 0;
    private boolean needsUpdate = true;
    private long lastUpdateTime = 0;

    public HomeMenu(HomeMenuConfig config, Player viewer, Menu prevMenu) {
        super(config, viewer, prevMenu);
        this.cfg = config;
        this.user = DBuyer.plugin().userDao.getOrCreate(viewer.getUniqueId().toString());
        this.itemRegistry = DBuyer.plugin().registries.buyerItems;

        addPlaceholderResolver(PLACEHOLDERS.bindCtx(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (needsUpdate) {
            long now = System.currentTimeMillis();
            if (now - lastUpdateTime > cfg.tick_update * 50L) {
                updateSellTotal();
                needsUpdate = false;
                lastUpdateTime = now;
            }
        }
    }

    private void markDirty() {
        needsUpdate = true;
    }

    @Override
    public void refresh() {
        super.refresh();
    }

    @Override
    protected void generate() {

    }

    @Override
    public void syncItems() {
        inventoryLike.syncNoDelete();
    }

    private void sellItems() {
        double totalMoney = 0;
        double totalPoints = 0;
        int soldCount = 0;

        final int[] slotsToClear = new int[cfg.slots.length];
        int clearCount = 0;
        final Inventory inv = getInventory();

        for (final int slot : cfg.slots) {
            final ItemStack item = inv.getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;

            if (Tag.SHULKER_BOXES.isTagged(item.getType())) {
                final BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
                final ShulkerBox shulker = (ShulkerBox) meta.getBlockState();
                final Inventory shulkerInv = shulker.getInventory();
                boolean changed = false;

                for (int i = 0; i < shulkerInv.getSize(); i++) {
                    final ItemStack sub = shulkerInv.getItem(i);
                    if (sub == null || sub.getType() == Material.AIR) continue;
                    if (InvUtil.hasPDC(sub)) continue;
                    final BuyerItem bi = findBuyerItem(sub.getType());
                    if (bi != null) {
                        totalMoney += bi.money * sub.getAmount() * user.getMultiplier();
                        totalPoints += bi.points * sub.getAmount();
                        shulkerInv.setItem(i, null);
                        changed = true;
                        soldCount += sub.getAmount();
                    }
                }

                if (changed) {
                    final ItemStack newShulker = item.clone();
                    final BlockStateMeta newMeta = (BlockStateMeta) newShulker.getItemMeta();
                    newMeta.setBlockState(shulker);
                    newShulker.setItemMeta(newMeta);
                    inv.setItem(slot, newShulker);
                }
                continue;
            }

            if (InvUtil.hasPDC(item)) continue;
            final BuyerItem bi = findBuyerItem(item.getType());
            if (bi != null) {
                totalMoney += bi.money * item.getAmount() * user.getMultiplier();
                totalPoints += bi.points * item.getAmount();
                slotsToClear[clearCount++] = slot;
                soldCount += item.getAmount();
            }
        }

        for (int i = 0; i < clearCount; i++) {
            inv.setItem(slotsToClear[i], null);
        }

        if (soldCount > 0) {
            user.addPoints(totalPoints);
            DBuyer.plugin().economy.depositPlayer(viewer.getUniqueId(), totalMoney);
            DBuyer.plugin().userDao.save(user);

            DBuyer.sendMessage("sell_items", viewer, PlaceholderResolver.<EventContext>of("count", soldCount)
                    .append("total_money", EconomyUtil.df.format(totalMoney))
                    .append("total_points", EconomyUtil.df.format(totalPoints))
                    .append("multiplier", EconomyUtil.df.format(user.getMultiplier()))
            );
        } else {
            DBuyer.sendMessage("no_items", viewer);
        }
        refresh();
        markDirty();
    }

    private void updateSellTotal() {
        double money = 0;
        double points = 0;
        for (int slot : cfg.slots) {
            final ItemStack item = getInventory().getItem(slot);
            if (item == null || item.getType() == Material.AIR) continue;
            if (Tag.SHULKER_BOXES.isTagged(item.getType())) {
                final ShulkerBox shulker = (ShulkerBox) ((BlockStateMeta) item.getItemMeta()).getBlockState();
                final Inventory inv = shulker.getInventory();
                for (final ItemStack sub : inv.getContents()) {
                    if (sub == null || sub.getType() == Material.AIR) continue;
                    if (InvUtil.hasPDC(sub)) continue;
                    final BuyerItem bi = findBuyerItem(sub.getType());
                    if (bi != null) {
                        money += bi.money * sub.getAmount() * user.getMultiplier();
                        points += bi.points * sub.getAmount();
                    }
                }
                continue;
            }
            if (InvUtil.hasPDC(item)) continue;
            final BuyerItem bi = findBuyerItem(item.getType());
            if (bi != null) {
                money += bi.money * item.getAmount() * user.getMultiplier();
                points += bi.points * item.getAmount();
            }
        }
        currentSellMoney = money;
        currentSellPoints = points;
    }

    private void returnItems() {
        final Inventory inv = getInventory();

        final ItemStack[] items = new ItemStack[cfg.slots.length];
        int count = 0;
        for (final int slot : cfg.slots) {
            final ItemStack item = inv.getItem(slot);
            if (item != null && !item.getType().isAir()) {
                items[count++] = item.clone();
                inv.setItem(slot, null);
            }
        }
        if (count > 0) {
            final ItemStack[] actualItems = new ItemStack[count];
            System.arraycopy(items, 0, actualItems, 0, count);
            InvUtil.giveOrDrop(viewer, actualItems);
        }
    }

    @Nullable
    private BuyerItem findBuyerItem(Material material) {
        for (final BuyerItem bi : itemRegistry.values()) {
            if (bi.matches(material)) {
                return bi;
            }
        }
        return null;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() != null && e.getClickedInventory() != e.getView().getTopInventory()) {
            e.setCancelled(false);
            markDirty();
            return;
        }

        if (isPutSlot(e.getSlot())) {
            e.setCancelled(false);
            markDirty();
            return;
        }
        super.onClick(e);
    }

    @Override
    public void onClick(InventoryDragEvent e) {
        if (e.getNewItems().keySet().stream().allMatch(this::isPutSlot)) {
            e.setCancelled(false);
            return;
        }
        super.onClick(e);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        returnItems();
        super.onClose(event);
    }

    private boolean isPutSlot(int slot) {
        return Arrays.stream(cfg.slots).anyMatch(s -> s == slot);
    }

    static void bootCommands(Command<ExecuteContext> base) {
        COMMANDS = base
                .sub(new Command<ExecuteContext>("[sell_items]")
                        .alias("[SELL_ITEMS]")
                        .executor((ctx, args) -> {
                            if (!(ctx.menu.isOpened())) return;
                            if (ctx.menu instanceof HomeMenu home) {
                                home.sellItems();
                            }
                        })
                )
                .sub(new Command<ExecuteContext>("[return_items]")
                        .alias("[RETURN_ITEMS]")
                        .executor((ctx, args) ->{
                            if (!(ctx.menu.isOpened())) return;
                            if (ctx.menu instanceof HomeMenu home) {
                                home.returnItems();
                            }
                        })
                )
        ;
    }

    @Override
    public Command<ExecuteContext> getCommands() {
        return COMMANDS;
    }

    public static class HomeMenuConfig extends MenuConfig {
        public static final PipelineYamlCodecBuilder<HomeMenuConfig> RAW_CODEC = new PipelineYamlCodecBuilder<>(HomeMenuConfig::new)
                .and(MenuConfig.RAW_CODEC)
                .field(MenuCodecs.SLOTS_CODEC, "slots", v -> v.slots, (m, v) -> m.slots = v)
                .field(YamlCodec.INT, "tick_update", v -> v.tick_update, (m, v) -> m.tick_update = v, 20)
                ;
        public static final YamlCodec<HomeMenuConfig> CODEC = RAW_CODEC.build();

        public int[] slots;
        public int tick_update;

        @Override
        public Menu create(Player viewer, @Nullable Menu previousMenu) {
            return new HomeMenu(this, viewer, previousMenu);
        }
    }
}
