package pvz.Enums.Commands;

public enum GreenHouseMenuCommands implements MenuCommand {
    ENTER_MENU("^show\\s+greenhouse$"),
    PLANT("^plant\\s+pot\\s+at\\s+\\((\\d+),\\s*(\\d+)\\)$"),
    COLLECT("^collect\\s+\\((\\d+),\\s*(\\d+)\\)$"),
    GROW("^grow\\s+\\((\\d+),\\s*(\\d+)\\)$"),
    EXIT("^menu\\s+exit\\s*$");

    private final String pattern;

    GreenHouseMenuCommands(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }
}