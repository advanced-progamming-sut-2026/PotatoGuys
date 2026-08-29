package com.pvz.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.pvz.models.AppContext;
import com.pvz.models.quests.Quest;
import com.pvz.models.quests.QuestCategory;
import com.pvz.models.quests.QuestLog;
import com.pvz.models.user.User;

/**
 * Controller for the Travel Log (quest) menu.
 * Reads/writes quest progress through the current user's QuestLog and
 * grants rewards when a completed quest is claimed.
 */
public class QuestController {

    private User getCurrentUser() {
        return AppContext.getInstance().getCurrentUser();
    }

    /** Active quests for a single category (Daily/Main/Epic), sorted by priority. */
    public List<Quest> getQuests(QuestCategory category) {
        User user = getCurrentUser();
        if (user == null) return new ArrayList<>();
        return user.getQuestLog().getDisplayableQuests(category);
    }

    /** All active quests across every category, sorted by priority. */
    public List<Quest> getAllQuests() {
        User user = getCurrentUser();
        if (user == null) return new ArrayList<>();
        QuestLog log = user.getQuestLog();
        List<Quest> all = new ArrayList<>();
        for (QuestCategory category : QuestCategory.values()) {
            all.addAll(log.getDisplayableQuests(category));
        }
        all.sort(Comparator.comparing(Quest::getPriority).reversed());
        return all;
    }

    public int getCoins() {
        User user = getCurrentUser();
        return user == null ? 0 : user.getProfile().getCoins();
    }

    public int getDiamonds() {
        User user = getCurrentUser();
        return user == null ? 0 : user.getProfile().getDiamonds();
    }

    /**
     * Milliseconds until the next daily-quest reset (next local midnight).
     * Returns a small positive value if the reset is overdue (shouldn't happen
     * because the reset is applied on read), but stays non-negative.
     */
    public long getMillisUntilDailyReset() {
        User user = getCurrentUser();
        if (user == null || user.getQuestLog() == null) return 0;
        return user.getQuestLog().millisUntilNextReset();
    }

    public String getMillisUntilDailyResetFormatted() {
        User user = getCurrentUser();
        if (user == null || user.getQuestLog() == null) return "00:00:00";
        return user.getQuestLog().formatTimeUntilReset();
    }

    /**
     * Attempts to claim the reward for the given quest id.
     * Returns a status message describing what happened.
     */
    public String claimQuest(String questId) {
        User user = getCurrentUser();
        if (user == null) return "No user logged in.";

        Quest quest = user.getQuestLog().findById(questId);
        if (quest == null) return "Quest not found.";
        if (!quest.isCompleted()) return "This quest isn't complete yet.";
        if (quest.isClaimed()) return "Reward already claimed.";

        quest.claim(user);
        user.saveUser();
        return "Reward claimed: " + quest.getRewardDescription();
    }
}
