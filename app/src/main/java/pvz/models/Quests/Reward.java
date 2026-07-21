package pvz.models.quests;

import pvz.models.user.User;

public abstract class Reward {
    private RewardType type;

    public Reward() {}

    public Reward(RewardType type) {
        this.type = type;
    }

    public RewardType getType() {
        return type;
    }

    public abstract void grant(User user);

    public abstract String getDescription();
}
