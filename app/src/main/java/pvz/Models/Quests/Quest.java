package pvz.Models.Quests;

import java.util.ArrayList;
import java.util.List;

import pvz.Models.User.User;

public class Quest {
    private String id;
    private String title;
    private String description;
    private QuestCategory category;
    private QuestPriority priority;
    private transient List<Reward> rewards;
    private Progress progress;
    private boolean active;
    private boolean claimed;
    private boolean repeatable;
    private String variable; // Stores dynamic 'n', family, or chapter targets

    public Quest() {
        this.rewards = new ArrayList<>();
        this.progress = new Progress();
        this.active = true;
        this.claimed = false;
        this.repeatable = false;
    }

    public Quest(String id, String title, String description, QuestCategory category,
                 QuestPriority priority, List<Reward> rewards, int progressTarget) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.rewards = rewards;
        this.progress = new Progress(progressTarget);
        this.active = true;
        this.claimed = false;
        this.repeatable = (category == QuestCategory.DAILY);
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public QuestCategory getCategory() { return category; }
    public QuestPriority getPriority() { return priority; }
    public List<Reward> getRewards() { return rewards; }
    public Progress getProgress() { return progress; }
    public boolean isActive() { return active; }
    public boolean isClaimed() { return claimed; }
    public boolean isRepeatable() { return repeatable; }
    public String getVariable() { return variable; }

    public void setActive(boolean active) { this.active = active; }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }
    public void setRepeatable(boolean repeatable) { this.repeatable = repeatable; }
    public void setProgress(Progress progress) { this.progress = progress; }
    public void setVariable(String variable) { this.variable = variable; }

    public void activate() { this.active = true; }
    public void deactivate() { this.active = false; }

    public void claim(User user) {
        if (progress.isComplete() && !claimed) {
            for (Reward reward : rewards) {
                reward.grant(user);
            }
            this.claimed = true;
            if (repeatable) {
                this.active = false;
            }
        }
    }

    public void resetDaily() {
        if (repeatable) {
            progress.reset();
            active = true;
            claimed = false;
        }
    }

    public boolean isCompleted() {
        return progress.isComplete();
    }

    public String getStatus() {
        if (claimed) return "CLAIMED";
        if (progress.isComplete()) return "COMPLETED";
        if (active) return "IN PROGRESS";
        return "LOCKED";
    }

    public String getRewardDescription() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rewards.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(rewards.get(i).getDescription());
        }
        return sb.toString();
    }
}