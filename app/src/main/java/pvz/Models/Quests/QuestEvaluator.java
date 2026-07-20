package pvz.Models.Quests;

import pvz.Models.Entities.Plants.Enums.PlantType;
import pvz.Models.Games.GameContext;
import pvz.Models.Games.GameStats;
import pvz.Models.Games.map.GameMap;
import pvz.Models.Games.map.Tile;
import pvz.Models.User.User;

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

        if (id.startsWith("daily_sun_catcher_")) {
            progress.addProgress(stats.getSunCollected());

        } else if (id.startsWith("main_chapter_hunter_")) {
            String chapter = quest.getVariable();
            progress.addProgress(stats.getZombiesKilledBySeasonMap().getOrDefault(chapter, 0));

        } else if (id.startsWith("daily_pro_plant_player_")) {
            String plantName = quest.getVariable();
            progress.addProgress(stats.getZombiesKilledByPlantType(plantName));

        } else if (id.equals("daily_only_cactus")) {
            progress.addProgress(stats.getZombiesKilledByCactus());

        } else if (id.startsWith("main_economical_vegetarian_")) {
            int maxLoss = Integer.parseInt(quest.getVariable());
            if (levelWon && stats.getPlantsLost() <= maxLoss) {
                progress.addProgress(1);
            }

        } else if (id.equals("epic_defense_master")) {
            if (levelWon && currentSun == 0) {
                progress.addProgress(1);
            }

        } else if (id.equals("main_quick_reflexes")) {
            progress.addProgress(stats.getZombiesKilledAfterFirstWaveIn30Sec());

        } else if (id.equals("daily_professional_destroyer")) {
            progress.addProgress(stats.getExplosivePlantsUsed());

        } else if (id.equals("daily_symmetry")) {
            if (levelWon && context != null && isGardenSymmetric(context)) {
                progress.addProgress(1);
            }

        } else if (id.startsWith("daily_family_slaughter_")) {
            String family = quest.getVariable();
            if (levelWon && stats.getKillingFamiliesUsed().size() == 1
                    && stats.getKillingFamiliesUsed().contains(family)) {
                progress.addProgress(1);
            }

        } else if (id.startsWith("daily_blooming_constraints_")) {
            String bannedFamily = quest.getVariable();
            if (levelWon && !stats.getPlantFamiliesUsed().contains(bannedFamily)) {
                progress.addProgress(1);
            }

        } else if (id.equals("epic_night_or_morning")) {
            if (levelWon && stats.isUsesNightPlants()) {
                progress.addProgress(1);
            }

        } else if (id.equals("daily_win_after_win")) {
            if (levelWon && difficulty >= 5) {
                stats.setConsecutiveWinsOnHighDiff(stats.getConsecutiveWinsOnHighDiff() + 1);
                progress.addProgress(1);
            } else if (!levelWon) {
                stats.setConsecutiveWinsOnHighDiff(0);
            }

        } else if (id.equals("daily_almost_victorious")) {
            progress.addProgress(stats.getZombiesKilledInCol0NoMower());

        } else if (id.equals("daily_anti_ocd")) {
            if (levelWon && context != null && !isGardenSymmetric(context)) {
                progress.addProgress(1);
            }

        } else if (id.equals("daily_cloudy_day")) {
            if (levelWon && stats.getSunProducerCount() <= 3) {
                progress.addProgress(1);
            }

        } else if (id.startsWith("daily_one_less_column_")) {
            int col = Integer.parseInt(quest.getVariable());
            if (levelWon && !stats.getColumnsUsedForPlanting().contains(col)) {
                progress.addProgress(1);
            }

        } else if (id.startsWith("daily_defenseless_row_")) {
            int row = Integer.parseInt(quest.getVariable());
            if (levelWon && !stats.getRowsUsedForPlanting().contains(row)) {
                progress.addProgress(1);
            }

        } else if (id.startsWith("daily_defenseless_cross_")) {
            int idx = Integer.parseInt(quest.getVariable());
            if (levelWon && !stats.getRowsUsedForPlanting().contains(idx)
                    && !stats.getColumnsUsedForPlanting().contains(idx)) {
                progress.addProgress(1);
            }

        } else if (id.startsWith("epic_lawnmowing_")) {
            progress.addProgress(stats.getLawnmowerKills());
        }
    }

    private static boolean isGardenSymmetric(GameContext context) {
        GameMap map = context.getMap();
        int rows = map.getRows();
        int cols = map.getColumns();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols / 2; c++) {
                String left = getPlantTypeAt(map, c, r);
                String right = getPlantTypeAt(map, cols - 1 - c, r);
                if (!java.util.Objects.equals(left, right)) return false;
            }
        }
        return true;
    }

    private static String getPlantTypeAt(GameMap map, int col, int row) {
        if (col < 0 || col >= map.getColumns() || row < 0 || row >= map.getRows()) return null;
        Tile tile = map.getTile(col, row);
        if (tile.getPlants().isEmpty()) return null;
        return tile.getPlants().getLast().getType().name();
    }
}
