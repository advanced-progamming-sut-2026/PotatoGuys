package pvz.Models.Seasons.Levels;

import pvz.Models.Engine.TickAware;
import pvz.Models.Entities.Zombies.ZombieGameContext;
import pvz.Models.Entities.Zombies.ZombieType;

import java.util.List;
import java.util.Random;

public class Wave implements TickAware {
    private int waveNumber;
    private boolean isFinalWave;
    private int baseTotalWaveCost;
    private List<WavePhase> phases;
    private ZombieGameContext context;
    private int lanes;
    private int difficulty;

    int currentPhase;
    int remainingInPhase;
    private int ticksUntilNextSpawn;
    private boolean done;
    private Random rand;

    public Wave(int waveNumber, boolean isFinalWave, int totalWaveCost, List<WavePhase> phases,
                ZombieGameContext context, int lanes, int difficulty) {
        this.waveNumber = waveNumber;
        this.isFinalWave = isFinalWave;
        this.baseTotalWaveCost = totalWaveCost;
        this.phases = phases;
        this.context = context;
        this.lanes = lanes;
        this.difficulty = difficulty;
        this.currentPhase = 0;
        this.remainingInPhase = phases.getFirst().getZombieCount();
        this.ticksUntilNextSpawn = phases.getFirst().getIntervalTicks();
        this.done = false;
        this.rand = new Random();
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

    public boolean isDone() {
        return done;
    }

    @Override
    public void enter() {
        context.log("Wave " + waveNumber + " started" + (isFinalWave ? " — FINAL WAVE!" : ""));
        if (phases.get(currentPhase).isBurst()) {
            context.log("Wave " + waveNumber + " phase " + (currentPhase + 1) + " [BURST]");
        }
    }

    @Override
    public void update() {
        if (done) return;

        ticksUntilNextSpawn--;
        if (ticksUntilNextSpawn > 0) return;

        WavePhase phase = phases.get(currentPhase);
        int spawnCount = phase.isBurst() ? 3 : 1;

        for (int i = 0; i < spawnCount && remainingInPhase > 0; i++) {
            spawnZombie(phase);
            remainingInPhase--;
        }

        if (remainingInPhase <= 0) {
            if (currentPhase >= phases.size() - 1) {
                done = true;
                context.log("Wave " + waveNumber + " completed.");
                return;
            }
            currentPhase++;
            remainingInPhase = phases.get(currentPhase).getZombieCount();
            context.log("Wave " + waveNumber + " phase " + (currentPhase + 1)
                    + (phases.get(currentPhase).isBurst() ? " [BURST]" : ""));
        }

        ticksUntilNextSpawn = phases.get(currentPhase).getIntervalTicks();
    }

    private void spawnZombie(WavePhase phase) {
        List<ZombieType> allowed = phase.getAllowedTypes();
        if (allowed == null || allowed.isEmpty()) return;

        ZombieType type = allowed.get(rand.nextInt(allowed.size()));
        int lane = rand.nextInt(lanes);
        int col = context.getColumns() - 1;

        context.spawnZombie(type.getAlias(), col, lane);
    }

    @Override
    public void dispose() {
        done = true;
    }
}
