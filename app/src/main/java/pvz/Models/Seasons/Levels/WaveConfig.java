package pvz.Models.Seasons.Levels;

import java.util.List;

public class WaveConfig {
    private int waveNumber;
    private boolean isFinalWave;
    private int totalWaveCost;
    private List<ZombieSpawnEntry> spawns;

    private boolean hasTornado;
    private int tornadoForwardColumns;

    public int getWaveNumber() {
        return waveNumber;
    }

    public boolean isFinalWave() {
        return isFinalWave;
    }

    public int getTotalWaveCost() {
        return totalWaveCost;
    }

    public List<ZombieSpawnEntry> getSpawns() {
        return spawns;
    }

    public boolean isHasTornado() {
        return hasTornado;
    }

    public int getTornadoForwardColumns() {
        return tornadoForwardColumns;
    }
}
