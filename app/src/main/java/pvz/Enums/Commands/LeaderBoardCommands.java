package pvz.Enums.Commands;

public enum LeaderBoardCommands implements MenuCommand {
    SHOW("^leaderboard(?:\\s+list)?\\s*$"),
    SORT("^leaderboard\\s+sort\\s+-s\\s+(\\w+)(?:\\s+-o\\s+(\\w+))?\\s*$"),
    EXIT("^menu\\s+exit\\s*$");

    private final String pattern;

    LeaderBoardCommands(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }
}
