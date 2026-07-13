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

    public Quest(String id, String title, String description, QuestCategory category, QuestPriority priority, List<Reward> rewards, int progressTarget) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.rewards = rewards;
        this.progress = new Progress(progressTarget);
        this.active = true; // Quests are active by default when assigned
    }

    // Getters...
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public QuestCategory getCategory() { return category; }
    public QuestPriority getPriority() { return priority; }
    public List<Reward> getRewards() { return rewards; }
    public boolean isActive() { return active; }
    public Progress getProgress() { return progress; }

    public void activate() { this.active = true; }
    public void deactivate() { this.active = false; }
    public void changePriority(QuestPriority priority) { this.priority = priority; }
    public void changeCategory(QuestCategory category) { this.category = category; }
    public void addReward(Reward reward) { this.rewards.add(reward); }
    public void removeReward(Reward reward) { this.rewards.remove(reward); }
}