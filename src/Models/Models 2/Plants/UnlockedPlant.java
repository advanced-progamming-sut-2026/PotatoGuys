public class UnlockedPlant extends Plant {

    private String plantType;

    public UnlockedPlant(String plantType) {
        super(8);
        this.plantType = plantType;
    }

    public String getPlantType() {
        return plantType;
    }
}
