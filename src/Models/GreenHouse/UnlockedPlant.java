package Models.GreenHouse;

public class UnlockedPlant extends GreenHousePlant {

    private String plantType;

    public UnlockedPlant(String plantType) {
        super(8);
        this.plantType = plantType;
    }

    public String getPlantType() {
        return plantType;
    }
}
