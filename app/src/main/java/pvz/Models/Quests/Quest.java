package pvz.Models.Quests;

import java.util.List;

public class Quest {
    private String id;
    private String title;
    private String description;
    private QuestCategory category;
    private QuestPriority priority;
    private List<Reward> rewards;
    private Progress progress;
    private boolean active;

    

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public QuestCategory getCategory() {
        return category;
    }

    public QuestPriority getPriority() {
        return priority;
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public boolean isActive() {
        return active;
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
