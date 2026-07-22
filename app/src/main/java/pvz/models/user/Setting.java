package pvz.models.user;

public class Setting {
    private int difficulty;

    // creating a setting with default difficulty
    public Setting(){
        difficulty = 3;
    }

    public Setting(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getDifficulty() {
        return difficulty;
    }
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
}
