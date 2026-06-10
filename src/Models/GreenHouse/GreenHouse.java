import java.util.ArrayList;
import java.util.List;

public class GreenHouse {

    private List<GreenHousePot> greenHousePots = new ArrayList<>();

    public GreenHouse() {

        for(int y=1;y<=4;y++){
            for(int x=1;x<=5;x++){

                boolean locked = column >= 2; 

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
