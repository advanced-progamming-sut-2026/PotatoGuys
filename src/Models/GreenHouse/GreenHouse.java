package Models.GreenHouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GreenHouse {

    public static final HEIGHT = 4 ;
    public static final WIDTH = 5 ;

    private List<GreenHousePot> greenHousePots = new ArrayList<>();

    public GreenHouse() {

        for(int y = 1; y<=HEIGHT ; y++){
            for(int x=1 ; x<= WIDTH ;x++){
                boolean locked = y >= 2;
                greenHousePots.add(new GreenHousePot(x,y,locked));
            }
        }
    }

    public GreenHousePot getPot(int x, int y){
        for(GreenHousePot greenHousePot : greenHousePots){
            if(greenHousePot.getX()==x && greenHousePot.getY()==y)
                return greenHousePot;
        }

        return null;
    }

}

    public List<GreenHousePot> getGreenHousePots() {
        return greenHousePots;
}

    public boolean isValidCoordinate(int x, int y) {
        return x >= 1 && x <= WIDTH && y >= 1 && y <= HEIGHT;
}

    public boolean unlockPot(int x , int y) {
        GreenHousePot pot = getPot(x, y);

        if (pot == null || !pot.isLocked()) {

        }

        pot.unlock();
        return true;
    }

    public boolean plantPotAt(int x, int y, GreenHousePlant plant) {
        GreenHousePot pot = getPot(x, y);

        if (pot == null || pot.isLocked() || !pot.isEmpty() || plant == null) {
            return false;
        }

        pot.setPlant(plant);
        return true;
    }

    public boolean plantRandomPotAt(int x, int y, List<String>List<String> unlockedPlantTypesWithPlantFood)
        GreenHousePlant plant =  createRandomPlant(unlockedPlantTypesWithPlantFood);
        return plantPotAt(x , y, plant);
        }

    public GreenHousePlant collect(int x , int y){
        GreenHousePot pot = getPot (x, y);

        if(pot == null || pot.isLocked() || pot.isEmpty()){
            reutrn null;
        }

        GreenHousePlant plant = pot.getPlant();

        if (!plant.isReady()) {
            return null
        }
        pot.clearPlant();
        return plant;
    }

    public boolean grow (int x, int y){
        GreenHousePot pot = getPot(x, y);

        if ( pot == null || pot.isLocked() || pot.isEmpty()){
            return false;
        }

        plant.makeReadyNow();
        return true;
    }


        public int getGrowCost(int x, int y){
            GreenHousePot pot == getPot (x, y);

            if (pot == null || pot.isLocked() || pot.isEmpty()) {
                return 0;
            }
            return pot.getPlant().remainingHours();
        }

    private GreenHousePlant createRandomPlant(List<String> unlockedPlantTypesWithPlantFood) {
        boolean shouldPlantMariGold = random.nextBoolean();

    if (shouldPlantMariGold || unlockedPlantTypesWithPlantFood == null || unlockedPlantTypesWithPlantFood.isEmpty()) {
        return new MariGold();
        }

        int index = random.nextInt(unlockedPlantTypesWithPlantFood.size());
        return new UnlockedPlant(unlockedPlantTypesWithPlantFood.get(index));
    }

}

