package Models.Seasons.Levels.SpecialLevels;

import Models.Seasons.Levels.SpecialLevel;

public class TimedWar extends SpecialLevel {
    private int timeLimitSeconds;
    private int targetZombieKills;

    public int getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public int getTargetZombieKills() {
        return targetZombieKills;
    }
}
