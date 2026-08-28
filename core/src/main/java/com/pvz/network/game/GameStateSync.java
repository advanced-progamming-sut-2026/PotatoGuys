package com.pvz.network.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pvz.controller.game.GameController;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.PlantFactory;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.sun.Sun;
import com.pvz.models.entities.sun.SunType;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.ZombieFactory;
import com.pvz.models.entities.zombies.fsm.WalkState;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.modes.variants.IZombieMode;

/**
 * Turns a host's GameContext into a lightweight GameSnapshot, and applies one
 * back into a guest's local GameContext.
 *
 * Guest-side reconstruction reuses the exact same PlantFactory/ZombieFactory
 * the single-player game already uses, so sprites/animations look right —
 * only hp/position get overwritten per snapshot, nothing is locally
 * re-simulated. Two things the engine normally does for you, that the guest
 * must do by hand here since it never runs GameEngine#update:
 *
 * 1. Plant#enter()/Zombie#enter() — this is where their FSM `currentState`
 * actually gets set (not in the constructor). Skipping it leaves
 * currentState null and draw() breaks.
 * 2. GameContext#flushPending() — spawnPlant/spawnZombie/spawnSun only
 * queue into a pending-add list; only flushPending() (normally called
 * from GameEngine#update) moves them into the live, rendered lists.
 */
public class GameStateSync {

    private GameStateSync() {
    }

    // ---- Host side: build
    // ----------------------------------------------------------

    public static GameSnapshot buildSnapshot(GameContext ctx) {
        GameSnapshot snap = new GameSnapshot();
        snap.currentSun = ctx.getCurrentSun();
        snap.gameOver = ctx.isGameOver();

        List<Zombie> sunProducers = List.of();
        if (ctx.getMode() instanceof IZombieMode izMode) {
            snap.brainsEaten = izMode.getBrainsEaten().clone();
            snap.plantSurvivalSecondsRemaining = izMode.getPlantSurvivalSecondsRemaining();
            sunProducers = izMode.getSunProducers();
        }
        List<GameSnapshot.PlantSnap> plants = new ArrayList<>();
        for (Plant p : ctx.getPlants()) {
            if (p.isDead())
                continue;
            GameSnapshot.PlantSnap ps = new GameSnapshot.PlantSnap();
            ps.id = System.identityHashCode(p);
            ps.type = p.getType().name();
            ps.col = p.getCol();
            ps.lane = p.getLane();
            ps.level = p.getLevel();
            ps.boosted = p.isBoosted();
            ps.hp = p.getHp();
            plants.add(ps);
        }
        snap.plants = plants;

        List<GameSnapshot.ZombieSnap> zombies = new ArrayList<>();
        for (Zombie z : ctx.getZombies()) {
            if (z.isDead())
                continue;
            GameSnapshot.ZombieSnap zs = new GameSnapshot.ZombieSnap();
            zs.id = System.identityHashCode(z);
            zs.alias = z.getSheet().getAlias();
            zs.x = z.getX();
            zs.lane = GameController.worldYtoLane(z.getY());
            zs.hp = z.getHp();
            zs.sunProducer = sunProducers.contains(z);
            zs.eating = z.getCurrentState() instanceof com.pvz.models.entities.zombies.fsm.EatState;
            zombies.add(zs);
        }
        snap.zombies = zombies;

        List<GameSnapshot.SunSnap> suns = new ArrayList<>();
        boolean isIZombie = ctx.getMode() instanceof IZombieMode;
        for (Sun s : ctx.getSuns()) {
            if (s.isDone())
                continue;
            // In I, Zombie the zombie-owned suns (from sun producers) are generated
            // locally on the guest, so the host only syncs the plant-owned ones — those
            // are the sunflower drops the zombie player can click to have the host
            // collect. In other modes sync all suns.
            if (isIZombie && s.getOwner() != Sun.SunOwner.PLANT)
                continue;
            if (!isIZombie && s.getOwner() == Sun.SunOwner.ZOMBIE)
                continue;
            GameSnapshot.SunSnap ss = new GameSnapshot.SunSnap();
            ss.id = System.identityHashCode(s);
            ss.sunType = s.getType().name();
            ss.col = s.getCol();
            ss.lane = s.getLane();
            ss.amount = s.getAmount();
            suns.add(ss);
        }
        snap.suns = suns;

        List<GameSnapshot.ProjectileSnap> projectiles = new ArrayList<>();
        for (com.pvz.models.entities.projectile.Projectile p : ctx.getProjectiles()) {
            if (p.isDead())
                continue;
            GameSnapshot.ProjectileSnap ps = new GameSnapshot.ProjectileSnap();
            ps.id = System.identityHashCode(p);
            ps.type = p.getType().name();
            ps.x = p.getX();
            ps.y = p.getY();
            ps.velX = p.getVelocity().x;
            ps.velY = p.getVelocity().y;
            ps.damage = p.getDamage();
            ps.lobbed = p.getMotionState()
                    instanceof com.pvz.models.entities.projectile.fsm.LobbedMotionState;
            ps.launchLane = com.pvz.controller.game.GameController.worldYtoLane(p.getY());
            projectiles.add(ps);
        }
        snap.projectiles = projectiles;

        return snap;
    }

    // ---- Guest side: apply
    // ----------------------------------------------------------

    /**
     * One of these per active match on the guest — tracks host-id -> local-object
     * correspondence across snapshots so add/update/remove can be diffed.
     */
    public static class GuestMirrorState {
        public final Map<Integer, Plant> plantsById = new HashMap<>();
        public final Map<Integer, Zombie> zombiesById = new HashMap<>();
        public final Map<Integer, Sun> sunsById = new HashMap<>();
        public final Map<Integer, com.pvz.models.entities.projectile.Projectile> projectilesById = new HashMap<>();

        /**
         * Reverse lookup: given a locally-reconstructed Sun the guest just clicked,
         * find the host's original synthetic id for it — needed to tell the host
         * which sun to collect, since the guest's own Sun object has a different
         * identityHashCode than the host's. Null if not found (e.g. it was already
         * removed by a snapshot that arrived between the click and this lookup).
         */
        public Integer idForSun(Sun s) {
            for (Map.Entry<Integer, Sun> entry : sunsById.entrySet()) {
                if (entry.getValue() == s)
                    return entry.getKey();
            }
            return null;
        }
    }

    public static void applySnapshot(GameContext ctx, GameSnapshot snap, GuestMirrorState mirror) {
        ctx.setCurrentSunDirect(snap.currentSun);

        if (ctx.getMode() instanceof IZombieMode izMode && snap.brainsEaten != null) {
            boolean[] local = izMode.getBrainsEaten();
            System.arraycopy(snap.brainsEaten, 0, local, 0, Math.min(local.length, snap.brainsEaten.length));
            izMode.setPlantSurvivalSecondsRemaining(snap.plantSurvivalSecondsRemaining);
        }

        applyPlants(ctx, snap.plants, mirror);
        applyZombies(ctx, snap.zombies, mirror);
        applySuns(ctx, snap.suns, mirror);
        applyProjectiles(ctx, snap.projectiles, mirror);

        // Moves everything just spawnPlant/spawnZombie/spawnSun'd (and everything
        // just removePlant/removeZombie/removeSun'd) from pending queues into the
        // live lists draw() actually reads — normally GameEngine#update does this.
        ctx.flushPending();

        if (snap.gameOver) {
            ctx.setGameOver(true);
        }
    }

    private static void applyPlants(GameContext ctx, List<GameSnapshot.PlantSnap> snaps, GuestMirrorState mirror) {
        if (snaps == null)
            return;
        Set<Integer> seen = new HashSet<>();
        for (GameSnapshot.PlantSnap ps : snaps) {
            seen.add(ps.id);
            Plant local = mirror.plantsById.get(ps.id);
            if (local == null) {
                PlantType type = PlantType.valueOf(ps.type);
                local = new PlantFactory().create(type, ps.col, ps.lane, ps.level, ps.boosted, ctx);
                if (local == null)
                    continue;
                ctx.spawnPlant(local);
                local.enter();
                mirror.plantsById.put(ps.id, local);
            }
            local.setHp(ps.hp);
        }
        mirror.plantsById.entrySet().removeIf(entry -> {
            if (seen.contains(entry.getKey()))
                return false;
            ctx.removePlant(entry.getValue());
            return true;
        });
    }

    private static void applyZombies(GameContext ctx, List<GameSnapshot.ZombieSnap> snaps, GuestMirrorState mirror) {
        if (snaps == null)
            return;
        Set<Integer> seen = new HashSet<>();
        for (GameSnapshot.ZombieSnap zs : snaps) {
            seen.add(zs.id);
            Zombie local = mirror.zombiesById.get(zs.id);
            if (local == null) {
                try {
                    local = new ZombieFactory().create(zs.alias, zs.x, zs.lane, ctx, 1, 1);
                } catch (Exception e) {
                    continue; // unknown alias, skip — shouldn't happen but don't crash the guest over it
                }
                if (zs.sunProducer) {
                    local.setPendingInitialState(new com.pvz.models.entities.zombies.fsm.IdleState());
                }
                ctx.spawnZombie(local);
                local.enter();
                mirror.zombiesById.put(zs.id, local);
                if (zs.sunProducer && ctx.getMode() instanceof IZombieMode izMode) {
                    izMode.registerRemoteSunProducer(local);
                }
            }
            local.setHp(zs.hp);
            local.setPosition(zs.x, local.getY());

            // Sync eating animation: switch to VisualEatState when host says
            // eating, back to WalkState when host says not eating.
            boolean currentlyEating = local.getCurrentState()
                    instanceof com.pvz.models.entities.zombies.fsm.VisualEatState;
            if (zs.eating && !currentlyEating) {
                local.changeState(new com.pvz.models.entities.zombies.fsm.VisualEatState());
            } else if (!zs.eating && currentlyEating) {
                local.changeState(new WalkState());
            }
        }
        mirror.zombiesById.entrySet().removeIf(entry -> {
            if (seen.contains(entry.getKey()))
                return false;
            ctx.removeZombie(entry.getValue());
            return true;
        });
    }

    private static void applySuns(GameContext ctx, List<GameSnapshot.SunSnap> snaps, GuestMirrorState mirror) {
        if (snaps == null)
            return;
        Set<Integer> seen = new HashSet<>();
        for (GameSnapshot.SunSnap ss : snaps) {
            seen.add(ss.id);
            if (!mirror.sunsById.containsKey(ss.id)) {
                Sun local = new Sun(SunType.valueOf(ss.sunType), ss.col, ss.lane, ss.amount, true, ctx);
                ctx.spawnSun(local);
                mirror.sunsById.put(ss.id, local);
            }
        }
        mirror.sunsById.entrySet().removeIf(entry -> {
            if (seen.contains(entry.getKey()))
                return false;
            ctx.removeSun(entry.getValue());
            return true;
        });
    }

    /**
     * Reconstructs host projectiles on the guest so the pea/melon/etc. flight
     * animation is actually visible (previously they were never synced at all).
     * We match by host id; existing entries just get their position/velocity
     * overwritten from the snapshot. Removed on the host are removed here.
     *
     * <p>The guest never runs collision, so reconstructed projectiles must not
     * register their hitboxes / engage the engine — we build them with
     * ProjectileFactory (for correct motion/effect states) but drop them into
     * the mirror only, not into ctx.getProjectiles(), and tick them by hand in
     * {@link #tickVisualsOnly}. Building via the factory guarantees the PAM path
     * / clip / scale for the drawn frame match the host's.
     */
    private static void applyProjectiles(GameContext ctx,
            List<GameSnapshot.ProjectileSnap> snaps, GuestMirrorState mirror) {
        if (snaps == null)
            return;
        Set<Integer> seen = new HashSet<>();
        for (GameSnapshot.ProjectileSnap ps : snaps) {
            seen.add(ps.id);
            com.pvz.models.entities.projectile.Projectile local = mirror.projectilesById.get(ps.id);
            if (local == null) {
                try {
                    com.pvz.models.entities.projectile.ProjectileType type =
                            com.pvz.models.entities.projectile.ProjectileType.valueOf(ps.type);
                    local = com.pvz.models.entities.projectile.ProjectileFactory.create(type, ctx,
                            new com.badlogic.gdx.math.Vector2(ps.x, ps.y),
                            new com.badlogic.gdx.math.Vector2(ps.velX, ps.velY), ps.damage);
                } catch (Exception e) {
                    continue; // unknown type — skip, shouldn't happen
                }
                if (ps.lobbed) {
                    com.pvz.models.entities.projectile.fsm.LobbedMotionState lobbed =
                            new com.pvz.models.entities.projectile.fsm.LobbedMotionState(
                                    new com.badlogic.gdx.math.Vector2(ps.x, ps.y), 0.8f, 90f);
                    local.setMotionState(lobbed);
                    local.setLobbed(ps.launchLane);
                }
                mirror.projectilesById.put(ps.id, local);
            }
            local.getPos().set(ps.x, ps.y);
            local.getVelocity().set(ps.velX, ps.velY);
        }
        mirror.projectilesById.entrySet().removeIf(entry -> {
            if (seen.contains(entry.getKey()))
                return false;
            entry.getValue().destroy();
            return true;
        });
    }

    /**
     * Guest-only: advances animation/visual state for the locally-mirrored
     * entities every frame, without running CollisionSystem or flushPending —
     * those stay exclusively the host's job. Any gameplay side-effect this
     * causes locally (e.g. a plant "deciding" to attack) is cosmetic-only: the
     * next snapshot overwrites hp/position/existence regardless.
     */
    public static void tickVisualsOnly(GuestMirrorState mirror, float dt) {
        for (Plant p : mirror.plantsById.values()) {
            if (!p.isDead())
                p.update(dt);
        }
        for (Zombie z : mirror.zombiesById.values()) {
            if (!z.isDead())
                z.update(dt);
        }
        for (Sun s : mirror.sunsById.values()) {
            if (!s.isDone())
                s.update(dt);
        }
        for (com.pvz.models.entities.projectile.Projectile p : mirror.projectilesById.values()) {
            if (!p.isDead())
                p.update(dt);
        }
    }

    /**
     * Guest-only: cards are TickAware too (cooldown decrements via Card#update),
     * but never get ticked since the guest doesn't run GameEngine. Without this,
     * every card's cosmetic cooldown overlay stays stuck at 100% forever — the
     * card is still fully usable (cooldownEnable is false for zombie cards
     * regardless), it just looks permanently blacked out.
     */
    public static void tickCardsOnly(GameContext ctx, float dt) {
        for (com.pvz.models.games.card.Card card : ctx.getCards()) {
            card.update(dt);
        }
    }
}
