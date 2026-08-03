package com.pvz.enums.commands;

public enum CollectionMenuCommand implements MenuCommand {
    SHOW_PLANTS("^menu\\s+collection\\s+show-plants\\s*$"),
    SHOW_ALL_PLANTS("^menu\\s+collection\\s+show-all-plants\\s*$"),
    SHOW_ZOMBIES("^menu\\s+collection\\s+show-zombies\\s*$"),
    SHOW_ALL_ZOMBIES("^menu\\s+collection\\s+show-all-zombies\\s*$"),
    SHOW_PLANT_INFO("^menu\\s+collection\\s+show-plant\\s+-p\\s+(?<plantName>\\w+)\\s*$"),
    SHOW_ZOMBIE_INFO("^menu\\s+collection\\s+show-zombie\\s+-z\\s+(?<zombieName>\\w+)\\s*$"),
    UPGRADE_PLANT("^menu\\s+collection\\s+upgrade-plant\\s+-p\\s+(?<plantName>\\w+)\\s*$"),
    PURCHASE_PLANT("^menu\\s+collection\\s+purchase-plant\\s+-p\\s+(?<plantName>\\w+)\\s*$"),
    EXIT("^menu\\s+exit\\s*$"),
    HELP("^\\s*help\\s*$");

    private final String pattern;
    CollectionMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }

    public static String getHelp() {
        return MenuCommand.generateHelp(CollectionMenuCommand.class, "Collection");
    }
}
