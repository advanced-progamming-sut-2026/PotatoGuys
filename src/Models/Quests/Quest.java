package Models.Quests;

import java.util.List;

public class Quest {
    private String id;
    private String name;
    private QuestCategory category;
    private QuestPriority priority;
    private List<Reward> rewards;
    private boolean active;

    public Quest(String id, String name, QuestCategory category, QuestPriority priority, List<Reward> rewards) {
    }

    public String getId() {
        return null;
    }

    public String getName() {
        return null;
    }

    public QuestCategory getCategory() {
        return null;
    }

    public QuestPriority getPriority() {
        return null;
    }

    public List<Reward> getRewards() {
        return null;
    }

    public boolean isActive() {
        return false;
    }

    public void activate() {
    }

    public void deactivate() {
    }

    public void changePriority(QuestPriority priority) {
    }

    public void changeCategory(QuestCategory category) {
    }

    public void addReward(Reward reward) {
    }

    public void removeReward(Reward reward) {
    }
}
