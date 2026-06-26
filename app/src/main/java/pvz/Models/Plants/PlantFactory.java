package pvz.Models.Plants;

import pvz.Models.Plants.Enums.PlantStorage;
import pvz.Models.Plants.Enums.PlantType;

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
