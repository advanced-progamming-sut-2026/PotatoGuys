package com.pvz.enums.commands;

public enum NetworkMenuCommand implements MenuCommand {
    CONNECT("(?i)connect\\s+(?<host>\\S+)\\s+(?<port>\\d+)\\s*$"),
    BACK("(?i)back\\s*$"),
    HELP("^\\s*help\\s*$");

    private final String pattern;

    NetworkMenuCommand(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    public static String getHelp() {
        return MenuCommand.generateHelp(NetworkMenuCommand.class, "Network");
    }
}
