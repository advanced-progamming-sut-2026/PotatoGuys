package pvz.Models.Commands;

public enum SettingsMenuCommand implements MenuCommand {
    CHANGE_DIFFICULTY("^menu\\s+settings\\s+change-difficulty\\s+-l\\s+(?<difficulty>\\d+)\\s*$"),
    EXIT("^menu\\s+exit\\s*$");

    private final String pattern;
    SettingsMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }
}
