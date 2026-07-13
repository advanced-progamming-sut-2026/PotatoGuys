package pvz.Models.Quests;

public class Progress {
    private int targetAmount;
    private int currentAmount;

    public Progress(int targetAmount) {
        this.targetAmount = targetAmount;
        this.currentAmount = 0;
    }

    public void addProgress(int amount) {
        this.currentAmount += amount;
        if (this.currentAmount > this.targetAmount) {
            this.currentAmount = this.targetAmount;
        }
    }

    public boolean isComplete() {
        return this.currentAmount >= this.targetAmount;
    }

    public int getTargetAmount() { return targetAmount; }
    public int getCurrentAmount() { return currentAmount; }

    @Override
    public String toString() {
        return currentAmount + "/" + targetAmount;
    }
}