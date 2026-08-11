package com.pvz.models.entities.projectile;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;
import com.pvz.PvZ2;
import com.pvz.controller.game.GameController;
import com.pvz.models.engine.FrameConfig;
import com.pvz.models.engine.TickAware;
import com.pvz.models.entities.zombies.Zombie;
import com.pvz.models.entities.zombies.effects.EffectType;
import com.pvz.models.entities.zombies.effects.StatusEffect;
import com.pvz.models.games.GameContext;
import com.pvz.models.games.map.tile.Tile;

public class Projectile implements TickAware {

    private final GameContext ctx;
    private final ProjectileType type;

    private float stateTime = 0f;

    private Vector2 pos;
    private Vector2 vel;

    private int lastCol;
    private int lastLane;

    private final float damage;
    private final boolean poison;
    private final boolean ice;
    private final boolean fire;
    private int pierceCount;
    private boolean bouncing;
    private boolean isDead = false;

    // شعاع برخورد تیر بر حسب خانه/کاشی
    private static final float HIT_RADIUS = 50f;

    // ثبت زامبی‌های برخورد کرده برای جلوگیری از دمیج مکرر در یک فریم (در حالت نفوذی/کمانه)
    private final Set<Zombie> hitZombies = new HashSet<>();

    public Projectile(GameContext ctx, ProjectileType type, float startX, float startY , float velX , float velY ,float damage, boolean poison, boolean ice, boolean fire,int pierceCount, Object target) {
        this.ctx = ctx;
        this.type = type;
        this.pos = new Vector2(startX, startY);
        this.vel = new Vector2(velX, velY);

        this.damage = damage;
        this.poison = poison;
        this.ice = ice;
        this.fire = fire;
        this.pierceCount = pierceCount;
    }

    public void setBouncing(boolean bouncing) {
        this.bouncing = bouncing;
    }

    public boolean isDead() {
        return isDead;
    }

    // ─── Tick Update Logic ───────────────────────────────────────────────────

    @Override
    public void enter() {
        stateTime = 0f;
        lastCol = (int) Math.floor(pos.x);
        lastLane = (int) Math.floor(pos.y);
    }

    @Override
    public void update(float dt) {
        stateTime += dt;
        if (isDead) return;

        pos.x += vel.x * 80 * dt;
        pos.y += vel.y * 80 * dt;

        int col = GameController.worldXtoCol(pos.x);
        int lane = GameController.worldYtoLane(pos.y);
        if (col < -0.5f || col >= ctx.getColumns() + 0.5f || lane < -0.5f || lane >= ctx.getLanes() + 0.5f) {
            destroy();
            return;
        }

        // ۳. چک کردن ورود به کاشی (Tile) جدید

        if (col != lastCol || lane != lastLane) {
            lastCol = col;
            lastLane = lane;

            try {
                Tile tile = ctx.getTileAt(lastCol, lastLane);
                tile.processHit(this);
            } catch (IndexOutOfBoundsException ex) {
                // اگر تیر از آرایه مپ خارج شد (با وجود چک قبلی به عنوان یک لایه امنیتی)
                destroy();
                return;
            }

            // اگر با برخورد به Tile تیر از بین رفت (مثلاً برخورد به مشعل یا موانع)، ادامه نده
            if (isDead) return;
        }

        // ۴. بررسی برخورد دوبعدی با تمام زامبی‌های فعال
        checkCollisions2D();
    }

    @Override
    public FrameConfig draw(){
        PvZ2.pamPlayer.draw(PvZ2.batch, "768/INITIAL/EFFECTS/T_PEA_PROJECTILE/T_PEA_PROJECTILE.PAM" , "animation", stateTime, pos.x, pos.y, true);
        return null;
    }

    @Override
    public void dispose() {}

    // ─── 2D Collision & Bounce Logic ─────────────────────────────────────────

    private void checkCollisions2D() {
        // بررسی تمام زامبی‌های موجود در بازی (برای پشتیبانی از تیرهای چندلاین و مورب)
        for (Zombie z : ctx.getZombies()) {
            if (z.isDead() || hitZombies.contains(z)) continue;

            float zX = z.getX();
            float zY = z.getY();

            // محاسبه فاصله اقلیدسی دوبعدی بین پرتابه و زامبی
            double distance = Math.hypot(zX - pos.x, zY - pos.y);

            if (distance <= HIT_RADIUS) {
                onHitZombie(z);
                if (isDead) break;
            }
        }
    }

    private void onHitZombie(Zombie zombie) {
        hitZombies.add(zombie);

        // اعمال دمیج به زامبی
        // در متد takeDamage کلاس Zombie، آرگومان poison تعیین می‌کند که دمیج از آرمور عبور کند یا خیر.
        zombie.takeDamage(damage, poison);

        // اعمال افکت‌های وضعیتی (Slow, Unfreeze, Poison)
        applyStatusEffects(zombie);

        ctx.log("[Projectile] " + type + " hit zombie at (" + zombie.getX() + ", " + zombie.getY() + ")");

        // ۱. مدیریت کمانه کردن (برای پیاز بولینگ / Bowling Bulb)
        if (bouncing) {
            handleBounce();
            return;
        }

        // ۲. مدیریت نفوذ تیر (برای کاکتوس، قارچ دودزا و تیرهای Strike-through)
        if (pierceCount > 0) {
            pierceCount--;
        } else {
            destroy();
        }
    }

    private void applyStatusEffects(Zombie zombie) {
        // توجه: مقادیر Duration در این بخش (مثل 100 یا 50) تیک‌های بازی هستند (هر 10 تیک = 1 ثانیه)
        // بر اساس سازنده‌ی (Constructor) کلاس StatusEffect در پروژه خود، ممکن است پارامترهای دیگری نیز نیاز باشد.

        if (ice) {
            // اعمال کندی؛ به طور مثال برای 10 ثانیه (100 تیک)
            zombie.applyEffect(new StatusEffect(EffectType.CHILL, 100));
        }
        if (fire) {
            // اعمال آتش؛ طبق متد applyEffect در Zombie، اعمال BURNING خودکار باعث حذف CHILL و FROZEN می‌شود.
            zombie.applyEffect(new StatusEffect(EffectType.BURNING, 20));

        }
        if (poison) {
            // اعمال سم تدریجی؛ به طور مثال برای 5 ثانیه (50 تیک)
            // (اگر دمیج مستقیم مد نظر نیست و قصد اعمال DoT از طریق کامپوننت را دارید)
            zombie.applyEffect(new StatusEffect(EffectType.POISONED, 50));
        }
    }

    private void handleBounce() {
        Zombie nearestNextZombie = null;
        double minDistance = Double.MAX_VALUE;

        // پیدا کردن نزدیک‌ترین زامبی زنده که تیر هنوز به آن برخورد نکرده است
        for (Zombie z : ctx.getZombies()) {
            if (z.isDead() || hitZombies.contains(z)) continue;

            double dist = Math.hypot(z.getX() - pos.x, z.getY() - pos.y);
            if (dist < minDistance) {
                minDistance = dist;
                nearestNextZombie = z;
            }
        }

        // اگر زامبی دیگری در محیط وجود داشت، تیر به سمت او تغییر جهت می‌دهد
        if (nearestNextZombie != null) {
            float targetX = nearestNextZombie.getX();
            float targetY = nearestNextZombie.getY();

            // تغییر بردار جهت پرتابه به سمت زامبی جدید
            // setVelocityVector(targetX - pos.x, targetY - pos.y);
            ctx.log("[Projectile] " + type + " bounced towards target at (" + targetX + ", " + targetY + ")");
        } else {
            // اگر زامبی دیگری در صفحه نبود، تیر نابود می‌شود
            destroy();
        }
    }

    public void destroy() {
        if (isDead) return;
        this.isDead = true;
        ctx.removeProjectile(this);
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public float getX() { return pos.x; }
    public float getY() { return pos.y; }
    public Vector2 getPos() { return pos; }
    public float getDamage() {return damage;}
    public ProjectileType getType() { return type; }
}
