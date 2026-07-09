package pvz.Enums.Commands;

public enum RegisterMenuCommand implements MenuCommand {
    ENTER_MENU("^menu\\s+enter\\s+(?<menuName>\\w+)\\s*$"),
    REGISTER("^register\\s+-u\\s+(?<username>\\S+)\\s+-p\\s+(?<password>\\S+)\\s+(?<passwordConfirm>\\S+)\\s+-n\\s+(?<nickname>\\S+)\\s+-e\\s+(?<email>\\S+)\\s+-g\\s+(?<gender>\\S+)\\s*$"),
    PICK_QUESTION("^pick\\s+question\\s+-q\\s+(?<questionId>\\d+)\\s+-a\\s+(?<answer>\\S+)\\s+-c\\s+(?<confirmAnswer>\\S+)\\s*$"),
    EXIT("^menu\\s+exit\\s*$"),

    //Validation Patterns
    USERNAME("^[a-zA-Z0-9\\-]+$"),

    PASSWORD_SPECIAL_CHAR("[?><,\"';:\\\\/|\\[\\]}{+=()*?&@^%$#!]"),
    PASSWORD_LOWERCASE_CHAR("[a-z]"),
    PASSWORD_UPPERCASE_CHAR("[A-Z]"),
    PASSWORD_NUMBER_CHAR("\\d"),

    EMAIL_FORMAT("^\\s*(?<username>\\S+)@(?<domain>\\S+)\\.(?<suffix>\\S+)\\s*$"),
    EMAIL_USERNAME("^[a-zA-Z0-9.\\-_]+$"),
    EMAIL_TWO_CONSECUTIVE_DOTS("[\\.]{2,}"),
    EMAIL_SUFFIX("^[a-zA-Z]+$");

    private final String pattern;
    RegisterMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }
}
