package com.pvz.controller.game.npc;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Maps {@code (season name, level number)} to the {@link NpcDialogueSequence}
 * shown before that level's objectives. Season names are matched
 * case-insensitively (e.g. "ancient egypt" / "Ancient Egypt").
 *
 * <p>Port of group-51's {@code LevelDialogueRegistry}, adapted to the PvZ2
 * project's string-based season names.
 */
public final class LevelDialogueRegistry {
    private static final String CRAZY_DAVE_PAM =
            "768/INITIAL/CRAZYDAVE/CRAZYDAVE/CRAZYDAVE.PAM";

    private static final Map<LevelKey, NpcDialogueSequence> LEVEL_DIALOGUES =
            createDialogues();

    private LevelDialogueRegistry() {
    }

    /**
     * @return the dialogue registered for the level, or {@code null} if none.
     */
    public static NpcDialogueSequence find(String seasonName, int levelNumber) {
        if (seasonName == null) {
            return null;
        }
        return LEVEL_DIALOGUES.get(new LevelKey(seasonName.toLowerCase(Locale.ROOT), levelNumber));
    }

    private static Map<LevelKey, NpcDialogueSequence> createDialogues() {
        Map<LevelKey, NpcDialogueSequence> dialogues = new HashMap<>();

        dialogues.put(
                new LevelKey("ancient egypt", 1),
                crazyDave(
                        "Whoa! Sand, pyramids, and zombies. "
                                + "Yep, definitely Ancient Egypt.",
                        "Plant carefully! Those bandaged brain-munchers "
                                + "are not here for the sightseeing.",
                        "Keep them away from the house, and try not to "
                                + "get sand in the lawn mower!"
                )
        );

        dialogues.put(
                new LevelKey("big wave beach", 1),
                crazyDave(
                        "Water on the lawn! Snorkels, waves, and palm "
                                + "trees - who invited the beach?",
                        "Zombies wade through the tide toward your house, "
                                + "so plant on land AND sea.",
                        "Watch the surf! Those snorkelers are sneakier "
                                + "than they look."
                )
        );

        dialogues.put(
                new LevelKey("dark ages", 1),
                crazyDave(
                        "Gravestones? In my backyard? Hmm, "
                                + "one taco too many...",
                        "Graves keep spawning zombies, so smash them "
                                + "before they overrun the lawn!",
                        "Knights and jugglers ahead. Stay sharp - that "
                                + "mist is no sleepy fog!"
                )
        );

        dialogues.put(
                new LevelKey("frostbite caves", 1),
                crazyDave(
                        "Brrr! A blizzard in my garden. "
                                + "Who left the freezer open?",
                        "The wind blows zombies across the ice, "
                                + "so mind where you plant!",
                        "Thaw out those frozen plants and keep an eye "
                                + "out for cave-crawlers!"
                )
        );

        dialogues.put(
                new LevelKey("izombie", 1),
                crazyDave(
                        "Flip side time! Today YOU are the zombie. "
                                + "Spooky, huh?",
                        "Spend sun to call in zombies and chomp through "
                                + "every plant on the lawn!",
                        "Eat them all before the lawnmower - or the "
                                + "veggies - chew you up first!"
                )
        );

        dialogues.put(
                new LevelKey("splitizombie", 1),
                crazyDave(
                        "A lawn split in two! Plants on one side, "
                                + "zombies on the other. Balanced!",
                        "Watch your own side while sending zombies down "
                                + "theirs. Don't forget your lawn!",
                        "Two fronts, one winner. Whoever's left standing "
                                + "gets the tacos!"
                )
        );

        dialogues.put(
                new LevelKey("vasebreaker", 1),
                crazyDave(
                        "Vases! Some hide plants, some hide zombies. "
                                + "It's a gamble!",
                        "Break them one at a time, grab the goodies, and "
                                + "pray the zombies stay locked up.",
                        "Keep your wits and your luck - lawn arcade, "
                                + "PvZ style!"
                )
        );

        dialogues.put(
                new LevelKey("wallnut bowling", 1),
                crazyDave(
                        "No sun today - just a bucket of wall-nuts! "
                                + "Bowling time!",
                        "Pick up, aim, and roll those nuts straight "
                                + "into the zombie pins!",
                        "Strike, spare, or gutter ball - no zombie "
                                + "reaches the house either way!"
                )
        );

        return Map.copyOf(dialogues);
    }

    private static NpcDialogueSequence crazyDave(String... lines) {
        return new NpcDialogueSequence(
                CRAZY_DAVE_PAM,
                "anim_enter",
                "anim_idle",
                List.of(
                        "anim_smalltalk",
                        "anim_crazyblahblah"
                ),
                "anim_leave",
                List.of(lines)
        );
    }

    private record LevelKey(String seasonName, int levelNumber) {
    }
}
