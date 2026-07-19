package pvz.Enums.Commands;

public enum GreenHouseMenuCommands implements MenuCommand {
    ENTER_MENU("^show\\s+greenhouse$"),
    PLANT("^plant\\s+pot\\s+at\\s+\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*)$"),
    COLLECT("^collect\\s+\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*)$"),
    GROW("^grow\\s+\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*)$"),
    ENTER_SHOP("^enter\\s+shop$"),
    EXIT("^menu\\s+exit\\s*$"),
    HELP("^\\s*help\\s*$");

    private final String pattern;

    GreenHouseMenuCommands(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    public static String getHelp() {
        return MenuCommand.generateHelp(GreenHouseMenuCommands.class, "GreenHouse");
    }
}
