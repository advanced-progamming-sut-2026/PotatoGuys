package pvz.Enums.Commands;

public enum LoginMenuCommand implements MenuCommand {
    ENTER_MENU("^menu\\s+enter\\s+(?<menuName>\\w+)\\s*$"),
    LOGIN("^login\\s+-u\\s+(?<username>\\S+)\\s+-p\\s+(?<password>\\S+)(?:\\s+(?<stayLoggedIn>-stay-logged-in))?\\s*$"),
    FORGET_PASSWORD("^forget\\s+password\\s+-u\\s+(?<username>\\S+)\\s+-e\\s+(?<email>\\S+)\\s*$"),
    ANSWER("^answer\\s+-a\\s+(?<answer>\\S+)\\s*$"),
    RESET_PASSWORD("^reset\\s+password\\s+-p\\s+(?<newPassword>\\S+)\\s*$"),
    EXIT("^menu\\s+exit\\s*$");

    private final String pattern;
    LoginMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }
}
