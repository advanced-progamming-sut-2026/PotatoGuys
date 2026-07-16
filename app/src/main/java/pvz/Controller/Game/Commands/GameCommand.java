package pvz.Controller.Game.Commands;

import pvz.Models.Games.GameContext;

/**
 * A single, self-contained user action (e.g. "plant", "break", "swap").
 * Commands are pattern-matched against {@code Capabilities} interfaces that
 * concrete {@link pvz.Models.Games.Modes.GameMode} implementations opt into,
 * so neither {@link CommandManager} nor {@link GameContext} ever need to know
 * which mini-games exist. Adding a new command or a new mini-game never
 * requires modifying this interface or {@code CommandManager}.
 */
public interface GameCommand {

    /** The token typed by the player to invoke this command (e.g. "plant"). */
    String getName();

    /** A short usage/help description shown to the player. */
    String getDescription();

    /** Execute the command against the current game state. */
    void execute(GameContext context, String[] args) throws Exception;
}
