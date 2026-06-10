package Models.Plants;

import java.util.List;
import java.util.stream.Stream;

import Models.DataTypes.Vector2;
import Models.Plants.Enums.PlantCategory;
import Models.Plants.Enums.PlantTag;
import Models.Plants.Enums.PlantType;

public abstract class Plant {
    private Vector2 position;
    private Vector2 speed;

    private final PlantType type;
    private final PlantCategory category;
    private final List<PlantTag> tags;
    private Strategy strategy;
    
    private int sunCost;
    private int baseHP;
    private int baseRecharge;
    private int baseActionInterval;

    private int Recharge;
    private int ActionInterval;
    private int HP;
    private int level;
    private boolean isBoosted;

}
