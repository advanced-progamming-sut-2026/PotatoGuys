package pvz.enums.commands;

public enum PreGameMenuCommand implements MenuCommand{
    SHOW_ALL_PLANTS("^\\s*show\\s+all\\s+plants\\s*$"),
    SHOW_AVAILABLE_PLANTS("^\\s*show\\s+available\\s+plants\\s*$"),
    PLANT_ADD("^\\s*add\\s+plant\\s+-t\\s+(?<type>.*)\\s*$"),
    PLANT_REMOVE("^\\s*remove\\s+plant\\s+-t\\s+(?<type>.*)$"),
    BOOST_PLANT("^\\s*boost\\s+plant\\s+-t\\s+(?<type>.*)$"),
    START_GAME("^\\s*start\\s+game\\s*$"),
    HELP("^\\s*help\\s*$");

    private final String pattern;
    PreGameMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }

    public static String getHelp() {
        return MenuCommand.generateHelp(PreGameMenuCommand.class, "Pre-Game");
    }
}
