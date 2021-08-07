import java.util.*;
import java.util.stream.Collectors;

/**
 * Model represents the state of the app. Everything that persists
 * for a longer period of time or is used by many parts of the code
 * should be defined in model.
 */
class Model {

    // MARK: - State

    private Game game;

    public Player black;
    public Player white;

    // MARK: - Constructor

    public Model() {
        this.white = new Player();
        this.black = new Player();

        this.game = null;
    }

    public Model(String nameBP, String nameWP, Player.Type typeBP, Player.Type typeWP) {
        this.game = new Game();

        this.black = new Player(nameWP, typeWP);
        this.white = new Player(nameBP, typeBP);
    }

    // MARK: - Methods

    /**
     * Returns the currently played game.
     *
     * @return
     */
    public Game getGame() {
        return this.game;
    }
}

/**
 * Outlines a state of a single game.
 */
class Game {

    /**
     * Pips is a 26-items long array. Even though there are only 24 pips in
     * the board, we use the first and the last one as a bar. Positive values
     * of items represent black checkers while negative ones represent whites.
     * <p>
     * White is trying to get all checkers to the left (i.e. towards 1) and
     * black is trying to get them all to the right (i.e. to 24).
     * <p>
     * 0th pip represents black player's bar, where value n means n checkers on bar,
     * and 25th pip represents white player's bar where value -n means n checkers on bar.
     */
    private int[] pips;

    /**
     * Tells the phase that the game is in.
     */
    private State state;

    enum State {
        // meaning of states:
        // STARTING - before the start, when players roll the die to determine who
        // starts first
        // MOVE_WHITE - white player on the move
        // MOVE_BLACK - black player on the move
        // DRAW - game ended in a draw
        // WIN_BLACK - black player won
        // WIN_WHITE - white player won
        STARTING,
        MOVE_WHITE,
        MOVE_BLACK,
        DRAW,
        WIN_BLACK,
        WIN_WHITE
    }


    // MARK: - Constructors

    public Game() {
        pips = new int[]{0, 2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0, 0, 0, -2, 0};
        this.state = State.STARTING;
    }

    // MARK: - Accessors

    /**
     * Returns the current board.
     * @return
     */
    public int[] getBoard() {
        return this.pips;
    }


    // MARK: - Methods

//    public List<LinkedList<Move>> getAllPlays(Checker checker, int[] dice) {
//        List<LinkedList<Move>> plays = new ArrayList<LinkedList<Move>>();
//
//        for (int i = 0; i < dice.length; i++) {
//            List<Move> moves = getValidMoves(checker, dice[i]);
//            int[] clone = new int[dice.length - 1];
//            for (int j = 0; j < dice.length && j != i; j++) {
//                clone[j - (j < i ? 1 : 0)] = dice[i];
//            }
//            for (LinkedList<Move> play : getAllPlays(checker, clone)) {
//                for (Move move : moves) {
//                    play.push(move);
//                    plays.add(play);
//                }
//            }
//        }
//
//        // removes duplicates and ensures all plays will be the same length
//        List<LinkedList<Move>> playsNoDup = plays.stream().distinct().collect(Collectors.toList());
//        playsNoDup.remove(new LinkedList<Move>());
//        if (playsNoDup.isEmpty()) {
//            playsNoDup.add(new LinkedList<Move>());
//        }
//        return playsNoDup;
//    }

    /**
     * Tells where the player may move the checkers from the starting point.
     *
     * @param start
     * @return
     */
    public Set<Integer> getMoves(int start) {
        int[] dies = new int[] {3, 5};

        HashSet<Integer> moves = new HashSet<Integer>();

        for(int die: dies) {
            for (int i = 0; i < this.pips.length; i++) {
                if (this.pips[start] * this.pips[i] >= 0) {

                }
            }
        }
        return moves;
    }

    /**
     * Performs a given move.
     *
     * @param start
     * @param end
     */
    public void move(int start, int end) {
        int direction = this.pips[start] / Math.abs(this.pips[start]);

        // Update the board.
        this.pips[start] -= direction;
        this.pips[end] += direction;


        // Throw the dice if necessary.

        // case in which the opposing player has been hit
//        if (c * pips[move.getEndPip()] == -1) {
//            pips[move.getStartPip()] -= c;
//            pips[move.getEndPip()] = c;
//            pips[(25 + 25 * c) / 2] -= c;
//        }
//        // usual case
//        else {
//            pips[move.getStartPip()] -= c;
//            pips[move.getEndPip()] += c;
//        }
    }

    /**
     * Tells whether a player can make a move.
     * @param start
     * @param end
     * @return
     */
//    private boolean isMoveValid(int start, int end) {
//        int c = (checker == Checker.BLACK ? 1 : -1);
//
//        // if player is move from the bar
//        if (move.getStartPip() == 0 || move.getStartPip() == 25) {
//            // if checkers on the board make sense
//            if ((pips[move.getStartPip()] > 0) && (c * pips[move.getEndPip()] > -2)) {
//                return false;
//            }
//
//            // if player has no checkers on the bar
//            if (pips[(25 - 25 * c) / 2] == 0) {
//                return false;
//            }
//        }
//        // usual case
//        else {
//            // if checkers on the board make sense
//            if ((c * pips[move.getStartPip()] > 0) && (c * pips[move.getEndPip()] > -2)) {
//                return false;
//            }
//
//            // if player has checkers on the bar
//            if (pips[(25 - 25 * c) / 2] != 0) {
//                return false;
//            }
//        }
//
//        // if the direction of moving is correct
//        if ((move.getEndPip() - move.getStartPip()) * c <= 0) {
//            return false;
//        }
//
//        return true;
//    }

//    private boolean canBearOff(Checker checker) {
//        int c = (checker == Checker.BLACK ? 0 : 7);
//
//        for (int i = c; i < 19 + c; i++) {
//            if (pips[i] * c > 0) {
//                return false;
//            }
//        }
//
//        return true;
//    }

}

/**
 * Player outlines a single player in the game.
 */
class Player {

    /**
     * Tells the name of the player that we display in the game.
     */
    private String name;

    /**
     * Tells whether a player is a human or a computer.
     */
    private Type type;

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


enum Checker {
    WHITE, BLACK
}

