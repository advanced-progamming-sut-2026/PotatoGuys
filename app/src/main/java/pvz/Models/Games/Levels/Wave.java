package pvz.Models.Games.Levels;

import java.util.List;
import java.util.Random;

import pvz.Models.AppContext;
import pvz.Models.Entities.Zombies.Zombie;
import pvz.Models.Entities.Zombies.ZombieFactory;
import pvz.Models.Entities.Zombies.ZombieType;
import pvz.Models.Games.GameContext;
import pvz.Models.User.Collection;

public class Wave{
    private int waveNumber;
    private boolean isFinalWave;
    private int baseTotalWaveCost;
    private List<WavePhase> phases;
    private int lanes;
    private int difficulty;

    private int currentPhase;
    private int remainingInPhase;
    private int ticksUntilNextSpawn;
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
        this.ticksUntilNextSpawn = phases.get(currentPhase  ).getIntervalTicks();
        this.done = false;
    }
    
    public Wave() {}

    private void ensureInitialized() {
        if (this.remainingInPhase == 0 && this.ticksUntilNextSpawn == 0 && !done) {
             this.currentPhase = 0;
             this.remainingInPhase = phases.get(currentPhase).getZombieCount();
             this.ticksUntilNextSpawn = phases.get(currentPhase).getIntervalTicks();
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

    public void updateWave(GameContext context) {
        ensureInitialized();
        if (done) return;

        ticksUntilNextSpawn--;
        if (ticksUntilNextSpawn > 0) return;

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

        ticksUntilNextSpawn = phases.get(currentPhase).getIntervalTicks();
    }

    private void spawnZombie(GameContext context, WavePhase phase) {
        List<ZombieType> allowed = phase.getAllowedTypes();
        if (allowed == null || allowed.isEmpty()) return;

        ZombieType type = allowed.get(rand.nextInt(allowed.size()));
        int lane = rand.nextInt(lanes);
        int col = context.getColumns() - 1;

        // Sandstorm: during final wave burst in Ancient Egypt, zombies are carried
        // deeper into the map (1-4 columns from the right edge)
        boolean isSandstorm = isFinalWave && phase.isBurst()
                && "ancient egypt".equalsIgnoreCase(context.getSeasonName());
        
        context.log("DEBUG: Checking sandstorm: isFinalWave=" + isFinalWave + ", isBurst=" + phase.isBurst() + ", season=" + context.getSeasonName() + ", isSandstorm=" + isSandstorm);
        
        if (isSandstorm) {
            col = context.getColumns() - 2 - rand.nextInt(4);
            context.log("A sandstorm carries a " + type.getAlias() + " to column " + col + "!");
        }

        Zombie newZombie = new ZombieFactory().create(type.getAlias(), col, lane, context, waveNumber, difficulty);
        context.spawnZombie(newZombie);

        //Adding this zombie type to user collection if user hasn't seen this type of zombie yet
        Collection collection=AppContext.getInstance().getCurrentUser().getProfile().getCollection();
        if (!collection.getUnlockedZombies().contains(type)){
            collection.unlockZombie(type);
        }
    }

    public void dispose() {
        done = true;
    }
}
