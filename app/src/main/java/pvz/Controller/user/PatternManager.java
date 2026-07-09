package pvz.Controller.user;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pvz.Enums.Commands.RegisterMenuCommand;
import pvz.Models.User.User;
import pvz.Utils.SaveManager;

public class PatternManager {

    public static String validateUsername(String username) {
        if (!username.matches(RegisterMenuCommand.USERNAME.getPattern())) {
            return "Invalid Username";
        } else if (SaveManager.getInstance().load("users/" + username + ".json", User.class) != null) {
            return "Username already exists";
        }
        return null;
    }

    public static String validatePassword(String password, String passwordConfirm) {
        Pattern specialCharPat = Pattern.compile(RegisterMenuCommand.PASSWORD_SPECIAL_CHAR.getPattern());
        Matcher specialCharMat = specialCharPat.matcher(password);

        Pattern lowercaseCharPat = Pattern.compile(RegisterMenuCommand.PASSWORD_LOWERCASE_CHAR.getPattern());
        Matcher lowercaseCharMat = lowercaseCharPat.matcher(password);

        Pattern uppercaseCharPat = Pattern.compile(RegisterMenuCommand.PASSWORD_UPPERCASE_CHAR.getPattern());
        Matcher uppercaseCharMat = uppercaseCharPat.matcher(password);

        Pattern numberCharPat = Pattern.compile(RegisterMenuCommand.PASSWORD_NUMBER_CHAR.getPattern());
        Matcher numberCharMat = numberCharPat.matcher(password);

        if (!specialCharMat.find()) {
            return "Password should contain special characters!";
        } else if (!lowercaseCharMat.find()) {
            return "Password should contain lowercase characters!";
        } else if (!uppercaseCharMat.find()) {
            return "Password should contain uppercase characters!";
        } else if (!numberCharMat.find()) {
            return "Password should contain digits!";
        } else if (password.length() < 8) {
            return "Password length must be at least 8 characters!";
        } else if (!password.equals(passwordConfirm)) {
            return "Password and its Confirm aren't equal!";
        }
        return null;
    }

    public static String validateNickname(String nickName) {
        if (nickName.length() < 3 || nickName.length() > 30) {
            return "Nickname length should be between 3 and 30 characters";
        }
        return null;
    }

    public static String validateEmail(String email) {
        Matcher emailMat = Pattern.compile(RegisterMenuCommand.EMAIL_FORMAT.getPattern()).matcher(email);
        if (!emailMat.matches()) {
            return "Invalid email format";
        }
        String username = emailMat.group("username");
        String domain = emailMat.group("domain");
        String suffix = emailMat.group("suffix");

        //username length
        if (username.length() < 3) {
            return "The email name is too short.";
        }

        // username can have letters, digits, dash(-), underline(_) and dot(.)
        if (!username.matches(RegisterMenuCommand.EMAIL_USERNAME.getPattern())) {
            return "The email name has invalid characters.";
        }

        //-----------
        char first = username.charAt(0);
        char last = username.charAt(username.length() - 1);
        if (first == '.' || first == '-' || first == '_' || last == '.' || last == '-' || last == '_') {
            return "The email name should start or end with letters or digits.";
        }

        //check if the email name have 2 consecutive dots
        Pattern dotPat = Pattern.compile(RegisterMenuCommand.EMAIL_TWO_CONSECUTIVE_DOTS.getPattern());
        Matcher dotMat = dotPat.matcher(username);
        if (dotMat.find()) {
            return "The email name shouldn't have two consecutive dots";
        }

        //check if domain suffix has at least two characters
        if (suffix.length() < 2) {
            return "The domain suffix for email is two short";
        }

        //domain can only have letters, digits and dash(-) characters so we can reuse Username regex
        if (!domain.matches(RegisterMenuCommand.USERNAME.getPattern())) {
            return "Invalid email domain";
        }

        //suffix can only have letters in it
        if (!suffix.matches(RegisterMenuCommand.EMAIL_SUFFIX.getPattern())) {
            return "Invalid domain suffix for email";
        }

        return null;
    }

}
