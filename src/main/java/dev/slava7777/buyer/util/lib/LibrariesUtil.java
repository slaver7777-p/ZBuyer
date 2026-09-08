package dev.slava7777.buyer.util.lib;

import dev.by1337.core.util.reflect.ClasspathUtil;
import dev.slava7777.buyer.DBuyer;

import java.nio.file.Path;

public class LibrariesUtil {
    private static final Path libPath = DBuyer.plugin().getDataFolder().toPath().resolve(".libraries");

    public static void boot() {
        if (!hasClass("org.mariadb.jdbc.Driver")) {
            ClasspathUtil.addUrl(DBuyer.plugin(),
                    BuyerRepositoryUtil.download(BuyerRepositoryUtil.MAVEN_REPO, "org.mariadb.jdbc:mariadb-java-client:3.5.2", libPath)
            );
        }
        if (!hasClass("com.zaxxer.hikari.HikariConfig")) {
            ClasspathUtil.addUrl(DBuyer.plugin(),
                    BuyerRepositoryUtil.download(BuyerRepositoryUtil.MAVEN_REPO, "com.zaxxer:HikariCP:7.0.2", libPath)
            );
        }
        if (!hasClass("org.sqlite.JDBC")) {
            ClasspathUtil.addUrl(DBuyer.plugin(),
                    BuyerRepositoryUtil.download(BuyerRepositoryUtil.MAVEN_REPO, "org.xerial:sqlite-jdbc:3.53.2.1", libPath)
            );
        }
        if (!hasClass("com.github.benmanes.caffeine.cache.Caffeine")) {
            ClasspathUtil.addUrl(DBuyer.plugin(),
                    BuyerRepositoryUtil.download(BuyerRepositoryUtil.MAVEN_REPO, "com.github.ben-manes.caffeine:caffeine:3.2.3", libPath)
            );
        }
    }

    private static boolean hasClass(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
