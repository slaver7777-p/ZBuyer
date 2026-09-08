package dev.slava7777.buyer.db;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.slava7777.buyer.DBuyer;
import dev.slava7777.buyer.buyer.auto.BuyerStatus;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class BuyerUserDao {
    private static final Logger LOGGER = LoggerFactory.getLogger("DBuyer#DAO");
    private final DataSource dataSource;
    private final Cache<String, BuyerUser> cache;
    private final Gson gson = new GsonBuilder().create();
    private final Queue<Operation> operations = new LinkedList<>();
    private final BukkitTask flushTask;

    public BuyerUserDao(DataSource dataSource) {
        this.dataSource = dataSource;
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build();
        createTableIfNotExists();

        flushTask = Bukkit.getScheduler().runTaskTimerAsynchronously(DBuyer.plugin(), this::flush, 40L, 40L);
    }

    private void createTableIfNotExists() {
        final String sql = "CREATE TABLE IF NOT EXISTS buyer_users (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "points DOUBLE DEFAULT 0," +
                "total_points DOUBLE DEFAULT 0," +
                "multiplier FLOAT DEFAULT 1.0," +
                "custom_multiplier BOOLEAN DEFAULT FALSE," +
                "auto_buy_mode VARCHAR(20) DEFAULT 'DISABLED'," +
                "auto_buy_items TEXT" +
                ")";
        try (final Connection conn = dataSource.getConnection();
             final Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            LOGGER.error("Failed to create table: {}", e.getMessage());
        }
    }

    public BuyerUser load(String uuid) {
        final BuyerUser cached = cache.getIfPresent(uuid);
        if (cached != null) return cached;

        final String sql = "SELECT points, total_points, multiplier, custom_multiplier, auto_buy_mode, auto_buy_items FROM buyer_users WHERE uuid = ?";
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            final ResultSet rs = ps.executeQuery();
            BuyerUser user;
            if (rs.next()) {
                user = new BuyerUser(uuid);
                user.setPoints(rs.getDouble("points"));
                user.setTotalPoints(rs.getDouble("total_points"));
                user.setMultiplier(rs.getFloat("multiplier"));
                user.setCustomMultiplier(rs.getBoolean("custom_multiplier"));
                user.setBuyerStatus(BuyerStatus.valueOf(rs.getString("auto_buy_mode")));
                final String itemsJson = rs.getString("auto_buy_items");
                if (itemsJson != null && !itemsJson.isEmpty()) {
                    final String[] items = gson.fromJson(itemsJson, String[].class);
                    for (final String itemId : items) {
                        user.getAutoBuyItemIds().add(itemId);
                    }
                }
            } else {
                user = new BuyerUser(uuid);
            }
            cache.put(uuid, user);
            return user;
        } catch (SQLException e) {
            LOGGER.error("Failed to load user {}: {}", uuid, e.getMessage());
            return new BuyerUser(uuid);
        }
    }

    public void save(BuyerUser user) {
        operations.offer(new Operation(user, OperationType.SAVE));
    }

    public void saveSync(BuyerUser user) {
        executeSave(user);
    }

    private void executeSave(BuyerUser user) {
        final String sql = "INSERT OR REPLACE INTO buyer_users (uuid, points, total_points, multiplier, custom_multiplier, auto_buy_mode, auto_buy_items) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUuid());
            ps.setDouble(2, user.getPoints());
            ps.setDouble(3, user.getTotalPoints());
            ps.setFloat(4, user.getMultiplier());
            ps.setBoolean(5, user.isCustomMultiplier());
            ps.setString(6, user.getBuyerStatus().name());
            final String itemsJson = gson.toJson(user.getAutoBuyItemIds().toArray(new String[0]));
            ps.setString(7, itemsJson);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to save user {}: {}", user.getUuid(), e.getMessage());
        }
    }

    private void flush() {
        if (operations.isEmpty()) return;
        Operation op;
        while ((op = operations.poll()) != null) {
            if (op.type == OperationType.SAVE) {
                executeSave(op.user);
            }
        }
    }

    public BuyerUser getOrCreate(String uuid) {
        return load(uuid);
    }

    public void invalidate(String uuid) {
        cache.invalidate(uuid);
    }

    public BuyerUser getIfLoaded(String uuid) {
        return cache.getIfPresent(uuid);
    }

    public void saveAll() {
        flush();
        for (final BuyerUser user : cache.asMap().values()) {
            executeSave(user);
        }
    }

    public void close() {
        if (flushTask != null) {
            flushTask.cancel();
        }
        saveAll();
    }

    private record Operation(BuyerUser user, OperationType type) {}

    private enum OperationType {
        SAVE
    }
}