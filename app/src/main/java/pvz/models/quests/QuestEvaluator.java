package pvz.models.quests;

import pvz.models.entities.plants.enums.PlantType;
import pvz.models.games.GameContext;
import pvz.models.games.GameStats;
import pvz.models.games.map.GameMap;
import pvz.models.games.map.tile.Tile;
import pvz.models.user.User;

public class QuestEvaluator {

    public static void evaluateAll(QuestLog questLog, GameStats stats, boolean levelWon,
                                   int currentSun, int difficulty, User user, GameContext context) {
        for (Quest quest : questLog.getAllQuests()) {
            if (!quest.isActive() || quest.isClaimed()) continue;

            evaluateQuest(quest, stats, levelWon, currentSun, difficulty, context);

            if (quest.isCompleted() && !quest.isClaimed()) {
                quest.claim(user);
            }
        }
    }

    private static void evaluateQuest(Quest quest, GameStats stats, boolean levelWon,
                                      int currentSun, int difficulty, GameContext context) {
        String id = quest.getId();
        Progress progress = quest.getProgress();
        int currentAmt = progress.getCurrentAmount();

        // =========================================================================
        // 1. CUMULATIVE QUESTS (Adds up across multiple games/levels)
        // =========================================================================

        if (id.startsWith("daily_sun_catcher_")) {
            progress.setCurrentAmount(currentAmt + stats.getSunCollected());
        }
        else if (id.startsWith("main_chapter_hunter_")) {
            String chapter = quest.getVariable();
            if (stats.getZombiesKilledBySeasonMap() != null) {
                progress.setCurrentAmount(currentAmt + stats.getZombiesKilledBySeasonMap().getOrDefault(chapter, 0));
            }
        }
        else if (id.startsWith("daily_pro_plant_player_")) {
            String plantName = quest.getVariable();
            progress.setCurrentAmount(currentAmt + stats.getZombiesKilledByPlantType(plantName));
        }
        else if (id.equals("daily_only_cactus")) {
            progress.setCurrentAmount(currentAmt + stats.getZombiesKilledByCactus());
        }
        else if (id.startsWith("epic_lawnmowing_")) {
            progress.setCurrentAmount(currentAmt + stats.getLawnmowerKills());
        }
        else if (id.equals("daily_almost_victorious")) {
            progress.setCurrentAmount(currentAmt + stats.getZombiesKilledInCol0NoMower());
        }
        else if (id.equals("daily_win_after_win")) {
            if (levelWon && difficulty >= 3) {
                progress.setCurrentAmount(currentAmt + 1);
            } else if (!levelWon && difficulty >= 3) {
                progress.setCurrentAmount(0); // Streak broken
            }
        }

        // =========================================================================
        // 2. SINGLE-MATCH CONDITIONS (Requires 'n' logic parsed from the variable)
        // =========================================================================

        else if (id.startsWith("main_economical_vegetarian_")) {
            if (levelWon) {
                int maxLossN = Integer.parseInt(quest.getVariable());
                if (stats.getPlantsLost() <= maxLossN) {
                    progress.setCurrentAmount(progress.getTargetAmount());
                }
            }
        }
        else if (id.equals("epic_defense_master")) {
            if (levelWon && currentSun == 0) {
                progress.setCurrentAmount(progress.getTargetAmount());
            }
        }
        else if (id.equals("main_quick_reflexes")) {
            if (stats.getZombiesKilledAfterFirstWaveIn30Sec() >= 10) {
                progress.setCurrentAmount(progress.getTargetAmount());
            }
        }
        else if (id.equals("daily_professional_destroyer")) {
            if (stats.getExplosivePlantsUsed() >= 3) {
                progress.setCurrentAmount(progress.getTargetAmount());
            }
        }
        else if (id.equals("daily_symmetry")) {
            if (levelWon && isGardenSymmetric(context)) {
                progress.setCurrentAmount(progress.getTargetAmount());
            }
        }
        else if (id.startsWith("daily_family_slaughter_")) {
            if (levelWon) {
                String targetFamily = quest.getVariable();
                if (stats.getKillingFamiliesUsed() != null &&
                        stats.getKillingFamiliesUsed().size() == 1 &&
                        stats.getKillingFamiliesUsed().contains(targetFamily)) {
                    progress.setCurrentAmount(progress.getTargetAmount());
                }
            }
        }
        else if (id.startsWith("daily_blooming_constraints_")) {
            if (levelWon) {
                String bannedFamily = quest.getVariable();
                if (stats.getPlantFamiliesUsed() != null && !stats.getPlantFamiliesUsed().contains(bannedFamily)) {
                    progress.setCurrentAmount(progress.getTargetAmount());
                }
            }
        }
        else if (id.equals("epic_night_or_morning")) {
            if (levelWon && stats.isUsesNightPlants()) {
                progress.setCurrentAmount(progress.getTargetAmount());
            }
        }
        else if (id.equals("daily_anti_ocd")) {
            if (levelWon && !isGardenSymmetric(context)) {
                progress.setCurrentAmount(progress.getTargetAmount());
            }
        }
        else if (id.equals("daily_cloudy_day")) {
            if (levelWon && stats.getSunProducerCount() <= 3) {
                progress.setCurrentAmount(progress.getTargetAmount());
            }
        }
        else if (id.startsWith("daily_one_less_column_")) {
            if (levelWon) {
                int bannedColN = Integer.parseInt(quest.getVariable());
                if (stats.getColumnsUsedForPlanting() != null && !stats.getColumnsUsedForPlanting().contains(bannedColN)) {
                    progress.setCurrentAmount(progress.getTargetAmount());
                }
            }
        }
        else if (id.startsWith("daily_defenseless_row_")) {
            if (levelWon) {
                int bannedRowN = Integer.parseInt(quest.getVariable());
                if (stats.getRowsUsedForPlanting() != null && !stats.getRowsUsedForPlanting().contains(bannedRowN)) {
                    progress.setCurrentAmount(progress.getTargetAmount());
                }
            }
        }
        else if (id.startsWith("daily_defenseless_cross_")) {
            if (levelWon) {
                int crossIndexN = Integer.parseInt(quest.getVariable());
                if (stats.getRowsUsedForPlanting() != null && stats.getColumnsUsedForPlanting() != null &&
                        !stats.getRowsUsedForPlanting().contains(crossIndexN) &&
                        !stats.getColumnsUsedForPlanting().contains(crossIndexN)) {
                    progress.setCurrentAmount(progress.getTargetAmount());
                }
            }
        }
    }

    private static boolean isGardenSymmetric(GameContext context) {
        if (context == null || context.getMap() == null) return false;

        GameMap map = context.getMap();
        int rows = map.getRows();
        int cols = map.getColumns();

        for (int r = 0; r < rows / 2; r++) {
            int mirrorRow = rows - 1 - r;
            for (int c = 0; c < cols; c++) {
                String plantTop = getPlantNameAt(map, c, r);
                String plantBottom = getPlantNameAt(map, c, mirrorRow);
                if (!java.util.Objects.equals(plantTop, plantBottom)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String getPlantNameAt(GameMap map, int col, int row) {
        Tile tile = map.getTile(col, row);
        if (tile != null && !tile.getPlants().isEmpty()) {
            return tile.getPlants().getLast().getType().name();
        }
        return null;
    }
}