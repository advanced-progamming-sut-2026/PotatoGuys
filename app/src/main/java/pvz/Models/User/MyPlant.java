package pvz.Models.User;

import pvz.Models.Entities.Plants.Enums.PlantType;

public class MyPlant {
    private PlantType Type;
    private int level;
    private boolean isBoosted;

    public PlantType getType() {
        return Type;
    }

    public void setType(PlantType type) {
        Type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isBoosted() {
        return isBoosted;
    }

    public void setBoosted(boolean boosted) {
        isBoosted = boosted;
    }
}
