package com.pvz.enums.commands;

public enum MainMenuCommand implements MenuCommand {
    ENTER_MENU("^menu\\s+enter\\s+(?<menuName>\\w+)\\s*$"),
    SHOW_CURRENT("^menu\\s+show\\s+current\\s*$"),
    EXIT("^menu\\s+exit\\s*$"),
    LOGOUT("^menu\\s+logout\\s*$"),
    HELP("^\\s*help\\s*$");

    private final String pattern;
    MainMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }

    public static String getHelp() {
        return MenuCommand.generateHelp(MainMenuCommand.class, "Main");
    }
}
