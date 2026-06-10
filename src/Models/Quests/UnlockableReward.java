package Models.Quests;

public class UnlockableReward extends Reward {
    private UnlockTargetType targetType;
    private String targetId;
    private UnlockState fromState;
    private UnlockState toState;

    public UnlockableReward(UnlockTargetType targetType, String targetId, UnlockState fromState, UnlockState toState) {
        super(null);
    }

    public UnlockTargetType getTargetType() {
        return null;
    }

    public String getTargetId() {
        return null;
    }

    public UnlockState getFromState() {
        return null;
    }

    public UnlockState getToState() {
        return null;
    }

    @Override
    public void grant() {
    }
}
