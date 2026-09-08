package dev.slava7777.buyer.menu;

import dev.by1337.bmenu.command.ExecuteContext;
import dev.by1337.bmenu.menu.command.MenuCommands;
import dev.by1337.cmd.Command;

class Commands {

    private static final Command<ExecuteContext> MENU_COMMANDS = new Command<ExecuteContext>("root").and(MenuCommands.getCommands());

    public static Command<ExecuteContext> create() {
        return MENU_COMMANDS.copy();
    }
}
