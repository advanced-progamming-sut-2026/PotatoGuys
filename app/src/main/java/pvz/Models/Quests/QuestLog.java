package pvz.Models.Quests;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class QuestLog {
    private List<Quest> quests;

    public QuestLog(List<Quest> quests) {
        this.quests = (quests != null) ? quests : new ArrayList<>();
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
        return quests.stream().filter(Quest::isActive).collect(Collectors.toList());
    }

    // Sorts by Priority: CRITICAL > HIGH > MEDIUM > LOW
    public List<Quest> sortByPriority(List<Quest> questList) {
        questList.sort(Comparator.comparing(Quest::getPriority).reversed());
        return questList;
    }
}