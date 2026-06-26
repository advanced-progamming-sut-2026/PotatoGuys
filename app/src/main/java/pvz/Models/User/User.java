package pvz.Models.User;

public class User {
    private String username;
    private String passwordHash;
    private String nickName;
    private String email;
    private Gender gender;
    private Profile profile;
    private Setting setting;
    private News news;
    private Score score;

    public User(String username, String passwordHash, String nickName, String email, Gender gender){
        this.username=username;
        this.passwordHash=passwordHash;
        this.nickName=nickName;
        this.email=email;
        this.gender=gender;
        this.profile=new Profile();
        this.setting=new Setting();
        this.news=new News();
        this.score=new Score();
    }

}
