package pvz.models.entities.projectile;

import java.util.HashSet;
import java.util.Set;

import pvz.models.engine.TickAware;
import pvz.models.entities.zombies.Zombie;
import pvz.models.entities.zombies.effects.EffectType;
import pvz.models.entities.zombies.effects.StatusEffect;
import pvz.models.games.GameContext;
import pvz.models.games.map.tile.Tile;

public class Projectile implements TickAware {

    private final GameContext ctx;
    private final ProjectileType type;

    private float x;        
    private float y;        

    private int lastCol;
    private int lastLane;

    private float dirX = 1.0f;
    private float dirY = 0.0f;
    private float speed;

    private final float damage;
    private final boolean poison;
    private final boolean ice;
    private final boolean fire;
    private int pierceCount;
    private boolean bouncing;
    private boolean isDead = false;

    // شعاع برخورد تیر بر حسب خانه/کاشی
    private static final float HIT_RADIUS = 0.45f;

    // ثبت زامبی‌های برخورد کرده برای جلوگیری از دمیج مکرر در یک فریم (در حالت نفوذی/کمانه)
    private final Set<Zombie> hitZombies = new HashSet<>();

    public Projectile(GameContext ctx, ProjectileType type, float lane, float col, 
                      float damage, boolean poison, boolean ice, boolean fire, 
                      int pierceCount, Object target) {
        this.ctx = ctx;
        this.type = type;
        this.y = lane;
        this.x = col;
        
        // مقداردهی اولیه موقعیت کاشی
        this.lastCol = (int) Math.floor(col);
        this.lastLane = (int) Math.floor(lane);
        
        this.damage = damage;
        this.poison = poison;
        this.ice = ice;
        this.fire = fire;
        this.pierceCount = pierceCount;
        this.speed = type.getSpeed();
    }

    // ─── Setters & Direction Vector ──────────────────────────────────────────

    /**
     * تنظیم بردار جهت حرکت (پشتیبانی از تمام جهت‌ها: چپ، راست، بالا، پایین و قطری)
     */
    public void setVelocityVector(float dx, float dy) {
        float length = (float) Math.hypot(dx, dy);
        if (length != 0) {
            // نرمال‌سازی بردار جهت برای یکنواخت ماندن سرعت حرکت در تمام زوایا
            this.dirX = dx / length;
            this.dirY = dy / length;
        }
    }

    public void setBouncing(boolean bouncing) {
        this.bouncing = bouncing;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isDead() {
        return isDead;
    }

    // ─── Tick Update Logic ───────────────────────────────────────────────────

    @Override
    public void enter() {}

    @Override
    public void update() {
        if (isDead) return;

        // ۱. جابه‌جایی دوبعدی پرتابه در محیط بازی
        x += dirX * speed;
        y += dirY * speed;

        // ۲. چک کردن خروج تیر از مرزهای نقشه
        if (x < -0.5f || x >= ctx.getColumns() + 0.5f || y < -0.5f || y >= ctx.getLanes() + 0.5f) {
            destroy();
            return;
        }

        // ۳. چک کردن ورود به کاشی (Tile) جدید
        int currentCol = (int) Math.floor(x);
        int currentLane = (int) Math.floor(y);

        if (currentCol != lastCol || currentLane != lastLane) {
            lastCol = currentCol;
            lastLane = currentLane;
            
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
    public void dispose() {}

    // ─── 2D Collision & Bounce Logic ─────────────────────────────────────────

    private void checkCollisions2D() {
        // بررسی تمام زامبی‌های موجود در بازی (برای پشتیبانی از تیرهای چندلاین و مورب)
        for (Zombie z : ctx.getZombies()) {
            if (z.isDead() || hitZombies.contains(z)) continue;

            float zX = z.getX();
            float zY = z.getLane();

            // محاسبه فاصله اقلیدسی دوبعدی بین پرتابه و زامبی
            double distance = Math.hypot(zX - this.x, zY - this.y);

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

        ctx.log("[Projectile] " + type + " hit zombie at (" + zombie.getX() + ", " + zombie.getLane() + ")");

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

            double dist = Math.hypot(z.getX() - this.x, z.getLane() - this.y);
            if (dist < minDistance) {
                minDistance = dist;
                nearestNextZombie = z;
            }
        }

        // اگر زامبی دیگری در محیط وجود داشت، تیر به سمت او تغییر جهت می‌دهد
        if (nearestNextZombie != null) {
            float targetX = nearestNextZombie.getX();
            float targetY = nearestNextZombie.getLane();
            
            // تغییر بردار جهت پرتابه به سمت زامبی جدید
            setVelocityVector(targetX - this.x, targetY - this.y);
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

    public float getX() { return x; }
    public float getY() { return y; }
    public float getDamage() {return damage;}
    public ProjectileType getType() { return type; }
}