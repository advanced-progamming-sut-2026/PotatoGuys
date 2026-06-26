package Models.Commands;

public enum RegisterMenuCommand implements MenuCommand {
    ENTER_MENU("^menu\\s+enter\\s+(?<menuName>\\w+)\\s*$"),
    REGISTER("^register\\s+-u\\s+(?<username>\\S+)\\s+-p\\s+(?<password>\\S+)\\s+(?<passwordConfirm>\\S+)\\s+-n\\s+(?<nickname>\\S+)\\s+-e\\s+(?<email>\\S+)\\s+-g\\s+(?<gender>\\S+)\\s*$"),
    PICK_QUESTION("^pick\\s+question\\s+-q\\s+(?<questionId>\\d+)\\s+-a\\s+(?<answer>\\S+)\\s+-c\\s+(?<category>\\S+)\\s*$"),
    ENTER_LOGIN("^menu\\s+enter\\s+login\\s*$"),
    EXIT("^menu\\s+exit\\s*$");

    private final String pattern;
    RegisterMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }
}
