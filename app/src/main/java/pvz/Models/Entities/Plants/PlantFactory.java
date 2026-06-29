package pvz.Models.Entities.Plants;

import pvz.Models.Entities.Plants.Enums.PlantStorage;
import pvz.Models.Entities.Plants.Enums.PlantType;

public class PlantFactory {
    public Plant createPlant(PlantType type){
        switch (type) {
            case Sunflower:
                return PlantStorage.Sunflower.getCopy();
        
            default:
                break;
        }
        return null;
    }
}
