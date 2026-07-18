package pvz.Models.Games.Levels;

import pvz.Models.Entities.Zombies.ZombieType;
import pvz.Models.Games.Levels.Data.*;
import pvz.Models.Games.map.GameMap;
import pvz.Models.Games.map.behaviors.DestructibleBehavior;
import pvz.Models.Games.map.behaviors.SlipperyBehavior;

import java.util.ArrayList;
import java.util.List;

public class LevelFactory {
    public static Level createLevel(LevelDefinition def) {
        GameMap map = new GameMap(def.map.rows, def.map.columns);
        if (def.map.specialTiles != null) {
            for (TileDefinition tileDef : def.map.specialTiles) {
                if (tileDef.behaviors != null) {
                    for (BehaviorDefinition behDef : tileDef.behaviors) {
                        switch (behDef.type) {
                            case "DESTRUCTIBLE" -> map.getTile(tileDef.x, tileDef.y).addBehavior(new DestructibleBehavior(behDef.hp, behDef.name));
                            case "SLIPPERY" -> map.getTile(tileDef.x, tileDef.y).addBehavior(new SlipperyBehavior(behDef.laneDelta));
                        }
                    }
                }
            }
        }

        if (def instanceof NormalLevelDefinition normalDef) {
            List<Wave> waves = new ArrayList<>();
            for (WaveDefinition waveDef : normalDef.waves) {
                List<WavePhase> phases = new ArrayList<>();
                for (WavePhaseDefinition phaseDef : waveDef.phases) {
                    List<ZombieType> allowedTypes = new ArrayList<>(phaseDef.allowedTypes);
                    phases.add(new WavePhase(phaseDef.zombieCount, phaseDef.intervalTicks, allowedTypes, phaseDef.isBurst));
                }
                waves.add(new Wave(waveDef.waveNumber, waveDef.isFinalWave, waveDef.delayTicks, phases, waveDef.lanes, waveDef.difficulty));
            }

            return new NormalLevel(map, normalDef.levelNumber, LevelType.NORMAL, normalDef.initialSun, waves);
        }
        return null;
    }
}
