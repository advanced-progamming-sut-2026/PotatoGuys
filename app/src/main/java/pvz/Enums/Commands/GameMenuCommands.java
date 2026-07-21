package pvz.enums.commands;

public enum GameMenuCommands implements MenuCommand {
    ENTER_MENU("^menu\\s+enter\\s+(?<menuName>\\w+)\\s*$"),
    ENTER_CHAPTER("^menu\\s+enter\\s+chapter\\s+-c\\s+(?<chapter>[1-4])\\s*$"),
    SELECT_MODE("^select\\s+game-mode\\s+-m\\s+(?<mode>[1-4])\\s*$"),
    SELECT_LEVEL("^select\\s+level\\s+-l\\s+(?<level>[1-5])\\s*$"),
    GREENHOUSE("^menu\\s+greenhouse\\s*$"),
    TRAVEL_LOG("^menu\\s+travel-log\\s*$"),
    LEADERBOARD("^menu\\s+leaderboard\\s*$"),
    COIN_WALLET("^menu\\s+coin-wallet\\s*$"),
    GEM_WALLET("^menu\\s+gem-wallet\\s*$"),
    CHEAT_ADD("^menu\\s+cheat\\s+add\\s+(?<amount>\\d+)\\s+(?<currency>coin|diamond)\\s*$"),
    EXIT("^menu\\s+exit\\s*$"),
    HELP("^\\s*help\\s*$");

    private final String pattern;
    GameMenuCommands(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }

    public static String getHelp() {
        return MenuCommand.generateHelp(GameMenuCommands.class, "Game Menu");
    }
}
