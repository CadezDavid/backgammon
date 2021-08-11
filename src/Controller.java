import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Controller extends JFrame implements BoardView.Delegate, SettingsView.Delegate {

    // MARK: - State

    private final Model model;
    /**
     * View may be anything that conforms to a JPanel. Ideally, controller doesn't
     * know much about the view itself besides the re-rendering options and size.
     */
    private JPanel view;

    // MARK: - Constructor

    public Controller() {
        super("Backgammon");

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.model = new Model();
        this.view = new SettingsView(this);

        this.add(this.view);

        Intelligence i = new Intelligence();

        Game game = model.getGame();

        for (Move move : i.getMoves(game.getPoints(), game.getPlayer(), game.getDice())) {
            System.out.print(move.getStartPoint());
            System.out.print(move.getEndPoint());
            System.out.println("\n");
        }
    }

    // MARK: - Methods

    /**
     * Starts a new game.
     */
    public void onStart() {
        this.render(new BoardView(this));
    }

    public void onStop() {
        this.render(new SettingsView(this));
    }

    /**
     * Recreates the window to present the current view.
     */
    private void render(JPanel view) {
        this.getContentPane().remove(this.view);

        this.view = view;

        this.getContentPane().add(this.view);
        this.setSize(this.view.getPreferredSize());

        this.revalidate();
        this.repaint();
    }

    @Override
    public Set<Integer> draggable(int start) {
        Game game = this.model.getGame();
        return game.getMoves(start);
    }

    @Override
    public Set<Integer> movable() {
        HashSet<Integer> points = new HashSet<Integer>();
        Game game = this.model.getGame();

        int[] checkers = game.getMovableCheckers();
        for (int i = 0; i < checkers.length; i++) {
            if (checkers[i] > 0)
                points.add(i);
        }

        return points;
    }

    @Override
    public void onDragged(BoardView.DraggedEvent event) {
        Game game = this.model.getGame();

        if (event.getSource() == this.view) {
            game.move(event.start, event.end);
        }
    }

    @Override
    public int[] dice() {
        Game game = this.model.getGame();
        int[] dice = new int[game.getDice().size()];

        for (int i = 0; i < game.getDice().size(); i++) {
            dice[i] = game.getDice().get(i);
        }

        return dice;
    }

    @Override
    public int[] board() {
        Game game = this.model.getGame();
        return game.getPoints();
    }
}

// MARK: - Intelligence

class Intelligence {

    private static Random r;
    private static Node tree;

    public Intelligence() {
        r = new Random();
    }

    public Set<Move> getMoves(int[] points, int direction, ArrayList<Integer> dice) {
        long startTime = System.currentTimeMillis();
        Set<Set<Move>> allMoves = allMovesFromDice(points, direction, dice);
        tree = new Node(points, null, 0);
        for (Set<Move> moves : allMoves) {
            tree.addChild(new Node(move(points, moves), moves, 1));
        }
        while (System.currentTimeMillis() < startTime + 2 * 10 * 10 * 10) {
            tree.search();
        }
        System.out.println(tree.children);
        return tree.bestChild().getMoves();
    }

    /**
     * Method that simulates a random play, but simplifies game logic for speed.
     */
    private static Set<Move> makeRandomMoves(int[] points, int direction) {
        Set<Move> moves = new HashSet<Move>();
        int i = 40;
        int bar = 25 * (1 - direction) / 2;
        while (points[bar] != 0 && 0 < i) {
            int end = direction * (r.nextInt(6) + 1) + bar;
            if (points[end] * direction >= 0) {
                points[end] += direction;
                points[bar] -= direction;
                moves.add(new Move(bar, end));
                i -= 20;
            } else if (points[end] * direction == -1) {
                points[end] = direction;
                points[bar] -= direction;
                moves.add(new Move(bar, end));
                i -= 20;
            } else
                i--;
        }
        while (0 < i) {
            int start = r.nextInt(24) + 1;
            if (points[start] * direction > 0) {
                int end = direction * (r.nextInt(6) + 1) + start;
                if (end * direction > 25 * (1 + direction) / 2) {
                    points[start] -= direction;
                } else if (points[end] * direction >= 0) {
                    points[end] += direction;
                    points[start] -= direction;
                    moves.add(new Move(start, end));
                    i -= 20;
                } else if (points[end] * direction == -1) {
                    points[end] = direction;
                    points[start] -= direction;
                    points[bar] = direction;
                    moves.add(new Move(start, end));
                    i -= 20;
                }
            }
            i--;
        }
        // return points;
        return moves;
    }

    private static Set<Set<Move>> allMovesFromDice(int[] points, int direction, ArrayList<Integer> dice) {
        int bar = 25 * (1 - direction) / 2;
        int opponentsBar = 25 * (1 + direction) / 2;
        Set<Set<Move>> allMoves = new HashSet<Set<Move>>();
        allMoves.add(new HashSet<Move>());

        for (int start = bar; start * direction < opponentsBar; start += direction) {
            for (Integer end : getMoves(points, direction, dice, start)) {

                Set<Move> rmoves = new HashSet<Move>();
                rmoves.add(new Move(start, end));
                int[] rpoints = move(points, rmoves);

                dice.remove(new Integer(Math.abs(end - start)));
                Set<Set<Move>> rAllMoves = allMovesFromDice(rpoints, direction, dice);
                dice.add(new Integer(Math.abs(end - start)));

                for (Set<Move> set : rAllMoves) {
                    set.add(new Move(start, end));
                    allMoves.add(set);
                }
            }
        }
        return allMoves;
    }

    public static int[] move(int[] points, Set<Move> moves) {
        for (Move move : moves) {
            int start = move.getStartPoint();
            int end = move.getEndPoint();
            int direction = (start < end ? 1 : -1);
            int opponentsBar = 25 * (1 + direction) / 2;

            if (points[end] * direction == -1) {
                points[end] = direction;
                points[start] -= direction;
                points[opponentsBar] -= direction;
            } else {
                points[end] += direction;
                points[start] -= direction;
            }
        }
        return points;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // copied from model
    //
    private static int getPointDirection(int[] board, int index) {
        // if (board[index] == 0) return 0;
        return board[index] / Math.abs(board[index]);
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
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Tree structure which intelligence uses for MCTS.
     */
    class Node {

        /**
         * Marks which player is on the move in this node.
         */
        private int direction;

        /**
         * The game state this node represents.
         */
        private int[] points;

        /**
         * Respresents moves that were made from parent node to this node.
         */
        private Set<Move> moves;

        /**
         * Number of searches that went through this node.
         */
        private int all;

        /**
         * Number of searches winning that went through this node.
         */
        private int wins;

        /**
         * Children of this node. They are populated only when node is expanded.
         */
        private List<Node> children;

        private int depth;

        public static int c = 1;

        public Node(int[] points, Set<Move> moves, int depth) {
            this.points = points;
            this.moves = moves;
            this.children = new LinkedList<Node>();
            this.depth = depth;
        }

        public void addChild(Node child) {
            this.children.add(child);
        }

        public static double UCT(Node root, Node child) {
            if (child.getAll() == 0) {
                return 100000;
            }
            return (child.getWins() / child.getAll() + c * Math.sqrt(Math.log(root.getAll()) / child.getAll()));
        }

        public Node bestChild() {
            double max = 0;
            Node best = children.get(0);
            for (Node child : children) {
                double uct = UCT(this, child);
                if (max < uct) {
                    best = child;
                    max = uct;
                }
            }
            return best;
        }

        public boolean search() {
            if (this.depth > 300) {
                return false;
            }
            Node child;
            if (children.isEmpty()) {
                Set<Move> moves = makeRandomMoves(points, direction);
                child = new Node(move(points, moves), moves, this.depth + 1);
            } else {
                child = bestChild();
            }
            boolean yield = child.search();
            if (yield) {
                wins++;
            }
            all++;
            return yield;
        }

        public int[] getPoints() {
            return points;
        }

        public int getAll() {
            return all;
        }

        public int getWins() {
            return wins;
        }

        public List<Node> getChildren() {
            return children;
        }

        public Set<Move> getMoves() {
            return this.moves;
        }

    }

}

/**
 * Move represents one move of a checker.
 */
class Move {
    private int startPoint;
    private int endPoint;

    public Move(int startPoint, int endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public int getStartPoint() {
        return startPoint;
    }

    public int getEndPoint() {
        return endPoint;
    }

}
