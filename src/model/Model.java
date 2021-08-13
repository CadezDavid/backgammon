package model;

import java.awt.Color;

/**
 * model.Model represents the state of the app. Everything that persists for a longer
 * period of time or is used by many parts of the code should be defined in
 * model.
 */
public class Model {

    // MARK: - Constants

    private static final Color WHITE_POINT = new Color(245, 223, 213);
    private static final Color BLACK_POINT = new Color(74, 47, 31);
    private static final Color WHITE_CHECKER = new Color(255, 232, 222);
    private static final Color BLACK_CHECKER = new Color(51, 46, 44);

    // MARK: - State

    private Game game;

    public Player black;
    public Player white;

    // MARK: - Constructor

    public Model() {
        this.white = new Player("White", Player.Type.HUMAN, WHITE_CHECKER, WHITE_POINT);
        this.black = new Player("Black", Player.Type.HUMAN, BLACK_CHECKER, BLACK_POINT);

        this.game = new Game();
    }

    // MARK: - Methods

    /**
     * Starts a new game.
     */
    public void startGame() {
        this.game = new Game();
    }

    /**
     * Returns the currently played game.
     */
    public Game getGame() {
        return this.game;
    }
}
