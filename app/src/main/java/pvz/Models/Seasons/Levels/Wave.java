package pvz.Models.Seasons.Levels;

import pvz.Models.Engine.GameEngine;
import pvz.Models.Engine.TickAware;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.ZombieType;

import java.util.List;
import java.util.Random;

public class Wave implements TickAware {
    private int waveNumber;
    private boolean isFinalWave;
    private int baseTotalWaveCost;
    List<WavePhase> phases;

    int currentPhase;
    int remainingInPhase;
    private int ticksUntilNextSpawn;

    public Wave(int waveNumber, boolean isFinalWave, int totalWaveCost, List<WavePhase> phases){
        this.waveNumber=waveNumber;
        this.isFinalWave=isFinalWave;
        this.baseTotalWaveCost =totalWaveCost;
        this.phases=phases;
        currentPhase=0;
        remainingInPhase=phases.getFirst().getZombieCount();
        ticksUntilNextSpawn=phases.getFirst().getIntervalTicks();
    }

    public int getWaveNumber() {
        return waveNumber;
    }

    public boolean isFinalWave() {
        return isFinalWave;
    }

    public int getBaseTotalWaveCost() {
        return baseTotalWaveCost;
    }

    @Override
    public void enter() {

    }

    Random rand=new Random();

    @Override
    public void update() {
        ticksUntilNextSpawn--;
        if (ticksUntilNextSpawn<=0){
            spawnZombie();
            remainingInPhase--;
            if (remainingInPhase<=0){
                if (currentPhase==phases.size()-1){
                    dispose();
                    return;
                }
                currentPhase++;
                remainingInPhase=phases.get(currentPhase).getZombieCount();
            }
            ticksUntilNextSpawn=phases.get(currentPhase).getIntervalTicks();
        }
    }

    private void spawnZombie(){
        Zombie zombie;
        GameEngine.getInstance().getToAdd(zombie);
    }

    @Override
    public void dispose() {

    }
}
