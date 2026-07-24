package pvz.models.entities.plants.actions.shooters;

import java.util.List;

import pvz.models.entities.plants.Plant;
import pvz.models.entities.plants.actions.CooldownPlantAction;
import pvz.models.entities.plants.enums.PlantCategory;
import pvz.models.entities.plants.enums.PlantTag;
import pvz.models.entities.projectile.Projectile;
import pvz.models.entities.projectile.ProjectileType;
import pvz.models.entities.zombies.Zombie;
import pvz.models.games.GameContext;
import pvz.models.games.map.tile.TileTags;

public class ShooterAction extends CooldownPlantAction {

    public ShooterAction(float intervalSeconds) {
        super(intervalSeconds);
    }

    @Override
    protected boolean canUse(Plant plant, GameContext ctx) {
        ShooterPattern pattern = plant.getSheet().getShooterPattern();
        // int maxRange = plant.getSheet().getMaxRange(); // -1 یعنی بی‌نهایت (کل لاین)

        return hasValidTarget(plant, ctx, pattern, -1); // -1 یعنی بی‌نهایت (کل لاین)
    }

    @Override
    protected void doExecute(Plant plant, GameContext ctx) {
        int count = Math.max(1, plant.getSheet().getDamage().getCount());

        // // پشتیبانی از گیاهان استکی (مثل Pea Pod)
        // if (plant.getStackCount() > 1) {
        // count = plant.getStackCount();
        // }

        boolean poisonous = plant.getSheet().hasTag(PlantTag.POISON);
        boolean chills = plant.getSheet().hasTag(PlantTag.ICE);
        boolean fire = plant.getSheet().hasTag(PlantTag.FIRE);

        int pierce = plant.getPierceCount();
        if (plant.getSheet().getCategory() == PlantCategory.STRIKE_THROUGH) {
            pierce += 2; // پایه نفوذ برای گیاهان Strike-through
        }

        ProjectileType type = plant.getSheet().getProjectileType();
        ShooterPattern pattern = plant.getSheet().getShooterPattern();

        // اجرای الگوی شلیک
        firePattern(plant, ctx, pattern, count, type, poisonous, chills, fire, pierce);
    }

    @Override
    public String getName() {
        return "Shoot";
    }

    // ─── Target Checking Logic ───────────────────────────────────────────────

    private boolean hasValidTarget(Plant plant, GameContext ctx, ShooterPattern pattern, int maxRange) {
        int row = plant.getLane();
        int col = plant.getCol();

        switch (pattern) {
            case FORWARD -> {
                return isZombieInDirection(ctx, row, col, 1, maxRange) || hasObstacleInFront(ctx, row, col);
            }
            case BIDIRECTIONAL -> {
                return isZombieInDirection(ctx, row, col, 1, maxRange) ||
                        isZombieInDirection(ctx, row, col, -1, maxRange);
            }
            case THREE_LANE -> {
                return isZombieInDirection(ctx, row - 1, col, 1, maxRange) ||
                        isZombieInDirection(ctx, row, col, 1, maxRange) ||
                        isZombieInDirection(ctx, row + 1, col, 1, maxRange);
            }
            case FIVE_WAY_STAR, DIAGONAL_FOUR -> {
                // برای گیاهان چندجهته، وجود هر زامبی زنده در بازی باعث شلیک می‌شود
                return !ctx.getZombies().isEmpty();
            }
            default -> {
                return false;
            }
        }
    }

    private boolean isZombieInDirection(GameContext ctx, int row, int startCol, int dx, int maxRange) {
        if (row < 0 || row >= ctx.getLanes())
            return false;

        for (Zombie z : ctx.getZombiesInLane(row)) {
            if (z.isDead())
                continue;

            float zX = z.getX();
            // بررسی وجود زامبی در جهت جلو (dx > 0) یا عقب (dx < 0)
            if (dx > 0 && zX >= startCol) {
                if (maxRange < 0 || (zX - startCol) <= maxRange)
                    return true;
            } else if (dx < 0 && zX <= startCol) {
                if (maxRange < 0 || (startCol - zX) <= maxRange)
                    return true;
            }
        }
        return false;
    }

    private boolean hasObstacleInFront(GameContext ctx, int row, int startCol) {
        for (int c = startCol + 1; c < ctx.getColumns(); c++) {
            List<TileTags> tags = ctx.getTileAt(c, row).getTags();
            if (tags.contains(TileTags.GRAVE) || tags.contains(TileTags.ICE_BLOCK)) {
                return true;
            }
        }
        return false;
    }

    // ─── Projectile Spawning Logic ───────────────────────────────────────────

    private void firePattern(Plant plant, GameContext ctx, ShooterPattern pattern, int count,
            ProjectileType type, boolean poison, boolean ice, boolean fire, int pierce) {
        int row = plant.getLane();
        int col = plant.getCol();
        float dmg = plant.getEffectiveDamage();

        for (int i = 0; i < count; i++) {
            switch (pattern) {
                case FORWARD -> spawnProjectile(ctx, type, row, col, 1.0f, 0.0f, dmg, poison, ice, fire, pierce);

                case BIDIRECTIONAL -> {
                    spawnProjectile(ctx, type, row, col, 1.0f, 0.0f, dmg, poison, ice, fire, pierce); // جلو
                    spawnProjectile(ctx, type, row, col, -1.0f, 0.0f, dmg, poison, ice, fire, pierce); // عقب
                }

                case THREE_LANE -> {
                    if (row - 1 >= 0)
                        spawnProjectile(ctx, type, row - 1, col, 1.0f, 0.0f, dmg, poison, ice, fire, pierce);
                    spawnProjectile(ctx, type, row, col, 1.0f, 0.0f, dmg, poison, ice, fire, pierce);
                    if (row + 1 < ctx.getLanes())
                        spawnProjectile(ctx, type, row + 1, col, 1.0f, 0.0f, dmg, poison, ice, fire, pierce);
                }

                case DIAGONAL_FOUR -> {
                    spawnProjectile(ctx, type, row, col, 1.0f, 1.0f, dmg, poison, ice, fire, pierce); // پایین-راست
                    spawnProjectile(ctx, type, row, col, 1.0f, -1.0f, dmg, poison, ice, fire, pierce); // بالا-راست
                    spawnProjectile(ctx, type, row, col, -1.0f, 1.0f, dmg, poison, ice, fire, pierce); // پایین-چپ
                    spawnProjectile(ctx, type, row, col, -1.0f, -1.0f, dmg, poison, ice, fire, pierce); // بالا-چپ
                }

                case FIVE_WAY_STAR -> {
                    spawnProjectile(ctx, type, row, col, 0.0f, -1.0f, dmg, poison, ice, fire, pierce); // بالا
                    spawnProjectile(ctx, type, row, col, 0.0f, 1.0f, dmg, poison, ice, fire, pierce); // پایین
                    spawnProjectile(ctx, type, row, col, -1.0f, 0.0f, dmg, poison, ice, fire, pierce); // عقب
                    spawnProjectile(ctx, type, row, col, 1.0f, -1.0f, dmg, poison, ice, fire, pierce); // بالا-راست
                    spawnProjectile(ctx, type, row, col, 1.0f, 1.0f, dmg, poison, ice, fire, pierce); // پایین-راست
                }
            }
        }
    }

    private void spawnProjectile(GameContext ctx, ProjectileType type, float row, float col,
            float dx, float dy, float dmg, boolean poison, boolean ice, boolean fire, int pierce) {
        Projectile bolt = new Projectile(ctx, type, row, col, dmg, poison, ice, fire, pierce, null);
        bolt.setVelocityVector(dx, dy); // مقداردهی بردار حرکت دوبعدی
        ctx.spawnProjectile(bolt);
    }
}