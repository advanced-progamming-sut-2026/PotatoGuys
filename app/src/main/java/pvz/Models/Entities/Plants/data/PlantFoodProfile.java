package pvz.models.entities.plants.data;

import java.util.List;

/** Parsed shape of a plant's Plant-Food effect (see {@link PlantFoodKind}). */
public final class PlantFoodProfile {
    private final PlantFoodKind kind;
    private final float amount;
    private final int count;
    private final float durationSeconds;
    private final List<String> flags;
    private final String description;

    public PlantFoodProfile(PlantFoodKind kind, float amount, int count,
                             float durationSeconds, List<String> flags, String description) {
        this.kind = kind;
        this.amount = amount;
        this.count = count;
        this.durationSeconds = durationSeconds;
        this.flags = flags == null ? List.of() : List.copyOf(flags);
        this.description = description;
    }

    public PlantFoodKind getKind()      { return kind; }
    public float getAmount()            { return amount; }
    public int getCount()               { return count; }
    public float getDurationSeconds()   { return durationSeconds; }
    public List<String> getFlags()      { return flags; }
    public String getDescription()      { return description; }
}
