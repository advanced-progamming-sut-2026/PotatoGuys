package pvz.Models.Quests;

public abstract class Reward {
    private RewardType type;

    public Reward(RewardType type) {
    }

    public RewardType getType() {
        return type;
    }

    public abstract void grant();
}
