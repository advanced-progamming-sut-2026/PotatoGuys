package pvz.Models.Quests;

import java.util.ArrayList;
import java.util.List;
import pvz.Models.Entities.Plants.Enums.PlantType;

public class QuestFactory {

    public static QuestLog createDefaultQuests() {
        QuestLog log = new QuestLog();

        // 1. Daily Sun Catcher (3000, 4000, 5000)
        for (int amount : new int[]{3000, 4000, 5000}) {
            Quest q = new Quest("daily_sun_catcher_" + amount, "Daily Sun Catcher",
                    "Collect " + amount + " units of sun during one day.",
                    QuestCategory.DAILY, QuestPriority.MEDIUM,
                    List.of(new CurrencyReward(CurrencyKind.COIN, amount / 100)), amount);
            q.setVariable(String.valueOf(amount));
            log.addQuest(q);
        }

        // 2. Chapter Hunter (Defeat 50 zombies in specific chapters)
        String[] chapters = {"Ancient Egypt", "Frostbite Caves", "Big Wave Beach", "Dark Ages"};
        for (String chapter : chapters) {
            Quest q = new Quest("main_chapter_hunter_" + chapter.replace(" ", "_"), "Chapter Hunter: " + chapter,
                    "Defeat 50 zombies from the " + chapter + " chapter.",
                    QuestCategory.MAIN, QuestPriority.HIGH,
                    List.of(new InventoryReward("SeedPacket", 10)), 50);
            q.setVariable(chapter);
            log.addQuest(q);
        }

        // 3. Professional Plant Player
        String[] killerPlants = {"Peashooter", "Cabbage-pult", "Melon-pult", "Bonk Choy"};
        for (String plant : killerPlants) {
            Quest q = new Quest("daily_pro_plant_player_" + plant.replace("-", ""), "Professional Plant Player",
                    "Kill 10 zombies only with " + plant + ".",
                    QuestCategory.DAILY, QuestPriority.HIGH,
                    List.of(new UnlockableReward(UnlockTargetType.PLANT, "Random")), 10);
            q.setVariable(plant);
            log.addQuest(q);
        }

        // 4. Only Cactus
        log.addQuest(new Quest("daily_only_cactus", "Only Cactus",
                "Kill 10 zombies only with cactus.",
                QuestCategory.DAILY, QuestPriority.HIGH,
                List.of(new CurrencyReward(CurrencyKind.GEM, 20)), 10));

        // 5. Economical Vegetarian (Victory without losing more than n plants: 0 to 5)
        for (int n = 0; n <= 5; n++) {
            Quest q = new Quest("main_economical_vegetarian_" + n, "Economical Vegetarian",
                    "Victory in a level without losing more than " + n + " plants.",
                    QuestCategory.MAIN, QuestPriority.HIGH,
                    List.of(new InventoryReward("SeedPacket", 20 - n)), 1);
            q.setVariable(String.valueOf(n));
            log.addQuest(q);
        }

        // 6. Defense Master
        log.addQuest(new Quest("epic_defense_master", "Defense Master",
                "Completing a level with exactly zero sun.",
                QuestCategory.EPIC, QuestPriority.CRITICAL,
                List.of(new CurrencyReward(CurrencyKind.GEM, 200)), 1));

        // 7. Quick Reflexes
        log.addQuest(new Quest("main_quick_reflexes", "Quick Reflexes",
                "Killing 10 zombies in less than 30 seconds from the start of the first zombie attack wave.",
                QuestCategory.MAIN, QuestPriority.MEDIUM,
                List.of(new CurrencyReward(CurrencyKind.COIN, 500)), 10));

        // 8. Professional Destroyer
        log.addQuest(new Quest("daily_professional_destroyer", "Professional Destroyer",
                "Using 3 explosive plants in one level.",
                QuestCategory.DAILY, QuestPriority.LOW,
                List.of(new CurrencyReward(CurrencyKind.COIN, 100)), 3));

        // 9. Symmetry
        log.addQuest(new Quest("daily_symmetry", "Symmetry",
                "The game garden must be symmetrical at the end.",
                QuestCategory.DAILY, QuestPriority.HIGH,
                List.of(new CurrencyReward(CurrencyKind.COIN, 500)), 1));

        // 10 & 11. Family Slaughter & Blooming in Constraints
        String[] families = {"SHOOTER", "LOBBER", "EXPLOSIVE", "WALL_NUT", "MELEE"};
        for (String family : families) {
            Quest qSlaughter = new Quest("daily_family_slaughter_" + family, "Family Slaughter",
                    "Only " + family + " plants are used to kill zombies.",
                    QuestCategory.DAILY, QuestPriority.MEDIUM,
                    List.of(new CurrencyReward(CurrencyKind.COIN, 1000)), 1);
            qSlaughter.setVariable(family);
            log.addQuest(qSlaughter);

            Quest qConstraints = new Quest("daily_blooming_constraints_" + family, "Blooming in Constraints",
                    "To win the level, no plants from the " + family + " family should be used.",
                    QuestCategory.DAILY, QuestPriority.HIGH,
                    List.of(new CurrencyReward(CurrencyKind.GEM, 100)), 1);
            qConstraints.setVariable(family);
            log.addQuest(qConstraints);
        }

        // 12. Night or Morning
        log.addQuest(new Quest("epic_night_or_morning", "Night or Morning",
                "Finishing a day game level with night plants (mushrooms).",
                QuestCategory.EPIC, QuestPriority.HIGH,
                List.of(new CurrencyReward(CurrencyKind.GEM, 20)), 1));

        // 13. Win After Win
        log.addQuest(new Quest("daily_win_after_win", "Win After Win",
                "Win 5 consecutive levels on the highest difficulty.",
                QuestCategory.DAILY, QuestPriority.MEDIUM,
                List.of(new CurrencyReward(CurrencyKind.COIN, 5000)), 5));

        // 14. Almost Victorious
        log.addQuest(new Quest("daily_almost_victorious", "Almost Victorious",
                "Kill 10 zombies in the first column of a row that has no lawnmower.",
                QuestCategory.DAILY, QuestPriority.MEDIUM,
                List.of(new CurrencyReward(CurrencyKind.COIN, 300)), 10));

        // 15. Anti-OCD
        log.addQuest(new Quest("daily_anti_ocd", "Anti-OCD",
                "Win a level where there is no symmetry in the garden (except the middle row).",
                QuestCategory.DAILY, QuestPriority.MEDIUM,
                List.of(new CurrencyReward(CurrencyKind.COIN, 800)), 1));

        // 16. Cloudy Day
        log.addQuest(new Quest("daily_cloudy_day", "Cloudy Day",
                "Win a level with only 3 sun-producing plants.",
                QuestCategory.DAILY, QuestPriority.HIGH,
                List.of(new CurrencyReward(CurrencyKind.GEM, 10)), 1));

        // 17. One Less Column (0 to 8 columns)
        for (int c = 0; c < 9; c++) {
            Quest q = new Quest("daily_one_less_column_" + c, "One Less Column",
                    "Win a level provided that no plant is planted in column " + c + ".",
                    QuestCategory.DAILY, QuestPriority.HIGH,
                    List.of(new CurrencyReward(CurrencyKind.GEM, 10)), 1);
            q.setVariable(String.valueOf(c));
            log.addQuest(q);
        }

        // 18. Defenseless Row (0 to 4 rows)
        for (int r = 0; r < 5; r++) {
            Quest q = new Quest("daily_defenseless_row_" + r, "Defenseless Row",
                    "Win a level provided that no plant is planted in row " + r + ".",
                    QuestCategory.DAILY, QuestPriority.HIGH,
                    List.of(new CurrencyReward(CurrencyKind.GEM, 20)), 1);
            q.setVariable(String.valueOf(r));
            log.addQuest(q);
        }

        // 19. Defenseless Cross
        int minSize = Math.min(5, 9); // min of rows and cols
        for (int i = 0; i < minSize; i++) {
            Quest q = new Quest("daily_defenseless_cross_" + i, "Defenseless Cross (" + i + "," + i + ")",
                    "Win a level without planting in row " + i + " or column " + i + ".",
                    QuestCategory.DAILY, QuestPriority.HIGH,
                    List.of(new CurrencyReward(CurrencyKind.GEM, 25)), 1);
            q.setVariable(String.valueOf(i));
            log.addQuest(q);
        }

        // 20. Lawnmowing Time (10, 20, 30, 40, 50)
        for (int n = 10; n <= 50; n += 10) {
            Quest q = new Quest("epic_lawnmowing_" + n, "Lawnmowing Time",
                    "Kill at least " + n + " zombies with a lawnmower.",
                    QuestCategory.EPIC, QuestPriority.MEDIUM,
                    List.of(new CurrencyReward(CurrencyKind.GEM, n)), n);
            q.setVariable(String.valueOf(n));
            log.addQuest(q);
        }

        return log;
    }

    public static List<Reward> findRewardsById(String questId) {
        for (Quest q : createDefaultQuests().getAllQuests()) {
            if (q.getId().equals(questId)) {
                return q.getRewards();
            }
        }
        return new ArrayList<>();
    }
}