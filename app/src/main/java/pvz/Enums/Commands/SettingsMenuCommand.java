package pvz.enums.commands;

public enum SettingsMenuCommand implements MenuCommand {
    CHANGE_DIFFICULTY("^menu\\s+settings\\s+change-difficulty\\s+-l\\s+(?<difficulty>\\d+)\\s*$"),
    EXIT("^menu\\s+exit\\s*$"),
    HELP("^\\s*help\\s*$");

    private final String pattern;
    SettingsMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }

    public static String getHelp() {
        return MenuCommand.generateHelp(SettingsMenuCommand.class, "Settings");
    }
}
