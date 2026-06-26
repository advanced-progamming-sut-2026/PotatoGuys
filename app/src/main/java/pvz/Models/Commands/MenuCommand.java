package pvz.Models.Commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface MenuCommand {
    String getPattern();

    default Matcher getMatcher(String input) {
        if (input == null) return null;
        Matcher matcher = Pattern.compile(this.getPattern()).matcher(input.trim());
        return matcher.matches() ? matcher : null;
    }
}
