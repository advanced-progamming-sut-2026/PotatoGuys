package pvz.Controller.Game.Commands;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import pvz.Models.Games.GameContext;

/**
 * Central command dispatcher. Registers every {@link GameCommand} exactly
 * once during class initialization, then parses raw input lines and routes
 * them to the matching command.
 *
 * <p>It does NOT need to register/unregister commands as the active
 * {@link pvz.Models.Games.Modes.GameMode} changes: unsupported command/mode
 * combinations are rejected by the command itself via capability pattern
 * matching (see {@link PlantCommand}, {@link BreakCommand}, {@link SwapCommand}).
 * Adding a new command or mini-game never requires modifying this class.
 */
public final class CommandManager {

    private static final Map<String, GameCommand> COMMANDS = new LinkedHashMap<>();

    static {
        register(new PlantCommand());
        register(new BreakCommand());
        register(new SwapCommand());
    }

    private CommandManager() {}

    private static void register(GameCommand command) {
        COMMANDS.put(command.getName().toLowerCase(), command);
    }

    /** Parses a raw input line (e.g. "plant peashooter 2 1") and executes the matching command. */
    public static void dispatch(GameContext context, String rawInput) {
        String trimmed = rawInput == null ? "" : rawInput.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        String[] tokens = trimmed.split("\\s+");
        String name = tokens[0].toLowerCase();
        String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

        GameCommand command = COMMANDS.get(name);
        if (command == null) {
            context.log("Unknown command: " + tokens[0]);
            return;
        }

        try {
            command.execute(context, args);
        } catch (Exception e) {
            context.log("Error executing '" + name + "': " + e.getMessage());
        }
    }

    public static Map<String, GameCommand> getCommands() {
        return Collections.unmodifiableMap(COMMANDS);
    }
}
