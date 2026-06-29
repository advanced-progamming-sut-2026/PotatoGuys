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

    public String getUsername() {
        return username;
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

    public Profile getProfile() {
        return profile;
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
