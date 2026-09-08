package dev.slava7777.buyer.menu;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.loader.MenuSubLoader;
import dev.by1337.cmd.Command;

public class MenuBooter {

    public static void boot(MenuSubLoader subLoader) {
        Command<ExecuteContext> base = Commands.create();
        HomeMenu.bootCommands(base.copy());
        SettingsMenu.bootCommands(base.copy());

        subLoader.menuCodecRegistry().register("dbuyer:home", HomeMenu.HomeMenuConfig.CODEC);
        subLoader.menuCodecRegistry().register("dbuyer:settings", SettingsMenu.SettingMenuConfig.CODEC);
    }
}
