package pvz.Controller.user;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;

import pvz.Enums.SecurityQuestions;
import pvz.Models.Entities.Plants.Enums.PlantType;
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
        String usernameValidation = PatternManager.validateUsername(username);
        if (usernameValidation != null) {
            return new Result(usernameValidation);
        }

        //checking password
        String passwordValidation = PatternManager.validatePassword(password, passwordConfirm);
        if (passwordValidation != null) {
            return new Result(passwordValidation);
        }

        //checking nickname
        String nicknameValidation = PatternManager.validateNickname(nickname);
        if (nicknameValidation != null) {
            return new Result(nicknameValidation);
        }

        //checking email
        String emailValidation = PatternManager.validateEmail(email);
        if (emailValidation != null) {
            return new Result(emailValidation);
        }

        //checking gender
        if (gender == null) {
            return new Result("Invalid gender");
        }

        User user = new User(username, passwordHash, nickname, email, gender);
        currentUser = user;

        //Successful registration
        StringBuilder resultMessage = new StringBuilder();
        resultMessage.append("User registered successfully! Please pick a security question:\n");
        for(int i = 1 ; i <= SecurityQuestions.QUESTIONS.size() ; i++){
            resultMessage.append((i) + ". " + SecurityQuestions.getQuestionByNumber(i)).append("\n");
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
        saveUser(currentUser);

        return new Result("Security question and answer set successfully!\nUser saved successfully!", new LoginMenu());
    }

    public void saveUser(User user){
        String id = UUID.randomUUID().toString();
        user.setId(id);
        user.getProfile().getCollection().addPlant(PlantType.Peashooter,1,false);
        user.getProfile().getCollection().addPlant(PlantType.Sunflower,1,false);
        user.getProfile().getCollection().addPlant(PlantType.Cabbagepult,1,false);
        user.getProfile().getCollection().addPlant(PlantType.BonkChoy,1,false);
        user.getProfile().getCollection().addPlant(PlantType.Repeater,1,false);
        user.getProfile().getCollection().addPlant(PlantType.TwinSunflower,1,false);
        user.getProfile().getCollection().addPlant(PlantType.PeaPod,1,false);
        user.getProfile().getCollection().addPlant(PlantType.SnowPea,1,false);
        HashMap<String , String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
        if(usernames == null){
            usernames = new HashMap<>();
        }
        usernames.put(user.getUsername() , user.getId());
        SaveManager.getInstance().save(usernames, "users/username.json");
        SaveManager.getInstance().save(user, "users/" + user.getId() + ".json");
    }

    public Result exit(Matcher matcher) {
        return new Result("Exiting the program...", null);
    }

}