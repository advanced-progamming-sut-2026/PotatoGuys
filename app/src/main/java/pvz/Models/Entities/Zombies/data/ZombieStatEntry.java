package pvz.Models.Entities.Zombies.data;

/**
 * Maps one entry in the JSON {@code ZombieStats} array, for example:
 * <pre>
 *   {"Type": "toughness", "Value": "toughness3"}
 *   {"Type": "speed",     "Value": "speed2"}
 * </pre>
 *
 * <p>These are <em>display labels</em> used by the Almanac / Collection menu.
 * Actual numeric stats come from {@code Hitpoints} and {@code Speed} in the sheet.
 */
public final class ZombieStatEntry {

    private final String statType;   // "toughness" | "speed"
    private final String statValue;  // "toughness1".."toughness8" | "speed0".."speed5"

    public ZombieStatEntry(String statType, String statValue) {
        this.statType = statType;
        this.statValue = statValue;
    }

    /**
     * Extracts the integer tier from the value string.
     * E.g. {@code "toughness3"} → {@code 3}, {@code "speed0"} → {@code 0}.
     *
     * @return the tier number, or {@code -1} if the value cannot be parsed
     */
    public int getTier() {
        String digits = statValue.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return -1;
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public boolean isToughness() {
        return "toughness".equalsIgnoreCase(statType);
    }

    public boolean isSpeed() {
        return "speed".equalsIgnoreCase(statType);
    }

    public String getStatType() { return statType; }
    public String getStatValue() { return statValue; }

    @Override
    public String toString() {
        return statType + "=" + statValue;
    }
}
