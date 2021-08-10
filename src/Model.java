import java.util.*;

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
     * Points is a 26-items long array. Even though there are only 24 points in
     * the board, we use the first and the last one as a bar. Positive values
     * of items represent black checkers while negative ones represent whites.
     * <p>
     * White is trying to get all checkers to the left (i.e. towards 1) and
     * black is trying to get them all to the right (i.e. to 24).
     * <p>
     * 0th point represents black player's bar, where value n means n checkers on bar,
     * and 25th point represents white player's bar where value -n means n checkers on bar.
     */
    private int[] points;

    /**
     * Currently active dice.
     */
    private List<Integer> dice;

    /**
     * Counts the turns in the game.
     */
    private int round;

    /**
     * Tells the order of the turns by direction (i.e. positive negative).
     */
    private final int[] turns;

    // MARK: - Constructors

    public Game() {
        this.points = new int[]{0, 2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0, 0, 0, -1, -1};
        this.round = 0;
        this.turns = new int[]{-1, 1};
        this.dice = new ArrayList<Integer>();

        this.roll();
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
    public int[] getDice() {
        int[] dice = new int[this.dice.size()];

        for (int i = 0; i < this.dice.size(); i++) {
            dice[i] = this.dice.get(i);
        }

        return dice;
    }


    // MARK: - Methods


    /**
     * Returns the direction of the point.
     */
    private static int getPointDirection(int[] board, int index) {
        if (board[index] == 0) return 0;
        return board[index] / Math.abs(board[index]);
    }

    private int getPointDirection(int index) {
        return getPointDirection(this.points, index);
    }


    /**
     * Tells the direction of the player that is currently playing.
     */
    private int getPlayer() {
        return this.turns[this.round % 2];
    }

    /**
     * Tells whether a player can make a move.
     */
    private static boolean isMoveValid(int[] board, int direction, int start, int end) {
        // Check that we are taking from the right pile.
        if (getPointDirection(board, start) * direction < 0) return false;

        // Number of checkers locked on the bar.
        int bar = (1 - direction) / 2 * 25;
        int locked = Math.abs(board[bar]);

        // Check if we are pulling from the bar and if we need to.
        if (locked > 0 && bar != start) return false;

        // Check if we are pulling off the board.
        if (end < 0 || end > 25) return true;

        // We can beat the other player.
        if (board[end] * direction < 0 && Math.abs(board[end]) == 1) return true;

        // Check that direction is respected.
        return board[start] * board[end] >= 0;
    }

    private boolean isMoveValid(int start, int end) {
        return isMoveValid(this.points, this.getPlayer(), start, end);
    }

    /**
     * Returns a 26-items long list telling how many moves there are from each field.
     */
    public int[] getMovableCheckers() {
        int[] movables = new int[26];

        for (int i = 0; i < movables.length; i++) {
            movables[i] = getMoves(i).size();
        }

        return movables;
    }

    /**
     * Tells where the player may move the checkers from the starting point.
     */
    public Set<Integer> getMoves(int start) {
        int direction = this.getPointDirection(start);

        HashSet<Integer> moves = new HashSet<Integer>();

        /**
         * We iterate over all dice combinations and check for each
         * combination whether we could make a reasonable move with it.
         */
        for (int i = 0; i < this.dice.size(); i++) {
            int end = start + this.dice.get(i) * direction;
            int[] board = this.points.clone();

            // Check the bounds.
            if (end < 0) end = 0;
            if (end > 25) end = 25;

            // Make sure that moves is valid.
            if (!this.isMoveValid(start, end)) continue;

            // Check if there's only one die left.
            if (this.dice.size() == 1)  {
                moves.add(end);
                continue;
            }

            // Otherwise, make the first move and see if we can make it out.
            board = move(board, start, end);

            second:
            for (int j = 0; j < this.dice.size(); j++) {
                // We can't repeat the same die.
                if (j == i) continue;

                // We check for each point with the same orientation whether we can make
                // a move for remaining number of points.
                for (int point = 0; point < board.length; point++) {
                    // Skip points that are not ours.
                    if (direction * board[point] <= 0) continue;

                    int player = this.getPlayer();

                    // Check if the move is valid.
                    if (isMoveValid(board, player, point, point + this.dice.get(j) * direction)) {
                        moves.add(end);
                        break second;
                    }
                }
            }
        }

        return moves;
    }

    /**
     * Rolls the dice.
     */
    private void roll() {
        this.dice = new ArrayList<Integer>();

        for (int i = 0; i < 2; i++) {
            this.dice.add((int) Math.ceil(Math.random() * 6));
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
        if (start == end) return board;

        // Figure out which checker are we trying to move.
        int direction = getPointDirection(board, start);

        // Remove the checker from the starting field.
        board[start] -= direction;

        // Check that we are still on the board when making a move.
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

    // todo when overflowing!
    public void move(int start, int end) {
        this.points = move(this.points, start, end);

        // Update the dice.
        Integer die = Math.abs(end - start);
        this.dice.remove(die);

        // New turn.
        if (this.dice.size() == 0) {
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



