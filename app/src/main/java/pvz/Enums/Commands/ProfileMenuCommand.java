package pvz.Enums.Commands;

public enum ProfileMenuCommand implements MenuCommand {
    CHANGE_USERNAME("^menu\\s+profile\\s+change-username\\s+-u\\s+(?<username>\\S+)\\s*$"),
    CHANGE_NICKNAME("^menu\\s+profile\\s+change-nickname\\s+-u\\s+(?<nickname>\\S+)\\s*$"),
    CHANGE_EMAIL("^menu\\s+profile\\s+change-email\\s+-e\\s+(?<email>\\S+)\\s*$"),
    CHANGE_PASSWORD("^menu\\s+profile\\s+change-password\\s+-p\\s+(?<newPassword>\\S+)\\s+-o\\s+(?<oldPassword>\\S+)\\s*$"),
    SHOW_INFO("^menu\\s+profile\\s+show-info\\s*$"),
    EXIT("^menu\\s+exit\\s*$");

    private final String pattern;
    ProfileMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }
}
