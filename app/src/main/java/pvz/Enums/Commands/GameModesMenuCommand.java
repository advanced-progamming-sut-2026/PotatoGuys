package pvz.Enums.Commands;

public enum GameModesMenuCommand implements MenuCommand {
    ADVENTURE("^1\\s*$"),
    HELP("^\\s*help\\s*$");

    private final String pattern;
    GameModesMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }

    public static String getHelp() {
        return MenuCommand.generateHelp(GameModesMenuCommand.class, "Game Modes");
    }
}
