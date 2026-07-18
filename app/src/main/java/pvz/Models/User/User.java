package pvz.Models.User;

import java.util.UUID;

import pvz.Models.Games.Seasons.Season;
import pvz.Utils.SaveManager;

import pvz.Models.GreenHouse.GreenHouse;
import pvz.Models.Quests.Quest;
import pvz.Models.Quests.QuestLog;
import pvz.Models.Quests.QuestFactory;


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
    private Score score;
    private GreenHouse greenHouse;
    private QuestLog questLog;

    public void saveUser(){
        SaveManager.getInstance().save(this, "users/" + id + ".json");
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

    public QuestLog getQuestLog() {
        if (questLog == null) {
            questLog = QuestFactory.createDefaultQuests();
        }
        return questLog;
    }

    public void refreshQuestLog() {
        QuestLog fresh = QuestFactory.createDefaultQuests();
        if (questLog != null) {
            for (Quest freshQuest : fresh.getAllQuests()) {
                Quest existing = questLog.findById(freshQuest.getId());
                if (existing != null) {
                    freshQuest.getProgress().setCurrentAmount(
                        existing.getProgress().getCurrentAmount()
                    );
                    freshQuest.setClaimed(existing.isClaimed());
                    if (existing.isClaimed()) freshQuest.deactivate();
                }
            }
        }
        this.questLog = fresh;
    }

    public void setQuestLog(QuestLog questLog) {
        this.questLog = questLog;
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
        this.score=new Score();
        this.questLog=QuestFactory.createDefaultQuests();

    }


}
