package pvz.Models.Quests;

public class UnlockableReward extends Reward {
    private UnlockTargetType targetType;
    private String targetId;
    private UnlockState fromState;
    private UnlockState toState;

    public UnlockableReward(RewardType type) {
        super(type);
    }

    public UnlockTargetType getTargetType() {
        return targetType;
    }



    public String getTargetId() {
        return targetId;
    }



    public UnlockState getFromState() {
        return fromState;
    }



    public UnlockState getToState() {
        return toState;
    }



    @Override
    public void grant() {
    }
}
