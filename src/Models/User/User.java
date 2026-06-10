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

    public boolean checkPassword(String password){
        return true;
    }
}
