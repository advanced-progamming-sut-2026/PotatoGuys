package Models.Plants;

import javax.print.attribute.standard.PrinterLocation;

import Models.Plants.Enums.PlantStorage;
import Models.Plants.Enums.PlantType;

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
