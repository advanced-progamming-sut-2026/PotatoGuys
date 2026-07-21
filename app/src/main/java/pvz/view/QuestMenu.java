package pvz.view;

import java.util.regex.Matcher;

import pvz.controller.QuestController;
import pvz.enums.commands.QuestMenuCommands;


public class QuestMenu implements Menu {
    private final QuestController controller;

    public QuestMenu() {
        controller = new QuestController();
    }

    @Override
    public Result handleInput(String input) {
        Matcher matcher;
        if ((matcher = QuestMenuCommands.MINI_GAME.getMatcher(input)) != null) return controller.startMiniGame(matcher);
        if ((matcher = QuestMenuCommands.SHOW_PAGE.getMatcher(input)) != null) return controller.showPage(matcher);
        if ((matcher = QuestMenuCommands.CLAIM_REWARD.getMatcher(input)) != null) return controller.claimReward(matcher);
        if ((matcher = QuestMenuCommands.SHOW_QUEST.getMatcher(input)) != null) return controller.showQuest(matcher);
        if ((matcher = QuestMenuCommands.SHOW_ALL.getMatcher(input)) != null) return controller.showAllPages(matcher);
        if ((matcher = QuestMenuCommands.EXIT.getMatcher(input)) != null) return controller.exit(matcher);
        if ((matcher = QuestMenuCommands.HELP.getMatcher(input)) != null) return new Result(QuestMenuCommands.getHelp());
        return new Result("Invalid command in Travel Log.", this);
    }

    @Override
    public String getName() {
        return "Travel Log";
    }

    @Override
    public Result onEnter() {
        return new Result("Entered Travel Log. Use 'travel log page <daily|main|epic|minigames>' to view quests or mini games.\n");
    }
}
