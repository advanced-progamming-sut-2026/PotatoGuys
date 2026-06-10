package Models.User;

public class User {
    private String Username;
    private String passwordHash;
    private String nickName;
    private String email;
    private Gender gender;
    private Profile myProfile;
    private Setting mySetting;
    private News myNews;
    private Score myScore;

    public String getUsername() {
        return Username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNickName() {
        return nickName;
    }

    public String getEmail() {
        return email;
    }

    public Gender getGender() {
        return gender;
    }

    public Profile getMyProfile() {
        return myProfile;
    }

    public Setting getMySetting() {
        return mySetting;
    }

    public News getMyNews() {
        return myNews;
    }

    public Score getMyScore() {
        return myScore;
    }

    public boolean checkPassword(String password){return true;}

    public String getUsername() {
        return Username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNickName() {
        return nickName;
    }

    public String getEmail() {
        return email;
    }

    public Gender getGender() {
        return gender;
    }

    public Profile getMyProfile() {
        return myProfile;
    }

    public Setting getMySetting() {
        return mySetting;
    }

    public News getMyNews() {
        return myNews;
    }

    public Score getMyScore() {
        return myScore;
    }
}
