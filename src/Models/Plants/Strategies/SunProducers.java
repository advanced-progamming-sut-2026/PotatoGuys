package Models.Plants.Strategies;

import Models.Plants.Strategy;
import Models.Plants.SunProducers.SunType;

public class SunProducers implements Strategy {
    private SunType defaultSunType;
    private int sunProduceTime;
    public SunProducers(SunType defaultSunType, int sunProduceTime) {
        this.defaultSunType = defaultSunType;
        this.sunProduceTime = sunProduceTime;
    }

    public void defaultApply(){

    }
    public void boostApply(){

    }

}
