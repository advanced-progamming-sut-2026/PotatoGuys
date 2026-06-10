package Models;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Commands {
    Nothing("");

    private final String pattern;
    private Commands(String pattern){
        this.pattern = pattern;
    }

    public Matcher getMatcher(String input){
        Matcher matcher = Pattern.compile(this.pattern).matcher(input.trim());
        if (matcher.matches()) {
            return matcher;
        }
        return null;
    }
}
