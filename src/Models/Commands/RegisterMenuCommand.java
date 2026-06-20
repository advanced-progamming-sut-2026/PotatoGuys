package Models.Commands;

public enum RegisterMenuCommand implements MenuCommand {
    REGISTER("^register\\s+-u\\s+(?<username>\\S+)\\s+-p\\s+(?<password>\\S+)\\s+(?<nickname>\\S+)\\s+-n\\s+(?<screenName>\\S+)\\s+-e\\s+(?<email>\\S+)\\s+-g\\s+(?<gender>\\S+)\\s*$"),
    PICK_QUESTION("^pick\\s+question\\s+-q\\s+(?<questionId>\\d+)\\s+-a\\s+(?<answer>\\S+)\\s+-c\\s+(?<category>\\S+)\\s*$"),
    ENTER_LOGIN("^menu\\s+enter\\s+login\\s*$"),
    EXIT("^menu\\s+exit\\s*$");

    private final String pattern;
    RegisterMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }
}
