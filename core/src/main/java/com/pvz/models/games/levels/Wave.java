
package com.pvz.models.games.levels;

import java.util.List;
import java.util.Random;

import com.pvz.controller.game.GameController;
import com.pvz.models.AppContext;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.ZombieFactory;
import com.pvz.models.entities.zombies.ZombieType;
import com.pvz.models.entities.zombies.fsm.SandstormCarryState;
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

    public List<WavePhase> getPhases() {
        return phases;
    }

    public int getTotalZombieCount() {
        if (phases == null) return 0;
        int total = 0;
        for (WavePhase phase : phases) {
            total += phase.getZombieCount();
        }
        return total;
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

    /** Difficulty used for spawned zombies: the logged-in user's Difficulty setting
     *  (1-5), clamped to a safe range. Falls back to the JSON-declared wave difficulty
     *  when no user/setting is available. */
    private int effectiveDifficulty() {
        com.pvz.models.user.User user = AppContext.getInstance().getCurrentUser();
        int value = difficulty;
        if (user != null && user.getSetting() != null) {
            value = user.getSetting().getDifficulty();
        }
        return Math.max(1, Math.min(5, value));
    }

    private void spawnZombie(GameContext context, WavePhase phase) {
        List<ZombieType> allowed = phase.getAllowedTypes();
        if (allowed == null || allowed.isEmpty())
            return;

        // Guard against unknown/unregistered types deserialized as null by Gson —
        // an invalid enum name in level data must not crash the wave.
        List<ZombieType> valid = allowed.stream()
            .filter(java.util.Objects::nonNull)
            .toList();
        if (valid.isEmpty())
            return;

        ZombieType type = valid.get(rand.nextInt(valid.size()));
        int lane = rand.nextInt(lanes);

        // Every zombie spawns off-map, same as normal — sandstorm zombies are
        // never teleported directly onto the lawn. What differs is what happens
        // after spawn: a normal zombie starts walking immediately, while a
        // sandstorm zombie starts inside SandstormCarryState, which hides it in
        // a traveling sand-cloud effect until it reaches its landing column.
        int spawnCol = context.getMap().getColumns() + 2;

        // Sandstorm: during final wave burst in Ancient Egypt, zombies are carried
        // deeper into the map (1-4 columns from the right edge) instead of
        // walking on from the edge like normal zombies.
        boolean isSandstorm = isFinalWave && phase.isBurst()
            && "ancient egypt".equalsIgnoreCase(context.getSeasonName());

        Zombie newZombie = new ZombieFactory().create(type.getAlias(), GameController.colToWorldX(spawnCol), lane, context, waveNumber, effectiveDifficulty());

        if (isSandstorm) {
            int targetCol = context.getMap().getColumns() - 2 - rand.nextInt(3);
            float targetX = GameController.colToWorldX(targetCol);
            newZombie.setPendingInitialState(new SandstormCarryState(targetX));
            context.log("A sandstorm carries a " + type.getAlias() + " toward column " + targetCol + "!");
        }

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


