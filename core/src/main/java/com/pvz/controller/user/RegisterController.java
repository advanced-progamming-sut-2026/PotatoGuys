package com.pvz.controller.user;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;

import com.pvz.enums.SecurityQuestions;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.games.seasons.Season;
import com.pvz.models.user.Gender;
import com.pvz.models.user.User;
import com.pvz.utils.PasswordUtils;
import com.pvz.utils.SaveManager;

public class RegisterController {

    private User currentUser;

    public String register(Matcher matcher) {
        String username = matcher.group("username");
        String password = matcher.group("password");
        String passwordConfirm = matcher.group("passwordConfirm");
        String nickname = matcher.group("nickname");
        String email = matcher.group("email");
        String genderString = matcher.group("gender");

        String validationResult = validateRegistrationInputs(username, password, passwordConfirm, nickname, email, genderString);
        if (validationResult != null) {
            return validationResult;
        }

        String passwordHash = PasswordUtils.hashPassword(password);
        Gender gender = Gender.getGender(genderString);

        User user = new User(username, passwordHash, nickname, email, gender);
        currentUser = user;

        return "User registered successfully! Please pick a security question.";
    }

    public String pickQuestion(Matcher matcher) {
        if (currentUser == null) {
            return "Please register first before picking a security question.";
        }
        String question = matcher.group("questionId");
        String answer = matcher.group("answer");
        String confirmAnswer = matcher.group("confirmAnswer");

        if (!answer.equals(confirmAnswer)) {
            return "Answer and its Confirm aren't equal!";
        }

        currentUser.setSecurityQuestion(question);
        currentUser.setSecurityAnswer(answer);
        saveUser(currentUser);

        return "Security question and answer set successfully! User saved successfully!";
    }

    private String validateRegistrationInputs(String username, String password, String passwordConfirm,
            String nickname, String email, String genderString) {
        String usernameValidation = PatternManager.validateUsername(username);
        if (usernameValidation != null) {
            return usernameValidation;
        }

        String passwordValidation = PatternManager.validatePassword(password, passwordConfirm);
        if (passwordValidation != null) {
            return passwordValidation;
        }

        String nicknameValidation = PatternManager.validateNickname(nickname);
        if (nicknameValidation != null) {
            return nicknameValidation;
        }

        String emailValidation = PatternManager.validateEmail(email);
        if (emailValidation != null) {
            return emailValidation;
        }

        Gender gender = Gender.getGender(genderString);
        if (gender == null) {
            return "Invalid gender";
        }

        return null;
    }

    public void saveUser(User user) {
        String id = UUID.randomUUID().toString();
        user.setId(id);
        user.getProfile().getCollection().unlockPlant(PlantType.Peashooter);
        user.getProfile().getCollection().unlockPlant(PlantType.Sunflower);
        user.getProfile().getCollection().unlockPlant(PlantType.Cabbagepult);
        user.getProfile().getCollection().unlockPlant(PlantType.BonkChoy);
        user.getProfile().getCollection().unlockPlant(PlantType.Repeater);
        user.getProfile().getCollection().unlockPlant(PlantType.TwinSunflower);
        user.getProfile().getCollection().unlockPlant(PlantType.Jalapeno);
        user.getProfile().getCollection().unlockPlant(PlantType.SnowPea);
        user.getProfile().getCollection().unlockPlant(PlantType.CherryBomb);
        user.getProfile().getCollection().unlockPlant(PlantType.Wallnut);
        Season ancientEgypt = new Season("Ancient Egypt");
        ancientEgypt.unlock();
        ancientEgypt.unlockLevel(1);
        user.getProfile().getSeasons().add(ancientEgypt);
        user.getProfile().getSeasons().add(new Season("Frostbite Caves"));
        user.getProfile().getSeasons().add(new Season("Dark Ages"));
        user.getProfile().getSeasons().add(new Season("Big Wave Beach"));
        user.getProfile().getNews().getMessages()
                .add(new com.pvz.models.user.Message("Welcome to Plants vs Zombies 2 " + user.getNickName() + "!"));
        HashMap<String, String> usernames = SaveManager.getInstance().load("users/username.json", HashMap.class);
        if (usernames == null) {
            usernames = new HashMap<>();
        }
        usernames.put(user.getUsername(), user.getId());
        SaveManager.getInstance().save(usernames, "users/username.json");
        SaveManager.getInstance().save(user, "users/" + user.getId() + ".json");
    }
}