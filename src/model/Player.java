package model;

import java.awt.*;

/**
 * model.Player outlines a single player in the game.
 */
public class Player {

    /**
     * Tells the name of the player that we display in the game.
     */
    public String name;

    /**
     * Tells whether a player is a human or a computer.
     */
    public Type type;

    public enum Type {
        COMPUTER, HUMAN
    }

    /**
     * The color of the checker on the board.
     */
    public Color checker;

    /**
     * Color of the point of this player.
     */
    public Color point;

    // MARK: - Constructor

    public Player(String name, Type type, Color checker, Color point) {
        this.name = name;
        this.type = type;
        this.checker = checker;
        this.point = point;
    }
}
