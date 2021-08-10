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
        this.allPlays = getAllPlays(points, -1, dice);
    }

    public Model() {
        this.white = new Player();
        this.black = new Player();

        this.points = new int[] { 0, 2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0, 0, 0, -1, -1 };
        this.state = State.MOVE_WHITE;
        // this.dice = new ArrayList<Integer>();

        this.roll();
        this.allPlays = getAllPlays(points, -1, dice);
    }

    // MARK: - Accessors

    /**
     * Returns the current board.
     */
    public int[] getBoard() {
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
     * Returns the direction of the point.
     */
    private static int getPointDirection(int[] board, int index) {
        if (board[index] == 0) {
            return 0;
        } else {
            return board[index] / Math.abs(board[index]);
        }
    }

    /**
     * Calculates allPossiblePlays for current dice and returns all moves that can
     * be played as first which start on startPoint.
     */
    public Set<Integer> getAllPlays(int startPoint) {
        Set<Integer> plays = new HashSet<Integer>();
        List<LinkedList<Utils.Move>> allPlays = getAllPlays(this.points, getDirection(), getDice());
        for (LinkedList<Utils.Move> play : allPlays) {
            if ((!play.isEmpty()) && play.getFirst().getStartPoint() == startPoint) {
                plays.add(play.getFirst().getEndPoint());
            }
        }
        return plays;
    }

    public static List<LinkedList<Utils.Move>> getAllPlays(int[] points, int direction, List<Integer> dice) {
        List<LinkedList<Utils.Move>> plays = new ArrayList<LinkedList<Utils.Move>>();

        if (dice.size() == 1) {
            for (int i = 0; i < 26; i++) {
                int end = i + direction * dice.get(0);
                Utils.Move move = new Utils.Move(i, end);
                LinkedList<Utils.Move> curr = new LinkedList<Utils.Move>();
                if (isMoveValid(points, direction, move)) {
                    curr.push(move);
                }
                plays.add(curr);
            }
            return plays;
        }

        for (int i = 0; i < dice.size(); i++) {

            List<Utils.Move> moves = getValidMoves(points, direction, dice.get(i));
            List<Integer> clone = new ArrayList<Integer>();
            for (int j = 0; j < dice.size(); j++) {
                clone.add(dice.get(j));
            }
            clone.remove(dice.get(i));

            List<LinkedList<Utils.Move>> playsRec = getAllPlays(move(points, new Utils.Move(i, i + direction * dice.get(0))),
                    direction, clone);
            for (LinkedList<Utils.Move> play : playsRec) {
                LinkedList<Utils.Move> tmp = (LinkedList<Utils.Move>) play.clone();
                for (Utils.Move move : moves) {
                    tmp.push(move);
                }
                plays.add(tmp);
            }
        }

        // removes duplicates and ensures all plays will be the same length
        plays.remove(new LinkedList<Utils.Move>());
        if (plays.isEmpty()) {
            plays.add(new LinkedList<Utils.Move>());
        }
        return plays;
    }

    public static List<Utils.Move> getValidMoves(int[] points, int direction, int die) {
        List<Utils.Move> moves = new ArrayList<>();

        for (int i = 0; i < 26; i++) {
            int end = i + direction * die;
            Utils.Move move = new Utils.Move(i, end);
            if (isMoveValid(points, direction, move)) {
                moves.add(move);
            }
        }
        return moves;
    }

    /**
     * Tells whether a player can make a move.
     */
    private static boolean isMoveValid(int[] points, int direction, Utils.Move move) {
        // if player has a checker on the start point
        if (points[move.getStartPoint()] * direction <= 0) {
            return false;
        }

        // if player has checkers on the bar and isn't moving those
        if (points[(25 - 25 * direction) / 2] != 0 && move.getStartPoint() != (25 - 25 * direction) / 2) {
            return false;
        }

        // if player is bearing off
        if (move.getEndPoint() <= 0 || 25 <= move.getEndPoint()) {
            if (!canBearOff(points, direction)) {
                return false;
            } else if (move.getEndPoint() < 0) {
                for (int i = 6; i < move.getStartPoint(); i--) {
                    if (points[i] < 0) {
                        return false;
                    }
                }
            } else if (25 < move.getEndPoint()) {
                for (int i = 18; i < move.getStartPoint(); i++) {
                    if (points[i] > 0) {
                        return false;
                    }
                }
            }
            return true;
        }

        // if checkers on the board make sense
        if (direction * points[move.getEndPoint()] < -1) {
            return false;
        }

        // if the direction of moving is correct
        if ((move.getEndPoint() - move.getStartPoint()) * direction <= 0) {
            return false;
        }

        return true;
    }

    /**
     * Performs a given move and returns a new board.
     */
    public static int[] move(int[] board, Utils.Move move) {
        // Make sure we are not writing to the original board.
        board = board.clone();

        // Check that we are performing a move.
        if (move.getStartPoint() == move.getEndPoint())
            return board;

        // Figure out which checker are we trying to move.
        int direction = getPointDirection(board, move.getStartPoint());

        // Remove the checker from the start point.
        board[move.getStartPoint()] -= direction;

        // Check that we are still on the board when making a move.
        if (0 < move.getEndPoint() && move.getEndPoint() < 25) {
            // Regularly move the checker if we are not beating.
            if (board[move.getEndPoint()] * direction >= 0) {
                board[move.getEndPoint()] += direction;
            } else {
                // If it is white player's move, black should be beat and vice-versa.
                // That is, when it's white, we should "add" one to the other player's
                // bench.
                board[(1 + direction) / 2 * 25] -= direction;
                board[move.getEndPoint()] = direction;
            }
        }

        return board;
    }

    private static boolean canBearOff(int[] points, int direction) {
        int c = 7 * (1 - direction) / 2;
        for (int i = c; i < 19 + c; i++) {
            if (points[i] * direction > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Performs a given move on this object and returns nothing.
     */
    public void move(Utils.Move m) {
        this.points = move(this.points, m);
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
        return getAllPlays(this.points, direction, dice);
    }

    private boolean isMoveValid(Utils.Move move) {
        return isMoveValid(this.points, this.getDirection(), move);
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

