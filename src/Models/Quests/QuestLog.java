package Models.Quests;

import java.util.List;

public class QuestLog {
    private List<Quest> quests;

    public QuestLog(List<Quest> quests) {
    }

    public void addQuest(Quest quest) {
    }

    public void removeQuest(String questId) {
    }

    public Quest findById(String questId) {
        return null;
    }

    public List<Quest> findByCategory(QuestCategory category) {
        return null;
    }

    public List<Quest> findByPriority(QuestPriority priority) {
        return null;
    }

    public List<Quest> getActiveQuests() {
        return null;
    }

    public List<Quest> sortByPriority() {
        return null;
    }
}
