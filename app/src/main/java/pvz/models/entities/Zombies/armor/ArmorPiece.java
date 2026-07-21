package pvz.models.entities.zombies.armor;

import java.util.HashSet;
import java.util.Set;

/**
 * A runtime armour component attached to a {@link pvz.models.entities.zombies.Zombie}.
 *
 * <p>Mapped from a JSON {@code ArmorPropertySheet}. The armour tracks its own HP
 * across three visual damage layers (intact → damaged → critical) using the
 * {@code ArmorLayerHealth} thresholds from the data sheet (typically 0.666 and 0.333).
 *
 * <h3>Damage routing logic</h3>
 * <ul>
 *   <li>Normal ({@code DAMAGEABLE} without {@code PASSDAMAGE}): absorbs damage;
 *       only the overflow amount propagates onward.</li>
 *   <li>{@code PASSDAMAGE} (e.g. Shoulder Armour): the armour takes damage
 *       <em>and</em> the full incoming amount is still passed to the next piece.
 *       This models the Dark Ages Knight where both shoulder pads and the crown
 *       can be depleted by the same hit.</li>
 * </ul>
 */
public class ArmorPiece {

    /** Maximum number of visual damage layers (intact + up to 2 damaged states). */
    private static final int MAX_LAYERS = 3;

    private final ArmorType type;
    private final float baseHealth;
    private final Set<ArmorFlag> flags;
    private final float[] layerThresholds; // fraction of baseHealth, e.g. [0.666, 0.333]

    private float currentHealth;
    private int layerIndex; // 0=intact, 1=damaged, 2=critical

    public ArmorPiece(ArmorType type, float baseHealth,
                      Set<ArmorFlag> flags, float[] layerThresholds) {
        this.type = type;
        this.baseHealth = baseHealth;
        this.flags = new HashSet<>(flags);
        this.layerThresholds = layerThresholds.clone();
        this.currentHealth = baseHealth;
        this.layerIndex = 0;
    }

    // ── Damage API ────────────────────────────────────────────────────────────

    /**
     * Route incoming damage through this armour piece.
     *
     * @param damage raw incoming damage (always positive)
     * @return the amount of damage that should continue to the next armour or body HP:
     *         <ul>
     *           <li>0 if this piece absorbed everything ({@code currentHealth} still &gt; 0)</li>
     *           <li>overflow ({@code damage - baseHealth}) if the piece was destroyed</li>
     *           <li>full {@code damage} if the {@code PASSDAMAGE} flag is set</li>
     *         </ul>
     */
    public float absorbDamage(float damage) {
        if (!flags.contains(ArmorFlag.DAMAGEABLE)) return damage;

        currentHealth -= damage;
        refreshLayerIndex();

        if (hasFlag(ArmorFlag.PASSDAMAGE)) {
            // Shoulder armour: deplete but pass all damage through
            if (currentHealth <= 0) currentHealth = 0;
            return damage;
        }

        if (currentHealth <= 0) {
            float overflow = -currentHealth; // positive remainder
            currentHealth = 0;
            return overflow;
        }
        return 0f; // absorbed all
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public boolean isDestroyed() { return currentHealth <= 0; }
    public boolean hasFlag(ArmorFlag flag) { return flags.contains(flag); }
    public boolean isHelm()      { return hasFlag(ArmorFlag.HELM); }
    public boolean isMetallic()  { return hasFlag(ArmorFlag.METALLIC); }

    /**
     * CLI status string appended next to the zombie's name.
     * Returns empty string when the armour is fully destroyed.
     * Examples: {@code "[Intact Bucket]"}, {@code "[Damaged Cone]"}, {@code "[Critical Crown]"}.
     */
    public String getCurrentArmorStatus() {
        if (isDestroyed()) return "";
        String[] labels = {"Intact", "Damaged", "Critical"};
        String label = (layerIndex < labels.length) ? labels[layerIndex] : "Critical";
        return "[" + label + " " + type.name() + "]";
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public ArmorType getType()         { return type; }
    public float getCurrentHealth()    { return currentHealth; }
    public float getBaseHealth()       { return baseHealth; }
    public int getLayerIndex()         { return layerIndex; }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void refreshLayerIndex() {
        if (baseHealth <= 0) return;
        float pct = currentHealth / baseHealth;
        if (layerThresholds.length >= 2 && pct <= layerThresholds[1]) {
            layerIndex = 2;
        } else if (layerThresholds.length >= 1 && pct <= layerThresholds[0]) {
            layerIndex = 1;
        } else {
            layerIndex = 0;
        }
    }
}
