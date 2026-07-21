package pvz.enums.commands;

public enum NewsMenuCommand implements MenuCommand {
    SHOW_UNREAD("^menu\\s+news\\s+show-unread\\s*$"),
    SHOW_ALL("^menu\\s+news\\s+show-all\\s*$"),
    EXIT("^menu\\s+exit\\s*$"),
    HELP("^\\s*help\\s*$");

    private final String pattern;
    NewsMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }

    public static String getHelp() {
        return MenuCommand.generateHelp(NewsMenuCommand.class, "News");
    }
}
