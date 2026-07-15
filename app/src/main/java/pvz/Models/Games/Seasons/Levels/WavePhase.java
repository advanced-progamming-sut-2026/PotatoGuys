package pvz.Models.Seasons.Levels;

import pvz.Models.Entities.Zombies.ZombieType;

import java.util.List;

public class WavePhase {
    private int zombieCount;              // تعداد زامبی‌هایی که در این فاز اسپاون می‌شن
    private int intervalTicks;            // فاصله بین هر اسپاون (بر حسب تیک)
    private List<ZombieType> allowedTypes; // نوع‌های مجاز با هزینه‌ی پایه

    public WavePhase(int zombieCount, int intervalTicks, List<ZombieType> allowedTypes, boolean isBurst) {
        this.setZombieCount(zombieCount);
        this.setIntervalTicks(intervalTicks);
        this.setAllowedTypes(allowedTypes);
        this.setBurst(isBurst);
    }

    private boolean isBurst;              // آیا این فاز یک اوج فشرده است؟ (اختیاری)

    public int getZombieCount() {
        return zombieCount;
    }

    public void setZombieCount(int zombieCount) {
        this.zombieCount = zombieCount;
    }

    public int getIntervalTicks() {
        return intervalTicks;
    }

    public void setIntervalTicks(int intervalTicks) {
        this.intervalTicks = intervalTicks;
    }

    public List<ZombieType> getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(List<ZombieType> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public boolean isBurst() {
        return isBurst;
    }

    public void setBurst(boolean burst) {
        isBurst = burst;
    }
    // getter, setter, constructor
}
