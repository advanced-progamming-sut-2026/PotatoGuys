package Controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Models.AppContext;
import Models.Commands.RegisterMenuCommand;
import Models.User.Gender;
import Models.User.User;
import Utils.PasswordUtils;
import View.LoginMenu;
import View.Menu;
import View.RegisterMenu;
import View.Result;

public class RegisterController {
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
                return new Result("You have to login!" , nextMenu);
        }
        return new Result("Enterned " + nextMenu.getName() , nextMenu);
    }

    public Result register(Matcher matcher) {
        String username=matcher.group("username");
        String password=matcher.group("password");
        String passwordConfirm=matcher.group("passwordConfirm");
        String nickname=matcher.group("nickname");
        String email=matcher.group("email");
        String genderString=matcher.group("gender");

        String passwordHash= PasswordUtils.hashPassword(password);
        Gender gender=Gender.getGender(genderString);

        //checking username
        String usernameValidation=validateUsername(username);
        if (usernameValidation!=null){
            return new Result(usernameValidation);
        }

        //checking password
        String passwordValidation=validatePassword(password,passwordConfirm);
        if (passwordValidation!=null){
            return new Result(passwordValidation);
        }

        //checking email


        User user=new User(username,passwordHash,nickname,email,gender);
        return null; }
    public Result pickQuestion(Matcher matcher) { return null; }
    public Result enterLogin(Matcher matcher) { return null; }
    public Result exit(Matcher matcher) {
        return new Result("Exiting the program...", null);
    }

    private String validateUsername(String username){
        if (!username.matches(RegisterMenuCommand.USERNAME.getPattern())){
            return "Invalid Username";
        } else if(AppContext.getInstance().getAllUsernames().contains(username)){
            return "Username already exists";
        }
        return null;
    }

    private String validatePassword(String password, String passwordConfirm){
        Pattern specialCharPat=Pattern.compile(RegisterMenuCommand.PASSWORD_SPECIAL_CHAR.getPattern());
        Matcher specialCharMat=specialCharPat.matcher(password);

        Pattern lowercaseCharPat=Pattern.compile(RegisterMenuCommand.PASSWORD_LOWERCASE_CHAR.getPattern());
        Matcher lowercaseCharMat=lowercaseCharPat.matcher(password);

        Pattern uppercaseCharPat=Pattern.compile(RegisterMenuCommand.PASSWORD_UPPERCASE_CHAR.getPattern());
        Matcher uppercaseCharMat=uppercaseCharPat.matcher(password);

        Pattern numberCharPat=Pattern.compile(RegisterMenuCommand.PASSWORD_NUMBER_CHAR.getPattern());
        Matcher numberCharMat=numberCharPat.matcher(password);

        if (!specialCharMat.find()){
            return "Password should contain special characters!";
        } else if(!lowercaseCharMat.find()){
            return "Password should contain lowercase characters!";
        } else if(!uppercaseCharMat.find()){
            return "Password should contain uppercase characters!";
        } else if(!numberCharMat.find()){
            return "Password should contain digits!";
        } else if(password.length()<8) {
            return "Password length must be at least 8 characters!";
        } else if (!password.equals(passwordConfirm)) {
            return "Password and its Confirm aren't equal!";
        }
        return null;
    }
}
