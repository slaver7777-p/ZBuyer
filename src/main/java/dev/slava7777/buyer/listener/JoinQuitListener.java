package dev.slava7777.buyer.listener;

import dev.slava7777.buyer.DBuyer;
import dev.slava7777.buyer.buyer.auto.BuyerStatus;
import dev.slava7777.buyer.db.BuyerUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        final String uuid = player.getUniqueId().toString();
        DBuyer.plugin().userDao.load(uuid);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        final String uuid = player.getUniqueId().toString();

        final BuyerUser user = DBuyer.plugin().userDao.getIfLoaded(uuid);
        if (user != null) {
            if (user.getBuyerStatus().equals(BuyerStatus.TEMP_ENABLED)) {
                user.setBuyerStatus(BuyerStatus.DISABLED);
            }
            DBuyer.plugin().userDao.save(user);
        }
    }
}
