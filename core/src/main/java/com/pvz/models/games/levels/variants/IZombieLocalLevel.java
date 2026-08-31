package com.pvz.models.games.levels.variants;

import java.util.List;

import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.games.levels.Level;
import com.pvz.models.games.levels.LevelType;
import com.pvz.models.games.map.data.GameMapDefinition;
import com.pvz.models.games.modes.GameModeType;
import com.pvz.models.user.MyPlant;

/**
 * A local two-player I,Zombie level: the plant side (mouse) defends the left
 * side of the board, the zombie side (keyboard) attacks from the right, both
 * on the same screen. Unlike {@link IZombieLevel} this carries a separate
 * starting sun pool for each side so the match is fair from the first second.
 */
public class IZombieLocalLevel extends Level {
    private List<MyPlant> basedPlants;
    private List<ZombieType> basedZombies;
    private int redLineColumn;
    private int plantSun;
    private int zombieSun;

    public IZombieLocalLevel() {
        super();
        this.redLineColumn = 6;
        this.plantSun = 150;
        this.zombieSun = 150;
    }

    public IZombieLocalLevel(GameMapDefinition gameMap, int levelNumber, LevelType type, int initialSun,
                             List<MyPlant> basedPlants, List<ZombieType> basedZombies,
                             int plantSun, int zombieSun) {
        super(GameModeType.SPLIT_IZOMBIE, gameMap, levelNumber, type, initialSun);
        this.basedPlants = basedPlants;
        this.basedZombies = basedZombies;
        this.redLineColumn = 4;
        this.plantSun = plantSun;
        this.zombieSun = zombieSun;
    }

    public List<MyPlant> getBasedPlants() {
        return basedPlants;
    }

    public List<ZombieType> getBasedZombies() {
        return basedZombies;
    }

    public int getRedLineColumn() {
        return redLineColumn;
    }

    /** Starting sun pool for the plant (mouse) player. */
    public int getPlantSun() {
        return plantSun;
    }

    /** Starting sun pool for the zombie (keyboard) player. */
    public int getZombieSun() {
        return zombieSun;
    }

    @Override
    public boolean hasPreGame() {
        return false;
    }
}
