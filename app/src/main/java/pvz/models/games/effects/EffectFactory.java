package pvz.models.games.effects;

import pvz.models.games.levels.data.EffectDefinition;

public class EffectFactory {
    public static ChapterEffect createEffect(EffectDefinition def) {
        if (def.type == null) return null;
        switch (def.type) {
            case "SAND_STORM":
                return new SandStormEffect(def.intervalTicks);
            case "COLD_WIND":
                return new ColdWindEffect(def.intervalTicks);
            case "BIG_WAVE_BEACH":
                return new BigWaveBeachEffect(def.minWaterColumn, def.maxWaterColumn);
            case "DARK_AGES":
            case "DARK_AGES_EFFECT":
                return new DarkAgesEffect(def.intervalTicks);
            default:
                return null;
        }
    }
}
