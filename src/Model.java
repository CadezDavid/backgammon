import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Model represents the state of the app. Everything that persists for a longer
 * period of time or is used by many parts of the code should be defined in
 * model.
 */
class Model {

    /**
     * Points is a 26-items long array. Even though there are only 24 points in the
     * board, we use the first and the last one as a bar. Positive values of items
     * represent black checkers while negative ones represent whites.
     * <p>
     * White is trying to get all checkers to the left (i.e. towards 1) and black is
     * trying to get them all to the right (i.e. to 24).
     * <p>
     * 0th point represents black player's bar, where value n means n checkers on
     * bar, and 25th point represents white player's bar where value -n means n
     * checkers on bar.
     */
    private int[] points;

    public Player black;
    public Player white;

    /**
     * Currently active dice.
     */
    private List<Integer> dice;

    /**
     * Tells the phase that the game is in.
     */
    private State state;

    /**
     * Tells all plays the current player can play. Could be used for telling the
     * state of a player's round instead of dice.
     */
    private List<LinkedList<Utils.Move>> allPlays;

    enum State {
        // meaning of states:
        // STARTING - before the start, when players roll the die to determine who
        // starts first
        // MOVE_WHITE - white player on the move
        // MOVE_BLACK - black player on the move
        // WIN_BLACK - black player won
        // WIN_WHITE - white player won
        STARTING, MOVE_WHITE, MOVE_BLACK, WIN_BLACK, WIN_WHITE
    }

    // MARK: - Constructors
    public Model(String nameBP, String nameWP, Player.Type typeBP, Player.Type typeWP) {
        this.black = new Player(nameWP, typeWP);
        this.white = new Player(nameBP, typeBP);

        this.points = new int[] { 0, 2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0, 0, 0, -1, -1 };
        this.state = State.MOVE_WHITE;
        // this.dice = new ArrayList<Integer>();

        this.roll();
        this.allPlays = Utils.getAllPlays(points, -1, dice);
    }

    public Model() {
        this.white = new Player();
        this.black = new Player();

        this.points = new int[] { 0, 2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0, 0, 0, -1, -1 };
        this.state = State.MOVE_WHITE;
        // this.dice = new ArrayList<Integer>();

        this.roll();
        this.allPlays = Utils.getAllPlays(points, -1, dice);
    }

    // MARK: - Accessors

    /**
     * Returns the current points.
     */
    public int[] getPoints() {
        return this.points;
    }

    /**
     * Returns the current dice.
     */
    public List<Integer> getDice() {
        return dice;
    }

    // MARK: - Methods


    /**
     * Calculates allPossiblePlays for current dice and returns all moves that can
     * be played as first which start on startPoint.
     */
    public Set<Integer> getAllPlays(int startPoint) {
        Set<Integer> plays = new HashSet<Integer>();
        List<LinkedList<Utils.Move>> allPlays = Utils.getAllPlays(this.points, getDirection(), getDice());
        for (LinkedList<Utils.Move> play : allPlays) {
            if ((!play.isEmpty()) && play.getFirst().getStartPoint() == startPoint) {
                plays.add(play.getFirst().getEndPoint());
            }
        }
        return plays;
    }





    /**
     * Performs a given move on this object and returns nothing.
     */
    public void move(Utils.Move m) {
        this.points = Utils.move(this.points, m);
        List<LinkedList<Utils.Move>> newAllPlays = new ArrayList<LinkedList<Utils.Move>>();
        for (LinkedList<Utils.Move> play : this.allPlays) {
            System.out.println(play.size());
            if (play.size() > 1 && play.getFirst().equals(m)) {
                play.pop();
                newAllPlays.add(play);
                System.out.print("Hej");
            }
        }
        this.allPlays = newAllPlays;
        if (newAllPlays.isEmpty()) {
            updateState();
            roll();
        }
        System.out.print(this.state);
    }

    private void updateState() {
        int white = 0;
        int black = 0;
        for (int i = 0; i < 26; i++) {
            if (points[i] > 0) {
                black += points[i];
            } else {
                white += points[i];
            }
        }

        if (black == 0) {
            state = State.WIN_BLACK;
        } else if (white == 0) {
            state = State.WIN_WHITE;
        } else {
            state = (state == State.MOVE_WHITE || state == State.STARTING ? State.MOVE_BLACK : State.MOVE_WHITE);
        }
    }

    public List<LinkedList<Utils.Move>> getAllPlays(List<Integer> dice) {
        int direction = (state == State.MOVE_BLACK ? 1 : -1);
        return Utils.getAllPlays(this.points, direction, dice);
    }

    private int getDirection() {
        return (state == State.MOVE_BLACK ? 1 : -1);
    }

    // i would put that in view, where player could "throw" the dice himself
    /**
     * Rolls the dice.
     */
    private void roll() {
        this.dice = new ArrayList<Integer>();

        for (int i = 0; i < 2; i++) {
            this.dice.add((int) Math.ceil(Math.random() * 6));
        }

        if (dice.get(0) == dice.get(1)) {
            dice.add(0);
            dice.add(0);
        }
    }

}

/**
 * Player outlines a single player in the game.
 */
class Player {

    /**
     * Tells the name of the player that we display in the game.
     */
    private final String name;

    /**
     * Tells whether a player is a human or a computer.
     */
    private final Type type;

    enum Type {
        COMPUTER, HUMAN
    }

    // MARK: - Constructor

    public Player() {
        this.name = "";
        this.type = Type.HUMAN;
    }

    public Player(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    // MARK: - Accessors

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}

