package com.pvz.enums.commands;

public enum ShopMenuCommands implements MenuCommand {
    ENTER_MENU("^shop\\s+list$"),
    SHOW_DAILY_OFFER("^shop\\s+daily$"),
    BUY_ITEM("^shop\\s+buy\\s+-i\\s+(\\d+)\\s+-n\\s+(\\d+)(?:\\s+-t\\s+(\\w+))?$"),
    EXIT("^menu\\s+exit\\s*$"),
    HELP("^\\s*help\\s*$");

    private final String pattern;

    ShopMenuCommands(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    public static String getHelp() {
        return MenuCommand.generateHelp(ShopMenuCommands.class, "Shop");
    }
}
