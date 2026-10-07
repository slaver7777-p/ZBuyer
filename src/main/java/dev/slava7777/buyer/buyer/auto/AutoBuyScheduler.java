package dev.slava7777.buyer.buyer.auto;

import dev.by1337.edsl.context.EventContext;
import dev.by1337.plc.PlaceholderResolver;
import dev.slava7777.buyer.DBuyer;
import dev.slava7777.buyer.buyer.BuyerItem;
import dev.slava7777.buyer.db.BuyerUser;
import dev.slava7777.buyer.db.BuyerUserDao;
import dev.slava7777.buyer.registry.BuyerRegistries;
import dev.slava7777.buyer.util.EconomyUtil;
import dev.slava7777.buyer.util.InvUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class AutoBuyScheduler extends BukkitRunnable {

    private final BuyerUserDao userDao;

    public AutoBuyScheduler(BuyerUserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void run() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final BuyerUser user = userDao.getOrCreate(player.getUniqueId().toString());
            if (!user.isAutoBuyEnabled()) continue;

            final Set<BuyerItem> autoItems = new HashSet<>();
            for (final int id : user.getAutoBuyItemIds()) {
                final BuyerItem item = DBuyer.plugin().registries.buyerItems.getByName(String.valueOf(id));
                if (item != null) autoItems.add(item);
            }
            if (autoItems.isEmpty()) continue;

            final ItemStack[] contents = player.getInventory().getContents();
            if (contents.length == 0) continue;

            double totalMoney = 0;
            double totalPoints = 0;
            int soldCount = 0;

            final float mul = user.getMultiplier();
            for (final ItemStack stack : contents) {
                if (stack == null || stack.getType().isAir()) continue;
                if (InvUtil.hasPDC(stack)) continue;

                for (final BuyerItem bi : autoItems) {
                    if (bi.matches(stack.getType())) {
                        final int amount = stack.getAmount();
                        totalMoney += bi.money * amount * mul;
                        totalPoints += bi.points * amount;
                        soldCount += amount;
                        stack.setAmount(0);
                        break;
                    }
                }
            }

            if (soldCount > 0) {
                user.addPoints(totalPoints);
                DBuyer.plugin().economy.depositPlayer(player.getUniqueId(), totalMoney);
                userDao.save(user);

                DBuyer.sendMessage("auto_sold", player,
                        PlaceholderResolver.<EventContext>of("count", soldCount)
                                .append("total_money", EconomyUtil.format(totalMoney))
                                .append("total_points", EconomyUtil.format(totalPoints))
                                .append("multiplier", EconomyUtil.format(user.getMultiplier()))
                );
            }
        }
    }

    public void start() {
        this.runTaskTimerAsynchronously(DBuyer.plugin(), 20L, 80L);
    }

    public void stop() {
        super.cancel();
    }
}
