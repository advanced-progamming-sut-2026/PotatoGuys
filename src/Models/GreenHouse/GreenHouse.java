package Models.GreenHouse;

public class GreenHouse {
    private Pot[][] pots;
    public GreenHouse() {}
    public Pot getPot(int x,int y){ return getPots()[0][0];}

    public Pot[][] getPots() {
        return pots;
    }
}
