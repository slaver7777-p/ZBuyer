package dev.slava7777.buyer.util;

import dev.by1337.core.BCore;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.Collection;

public class InvUtil {

    public static boolean giveOrDrop(Player player, ItemStack... items) {
        if (items.length == 0) return false;
        final Collection<ItemStack> leftover = !canGiveItems(player) ? Arrays.asList(items) : player.getInventory().addItem(items).values();
        for (final ItemStack value : leftover) {
            player.getWorld().dropItemNaturally(player.getLocation(), value);
        }
        return leftover.isEmpty();
    }

    public static boolean canGiveItems(Player player){
        return !player.isDead() && player.isOnline();
    }

    public static boolean tryAddFully(ItemStack item, Inventory to, boolean dryRun) {
        return tryAddFully(item, to, to.getSize(), dryRun);
    }

    public static boolean tryAddFully(ItemStack item, PlayerInventory to, boolean dryRun) {
        return tryAddFully(item, to, 36, dryRun);
    }

    public static boolean tryAddFully(ItemStack item, Inventory to, int size, boolean dryRun) {
        if (item.isEmpty()) return true;
        int maxStack = item.getMaxStackSize();
        int count = item.getAmount();
        final Int2IntArrayMap map = new Int2IntArrayMap();
        for (int slot = 0; slot < size; slot++) {
            if (count == 0) break;
            var current = to.getItem(slot);
            int base;
            if (current == null) {
                base = 0;
            } else if (current.isSimilar(item)) {
                base = current.getAmount();
            } else {
                continue;
            }
            int x = Math.min(maxStack - base, count);
            if (x != 0) {
                count -= x;
                map.put(slot, x + base);
            }
        }
        if (dryRun) return count == 0;
        if (count != 0) return false;
        for (Int2IntMap.Entry entry : map.int2IntEntrySet()) {
            var i = to.getItem(entry.getIntKey());
            if (i != null) {
                i.setAmount(entry.getIntValue());
            } else {
                to.setItem(entry.getIntKey(), item.asQuantity(entry.getIntValue()));
            }
        }
        BCore.getItemStackSerializer().
        return true;
    }

    public static boolean hasPDC(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return !item.getItemMeta().getPersistentDataContainer().getKeys().isEmpty();
    }
}
