package pvz.Models.Quests;

import java.util.Set;

import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Games.GameStats;
import pvz.Models.User.User;

public class QuestEvaluator {

    public static void evaluateAll(QuestLog questLog, GameStats stats, boolean levelWon, int currentSun, int difficulty) {
        for (Quest quest : questLog.getAllQuests()) {
            if (!quest.isActive() || quest.isClaimed()) continue;
            evaluateQuest(quest, stats, levelWon, currentSun, difficulty);
        }
    }

    private static void evaluateQuest(Quest quest, GameStats stats, boolean levelWon, int currentSun, int difficulty) {
        String id = quest.getId();
        Progress progress = quest.getProgress();

        switch (id) {
            case "daily_sun_catcher_3000":
            case "daily_sun_catcher_4000":
            case "daily_sun_catcher_5000":
                progress.addProgress(stats.getSunCollected());
                break;

            case "daily_pro_plant_player":
                progress.addProgress(stats.getZombiesKilledByPlant());
                break;

            case "daily_only_cactus":
                progress.addProgress(stats.getZombiesKilledByCactus());
                break;

            case "daily_professional_destroyer":
                progress.addProgress(stats.getExplosivePlantsUsed());
                break;

            case "daily_symmetry":
                if (levelWon && isGardenSymmetric()) {
                    progress.addProgress(1);
                }
                break;

            case "daily_family_slaughter_peashooter":
                if (levelWon && stats.getPlantsUsedToKill().size() > 0 && isOnlyFamily(stats, "SHOOTER")) {
                    progress.addProgress(1);
                }
                break;

            case "daily_family_slaughter_sunflower":
                if (levelWon && stats.getPlantsUsedToKill().size() > 0 && isOnlyFamily(stats, "SUN_PRODUCER")) {
                    progress.addProgress(1);
                }
                break;

            case "daily_blooming_constraints_shooter":
                if (levelWon && !stats.getPlantFamiliesUsed().contains("SHOOTER")) {
                    progress.addProgress(1);
                }
                break;

            case "daily_blooming_constraints_explosive":
                if (levelWon && !stats.getPlantFamiliesUsed().contains("EXPLOSIVE")) {
                    progress.addProgress(1);
                }
                break;

            case "daily_win_after_win":
                if (levelWon && difficulty >= 5) {
                    stats.setConsecutiveWinsOnHighDiff(stats.getConsecutiveWinsOnHighDiff() + 1);
                    progress.addProgress(1);
                } else if (!levelWon) {
                    stats.setConsecutiveWinsOnHighDiff(0);
                }
                break;

            case "daily_almost_victorious":
                progress.addProgress(stats.getZombiesKilledInCol0NoMower());
                break;

            case "daily_anti_ocd":
                if (levelWon && !isGardenSymmetricExceptMiddle()) {
                    progress.addProgress(1);
                }
                break;

            case "daily_cloudy_day":
                if (levelWon && stats.getSunProducerCount() <= 3) {
                    progress.addProgress(1);
                }
                break;

            case "daily_one_less_column_2":
                if (levelWon && !stats.getColumnsUsedForPlanting().contains(2)) {
                    progress.addProgress(1);
                }
                break;

            case "daily_one_less_column_3":
                if (levelWon && !stats.getColumnsUsedForPlanting().contains(3)) {
                    progress.addProgress(1);
                }
                break;

            case "daily_defenseless_row_1":
                if (levelWon && !stats.getRowsUsedForPlanting().contains(1)) {
                    progress.addProgress(1);
                }
                break;

            case "daily_defenseless_row_3":
                if (levelWon && !stats.getRowsUsedForPlanting().contains(3)) {
                    progress.addProgress(1);
                }
                break;

            case "daily_defenseless_cross_2_2":
                if (levelWon && !stats.getRowsUsedForPlanting().contains(2)
                        && !stats.getColumnsUsedForPlanting().contains(2)) {
                    progress.addProgress(1);
                }
                break;

            case "main_chapter_hunter_egypt":
                progress.addProgress(stats.getZombiesKilledBySeasonMap().getOrDefault("Ancient Egypt", 0));
                break;
            case "main_chapter_hunter_caves":
                progress.addProgress(stats.getZombiesKilledBySeasonMap().getOrDefault("Frostbite Caves", 0));
                break;
            case "main_chapter_hunter_ages":
                progress.addProgress(stats.getZombiesKilledBySeasonMap().getOrDefault("Dark Ages", 0));
                break;
            case "main_chapter_hunter_beach":
                progress.addProgress(stats.getZombiesKilledBySeasonMap().getOrDefault("Big Wave Beach", 0));
                break;

            case "main_economical_0":
            case "main_economical_1":
            case "main_economical_2":
            case "main_economical_3":
            case "main_economical_4":
            case "main_economical_5":
                if (levelWon) {
                    int maxLoss = Integer.parseInt(id.split("_")[2]);
                    if (stats.getPlantsLost() <= maxLoss) {
                        progress.addProgress(1);
                    }
                }
                break;

            case "main_quick_reflexes":
                if (stats.isFirstWaveStarted()) {
                    progress.addProgress(stats.getZombiesKilledAfterFirstWaveIn30Sec());
                }
                break;

            case "epic_defense_master":
                if (levelWon && currentSun == 0) {
                    progress.addProgress(1);
                }
                break;

            case "epic_night_or_morning":
                if (levelWon && stats.isUsesNightPlants()) {
                    progress.addProgress(1);
                }
                break;

            case "epic_lawnmowing_10":
            case "epic_lawnmowing_20":
            case "epic_lawnmowing_30":
            case "epic_lawnmowing_40":
            case "epic_lawnmowing_50":
                progress.addProgress(stats.getLawnmowerKills());
                break;
        }
    }

    private static boolean isGardenSymmetric() {
        return false;
    }

    private static boolean isGardenSymmetricExceptMiddle() {
        return false;
    }

    private static boolean isOnlyFamily(GameStats stats, String family) {
        Set<PlantType> used = stats.getPlantsUsedToKill();
        for (PlantType pt : used) {
            String cat = getPlantCategory(pt);
            if (cat != null && !cat.equals(family)) return false;
        }
        return true;
    }

    private static String getPlantCategory(PlantType type) {
        switch (type) {
            case Peashooter: case Repeater: case Threepeater: case SnowPea:
            case Rotobaga: case PeaPod: case SplitPea: case Citron:
            case Caulipower: case ElectricBlueberry: case BowlingBulb:
            case Cactus: case FirePeashooter: case Starfruit: case GooPeashooter:
            case MegaGatlingPea:
                return "SHOOTER";
            case Sunflower: case TwinSunflower: case Sunshroom: case PrimalSunflower:
            case GoldBloom:
                return "SUN_PRODUCER";
            case Cabbagepult: case Kernelpult: case Melonpult: case WinterMelon:
            case Pepperpult:
                return "LOBBER";
            case CherryBomb: case Jalapeno: case Doomshroom: case Grapeshot:
            case PotatoMine: case PrimalPotatoMine:
                return "EXPLOSIVE";
            case BonkChoy: case Chomper: case WasabiWhip: case Kiwibeast:
                return "MELEE";
            case Wallnut: case Tallnut: case Endurian: case Garlic:
            case SweetPotato: case Pumpkin:
                return "WALL_NUT";
            case Seashroom: case Puffshroom: case Fumeshroom: case Magnetshroom:
            case Hypnoshroom: case Iceshroom: case Cattail: case TangleKelp:
            case IcebergLettuce: case Torchwood: case SunBean: case Explodeonut:
            case GraveBuster: case HotPotato: case LilyPad: case Imitater:
                return "MODIFIER";
            default:
                return null;
        }
    }
}
