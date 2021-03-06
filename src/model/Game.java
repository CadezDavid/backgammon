package model;


import java.util.*;

/**
 * Outlines a state of a single game.
 */
public class Game {

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
     * Rounds is a linked list of board states.
     */
    private LinkedList<int[]> rounds;

    /**
     * Tells the order of the players by direction (i.e. positive negative).
     */
    private final int[] turns;

    // MARK: - Constructors

    public Game() {
        this.points = new int[]{0, 2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0, 0, 0, -2, 0};
        this.rounds = new LinkedList<>();
        this.turns = new int[]{-1, 1};
        this.dice = new ArrayList<>();

        this.roll();
    }

    // MARK: - Accessors

    /**
     * Returns the current board.
     */
    public int[] getPoints() {
        return this.points;
    }

    public static int[] clonePoints(int[] points) {
        int[] clone = new int[26];
        System.arraycopy(points, 0, clone, 0, 26);
        return clone;
    }

    /**
     * Returns the current dice.
     */
    public ArrayList<Integer> getDice() {
        return this.dice;
    }

    public enum State {
        IN_PROGRESS, WIN_BLACK, WIN_WHITE
    }

    /**
     * Tells the state of the game from a given board.
     */
    public static State getState(int[] points) {
        // Remaining checkers on the board.
        int whites = 0;
        int blacks = 0;

        for (int checkers : points) {
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
     * Tells the current state of the game.
     */
    public State getState() {
        return getState(this.points);
    }

    /**
     * Tells the direction of the player that is currently playing.
     */
    public int getTurn() {
        return this.turns[this.rounds.size() % 2];
    }

    /**
     * Returns the direction of the point.
     */
    private static int getPointDirection(int[] board, int index) {
        if (board[index] == 0)
            return 0;
        return board[index] / Math.abs(board[index]);
    }

    /**
     * Tells the index of the given player's bar on the board.
     */
    private static int getPlayerBar(int direction) {
        return (1 - direction) / 2 * 25;
    }

    /**
     * Tells whether a player can make a move. It has no idea about the dice or
     * anything. It only tells whether the move is strictly valid.
     */
    public static boolean isMoveValid(int[] points, int start, int end) {
        if (start == end)
            return false;

        int diff = end - start;
        int direction = diff / Math.abs(diff);

        // Check that we are taking from the right pile.
        if (getPointDirection(points, start) * direction < 0)
            return false;

        // Number of checkers locked on the bar.
        int bar = getPlayerBar(direction);
        int locked = Math.abs(points[bar]);

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
             * model.Player can only bear checkers off the board if all of them are home.
             */
            int first = 7 * (1 - direction) / 2; // 0 or 7
            int point = 17 + first; // 17 or 24

            for (; first < point; point--) {
                if (points[point] * direction > 0)
                    return false;
            }

            return true;
        }

        // We can hit the other player only if it has a single checker there.
        if (points[end] * direction < 0)
            return Math.abs(points[end]) == 1;

        // Check that direction is respected.
        return points[start] * points[end] >= 0;
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
        int player = this.getTurn();
        return getMoves(this.points, player, this.dice, start);
    }

    /**
     * Tells where the player may move the checkers from the starting point.
     */
    public static Set<Integer> getMoves(int[] points, int player, ArrayList<Integer> dice, int start) {
        HashSet<Integer> moves = new HashSet<>();

        // Check that there's anything to move.
        if (points[start] == 0 || dice.size() == 0)
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

        // In rare cases, we might have to calculate more moves.
        if (!moves.isEmpty())
            return moves;

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
        int higher = Collections.max(dice);
        int lower = Collections.min(dice);

        for (int move : new int[]{higher, lower}) {
            int end = start + move * direction;

            if (isPossibleMove(points, start, end, new ArrayList<>())) {
                moves.add(end);
                break;
            }
        }

        if (!moves.isEmpty())
            return moves;

        /*
         * Lastly, we check if there are any checkers that we can bear off but are
         * closer to the edge than our dice. We figure out where the current player's
         * bar is and move away from home to the bar to figure out the furthest checker.
         */
        int maximum = Collections.max(dice);

        int bar = getPlayerBar(direction);
        int home = 25 - bar;
        int furthest = home;

        for (int i = home; bar * direction < i * direction; i -= direction) {
            if (points[i] * direction > 0)
                furthest = i;
        }

        int distance = Math.abs(home - start);
        if (Math.abs(furthest - home) == distance && distance < maximum)
            moves.add(home);

        return moves;
    }

    /**
     * Tells whether a player could make a given move and use all dice.
     */
    private static boolean isPossibleMove(int[] points, int start, int end, ArrayList<Integer> dice) {
        int diff = end - start;
        int direction = diff / Math.abs(diff);

        // Make sure that moves is valid.
        if (!isMoveValid(points, start, end))
            return false;

        // Check if this is the last move.
        if (dice.size() == 0)
            return true;

        // Otherwise, make the first move and see if we can recursively make it.
        int[] board = move(clonePoints(points), start, end);

        for (int j = 0; j < dice.size(); j++) {
            int die = dice.get(j);

            // We check for each point with the same orientation whether we can make
            // a move for remaining number of points.
            for (int point = 0; point < board.length; point++) {
                // Skip points that are not ours.
                if (direction * board[point] <= 0)
                    continue;

                // Copy the dice and remove the current dice.
                ArrayList<Integer> rdice = (ArrayList<Integer>) dice.clone();
                rdice.remove(j);

                // Check if we can meaningfully make other moves.
                if (isPossibleMove(board, point, point + die * direction, rdice))
                    return true;
            }
        }

        return false;
    }

    // MARK: - Methods

    /**
     * Rolls the dice.
     */
    private void roll() {
        this.dice = new ArrayList<>();

        // Roll the dice.
        for (int i = 0; i < 2; i++) {
            // this.dice.add(6);
            this.dice.add((int) Math.ceil(Math.random() * 6));
        }

        // Double the points on combo.
        if (this.dice.get(0).equals(this.dice.get(1))) {
            int val = this.dice.get(0);

            this.dice.add(val);
            this.dice.add(val);
        }
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
        if (0 < end && end < 25) {
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

    /**
     * Performs a move on a board, takes out the used die and pushes previous points
     * to round.
     */
    public void move(int start, int end) {
        if (start == end)
            return;

        // Update the dice.
        Integer die = Math.abs(end - start);
        if (!this.dice.remove(die)) {
            // Remove the largest die since we took the checker off the board
            // which was closer than our dice.
            Integer maximum = Collections.max(this.dice);
            this.dice.remove(maximum);
        }

        // New turn.
        if (this.dice.size() == 0) {
            this.rounds.push(Game.clonePoints(this.points));
            this.roll();
        }

        this.points = move(this.points, start, end);
    }

    /**
     * Calculates the next turn in case there's no moves.
     */
    public void next() {
        int moves = Arrays.stream(this.getMovableCheckers()).sum();
        if (moves > 0) return;

        this.rounds.push(this.points.clone());
        this.roll();
    }

    /**
     * Performs a moves on a board and takes out the used die. Reverses the board to
     * previous round and rolls the dice.
     */
    public void undo() {
        if (!rounds.isEmpty()) {
            this.points = this.rounds.pop();
            roll();
        }
    }
}

