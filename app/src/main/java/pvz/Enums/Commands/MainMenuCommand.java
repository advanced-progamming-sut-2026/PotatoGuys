package pvz.Enums.Commands;

public enum MainMenuCommand implements MenuCommand {
    ENTER_MENU("^menu\\s+enter\\s+(?<menuName>\\w+)\\s*$"),
    SHOW_CURRENT("^menu\\s+show\\s+current\\s*$"),
    EXIT("^menu\\s+exit\\s*$"),
    LOGOUT("^menu\\s+logout\\s*$");

    private final String pattern;
    MainMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }
}
