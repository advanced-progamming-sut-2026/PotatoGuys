package pvz.Controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pvz.Enums.SecurityQuestions;
import pvz.Enums.Commands.RegisterMenuCommand;
import pvz.Models.AppContext;
import pvz.Models.User.Gender;
import pvz.Models.User.User;
import pvz.Utils.PasswordUtils;
import pvz.Utils.SaveManager;
import pvz.View.LoginMenu;
import pvz.View.Menu;
import pvz.View.PickSecurityQuestionMenu;
import pvz.View.RegisterMenu;
import pvz.View.Result;

public class RegisterController {
    private User currentUser;

    public Result enterMenu(Matcher matcher) {
        String menuName = matcher.group("menuName");
        menuName = menuName.replaceAll("  ", "");
        Menu nextMenu = null;
        switch (menuName.trim()) {
            case "login":
                nextMenu = new LoginMenu();
                break;
            default:
                nextMenu = new RegisterMenu();
                return new Result("You have to login!", nextMenu);
        }
        return new Result("Enterned " + nextMenu.getName(), nextMenu);
    }

    public Result register(Matcher matcher) {
        String username = matcher.group("username");
        String password = matcher.group("password");
        String passwordConfirm = matcher.group("passwordConfirm");
        String nickname = matcher.group("nickname");
        String email = matcher.group("email");
        String genderString = matcher.group("gender");

        String passwordHash = PasswordUtils.hashPassword(password);
        Gender gender = Gender.getGender(genderString);

        //checking username
        String usernameValidation = validateUsername(username);
        if (usernameValidation != null) {
            return new Result(usernameValidation);
        }

        //checking password
        String passwordValidation = validatePassword(password, passwordConfirm);
        if (passwordValidation != null) {
            return new Result(passwordValidation);
        }

        //checking nickname
        String nicknameValidation = validateNickname(nickname);
        if (nicknameValidation != null) {
            return new Result(nicknameValidation);
        }

        //checking email
        String emailValidation = validateEmail(email);
        if (emailValidation != null) {
            return new Result(emailValidation);
        }

        //checking gender
        if (gender == null) {
            return new Result("Invalid gender");
        }

        User user = new User(username, passwordHash, nickname, email, gender);
        currentUser = user;

        //temporary
        StringBuilder resultMessage = new StringBuilder();
        resultMessage.append("User registered successfully! Please pick a security question:\n");
        for(int i = 0 ; i < SecurityQuestions.QUESTIONS.size() ; i++){
            resultMessage.append((i+1) + ". " + SecurityQuestions.getQuestionByNumber(i)).append("\n");
        }
        resultMessage.append("\nPlease use the command 'pick question -q <questionId> -a <answer> -c <confirmAnswer>' to pick a security question and set your answer.");
        return new Result(resultMessage.toString(), new PickSecurityQuestionMenu(this));
    }

    public Result pickQuestion(Matcher matcher) {
        String question = matcher.group("questionId");
        String answer = matcher.group("answer");
        String confirmAnswer = matcher.group("confirmAnswer");
        
        if(!answer.equals(confirmAnswer)){
            return new Result("Answer and its Confirm aren't equal!");
        }
        
        currentUser.setSecurityQuestion(question);
        currentUser.setSecurityAnswer(answer);
        SaveManager.getInstance().save(currentUser, "users/" + currentUser.getUsername() + ".json");

        return new Result("Security question and answer set successfully!\nUser saved successfully!", new LoginMenu());
    }

    public Result exit(Matcher matcher) {
        return new Result("Exiting the program...", null);
    }

    private String validateUsername(String username) {
        if (!username.matches(RegisterMenuCommand.USERNAME.getPattern())) {
            return "Invalid Username";
        } else if (AppContext.getInstance().getAllUsernames().contains(username)) {
            return "Username already exists";
        }
        return null;
    }

    private String validatePassword(String password, String passwordConfirm) {
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

    private String validateNickname(String nickName) {
        if (nickName.length() < 3 || nickName.length() > 30) {
            return "Nickname length should be between 3 and 30 characters";
        }
        return null;
    }

    private String validateEmail(String email) {
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
