package pvz.Controller;

import pvz.Models.Quests.Quest;
import pvz.Models.Quests.QuestCategory;
import pvz.Models.Quests.QuestLog;
import pvz.Models.Quests.Reward;
import pvz.Models.User.User;
import java.util.List;

public class QuestController {
    private User currentUser;

    public QuestController(User currentUser) {
        this.currentUser = currentUser;

        /*
         * TODO: Game Initialization
         * * Your quests.csv file acts as the database for creating these objects.
         * In your main Game Initialization phase, you will write a simple CSV reader
         * that parses that file, maps words like "روزانه" to QuestCategory.DAILY
         * and "بالا" to QuestPriority.HIGH, creates Quest objects using the parameters,
         * and adds them to the QuestLog.
         */
    }

    // Displays the Travel Log page based on the requested category
    public String showTravelLogPage(String pageName) {
        if (currentUser == null) return "No user logged in.";

        QuestLog log = currentUser.getQuestLog();
        List<Quest> filteredQuests;

        // Match the pageName to the Category (ADVENTURE/MAIN, SPECIAL, MINIGAME, EPIC, DAILY)
        try {
            QuestCategory category = QuestCategory.valueOf(pageName.toUpperCase());
            filteredQuests = log.findByCategory(category);
        } catch (IllegalArgumentException e) {
            return "Invalid Travel Log page. Available pages: DAILY, MAIN, EPIC.";
        }

        if (filteredQuests.isEmpty()) {
            return "No active quests in this category.";
        }

        // Sort by priority before displaying
        filteredQuests = log.sortByPriority(filteredQuests);

        StringBuilder sb = new StringBuilder();
        sb.append("--- Travel Log: ").append(pageName.toUpperCase()).append(" ---\n");
        for (Quest q : filteredQuests) {
            sb.append("[").append(q.getPriority()).append("] ")
                    .append(q.getTitle()).append("\n")
                    .append("  ").append(q.getDescription()).append("\n")
                    .append("  Progress: ").append(q.getProgress().toString()).append("\n");
        }
        return sb.toString();
    }

    // Called when a user completes a quest objective
    public String claimQuestReward(String questId) {
        Quest quest = currentUser.getQuestLog().findById(questId);

        if (quest == null) return "Quest not found.";
        if (!quest.getProgress().isComplete()) return "Quest is not completed yet.";
        if (!quest.isActive()) return "Reward already claimed.";

        // Grant all rewards attached to this quest
        for (Reward reward : quest.getRewards()) {
            reward.grant(currentUser);
        }

        quest.deactivate(); // Mark as claimed
        return "Rewards claimed for quest: " + quest.getTitle();
    }
}