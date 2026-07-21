package pvz.models.entities.zombies.data;

/**
 * Maps one entry in the JSON {@code ScaledProps} array and implements the
 * scaling formula used to ramp zombie stats across wave tiers.
 *
 * <h3>Supported formulas</h3>
 * <dl>
 *   <dt>{@code standard}</dt>
 *   <dd>Scales the stat upward per wave tier:
 *       <pre>
 *         scaledValue = base
 *                     × Arg1^tier          (exponential growth)
 *                     × (1 + Arg2 × tier)  (linear bonus on top)
 *       </pre>
 *       With the typical JSON values {@code Arg1=1.3, Arg2=0.05}, a zombie at
 *       tier 3 (late-game wave) will have roughly 2.8× its base HP.</dd>
 *   <dt>{@code constant}</dt>
 *   <dd>The stat is always the base value regardless of tier
 *       (used for {@code Speed} and {@code WavePointCost}).</dd>
 * </dl>
 *
 * <p>The {@code tier} value is derived from the current wave index inside
 * {@link pvz.models.entities.zombies.Zombie#TICKS_PER_SECOND} calculation logic.
 */
public final class ScaledProp {

    public static final String FORMULA_STANDARD = "standard";
    public static final String FORMULA_CONSTANT = "constant";

    private final String key;      // "Hitpoints" | "EatDPS" | "Speed" | "SmashDamage" etc.
    private final String formula;
    private final float arg1;      // exponent base  (e.g. 1.3)
    private final float arg2;      // linear factor  (e.g. 0.05)

    public ScaledProp(String key, String formula, float arg1, float arg2) {
        this.key = key;
        this.formula = formula;
        this.arg1 = arg1;
        this.arg2 = arg2;
    }

    /**
     * Compute the scaled value for this property.
     *
     * @param baseValue the raw value from the property sheet (e.g. 190 HP)
     * @param tier      0-based wave/difficulty tier (0 = no scaling = wave 1)
     * @return the scaled value, never less than {@code baseValue}
     */
    public float scale(float baseValue, int tier) {
        if (FORMULA_CONSTANT.equalsIgnoreCase(formula) || tier <= 0) {
            return baseValue;
        }
        double expFactor    = Math.pow(arg1, tier);
        double linearFactor = 1.0 + arg2 * tier;
        return (float) (baseValue * expFactor * linearFactor);
    }

    public String getKey()     { return key; }
    public String getFormula() { return formula; }
    public float  getArg1()    { return arg1; }
    public float  getArg2()    { return arg2; }

    @Override
    public String toString() {
        return "ScaledProp{key='" + key + "', formula='" + formula
                + "', arg1=" + arg1 + ", arg2=" + arg2 + '}';
    }
}
