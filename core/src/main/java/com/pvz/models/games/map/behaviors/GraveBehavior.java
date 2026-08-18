package com.pvz.models.games.map.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.pvz.PvZ2;
import com.pvz.models.AppContext;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.entities.plants.enums.PlantType;
import com.pvz.models.entities.projectile.Projectile;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.card.PlantCard;
import com.pvz.models.games.map.tile.Tile;
import com.pvz.models.games.map.tile.TileTags;

public class GraveBehavior implements TileBehavior {
    private static final String EGYPT_PAM = "768/INITIAL/GRAVESTONES/EGYPT_HIEROGLYPH/EGYPT_HIEROGLYPH.PAM";
    private static final String DARK_NOOP_PAM = "768/FULL/GRAVESTONES/DARK_NOOP/DARK_NOOP.PAM";
    private static final String DARK_SUN_PAM = "768/FULL/GRAVESTONES/DARK_SUN/DARK_SUN.PAM";
    private static final String DARK_PLANTFOOD_PAM = "768/FULL/GRAVESTONES/DARK_PLANTFOOD/DARK_PLANTFOOD.PAM";

    private static final String[] EGYPT_DAMAGE_CLIPS = {"undamaged","damage1","damage2","damage3","damage4"};
    private static final String[] DARK_DAMAGE_CLIPS = {"undamaged","damage1","damage2","damage3","damage4"};
    private static final float FLASH_DURATION = 0.28f;

    float stateTime;
    private float flashTimer;

    public enum GraveReward {
        NONE, SUN_50, PLANT_FOOD
    }

    private float hp;
    private final float maxHp;
    private final String name;
    private final GraveReward reward;
    private final Tile tile;

    public GraveBehavior(Tile tile, float hp, String name) {
        this(tile, hp, name, GraveReward.NONE);
    }

    public GraveBehavior(Tile tile, float hp, String name, GraveReward reward) {
        this.tile=tile;
        this.hp = hp;
        this.maxHp = hp;
        this.name = name;
        this.reward = reward;
        stateTime=0;
    }

    public GraveReward getReward() {
        return reward;
    }

    @Override
    public void onProjectileHit(Projectile p, Tile tile) {
        this.hp -= p.getDamage();
        flashTimer = FLASH_DURATION;
        p.destroy(); // Projectile is consumed by grave/ice
        if (this.hp <= 0) {
            destroyGrave(tile, AppContext.getInstance().getGameContext());
        }
    }

    public void destroyGrave(Tile tile, GameContext ctx) {
        tile.removeBehavior(this);
        tile.getTags().removeAll(tile.getTags().stream().filter(t->t.equals(TileTags.GRAVE)).toList());
        tile.getTags().remove(TileTags.ICE_BLOCK);

        if (ctx != null) {
            grantReward(ctx);
        }
    }

    public void grantReward(GameContext ctx) {
        if (reward == GraveReward.SUN_50) {
            ctx.addSun(50);
            ctx.log("Grave reward collected: 50 Sun!");
        } else if (reward == GraveReward.PLANT_FOOD) {
            ctx.addPlantFood(1);
            ctx.log("Grave reward collected: Plant Food!");
        }
    }

    @Override
    public void processHit(float damage) {
        TileBehavior.super.processHit(damage);
        hp-=damage;
        flashTimer = FLASH_DURATION;
        if (this.hp <= 0) {
            destroyGrave(tile, AppContext.getInstance().getGameContext());
        }
    }

    @Override
    public String getStatus() {
        return "\n    HP: "+hp;
    }

    @Override
    public boolean canPlant(PlantCard p, Tile tile) {
        return p.getPlant().getType() == PlantType.GraveBuster;
    }

    @Override
    public String getName() { return name; }

    @Override
    public void update(GameContext ctx, Tile tile, float dt) {
        TileBehavior.super.update(ctx, tile, dt);
        stateTime+=dt;
        if (flashTimer > 0f) flashTimer = Math.max(0f, flashTimer - dt);
    }

    @Override
    public FrameConfig draw(Tile tile) {
        TileBehavior.super.draw(tile);
        Vector2 pos = new Vector2(tile.getX()+Tile.WIDTH/2,tile.getY()+Tile.HEIGHT/2);
        Vector2 scale = new Vector2(0.65f,0.65f);
        float ratio = Math.max(0f, hp / maxHp);
        int idx;
        if (ratio > 0.8f) idx = 0;
        else if (ratio > 0.6f) idx = 1;
        else if (ratio > 0.4f) idx = 2;
        else idx = 3;

        String pamId;
        String[] clips;
        GameContext ctx = AppContext.getInstance().getGameContext();
        boolean isDarkAges = ctx != null && "dark ages".equalsIgnoreCase(ctx.getSeasonName());

        if (isDarkAges) {
            switch (reward) {
                case SUN_50 -> pamId = DARK_SUN_PAM;
                case PLANT_FOOD -> pamId = DARK_PLANTFOOD_PAM;
                default -> pamId = DARK_NOOP_PAM;
            }
            clips = DARK_DAMAGE_CLIPS;
        } else {
            pamId = EGYPT_PAM;
            clips = EGYPT_DAMAGE_CLIPS;
        }

        FrameConfig fc = new FrameConfig(pamId, clips[idx], stateTime, pos, scale, null, false);
        if (flashTimer > 0f) {
            fc.setColor(5f, 5f, 5f, 0.6f);
        }
        return fc;
    }
}
