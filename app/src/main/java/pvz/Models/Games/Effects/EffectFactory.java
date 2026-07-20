package pvz.Models.Games.Effects;

import pvz.Models.Games.Levels.Data.EffectDefinition;

public class EffectFactory {
    public static ChapterEffect createEffect(EffectDefinition def) {
        if (def.type == null) return null;
        switch (def.type) {
            case "SAND_STORM":
                return new SandStormEffect(def.intervalTicks);
            default:
                return null;
        }
    }
}
