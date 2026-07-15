package pvz.Enums.Commands;

public enum ShopMenuCommands implements MenuCommand {
    EXIT("^menu\\s+exit\\s*$");

    private final String pattern;
    ShopMenuCommands(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }
}