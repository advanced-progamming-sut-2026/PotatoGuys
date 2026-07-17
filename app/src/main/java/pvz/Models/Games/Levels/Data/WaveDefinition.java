package pvz.Models.Games.Levels.Data;

import java.util.List;

public class WaveDefinition {
    public int waveNumber;
    public boolean isFinalWave;
    public int delayTicks;
    public List<WavePhaseDefinition> phases;
    public int lanes;
    public int difficulty;
}
