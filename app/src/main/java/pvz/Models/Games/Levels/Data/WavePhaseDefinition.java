package pvz.Models.Games.Levels.Data;

import pvz.Models.Entities.Zombies.ZombieType;

import java.util.List;

public class WavePhaseDefinition {
    public int zombieCount;
    public int intervalTicks;
    public List<ZombieType> allowedTypes;
    public boolean isBurst;
}
