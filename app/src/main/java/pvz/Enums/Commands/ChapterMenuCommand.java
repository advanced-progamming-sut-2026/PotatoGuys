package pvz.Enums.Commands;

public enum ChapterMenuCommand implements MenuCommand {
    SELECT_LEVEL("^\\s*(?<level>[1-3])\\s*$"),
    HELP("^\\s*help\\s*$");

    private final String pattern;
    ChapterMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }

    public static String getHelp() {
        return MenuCommand.generateHelp(ChapterMenuCommand.class, "Chapter");
    }
}
