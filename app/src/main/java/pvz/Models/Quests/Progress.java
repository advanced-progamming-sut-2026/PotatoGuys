package pvz.models.quests;

public class Progress {
    private int targetAmount;
    private int currentAmount;

    public Progress() {
        this.targetAmount = 0;
        this.currentAmount = 0;
    }

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

    public void reset() {
        this.currentAmount = 0;
    }

    public boolean isComplete() {
        return this.currentAmount >= this.targetAmount;
    }

    public int getTargetAmount() { return targetAmount; }
    public int getCurrentAmount() { return currentAmount; }

    public void setCurrentAmount(int currentAmount) {
        this.currentAmount = currentAmount;
    }

    public void setTargetAmount(int targetAmount) {
        this.targetAmount = targetAmount;
    }

    @Override
    public String toString() {
        return currentAmount + "/" + targetAmount;
    }
}
