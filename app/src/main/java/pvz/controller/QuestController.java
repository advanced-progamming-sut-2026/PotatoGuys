package pvz.controller;

import java.util.List;
import java.util.regex.Matcher;

import pvz.controller.game.GameController;
import pvz.models.AppContext;
import pvz.models.games.GameContext;
import pvz.models.games.levels.Level;
import pvz.models.games.levels.LevelLoader;
import pvz.models.quests.Quest;
import pvz.models.quests.QuestCategory;
import pvz.models.quests.QuestLog;
import pvz.models.user.User;
import pvz.view.MainMenu;
import pvz.view.Result;
import pvz.view.game.PreGameMenu;
import pvz.view.game.RunningGameMenu;

public class QuestController {

    private User getCurrentUser() {
        return AppContext.getInstance().getCurrentUser();
    }

    public Result showPage(Matcher matcher) {
        User user = getCurrentUser();
        if (user == null)
            return new Result("No user logged in.");

        String pageName = matcher.group(1).toLowerCase();

        if (pageName.equals("daily") || pageName.equals("main") || pageName.equals("epic")) {
            return showQuestsByCategory(user, pageName);
        } else if (pageName.equals("minigames")) {
            return showMiniGames(matcher);
        } else {
            return new Result("Invalid page. Available pages: daily, main, epic.");
        }

    }

    public Result showMiniGames(Matcher matcher) {
        return new Result(
                "Mini games:\n1. Vasebreaker\n2. Wallnut Bowling\n3. I, Zombie\n4. " +
                 "Beghouled\n5. Zombotany\nUsing 'mini game -t <miniGameNumber> -l <level>' to start a mini game.");
    }

    private Result showQuestsByCategory(User user, String pageName) {
        QuestCategory category;
        try {
            category = QuestCategory.valueOf(pageName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return new Result("Invalid page. Available pages: daily, main, epic.");
        }

        QuestLog log = user.getQuestLog();
        List<Quest> quests = log.getDisplayableQuests(category);

        if (quests.isEmpty()) {
            return new Result("No active quests in " + pageName + " category.");
        }

        StringBuilder sb = new StringBuilder("=== Travel Log: " + category.name() + " ===");
        for (Quest q : quests) {
            sb.append("\n\n[");
            sb.append(q.getPriority()).append("] ");
            sb.append(q.getTitle());
            sb.append("\n  ");
            sb.append(q.getDescription());
            sb.append("\n  Progress: ").append(q.getProgress().toString());
            sb.append(" | Status: ").append(q.getStatus());
            sb.append("\n  Reward: ").append(q.getRewardDescription());
            if (q.isCompleted() && !q.isClaimed()) {
                sb.append("\n  >> Use 'claim quest ").append(q.getId()).append("' to claim reward!");
            }
        }
        return new Result(sb.toString());
    }

    public Result showAllPages(Matcher matcher) {
        User user = getCurrentUser();
        if (user == null)
            return new Result("No user logged in.");

        QuestLog log = user.getQuestLog();
        StringBuilder sb = new StringBuilder("=== Travel Log Summary ===");

        for (QuestCategory cat : QuestCategory.values()) {
            List<Quest> quests = log.getDisplayableQuests(cat);
            sb.append("\n\n--- ").append(cat.name()).append(" (").append(quests.size()).append(" quests) ---");
            for (Quest q : quests) {
                sb.append("\n [").append(q.getPriority()).append("] ")
                        .append(q.getTitle()).append(" - ").append(q.getStatus());
            }
        }
        return new Result(sb.toString());
    }

    public Result showQuest(Matcher matcher) {
        User user = getCurrentUser();
        if (user == null)
            return new Result("No user logged in.");

        String questId = matcher.group(1);
        QuestLog log = user.getQuestLog();
        Quest quest = log.findById(questId);

        if (quest == null) {
            return new Result("Quest not found: " + questId);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(quest.getTitle()).append(" ===");
        sb.append("\nCategory: ").append(quest.getCategory().name());
        sb.append("\nPriority: ").append(quest.getPriority().name());
        sb.append("\nDescription: ").append(quest.getDescription());
        sb.append("\nProgress: ").append(quest.getProgress().toString());
        sb.append("\nStatus: ").append(quest.getStatus());
        sb.append("\nReward: ").append(quest.getRewardDescription());
        if (quest.getVariable() != null) {
            sb.append("\nVariable: ").append(quest.getVariable());
        }

        if (quest.isCompleted() && !quest.isClaimed()) {
            sb.append("\n>> Use 'claim quest ").append(questId).append("' to claim your reward!");
        }
        return new Result(sb.toString());
    }

    public Result claimReward(Matcher matcher) {
        User user = getCurrentUser();
        if (user == null)
            return new Result("No user logged in.");

        String questId = matcher.group(1);
        QuestLog log = user.getQuestLog();
        Quest quest = log.findById(questId);

        if (quest == null) {
            return new Result("Quest not found: " + questId);
        }
        if (!quest.isCompleted()) {
            return new Result("Quest is not completed yet. Progress: " + quest.getProgress().toString());
        }
        if (quest.isClaimed()) {
            return new Result("Reward already claimed for this quest.");
        }

        quest.claim(user);
        if (quest.getCategory() == QuestCategory.DAILY) {
            user.getScore().setDailyQuests(user.getScore().getDailyQuests() + 1);
        } else {
            user.getScore().setNonDailyQuests(user.getScore().getNonDailyQuests() + 1);
        }
        user.saveUser();
        return new Result("Rewards claimed for quest: " + quest.getTitle() + "!");
    }

    public Result startMiniGame(Matcher matcher) {
        int miniGameNumber = Integer.parseInt(matcher.group("miniGameNumber"));
        String miniGame = null;
        switch (miniGameNumber) {
            case 1:
                miniGame = "Vasebreaker";
                break;
            case 2:
                miniGame = "Wallnut Bowling";
                break;
            case 3:
                miniGame = "IZombie";
                break;
            // case 4:
            // miniGame = "Beghouled";
            // break;
            // case 5:
            // miniGame = "Zombotany";
            // break;
            default:
                return new Result("Invalid mini game number!");
        }
        int levelNumber = Integer.parseInt(matcher.group("level"));
        Level level = LevelLoader.loadLevel(miniGame, levelNumber);
        if (level.hasPreGame())
            return new Result(new PreGameMenu(level));
        GameContext context = new GameContext(level);
        AppContext.getInstance().setGameContext(context);
        return new Result("Game started!", new RunningGameMenu(new GameController(context)));
    }

    public Result exit(Matcher matcher) {
        return new Result("Exited to " + new MainMenu().getName(), new MainMenu());
    }
}
