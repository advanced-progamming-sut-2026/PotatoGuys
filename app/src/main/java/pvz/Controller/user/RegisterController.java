package pvz.controller.user;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;

import pvz.enums.SecurityQuestions;
import pvz.models.entities.plants.enums.PlantType;
import pvz.models.games.seasons.Season;
import pvz.models.user.Gender;
import pvz.models.user.Message;
import pvz.models.user.User;
import pvz.utils.PasswordUtils;
import pvz.utils.SaveManager;
import pvz.view.LoginMenu;
import pvz.view.Menu;
import pvz.view.PickSecurityQuestionMenu;
import pvz.view.RegisterMenu;
import pvz.view.Result;

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
        user.getProfile().getCollection().unlockPlant(PlantType.Peashooter);
        user.getProfile().getCollection().unlockPlant(PlantType.Sunflower);
        user.getProfile().getCollection().unlockPlant(PlantType.Cabbagepult);
        user.getProfile().getCollection().unlockPlant(PlantType.BonkChoy);
        user.getProfile().getCollection().unlockPlant(PlantType.Repeater);
        user.getProfile().getCollection().unlockPlant(PlantType.TwinSunflower);
        user.getProfile().getCollection().unlockPlant(PlantType.PeaPod);
        user.getProfile().getCollection().unlockPlant(PlantType.SnowPea);
        Season ancientEgypt=new Season("Ancient Egypt");
        ancientEgypt.unlock();
        ancientEgypt.unlockLevel(1);
        user.getProfile().getSeasons().add(ancientEgypt);
        user.getProfile().getSeasons().add(new Season("Frostbite Caves"));
        user.getProfile().getSeasons().add(new Season("Dark Ages"));
        user.getProfile().getSeasons().add(new Season("Big Wave Beach"));
        user.getProfile().getNews().getMessages().add(new Message("Welcome to Plants vs Zombies 2 "+user.getNickName()+"!"));
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