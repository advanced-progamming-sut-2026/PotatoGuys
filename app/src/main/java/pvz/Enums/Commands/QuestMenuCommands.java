package pvz.Enums.Commands;

public enum QuestMenuCommands implements MenuCommand {
    SHOW_PAGE("^travel\\s+log\\s+page\\s+(\\w+)\\s*$"),
    CLAIM_REWARD("^claim\\s+quest\\s+(\\S+)\\s*$"),
    SHOW_QUEST("^show\\s+quest\\s+(\\S+)\\s*$"),
    SHOW_ALL("^travel\\s+log\\s*$"),
    EXIT("^menu\\s+exit\\s*$");

    private final String pattern;

    QuestMenuCommands(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }
}
