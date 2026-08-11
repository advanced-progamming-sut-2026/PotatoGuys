package com.pvz.models.entities.sun;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.engine.TickAware;
import com.pvz.models.entities.plants.Plant;
import com.pvz.models.entities.plants.data.DamageKind;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.games.GameContext;
import com.pvz.view.game.GameScreen;

public class Sun implements TickAware {
    private static final String SUN_PAM = "768/INITIAL/EFFECTS/SUN/SUN.PAM";
    private static final String SUN_CLIP = "animation";

    private static final float DEFAULT_LIFESPAN_SECONDS = 50f;
    private static final float DEFAULT_FALL_SPEED = 70f;

    private final SunType type;
    private final int col;
    private final int lane;
    private final int amount;
    private final GameContext context;

    private float fallSpeed;

    private boolean fallen;
    private boolean collected;

    private float stateTime;
    private Vector2 currentPos;
    private Vector2 targetPos;

    /** Backward-compatible constructor (plant-produced suns, no falling). */
    public Sun(SunType type, int col, int lane, int amount) {
        this(type, col, lane, amount, false, null);
    }

    public Sun(SunType type, int col, int lane, int amount, boolean startFalling, GameContext context) {
        this.type = type;
        this.col = col;
        this.lane = lane;
        this.amount = amount;
        this.context = context;
        this.fallen = !startFalling;
        this.stateTime = 0;
        this.fallSpeed=DEFAULT_FALL_SPEED;
        if (startFalling){
            currentPos=new Vector2(GameController.colToWorldX(col),GameScreen.SCREEN_HEIGHT);
            targetPos=new Vector2(GameController.colToWorldX(col),GameController.laneToWorldY(lane));
        } else {
            currentPos=new Vector2(GameController.colToWorldX(col),GameController.laneToWorldY(lane));
            targetPos=new Vector2(currentPos);
        }
    }

    @Override
    public FrameConfig draw(){
        PvZ2.pamPlayer.draw(PvZ2.batch, SUN_PAM, SUN_CLIP, stateTime, currentPos.x, currentPos.y,0.7f,0.7f, true);
        return null;
    }

    @Override
    public void enter() { }

    @Override
    public void update(float dt) {
        stateTime += dt;
        if (collected) return;

        if (!fallen) {
            currentPos.add(0,-fallSpeed*dt);
            if (currentPos.y<targetPos.y){
                fallen=true;
                if (context != null) {
                    context.log("Sun reached the ground at position (" + currentPos.x + ", " + currentPos.y + ")");
                }
                if (type == SunType.RADIOACTIVE) {
                    convertToNormal();
                }
            }
        }
    }

    @Override
    public void dispose() { }

    public void collect(GameContext ctx) {
        if (isDone()) return;
        collected = true;

        if (type == SunType.RADIOACTIVE && !fallen) {
            dealExplosionDamage(ctx);
        } else {
            ctx.addSun(getAmount());
        }
        ctx.removeSun(this);
    }

    private void dealExplosionDamage(GameContext ctx) {
        ctx.log("Radioactive sun exploded at position (" + col + ", " + lane + ")!");

        List<Zombie> zombiesHit = new ArrayList<>();
        List<Plant> plantsHit = new ArrayList<>();

        for (int c = col - 2; c <= col + 2; c++) {
            for (int l = lane - 2; l <= lane + 2; l++) {
                if (c < 0 || c >= ctx.getMap().getColumns() || l < 0 || l >= ctx.getMap().getLanes()) continue;
                zombiesHit.addAll(ctx.getZombiesAt(c, l));
                plantsHit.addAll(ctx.getPlantsAt(c, l));
            }
        }
        for (Zombie z : zombiesHit) z.takeDamage(150);
        for (Plant p : plantsHit) p.takeDamage(150, DamageKind.FIXED);

        List<Zombie> zombiesCenter = new ArrayList<>();
        List<Plant> plantsCenter = new ArrayList<>();

        for (int c = col - 1; c <= col + 1; c++) {
            for (int l = lane - 1; l <= lane + 1; l++) {
                if (c < 0 || c >= ctx.getMap().getColumns() || l < 0 || l >= ctx.getMap().getLanes()) continue;
                zombiesCenter.addAll(ctx.getZombiesAt(c, l));
                plantsCenter.addAll(ctx.getPlantsAt(c, l));
            }
        }
        for (Zombie z : zombiesCenter) z.takeDamage(80);
        for (Plant p : plantsCenter) p.takeDamage(80, DamageKind.FIXED);
    }

    private void convertToNormal() {
        if (context != null) {
            context.log("Radioactive sun at (" + col + ", " + lane + ") became a normal sun upon reaching the ground.");
        }
    }

    public boolean isFalling()    { return !fallen; }
    public boolean isExpired()    { return !collected && fallen && stateTime>DEFAULT_LIFESPAN_SECONDS; }
    public boolean isCollected()  { return collected; }
    public boolean isDone()       { return collected || isExpired(); }

    public SunType getType() { return type; }
    public int getCol()      { return col; }
    public int getLane()     { return lane; }
    public int getAmount()   { return amount > 0 ? amount : type.getAmountSun(); }
    public Vector2 getCurrentPos() { return currentPos; }
    public float getX()      { return currentPos != null ? currentPos.x : GameController.colToWorldX(col); }
    public float getY()      { return currentPos != null ? currentPos.y : GameController.laneToWorldY(lane); }
}
