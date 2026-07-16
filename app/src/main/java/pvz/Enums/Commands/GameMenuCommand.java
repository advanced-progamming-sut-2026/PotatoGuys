package pvz.Enums.Commands;

public enum GameMenuCommand implements MenuCommand {
    ADVANCE_TIME("^advance\\s+time\\s+-t\\s+(?<ticks>\\d+)\\s+ticks\\s*$"),
    COLLECT_SUN("^collect\\s+sun\\s+-l\\s+\\(\\s*(?<sunX>\\d+)\\s*,\\s*(?<sunY>\\d+)\\s*\\)\\s*$"),
    SHOW_SUN("^show\\s+sun\\s+amount\\s*$"),
    CHEAT_SUN("^cheat\\s+add\\s+-n\\s+(?<sunCount>\\d+)\\s+suns\\s*$"),
    RELEASE_NUKE("^release\\s+the\\s+nuke\\s*$"),
    PLANT("^plant\\s+plant\\s+-t\\s+(?<plantType>.+)\\s+-l\\s+\\(\\s*(?<plantX>\\d+)\\s*,\\s*(?<plantY>\\d+)\\s*\\)\\s*$"),
    CHEAT_COOLDOWN("^cheat\\s+remove-cooldown\\s*$"),
    PLUCK_PLANT("^pluck\\s+plant\\s+-l\\s+\\(\\s*(?<pluckX>\\d+)\\s*,\\s*(?<pluckY>\\d+)\\s*\\)\\s*$"),
    FEED_PLANT("^feed\\s+plant\\s+-l\\s+\\(\\s*(?<feedX>\\d+)\\s*,\\s*(?<feedY>\\d+)\\s*\\)\\s*$"),
    CHEAT_PLANT_FOOD("^cheat\\s+add-plant-food\\s*$"),
    SHOW_MAP("^show\\s+map\\s*$"),
    SHOW_PLANTS("^show\\s+plants\\s*$"),
    SHOW_ZOMBIES("^show\\s+zombies\\s*$"),
    SHOW_PROJECTILE("^show\\s+projectiles\\s*$"),
    SHOW_SUNS("^show\\s+suns\\s*$"),
    SHOW_TILE_STATUS("^show\\s+tile\\s+status\\s+-l\\s+\\(\\s*(?<tileX>\\d+)\\s*,\\s*(?<tileY>\\d+)\\s*\\)\\s*$"),
    CHEAT_SPAWN_ZOMBIE("^cheat\\s+spawn-zombie\\s+-t\\s+(?<zombieType>\\w+)\\s+-l\\s+<\\s*(?<zombieX>\\d+)\\s*,\\s*(?<zombieY>\\d+)\\s*>\\s*$"),
    HELP("^\\s*help\\s*$");

    private final String pattern;
    GameMenuCommand(String pattern) { this.pattern = pattern; }
    @Override public String getPattern() { return this.pattern; }
    
    public static String getHelp() {
        StringBuilder result = new StringBuilder();
        result.append("=== Game Commands Help ===\n");
        
        for (GameMenuCommand command : values()) {
            String cleaned = command.getPattern();

            cleaned = cleaned.replaceAll("^\\^", "").replaceAll("\\$$", "");
            cleaned = cleaned.replaceAll("\\(\\?\\<(\\w+)\\>[^\\)]+\\)", "<$1>");
            cleaned = cleaned.replace("\\(", "(").replace("\\)", ")");
            cleaned = cleaned.replace("\\<", "<").replace("\\>", ">");
            cleaned = cleaned.replaceAll("\\\\s\\*", "");
            cleaned = cleaned.replaceAll("\\\\s\\+", " ");
            cleaned = cleaned.replaceAll("\\s+", " ").trim();

            result.append(String.format("%-20s : %s\n", command.name(), cleaned));
        }

        return result.toString();
    }
}
