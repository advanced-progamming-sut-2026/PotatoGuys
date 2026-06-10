import java.util.ArrayList;
import java.util.List;

public class GreenHouse {

    private List<Pot> pots = new ArrayList<>();

    public Greenhouse() {

        for(int y=1;y<=4;y++){
            for(int x=1;x<=5;x++){

                boolean locked = column >= 2; 

                pots.add(new Pot(x,y,locked));
            }
        }
    }

    public Pot getPot(int x,int y){

        for(Pot pot : pots){
            if(pot.getX()==x && pot.getY()==y)
                return pot;
        }

        return null;
    }

}
