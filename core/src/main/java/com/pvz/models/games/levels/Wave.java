package com.pvz.models.games.levels;

import java.util.List;
import java.util.Random;

import com.pvz.models.AppContext;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.ZombieFactory;
import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.games.GameContext;
import com.pvz.models.user.Collection;

public class Wave {
    private int waveNumber;
    private boolean isFinalWave;
    private int baseTotalWaveCost;
    private List<WavePhase> phases;
    private int lanes;
    private int difficulty;

    private int currentPhase;
    private int remainingInPhase;
    private float secondsUntilNextSpawn;
    private boolean done;
    private static Random rand = new Random();

    public Wave(int waveNumber, boolean isFinalWave, int totalWaveCost, List<WavePhase> phases,
            int lanes, int difficulty) {
        this.waveNumber = waveNumber;
        this.isFinalWave = isFinalWave;
        this.baseTotalWaveCost = totalWaveCost;
        this.phases = phases;
        this.lanes = lanes;
        this.difficulty = difficulty;
        this.currentPhase = 0;
        this.remainingInPhase = phases.get(currentPhase).getZombieCount();
        this.secondsUntilNextSpawn = phases.get(currentPhase).getIntervalSeconds();
        this.done = false;
    }

    public Wave() {
    }

    private void ensureInitialized() {
        if (this.remainingInPhase == 0 && this.secondsUntilNextSpawn == 0 && !done) {
            this.currentPhase = 0;
            this.remainingInPhase = phases.get(currentPhase).getZombieCount();
            this.secondsUntilNextSpawn = phases.get(currentPhase).getIntervalSeconds();
        }
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

    public void startWave(GameContext context) {
        ensureInitialized();
        context.log("Wave " + waveNumber + " started" + (isFinalWave ? " — FINAL WAVE!" : ""));
        if (phases.get(currentPhase).isBurst()) {
            context.log("Wave " + waveNumber + " phase " + (currentPhase + 1) + " [BURST]");
        }
    }

    public void updateWave(GameContext context, float dt) {
        ensureInitialized();
        if (done)
            return;

        secondsUntilNextSpawn -= dt;
        if (secondsUntilNextSpawn > 0.01)
            return;

        WavePhase phase = phases.get(currentPhase);
        int spawnCount = phase.isBurst() ? 3 : 1;

        for (int i = 0; i < spawnCount && remainingInPhase > 0; i++) {
            spawnZombie(context, phase);
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

        secondsUntilNextSpawn = phases.get(currentPhase).getIntervalSeconds();
    }

    private void spawnZombie(GameContext context, WavePhase phase) {
        List<ZombieType> allowed = phase.getAllowedTypes();
        if (allowed == null || allowed.isEmpty())
            return;

        ZombieType type = allowed.get(rand.nextInt(allowed.size()));
        int lane = rand.nextInt(lanes);
        int col = context.getColumns() - 1;

        // Sandstorm: during final wave burst in Ancient Egypt, zombies are carried
        // deeper into the map (1-4 columns from the right edge)
        boolean isSandstorm = isFinalWave && phase.isBurst()
                && "ancient egypt".equalsIgnoreCase(context.getSeasonName());

        context.log("DEBUG: Checking sandstorm: isFinalWave=" + isFinalWave + ", isBurst=" + phase.isBurst()
                + ", season=" + context.getSeasonName() + ", isSandstorm=" + isSandstorm);

        if (isSandstorm) {
            col = context.getColumns() - 2 - rand.nextInt(4);
            context.log("A sandstorm carries a " + type.getAlias() + " to column " + col + "!");
        }

        Zombie newZombie = new ZombieFactory().create(type.getAlias(), col, lane, context, waveNumber, difficulty);
        context.spawnZombie(newZombie);

        // Adding this zombie type to user collection if user hasn't seen this type of
        // zombie yet
        Collection collection = AppContext.getInstance().getCurrentUser().getProfile().getCollection();
        if (!collection.getUnlockedZombies().contains(type)) {
            collection.unlockZombie(type);
        }
    }

    public void dispose() {
        done = true;
    }
}
