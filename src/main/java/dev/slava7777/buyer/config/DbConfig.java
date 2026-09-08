package dev.slava7777.buyer.config;

import dev.by1337.yaml.decoder.PipelineYamlDecoder;
import dev.by1337.yaml.decoder.YamlDecoder;

public class DbConfig {
    public String type;
    public String host;
    public int port;
    public String database;
    public String username;
    public String password;
    public HikariSettings hikari = new HikariSettings();
    public SQLiteSettings sqlite = new SQLiteSettings();

    public static final YamlDecoder<DbConfig> DECODER = new PipelineYamlDecoder<>(DbConfig::new)
            .field(YamlDecoder.STRING, "type", (c, v) -> c.type = v)
            .field(YamlDecoder.STRING, "host", (c, v) -> c.host = v)
            .field(YamlDecoder.INT, "port", (c, v) -> c.port = v)
            .field(YamlDecoder.STRING, "database", (c, v) -> c.database = v)
            .field(YamlDecoder.STRING, "username", (c, v) -> c.username = v)
            .field(YamlDecoder.STRING, "password", (c, v) -> c.password = v)
            .field(HikariSettings.DECODER, "hikari", (c, v) -> c.hikari = v)
            .field(SQLiteSettings.DECODER, "sqlite", (c, v) -> c.sqlite = v)
            .build();

    public static class HikariSettings {
        public int maximumPoolSize = 10;
        public int minimumIdle = 10;
        public long connectionTimeout = 30000;
        public long idleTimeout = 600000;
        public long maxLifetime = 1800000;

        public static final YamlDecoder<HikariSettings> DECODER = new PipelineYamlDecoder<>(HikariSettings::new)
                .field(YamlDecoder.INT, "maximum-pool-size", (s, v) -> s.maximumPoolSize = v)
                .field(YamlDecoder.INT, "minimum-idle", (s, v) -> s.minimumIdle = v)
                .field(YamlDecoder.LONG, "connection-timeout", (s, v) -> s.connectionTimeout = v)
                .field(YamlDecoder.LONG, "idle-timeout", (s, v) -> s.idleTimeout = v)
                .field(YamlDecoder.LONG, "max-lifetime", (s, v) -> s.maxLifetime = v)
                .build();
    }

    public static class SQLiteSettings {
        public boolean enableWAL = true;
        public boolean enableForeignKeys = true;

        public static final YamlDecoder<SQLiteSettings> DECODER = new PipelineYamlDecoder<>(SQLiteSettings::new)
                .field(YamlDecoder.BOOL, "enable-wal", (s, v) -> s.enableWAL = v)
                .field(YamlDecoder.BOOL, "enable-foreign-keys", (s, v) -> s.enableForeignKeys = v)
                .build();
    }
}