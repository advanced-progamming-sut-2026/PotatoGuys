package pvz.Models.Games.Levels;

import pvz.Models.Entities.Zombies.ZombieType;
import pvz.Models.Games.Levels.Data.LevelDefinition;
import pvz.Models.Games.Levels.Data.NormalLevelDefinition;
import pvz.Models.Games.Levels.Data.TileDefinition;
import pvz.Models.Games.Levels.Data.WaveDefinition;
import pvz.Models.Games.Levels.Data.WavePhaseDefinition;
import pvz.Models.Games.map.GameMap;
import pvz.Models.Games.map.TileType;

import java.util.ArrayList;
import java.util.List;

public class LevelFactory {
    public static Level createLevel(LevelDefinition def) {
        GameMap map;
        if (def.map != null) {
            map = new GameMap(def.map.rows, def.map.columns);
            if (def.map.specialTiles != null) {
                for (TileDefinition tileDef : def.map.specialTiles) {
                    map.getTile(tileDef.x, tileDef.y).setType(TileType.valueOf(tileDef.type));
                }
            }
        } else {
            map = new GameMap(); // Default map
        }
        
        if (def instanceof NormalLevelDefinition normalDef) {
            List<Wave> waves = new ArrayList<>();
            for (WaveDefinition waveDef : normalDef.waves) {
                List<WavePhase> phases = new ArrayList<>();
                for (WavePhaseDefinition phaseDef : waveDef.phases) {
                    List<ZombieType> allowedTypes = new ArrayList<>();
                    for (String typeName : phaseDef.allowedTypes) {
                        allowedTypes.add(ZombieType.valueOf(typeName));
                    }
                    phases.add(new WavePhase(phaseDef.zombieCount, phaseDef.intervalTicks, allowedTypes, phaseDef.isBurst));
                }
                waves.add(new Wave(waveDef.waveNumber, waveDef.isFinalWave, waveDef.delayTicks, phases, waveDef.lanes, waveDef.difficulty));
            }
            return new NormalLevel(map, normalDef.levelNumber, LevelType.NORMAL, normalDef.initialSun, waves);
        }
        return null;
    }
}
