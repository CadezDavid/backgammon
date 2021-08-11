import java.lang.reflect.Array;
import java.util.*;

/**
 * Model represents the state of the app. Everything that persists for a longer
 * period of time or is used by many parts of the code should be defined in
 * model.
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

        this.game = new Game();
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

    /**
     * Currently active dice.
     */
    private ArrayList<Integer> dice;

    /**
     * Counts the turns in the game.
     */
    private int round;

    /**
     * Tells the order of the players by direction (i.e. positive negative).
     */
    private final int[] turns;

    // MARK: - Constructors

    public Game() {
        this.points = new int[] { 0, 2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0, 0, 0, -1, -1 };
        this.round = 0;
        this.turns = new int[] { -1, 1 };
        this.dice = new ArrayList<Integer>();

        this.roll();
    }

    // MARK: - Accessors

    /**
     * Returns the current board.
     */
    public int[] getPoints() {
        return this.points;
    }

    /**
     * Returns the current dice.
     */
    public List<Integer> getDice() {
        return this.dice;
    }

    enum State {
        IN_PROGRESS, WIN_BLACK, WIN_WHITE
    }

    /**
     * Tells the current state of the game.
     */
    public State getState() {
        // Remaining checkers on the board.
        int whites = 0;
        int blacks = 0;

        for (int i = 0; i < this.points.length; i++) {
            int checkers = this.points[i];

            if (checkers < 0)
                whites -= checkers;
            else
                blacks += checkers;
        }

        // Calculate the winner.
        if (whites == 0)
            return State.WIN_WHITE;
        if (blacks == 0)
            return State.WIN_BLACK;
        return State.IN_PROGRESS;
    }

    /**
     * Tells the direction of the player that is currently playing.
     */
    public int getPlayer() {
        return this.turns[this.round % 2];
    }

    // MARK: - Methods

    /**
     * Returns the direction of the point.
     */
    private static int getPointDirection(int[] board, int index) {
        // if (board[index] == 0) return 0;
        return board[index] / Math.abs(board[index]);
    }

    /**
     * Tells whether a player can make a move. It has no idea about the dice or
     * anything. It only tells whether the move is strictly valid.
     *
     * @param direction Tells the direction of the player.
     */
    private static boolean isMoveValid(int[] board, int direction, int start, int end) {
        // Check that we are moving in the right direction.
        if ((end - start) * direction < 0)
            return false;

        // Check that we are taking from the right pile.
        if (getPointDirection(board, start) * direction < 0)
            return false;

        // Number of checkers locked on the bar.
        int bar = (1 - direction) / 2 * 25;
        int locked = Math.abs(board[bar]);

        // Check if we have any checkers locked on the bar.
        if (locked > 0 && bar != start)
            return false;

        // Check if we are moving to an illegal place.
        if (end < 0 || end > 25)
            return false;

        // Check if we are bearing off.
        int home = 25 - bar;
        if (end == home) {
            /*
             * We check every point besides the ones at home if there's our checker on it.
             * There shouldn't be!
             *
             * Player can only bear checkers off the board if all of them are home.
             */
            int first = 7 * (1 - direction) / 2; // 0 or 7
            int point = 17 + first; // 17 or 24

            for (; first < point; point--) {
                if (board[point] * direction > 0)
                    return false;
            }

            return true;
        }

        // We can hit the other player.
        if (board[end] * direction < -1)
            return false;

        // Check that direction is respected.
        return board[start] * board[end] >= 0;
    }

    /**
     * Returns a 26-items long list telling how many moves there are from each
     * field.
     */
    public int[] getMovableCheckers() {
        int[] movables = new int[26];

        for (int i = 0; i < movables.length; i++) {
            movables[i] = getMoves(i).size();
        }

        return movables;
    }

    /**
     * Tells where the player may move the checker from the given starting point and
     * the current state of the game.
     */
    public Set<Integer> getMoves(int start) {
        int player = this.getPlayer();
        return getMoves(this.points, player, this.dice, start);
    }

    /**
     * Tells where the player may move the checkers from the starting point.
     */
    public static Set<Integer> getMoves(int[] points, int player, ArrayList<Integer> dice, int start) {
        HashSet<Integer> moves = new HashSet<Integer>();

        // Check that there's anything to move.
        if (points[start] == 0)
            return moves;

        // Calculate the color of the checker we are moving.
        int direction = getPointDirection(points, start);

        // If the other player is on the move you can't make any moves.
        if (player * direction < 0)
            return moves;

        /*
         * We iterate over all dice combinations and check for each combination whether
         * we could make a reasonable move with it.
         */
        for (int i = 0; i < dice.size(); i++) {
            int end = start + direction * dice.get(i);

            // Copy the dice and remove the current dice.
            ArrayList<Integer> rdice = (ArrayList<Integer>) dice.clone();
            rdice.remove(i);

            if (isPossibleMove(points, start, end, rdice))
                moves.add(end);
        }

        /*
         * Check if we can make any move anywhere. If we find a move we can make we
         * should just return what we have found so far.
         */
        for (int i = 0; i < points.length; i++) {
            if (points[i] == 0)
                continue;

            for (int j = 0; j < dice.size(); j++) {
                int end = i + direction * dice.get(j);

                // Copy the dice and remove the current dice.
                ArrayList<Integer> rdice = (ArrayList<Integer>) dice.clone();
                rdice.remove(j);

                if (isPossibleMove(points, i, end, rdice))
                    return moves;
            }
        }

        /*
         * If we can't make any move here, check if we can make any single move
         * anywhere. We don't have to check every of the four moves since they are all
         * the same dice, and we can just repeat the move until we run out of options.
         */
        if (moves.isEmpty() && dice.size() > 0) {
            int higher = Collections.max(dice);
            int lower = Collections.min(dice);

            for (int move : new int[] { higher, lower }) {
                int end = start + move * direction;

                if (isPossibleMove(points, start, end, new ArrayList<>())) {
                    moves.add(end);
                    break;
                }
            }
        }

        return moves;
    }

    // todo: check if we are bearing off the furthest checker

    /**
     * Tells whether a player could make a given move and use all dice.
     */
    private static boolean isPossibleMove(int[] points, int start, int end, ArrayList<Integer> dice) {
        int diff = end - start;
        int direction = diff / Math.abs(diff);

        // Make sure that moves is valid.
        if (!isMoveValid(points, direction, start, end))
            return false;

        // Check if this is the last move.
        if (dice.size() == 0)
            return true;

        // Otherwise, make the first move and see if we can recursively make it.
        int[] board = move(points.clone(), start, end);

        for (int j = 0; j < dice.size(); j++) {
            int die = dice.get(j);

            // We check for each point with the same orientation whether we can make
            // a move for remaining number of points.
            for (int point = 0; point < board.length; point++) {
                // Skip points that are not ours.
                if (direction * board[point] <= 0)
                    continue;

                // Copy the dice and remove the current dice.
                ArrayList<Integer> rdice = (ArrayList) dice.clone();
                rdice.remove(j);

                // Check if we can meaningfully make other moves.
                if (isPossibleMove(board, point, point + die * direction, rdice))
                    return true;
            }
        }

        return false;
    }

    /**
     * Rolls the dice.
     */
    private void roll() {
        this.dice = new ArrayList<Integer>();

        // Roll the dice.
        for (int i = 0; i < 2; i++) {
            // this.dice.add(6);
            this.dice.add((int) Math.ceil(Math.random() * 6));
        }

        // Double the points on combo.
        if (dice.get(0).equals(dice.get(1))) {
            int val = dice.get(0);

            dice.add(val);
            dice.add(val);
        }

        System.out.println(this.dice);
    }

    /**
     * Performs a given move and returns a board.
     */
    public static int[] move(int[] board, int start, int end) {
        // Make sure we are not writing to the original board.
        board = board.clone();

        // Check that we are performing a move.
        if (start == end)
            return board;

        // Figure out which checker are we trying to move.
        int direction = getPointDirection(board, start);

        // Remove the checker from the starting field.
        board[start] -= direction;

        // Check if we are still on the board when making a move.
        if (1 < end && end < 25) {
            if (board[end] * direction >= 0) {
                // Regularly move the checker if we are not beating.
                board[end] += direction;
            } else {
                // If it is white player's move, black should be beat and vice-versa.
                // That is, when it's white, we should "add" one to the other player's
                // bench.
                board[(1 + direction) / 2 * 25] -= direction;
                board[end] = direction;
            }
        }

        return board;
    }

    public void move(int start, int end) {
        this.points = move(this.points, start, end);

        // Update the dice.
        Integer die = Math.abs(end - start);
        this.dice.remove(die);

        int moves = Arrays.stream(this.getMovableCheckers()).sum();

        // New turn.
        if (this.dice.size() == 0 || moves == 0) {
            this.round++;
            this.roll();
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
