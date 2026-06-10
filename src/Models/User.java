package Models;

public class User {
    private String Username;
    private String passwordHash;
    private String nickName;
    private String email;
    private Gender gender;

    private Profile myProfile;
    private Setting mySetting;
    private News myNews;

    public User(String username, String passwordHash, String nickName, String email, Gender gender, Profile myProfile, Setting mySetting, News myNews) {
        Username = username;
        this.passwordHash = passwordHash;
        this.nickName = nickName;
        this.email = email;
        this.gender = gender;
        this.myProfile = myProfile;
        this.mySetting = mySetting;
        this.myNews = myNews;
    }
    
    public String getUsername() {
        return Username;
    }
    public void setUsername(String username) {
        Username = username;
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
    public Profile getMyProfile() {
        return myProfile;
    }
    public void setMyProfile(Profile myProfile) {
        this.myProfile = myProfile;
    }
    public Setting getMySetting() {
        return mySetting;
    }
    public void setMySetting(Setting mySetting) {
        this.mySetting = mySetting;
    }
    public News getMyNews() {
        return myNews;
    }
    public void setMyNews(News myNews) {
        this.myNews = myNews;
    }
}
