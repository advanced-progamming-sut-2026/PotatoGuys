package pvz.models.quests;

import pvz.models.entities.plants.enums.PlantType;
import pvz.models.user.User;

public class UnlockableReward extends Reward {
    private UnlockTargetType targetType;
    private String targetId;

    public UnlockableReward() {}

    public UnlockableReward(UnlockTargetType targetType, String targetId) {
        super(RewardType.UNLOCKABLE);
        this.targetType = targetType;
        this.targetId = targetId;
    }

    public UnlockTargetType getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    @Override
    public void grant(User user) {
        if (user == null) return;
        if (targetType == UnlockTargetType.PLANT) {
            PlantType plantType = PlantType.getTypeByName(targetId);
            if (plantType != null && user.getProfile().getCollection().getPlant(plantType) == null) {
                user.getProfile().getCollection().unlockPlant(plantType);
            }
        }
    }

    @Override
    public String getDescription() {
        return "Unlock " + targetId;
    }
}
