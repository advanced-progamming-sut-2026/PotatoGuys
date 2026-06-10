package Models.Quests;

public abstract class Reward {
    private RewardType type;

    public Reward(RewardType type) {
    }

    public RewardType getType() {
        return null;
    }

    public abstract void grant();
}
