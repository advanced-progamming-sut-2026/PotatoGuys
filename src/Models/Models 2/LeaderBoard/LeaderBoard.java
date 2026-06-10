import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeaderBoard {

    private List<PlayerScore> players = new ArrayList<>();

    public void sortByScore(){

        players.sort(
            Comparator.comparing(PlayerScore::getHighestScore)
        );

    }
}
