package pvz.Enums.Commands;

public enum MiniGamesMenuCommand implements MenuCommand {
    MINI_GAME("^mini\\s+game\\s+(IZombie|Vasebreaker|WallnutBowling)\\s*$"),
    HELP("^\\s*help\\s*$");

    private final String pattern;
    MiniGamesMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }

    public static String getHelp() {
        return MenuCommand.generateHelp(MiniGamesMenuCommand.class, "Mini Games");
    }
}
