package pvz.Models.Entities.Plants.Enums;

import java.util.ArrayList;
import java.util.List;

import pvz.Models.DataTypes.Vector2;
import pvz.Models.Entities.Plants.Plant;
import pvz.Models.Entities.Plants.Strategy;
import pvz.Models.Entities.Plants.Strategies.SunProducers;
import pvz.Models.Entities.Sun.SunType;
import pvz.Models.GameSession;
import pvz.Models.User.User;

public enum PlantStorage {

    Sunflower(PlantType.Sunflower, PlantCategory.SUN_PRODUCER, new ArrayList<>(List.of(PlantTag.DAY)), 50, 300, 0, 24, new SunProducers(SunType.NORMAL, 10));


    private PlantType type;
    private PlantCategory category;
    private List<PlantTag> tags;
    private int sunCost;
    private int baseHP;
    private int z;
    private int baseActionInterval;
    private int baseRecharge;
    private Strategy strategy;

    private PlantStorage(PlantType type, PlantCategory category, List<PlantTag> tags, int sunCost, int baseHP,
                         int baseRecharge, int baseActionInterval, Strategy strategy) {
        this.setType(type);
        this.setCategory(category);
        this.setTags(tags);
        this.setSunCost(sunCost);
        this.setBaseHP(baseHP);
        this.setBaseRecharge(baseRecharge);
        this.setBaseActionInterval(baseActionInterval);
        this.setStrategy(strategy);
    }

    public Plant getCopy(Vector2 position) {
        User user = GameSession.getInstance().getCurrentUser();
        return new Plant(
                position, type, category, tags, strategy, sunCost, baseHP, baseRecharge,
                baseActionInterval, user.getProfile().getCollection().getPlantLevel(type),
                user.getProfile().getCollection().isPlantBoosted(type)
        );
    }

    public PlantType getType() {
        return type;
    }

    public void setType(PlantType type) {
        this.type = type;
    }

    public PlantCategory getCategory() {
        return category;
    }

    public void setCategory(PlantCategory category) {
        this.category = category;
    }

    public List<PlantTag> getTags() {
        return tags;
    }

    public void setTags(List<PlantTag> tags) {
        this.tags = tags;
    }

    public int getSunCost() {
        return sunCost;
    }

    public void setSunCost(int sunCost) {
        this.sunCost = sunCost;
    }

    public int getBaseHP() {
        return baseHP;
    }

    public void setBaseHP(int baseHP) {
        this.baseHP = baseHP;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getBaseActionInterval() {
        return baseActionInterval;
    }

    public void setBaseActionInterval(int baseActionInterval) {
        this.baseActionInterval = baseActionInterval;
    }

    public int getBaseRecharge() {
        return baseRecharge;
    }

    public void setBaseRecharge(int baseRecharge) {
        this.baseRecharge = baseRecharge;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }
}
