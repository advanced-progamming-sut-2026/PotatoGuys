package pvz.Models.Plants.Strategies;

import pvz.Models.Plants.Strategy;
import pvz.Models.Sun.SunType;

public class SunProducers implements Strategy {
    private SunType defaultSunType;
    private int sunProduceTime;
    

    public SunProducers(SunType defaultSunType, int sunProduceTime) {
        this.defaultSunType = defaultSunType;
        this.sunProduceTime = sunProduceTime;
    }
    public SunType getDefaultSunType() {
        return defaultSunType;
    }

    public int getSunProduceTime() {
        return sunProduceTime;
    }

    public void defaultApply(){

    }
    public void boostApply(){

    }

}
