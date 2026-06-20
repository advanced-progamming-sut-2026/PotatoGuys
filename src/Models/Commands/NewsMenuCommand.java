package Models.Commands;

public enum NewsMenuCommand implements MenuCommand {
    SHOW_UNREAD("^menu\\s+news\\s+show-unread\\s*$"),
    SHOW_ALL("^menu\\s+news\\s+show-all\\s*$"),
    EXIT("^menu\\s+exit\\s*$");

    private final String pattern;
    NewsMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }
}
