package dev.slava7777.buyer;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dev.by1337.bmenu.BMenu;
import dev.by1337.bmenu.loader.MenuSubLoader;
import dev.by1337.cmd.Command;
import dev.by1337.core.command.bcmd.CommandWrapper;
import dev.by1337.edsl.context.EventContext;
import dev.by1337.plc.PlaceholderResolver;
import dev.by1337.yaml.YamlMap;
import dev.slava7777.buyer.buyer.auto.AutoBuyScheduler;
import dev.slava7777.buyer.config.Config;
import dev.slava7777.buyer.db.BuyerUserDao;
import dev.slava7777.buyer.db.DataBase;
import dev.slava7777.buyer.eco.VaultHook;
import dev.slava7777.buyer.listener.JoinQuitListener;
import dev.slava7777.buyer.menu.HomeMenu;
import dev.slava7777.buyer.menu.MenuBooter;
import dev.slava7777.buyer.registry.BuyerRegistries;
import dev.slava7777.buyer.util.lib.LibrariesUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

public final class DBuyer extends JavaPlugin {

    private static DBuyer plugin;
    private MenuSubLoader subLoader;
    public VaultHook economy;
    private CommandWrapper cmdWrapper;
    public Config config;
    public final BuyerRegistries registries = new BuyerRegistries();
    public DataBase dataBase;
    public BuyerUserDao userDao;
    public AutoBuyScheduler scheduler;

    public DBuyer() {
        plugin = this;
        LibrariesUtil.boot();
    }

    @Override
    public void onLoad() {
        plugin = this;

        final var result = Config.DECODER.decode(ResourceUtil.load("config.yml", this).get(), this);
        config = result.result();
        if (config == null) {
            throw new RuntimeException(result.error());
        } else if (result.hasError()) {
            getSLF4JLogger().error("Failed to load cfg\n{}", result.error());
        }

        subLoader = new MenuSubLoader(new File(getDataFolder(), "menus"), this, BMenu.menuLoader());
        MenuBooter.boot(subLoader);
        BMenu.menuLoader().registerSubLoader(this, subLoader);

        final File bmHome = BMenu.menuLoader().homeDir();
        if (!new File(bmHome, "dbuyer").exists()) {
            ResourceUtil.saveIfNotExist("menu.txt", this);
            ResourceUtil.saveIfNotExist("menus/home.yml", this, new File(bmHome, "dbuyer/home.yml"));
            ResourceUtil.saveIfNotExist("menus/setting.yml", this, new File(bmHome, "dbuyer/setting.yml"));
        }

    }

    @Override
    public void onEnable() {

        economy = new VaultHook();

        config.boot(registries);

        registries.writeLock();

        dataBase = new DataBase(config.dbConfig);
        userDao = new BuyerUserDao(dataBase.getDataSource());

        scheduler = new AutoBuyScheduler(userDao);
        scheduler.start();
        getServer().getPluginManager().registerEvents(new JoinQuitListener(), this);

        cmdWrapper = new CommandWrapper(createCommand(), this);
        cmdWrapper.setAliases(config.commands.aliases);
        cmdWrapper.register();

    }

    @Override
    public void onDisable() {
        cmdWrapper.close();
        scheduler.stop();
        userDao.close();
        BMenu.menuLoader().unregisterSubLoader(this);
        plugin = null;
    }

    public static DBuyer plugin() {
        return plugin;
    }

    public static void sendMessage(String key, Player player, PlaceholderResolver<EventContext> c) {
        plugin().config.eventCtx.call(key, plugin().config.eventCtx.newContext().source(player).placeholders(c).build());
    }

    public static void sendMessage(String key, Player player) {
        plugin().config.eventCtx.call(key, player);
    }

    private Command<CommandSender> createCommand() {
        final var cmdConf = config.commands;
        return new Command<CommandSender>(cmdConf.rename("buyer"))
                .executor((s, args) -> open(s, cmdConf.buyer_menu))
                .sub(new Command<CommandSender>(cmdConf.rename("settings"))
                        .executor((s, args) -> open(s, cmdConf.setting_menu))
                );
    }

    private void open(CommandSender sender, String menuId) {
        if (sender instanceof Player player) {
            var menu = BMenu.menuLoader().create(menuId, player, null);
            if (menu instanceof HomeMenu) {
                menu.open();
            }
        }
    }

    public static class ResourceUtil {

        @NotNull
        @CanIgnoreReturnValue
        public static File saveIfNotExist(@NotNull String path, @NotNull Plugin plugin, File outFile) {
            path = path.replace('\\', '/');
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            if (!outFile.exists()) {
                outFile.getParentFile().mkdirs();
                try (
                        final FileOutputStream fw = new FileOutputStream(outFile);
                        final var in = plugin.getResource(path)
                ) {
                    in.transferTo(fw);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            return outFile;
        }

        @NotNull
        @CanIgnoreReturnValue
        public static File saveIfNotExist(@NotNull String path, @NotNull Plugin plugin) {
            path = path.replace('\\', '/');
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            final var f = new File(plugin.getDataFolder(), path);
            if (!f.exists()) {
                plugin.saveResource(path, false);
            }
            return f;
        }

        public static YamlMap load(@NotNull String path, @NotNull Plugin plugin) {
            return YamlMap.load(saveIfNotExist(path, plugin));
        }
    }
}
