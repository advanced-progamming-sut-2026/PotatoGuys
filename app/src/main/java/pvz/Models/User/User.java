package pvz.Models.User;

import java.util.UUID;

import pvz.Utils.SaveManager;

import pvz.Models.GreenHouse.GreenHouse;


public class User {
    private String id;
    private String username;
    private String passwordHash;
    private String nickName;
    private String email;
    private Gender gender;
    private String securityQuestion;
    private String securityAnswer;
    private Profile profile;
    private Setting setting;
    private News news;
    private Score score;
    private GreenHouse greenHouse;

    public void saveUser(){
        SaveManager.getInstance().save(this, "saves/" + id + ".json");
    }


    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public Profile getProfile() {
        return profile;
    }

    public GreenHouse getGreenHouse() {
        return greenHouse;
    }

    public void setGreenHouse(GreenHouse greenHouse) {
        this.greenHouse = greenHouse;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Setting getSetting() {
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }

    public Score getScore() {
        return score;
    }

    public void setScore(Score score) {
        this.score = score;
    }

    public User(String username, String passwordHash, String nickName, String email, Gender gender){
        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.passwordHash=passwordHash;
        this.nickName=nickName;
        this.email=email;
        this.gender=gender;
        this.profile=new Profile();
        this.greenHouse=new GreenHouse();
        this.setting=new Setting();
        this.news=new News();
        this.score=new Score();
    }

}
