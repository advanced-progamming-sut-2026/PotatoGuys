package pvz.Models.Quests;

import java.util.ArrayList;
import java.util.List;

public class QuestFactory {

    public static QuestLog createDefaultQuests() {
        QuestLog log = new QuestLog();

        // === DAILY QUESTS ===
        log.addQuest(new Quest(
            "daily_sun_catcher_3000", "Daily Sun Catcher (3000)",
            "Collect 3000 units of sun during one day.",
            QuestCategory.DAILY, QuestPriority.MEDIUM,
            List.of(new CurrencyReward(CurrencyKind.COIN, 30)), 3000
        ));
        log.addQuest(new Quest(
            "daily_sun_catcher_4000", "Daily Sun Catcher (4000)",
            "Collect 4000 units of sun during one day.",
            QuestCategory.DAILY, QuestPriority.MEDIUM,
            List.of(new CurrencyReward(CurrencyKind.COIN, 40)), 4000
        ));
        log.addQuest(new Quest(
            "daily_sun_catcher_5000", "Daily Sun Catcher (5000)",
            "Collect 5000 units of sun during one day.",
            QuestCategory.DAILY, QuestPriority.MEDIUM,
            List.of(new CurrencyReward(CurrencyKind.COIN, 50)), 5000
        ));

        log.addQuest(new Quest(
            "daily_pro_plant_player", "Professional Plant Player",
            "Kill 10 zombies only with plants.",
            QuestCategory.DAILY, QuestPriority.HIGH,
            List.of(new UnlockableReward(UnlockTargetType.PLANT, "CherryBomb")), 10
        ));

        log.addQuest(new Quest(
            "daily_only_cactus", "Only Cactus",
            "Kill 10 zombies only with Cactus.",
            QuestCategory.DAILY, QuestPriority.HIGH,
            List.of(new CurrencyReward(CurrencyKind.GEM, 20)), 10
        ));

        log.addQuest(new Quest(
            "daily_professional_destroyer", "Professional Destroyer",
            "Use 3 explosive plants in one level.",
            QuestCategory.DAILY, QuestPriority.LOW,
            List.of(new CurrencyReward(CurrencyKind.COIN, 100)), 3
        ));

        log.addQuest(new Quest(
            "daily_symmetry", "Symmetry",
            "Win a level where the garden is symmetrical at the end.",
            QuestCategory.DAILY, QuestPriority.HIGH,
            List.of(new CurrencyReward(CurrencyKind.COIN, 500)), 1
        ));

        log.addQuest(new Quest(
            "daily_family_slaughter_peashooter", "Family Slaughter (Peashooter)",
            "Win a level using only Peashooter family plants to kill zombies.",
            QuestCategory.DAILY, QuestPriority.MEDIUM,
            List.of(new CurrencyReward(CurrencyKind.COIN, 1000)), 1
        ));
        log.addQuest(new Quest(
            "daily_family_slaughter_sunflower", "Family Slaughter (Sunflower)",
            "Win a level using only Sunflower family plants to kill zombies.",
            QuestCategory.DAILY, QuestPriority.MEDIUM,
            List.of(new CurrencyReward(CurrencyKind.COIN, 1000)), 1
        ));

        log.addQuest(new Quest(
            "daily_blooming_constraints_shooter", "Blooming in Constraints (Shooter)",
            "Win a level without using any Shooter family plants.",
            QuestCategory.DAILY, QuestPriority.HIGH,
            List.of(new CurrencyReward(CurrencyKind.GEM, 100)), 1
        ));
        log.addQuest(new Quest(
            "daily_blooming_constraints_explosive", "Blooming in Constraints (Explosive)",
            "Win a level without using any Explosive family plants.",
            QuestCategory.DAILY, QuestPriority.HIGH,
            List.of(new CurrencyReward(CurrencyKind.GEM, 100)), 1
        ));

        log.addQuest(new Quest(
            "daily_win_after_win", "Win After Win",
            "Win 5 consecutive levels on the highest difficulty.",
            QuestCategory.DAILY, QuestPriority.MEDIUM,
            List.of(new CurrencyReward(CurrencyKind.COIN, 5000)), 5
        ));

        log.addQuest(new Quest(
            "daily_almost_victorious", "Almost Victorious",
            "Kill 10 zombies in the first column of a row that has no lawnmower.",
            QuestCategory.DAILY, QuestPriority.MEDIUM,
            List.of(new CurrencyReward(CurrencyKind.COIN, 300)), 10
        ));

        log.addQuest(new Quest(
            "daily_anti_ocd", "Anti-OCD",
            "Win a level where there is no symmetry in the garden (except the middle row).",
            QuestCategory.DAILY, QuestPriority.MEDIUM,
            List.of(new CurrencyReward(CurrencyKind.COIN, 800)), 1
        ));

        log.addQuest(new Quest(
            "daily_cloudy_day", "Cloudy Day",
            "Win a level with only 3 sun-producing plants.",
            QuestCategory.DAILY, QuestPriority.HIGH,
            List.of(new CurrencyReward(CurrencyKind.GEM, 10)), 1
        ));

        log.addQuest(new Quest(
            "daily_one_less_column_2", "One Less Column (2)",
            "Win a level without planting in column 2.",
            QuestCategory.DAILY, QuestPriority.HIGH,
            List.of(new CurrencyReward(CurrencyKind.GEM, 10)), 1
        ));
        log.addQuest(new Quest(
            "daily_one_less_column_3", "One Less Column (3)",
            "Win a level without planting in column 3.",
            QuestCategory.DAILY, QuestPriority.HIGH,
            List.of(new CurrencyReward(CurrencyKind.GEM, 10)), 1
        ));

        log.addQuest(new Quest(
            "daily_defenseless_row_1", "Defenseless Row (1)",
            "Win a level without planting in row 1.",
            QuestCategory.DAILY, QuestPriority.HIGH,
            List.of(new CurrencyReward(CurrencyKind.GEM, 20)), 1
        ));
        log.addQuest(new Quest(
            "daily_defenseless_row_3", "Defenseless Row (3)",
            "Win a level without planting in row 3.",
            QuestCategory.DAILY, QuestPriority.HIGH,
            List.of(new CurrencyReward(CurrencyKind.GEM, 20)), 1
        ));

        log.addQuest(new Quest(
            "daily_defenseless_cross_2_2", "Defenseless Cross (2,2)",
            "Win a level without planting in row 2 or column 2.",
            QuestCategory.DAILY, QuestPriority.HIGH,
            List.of(new CurrencyReward(CurrencyKind.GEM, 25)), 1
        ));

        // === MAIN QUESTS ===
        log.addQuest(new Quest(
            "main_chapter_hunter_egypt", "Chapter Hunter (Ancient Egypt)",
            "Defeat 50 zombies from the Ancient Egypt chapter.",
            QuestCategory.MAIN, QuestPriority.HIGH,
            List.of(new InventoryReward("seed_packet", 10)), 50
        ));
        log.addQuest(new Quest(
            "main_chapter_hunter_caves", "Chapter Hunter (Frostbite Caves)",
            "Defeat 50 zombies from the Frostbite Caves chapter.",
            QuestCategory.MAIN, QuestPriority.HIGH,
            List.of(new InventoryReward("seed_packet", 10)), 50
        ));
        log.addQuest(new Quest(
            "main_chapter_hunter_ages", "Chapter Hunter (Dark Ages)",
            "Defeat 50 zombies from the Dark Ages chapter.",
            QuestCategory.MAIN, QuestPriority.HIGH,
            List.of(new InventoryReward("seed_packet", 10)), 50
        ));
        log.addQuest(new Quest(
            "main_chapter_hunter_beach", "Chapter Hunter (Big Wave Beach)",
            "Defeat 50 zombies from the Big Wave Beach chapter.",
            QuestCategory.MAIN, QuestPriority.HIGH,
            List.of(new InventoryReward("seed_packet", 10)), 50
        ));

        log.addQuest(new Quest(
            "main_economical_0", "Economical Vegetarian (Perfect)",
            "Win a level without losing any plants.",
            QuestCategory.MAIN, QuestPriority.HIGH,
            List.of(new InventoryReward("seed_packet", 20)), 1
        ));
        log.addQuest(new Quest(
            "main_economical_1", "Economical Vegetarian (1 Loss)",
            "Win a level losing at most 1 plant.",
            QuestCategory.MAIN, QuestPriority.HIGH,
            List.of(new InventoryReward("seed_packet", 19)), 1
        ));
        log.addQuest(new Quest(
            "main_economical_2", "Economical Vegetarian (2 Losses)",
            "Win a level losing at most 2 plants.",
            QuestCategory.MAIN, QuestPriority.HIGH,
            List.of(new InventoryReward("seed_packet", 18)), 1
        ));
        log.addQuest(new Quest(
            "main_economical_3", "Economical Vegetarian (3 Losses)",
            "Win a level losing at most 3 plants.",
            QuestCategory.MAIN, QuestPriority.HIGH,
            List.of(new InventoryReward("seed_packet", 17)), 1
        ));
        log.addQuest(new Quest(
            "main_economical_4", "Economical Vegetarian (4 Losses)",
            "Win a level losing at most 4 plants.",
            QuestCategory.MAIN, QuestPriority.HIGH,
            List.of(new InventoryReward("seed_packet", 16)), 1
        ));
        log.addQuest(new Quest(
            "main_economical_5", "Economical Vegetarian (5 Losses)",
            "Win a level losing at most 5 plants.",
            QuestCategory.MAIN, QuestPriority.HIGH,
            List.of(new InventoryReward("seed_packet", 15)), 1
        ));

        log.addQuest(new Quest(
            "main_quick_reflexes", "Quick Reflexes",
            "Kill 10 zombies in less than 30 seconds from the start of the first zombie attack wave.",
            QuestCategory.MAIN, QuestPriority.MEDIUM,
            List.of(new CurrencyReward(CurrencyKind.COIN, 500)), 10
        ));

        // === CHALLENGE / EPIC QUESTS ===
        log.addQuest(new Quest(
            "epic_defense_master", "Defense Master",
            "Complete a level with exactly zero sun.",
            QuestCategory.EPIC, QuestPriority.CRITICAL,
            List.of(new CurrencyReward(CurrencyKind.GEM, 200)), 1
        ));

        log.addQuest(new Quest(
            "epic_night_or_morning", "Night or Morning",
            "Finish a day game level with night plants (mushrooms).",
            QuestCategory.EPIC, QuestPriority.HIGH,
            List.of(new CurrencyReward(CurrencyKind.GEM, 20)), 1
        ));

        log.addQuest(new Quest(
            "epic_lawnmowing_10", "Lawnmowing Time (10)",
            "Kill at least 10 zombies with a lawnmower.",
            QuestCategory.EPIC, QuestPriority.MEDIUM,
            List.of(new CurrencyReward(CurrencyKind.GEM, 10)), 10
        ));
        log.addQuest(new Quest(
            "epic_lawnmowing_20", "Lawnmowing Time (20)",
            "Kill at least 20 zombies with a lawnmower.",
            QuestCategory.EPIC, QuestPriority.MEDIUM,
            List.of(new CurrencyReward(CurrencyKind.GEM, 20)), 20
        ));
        log.addQuest(new Quest(
            "epic_lawnmowing_30", "Lawnmowing Time (30)",
            "Kill at least 30 zombies with a lawnmower.",
            QuestCategory.EPIC, QuestPriority.MEDIUM,
            List.of(new CurrencyReward(CurrencyKind.GEM, 30)), 30
        ));
        log.addQuest(new Quest(
            "epic_lawnmowing_40", "Lawnmowing Time (40)",
            "Kill at least 40 zombies with a lawnmower.",
            QuestCategory.EPIC, QuestPriority.MEDIUM,
            List.of(new CurrencyReward(CurrencyKind.GEM, 40)), 40
        ));
        log.addQuest(new Quest(
            "epic_lawnmowing_50", "Lawnmowing Time (50)",
            "Kill at least 50 zombies with a lawnmower.",
            QuestCategory.EPIC, QuestPriority.MEDIUM,
            List.of(new CurrencyReward(CurrencyKind.GEM, 50)), 50
        ));

        return log;
    }
}
