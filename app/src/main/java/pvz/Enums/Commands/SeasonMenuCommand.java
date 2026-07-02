package pvz.Enums.Commands;

public enum SeasonMenuCommand implements MenuCommand {
    ENTER_MENU("^menu\\s+enter\\s+(?<menuName>\\w+)\\s*$"),
    ENTER_CHAPTER("^menu\\s+enter\\s+chapter\\s+-c\\s+(?<chapterName>\\w+)\\s*$"),
    GREENHOUSE("^menu\\s+greenhouse\\s*$"),
    TRAVEL_LOG("^menu\\s+travel-log\\s*$"),
    LEADERBOARD("^menu\\s+leaderboard\\s*$"),
    COIN_WALLET("^menu\\s+coin-wallet\\s*$"),
    GEM_WALLET("^menu\\s+gem-wallet\\s*$"),
    CHEAT_ADD("^menu\\s+cheat\\s+add\\s+(?<amount>\\d+)\\s+(?<currency>coin|diamond)\\s*$"),
    EXIT("^menu\\s+exit\\s*$");

    private final String pattern;
    SeasonMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }
}
