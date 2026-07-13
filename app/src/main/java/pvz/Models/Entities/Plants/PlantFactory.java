package pvz.Models.Entities.Plants;

import pvz.Models.DataTypes.Vector2;
import pvz.Models.Entities.Plants.Enums.PlantStorage;
import pvz.Models.Entities.Plants.Enums.PlantType;

public class PlantFactory {
    public Plant createPlant(PlantType type, Vector2 position){
        switch (type) {
            case Sunflower:
                return PlantStorage.Sunflower.getCopy(position);
        
            default:
                break;
        }
        return null;
    }
}
