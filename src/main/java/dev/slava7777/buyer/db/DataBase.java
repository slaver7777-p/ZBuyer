package dev.slava7777.buyer.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.slava7777.buyer.DBuyer;
import dev.slava7777.buyer.config.DbConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class DataBase {
    private static final Logger LOGGER = LoggerFactory.getLogger("DBuyer#DB");
    private final DataSource dataSource;

    public DataBase(DbConfig dbConfig) {
        final HikariConfig hikariConfig = new HikariConfig();
        String jdbcUrl;

        if ("sqlite".equalsIgnoreCase(dbConfig.type)) {
            String dbPath = new File(DBuyer.plugin().getDataFolder(), dbConfig.database).getAbsolutePath();
            jdbcUrl = "jdbc:sqlite:" + dbPath;
            if (dbConfig.sqlite.enableWAL) {
                jdbcUrl += "?journal_mode=WAL";
            }
            if (dbConfig.sqlite.enableForeignKeys) {
                jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "foreign_keys=ON";
            }
        } else if ("mysql".equalsIgnoreCase(dbConfig.type)) {
            jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&characterEncoding=utf8",
                    dbConfig.host, dbConfig.port, dbConfig.database);
            hikariConfig.setUsername(dbConfig.username);
            hikariConfig.setPassword(dbConfig.password);
        } else if ("mariadb".equalsIgnoreCase(dbConfig.type)) {
            jdbcUrl = String.format("jdbc:mariadb://%s:%d/%s?useSSL=false&characterEncoding=utf8",
                    dbConfig.host, dbConfig.port, dbConfig.database);
            hikariConfig.setUsername(dbConfig.username);
            hikariConfig.setPassword(dbConfig.password);
        } else {
            throw new IllegalStateException("Unsupported database type: " + dbConfig.type);
        }

        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setMaximumPoolSize(dbConfig.hikari.maximumPoolSize);
        hikariConfig.setMinimumIdle(dbConfig.hikari.minimumIdle);
        hikariConfig.setConnectionTimeout(dbConfig.hikari.connectionTimeout);
        hikariConfig.setIdleTimeout(dbConfig.hikari.idleTimeout);
        hikariConfig.setMaxLifetime(dbConfig.hikari.maxLifetime);
        hikariConfig.setPoolName("DBuyer-HikariPool");

        this.dataSource = new HikariDataSource(hikariConfig);

        try (final Connection ignored = dataSource.getConnection()) {
            LOGGER.info("Successfully connected to {} database!", dbConfig.type);
        } catch (SQLException e) {
            LOGGER.error("Failed to connect to database: {}", e.getMessage());
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
