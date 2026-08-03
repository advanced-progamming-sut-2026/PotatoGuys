package com.pvz.enums.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface MenuCommand {
    String getPattern();

    default Matcher getMatcher(String input) {
        if (input == null) return null;
        Matcher matcher = Pattern.compile(this.getPattern()).matcher(input.trim());
        return matcher.matches() ? matcher : null;
    }

    static <E extends Enum<E> & MenuCommand> String generateHelp(Class<E> enumClass, String menuName) {
        StringBuilder result = new StringBuilder();
        result.append("=== ").append(menuName).append(" Commands Help ===\n");
        for (E command : enumClass.getEnumConstants()) {
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
