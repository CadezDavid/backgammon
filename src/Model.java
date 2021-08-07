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
    private int[] turns;

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
        this.pips = new int[]{0, 2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0, 0, 0, -2, 0};
        this.state = State.STARTING;
        this.round = 0;
        this.turns = new int[]{-1, 1};
        this.dice = new ArrayList<Integer>();

        this.roll();
    }

    // MARK: - Accessors

    /**
     * Returns the current board.
     *
     * @return
     */
    public int[] getBoard() {
        return this.pips;
    }


    // MARK: - Methods


    /**
     * Returns the direction of the pip.
     *
     * @param index
     * @return
     */
    private int getPipDirection(int[] board, int index) {
        return board[index] / Math.abs(board[index]);
    }

    private int getPipDirection(int index) {
        return this.getPipDirection(this.pips, index);
    }


    /**
     * Tells the direction of the player that is currently playing.
     *
     * @return
     */
    private int getPlayer() {
        return this.turns[this.round % 2];
    }

    /**
     * Tells whether a player can make a move.
     *
     * @param start
     * @param end
     * @return
     */
    private boolean isMoveValid(int[] board, int turn, int start, int end) {
        // Check that we are taking from the right pile.
        if (this.getPipDirection(board, start) * turn < 0) return false;

        List<Integer> bar = Arrays.asList(0, 25);
        // Number of checkers locked on teh bar.
        int locked = board[0] + board[25];

        // Check if we are pulling from the bar.
        if (locked > 0 && !bar.contains(start)) return false;
        if (locked == 0 && bar.contains(start)) return false;

        // Check if we are pulling off the board.
        if (end < 0 || end > 25) return true;

        // Check that direction is respected.
        return board[start] * board[end] >= 0;
    }

    private boolean isMoveValid(int start, int end) {
        return this.isMoveValid(this.pips, this.getPlayer(), start, end);
    }

    /**
     * Tells where the player may move the checkers from the starting point.
     *
     * @param start
     * @return
     */
    public Set<Integer> getMoves(int start) {
        int player = this.turns[this.round % 2];
        int direction = this.getPipDirection(start);

        HashSet<Integer> moves = new HashSet<Integer>();

        /**
         * We iterate over all dice combinations and check for each
         * combination whether we could make a reasonable move with it.
         */
        for (int i = 0; i < this.dice.size(); i++) {
            int end = start + this.dice.get(i) * direction;
            int[] board = this.pips.clone();

            // Check the bounds.
            if (end < 1 || 25 < end) continue;

            // Check that we are dropping checker on our stones.
            if (direction * board[end] < 0) continue;

            // Check if there's only one die left.
            if (this.dice.size() == 1 && this.isMoveValid(start, end)) {
                moves.add(end);
                continue;
            }

            // Otherwise, make the first move and see if we can make it out.
            board[start] -= direction;

            if (1 < end && end < 25) {
                board[end] += direction;
            }


            second:
            for (int j = 0; j < this.dice.size(); j++) {
                // We can't repeat the same die.
                if (j == i) continue;

                // We check for each pip with the same orientation whether we can make
                // a move for remaining number of points.
                for (int pip = 0; pip < board.length; pip++) {
                    // Skip pips that are not ours.
                    if (direction * board[pip] <= 0) continue;

                    // Check if the move is valid.
                    if (this.isMoveValid(board, player, pip, pip + this.dice.get(j) * direction)) {
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
     *
     * @return
     */
    private void roll() {
        this.dice = new ArrayList<Integer>();

        for (int i = 0; i < 2; i++) {
            this.dice.add((int) Math.ceil(Math.random() * 6));
        }
    }

    /**
     * Performs a given move.
     *
     * @param start
     * @param end
     */
    public void move(int start, int end) {
        // Check that we are performing a move.
        if (start == end) return;

        int direction = this.getPipDirection(start);

        // Update the board.
        this.pips[start] -= direction;
        this.pips[end] += direction;

        // Update the dice.
        Integer die = Math.abs(end - start);

        this.dice.remove(die);

        // New turn.
        if (this.dice.size() == 0) {

            this.round++;
            this.roll();
        }

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
//
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

