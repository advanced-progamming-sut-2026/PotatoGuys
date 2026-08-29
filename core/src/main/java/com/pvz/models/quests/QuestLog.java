package com.pvz.models.quests;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class QuestLog {
    private List<Quest> quests;
    private long lastDailyReset;

    public QuestLog() {
        this.quests = new ArrayList<>();
        this.lastDailyReset = System.currentTimeMillis();
    }

    public QuestLog(List<Quest> quests) {
        this.quests = (quests != null) ? quests : new ArrayList<>();
        this.lastDailyReset = System.currentTimeMillis();
    }

    public void addQuest(Quest quest) {
        this.quests.add(quest);
    }

    public void removeQuest(String questId) {
        quests.removeIf(q -> q.getId().equals(questId));
    }

    public Quest findById(String questId) {
        return quests.stream().filter(q -> q.getId().equals(questId)).findFirst().orElse(null);
    }

    public List<Quest> findByCategory(QuestCategory category) {
        return quests.stream().filter(q -> q.getCategory() == category).collect(Collectors.toList());
    }

    public List<Quest> findByPriority(QuestPriority priority) {
        return quests.stream().filter(q -> q.getPriority() == priority).collect(Collectors.toList());
    }

    public List<Quest> getActiveQuests() {
        checkAndResetDaily();
        return quests.stream().filter(Quest::isActive).collect(Collectors.toList());
    }

    public List<Quest> getAllQuests() {
        return new ArrayList<>(quests);
    }

    public List<Quest> getUnclaimedCompleted() {
        return quests.stream()
                .filter(q -> q.isCompleted() && !q.isClaimed() && q.isActive())
                .collect(Collectors.toList());
    }

    public List<Quest> sortByPriority(List<Quest> questList) {
        questList.sort(Comparator.comparing(Quest::getPriority).reversed());
        return questList;
    }

    public void resetDailyQuests() {
        for (Quest q : quests) {
            if (q.getCategory() == QuestCategory.DAILY) {
                q.resetDaily();
            }
        }
        lastDailyReset = System.currentTimeMillis();
    }

    public void checkAndResetDaily() {
        LocalDate lastResetDate = LocalDate.ofInstant(
            java.time.Instant.ofEpochMilli(lastDailyReset), ZoneId.systemDefault());
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        if (lastResetDate.isBefore(today)) {
            resetDailyQuests();
        }
    }

    public long getLastDailyReset() {
        return lastDailyReset;
    }

    /** Milliseconds remaining until the next daily reset (next local midnight). */
    public long millisUntilNextReset() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        java.time.LocalDateTime todayMidnight = today.plusDays(1).atStartOfDay();
        long nextReset = todayMidnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long remaining = nextReset - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /** ISO 8601 string (HH:mm:ss) of the time remaining until the next daily reset. */
    public String formatTimeUntilReset() {
        long remaining = millisUntilNextReset();
        long totalSeconds = remaining / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void setLastDailyReset(long lastDailyReset) {
        this.lastDailyReset = lastDailyReset;
    }

    public List<Quest> getDisplayableQuests(QuestCategory category) {
        checkAndResetDaily();
        return sortByPriority(new ArrayList<>(
            quests.stream()
                .filter(q -> q.getCategory() == category && q.isActive())
                .collect(Collectors.toList())
        ));
    }
}
