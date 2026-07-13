package pvz.View;

import pvz.Controller.QuestController;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestMenu implements Menu {
    private final QuestController controller;

    public QuestMenu(QuestController controller) {
        this.controller = controller;
    }

    @Override
    public Result handleInput(String input) {
        // Handle command: travel log page <page_name>
        Pattern pagePattern = Pattern.compile("travel log page (\\w+)");
        Matcher matcher = pagePattern.matcher(input);

        if (matcher.matches()) {
            String pageName = matcher.group(1);
            String output = controller.showTravelLogPage(pageName);
            return new Result(output);
        }

        // Command to claim a reward (custom command for testing/interaction)
        Pattern claimPattern = Pattern.compile("claim quest (\\w+)");
        Matcher claimMatcher = claimPattern.matcher(input);

        if (claimMatcher.matches()) {
            String questId = claimMatcher.group(1);
            return new Result(controller.claimQuestReward(questId));
        }

        return new Result("Invalid command.");
    }

    @Override
    public String getName() {
        return "Travel Log";
    }

    @Override
    public Result onEnter() {
        return new Result("Entered Travel Log (Quests).");
    }
}

//