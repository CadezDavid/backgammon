import javax.swing.*;
import java.util.*;

public class Controller extends JFrame implements BoardView.Delegate, SettingsView.Delegate {

    // MARK: - State

    private final Model model;
    /**
     * View may be anything that conforms to a JPanel. Ideally,
     * controller doesn't know much about the view itself besides the
     * re-rendering options and size.
     */
    private JPanel view;

    // MARK: - Constructor

    public Controller() {
        super("Backgammon");

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.model = new Model();
        this.view = new SettingsView(this);

        this.add(this.view);
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
            if (checkers[i] > 0) points.add(i);
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

    public Intelligence() {
    }

    public Move getMove(int[] points, int direction, int[] dice) {
        return new Move(1, 2);
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

    private static int[] randomDice() {
        int a = r.nextInt(6) + 1;
        int b = r.nextInt(6) + 1;
        if (a == b) {
            return new int[] { a, a, a, a };
        }
        return new int[] { a, b };
    }

    /**
     * Tree structure which intelligence uses for MCTS.
     */
    class Node {

        /**
         * The game state this node represents.
         */
        int[] points;
        /**
         * Number of searches that went through this node.
         */
        int all;
        /**
         * Number of searches which ended in a win that went through this node.
         */
        int wins;
        List<Node> children;
        int c;

        public Node(int[] points) {
            this.points = points;
            this.children = new LinkedList<Node>();
            this.c = 1;
        }

        public Node addChild(Node child) {
            this.children.add(child);
            return child;
        }

        public double UCT(Node child) {
            return (child.getWins() / child.getAll() + c * Math.sqrt(Math.log(this.getAll()) / child.getAll()));
        }

        public Node bestChild() {
            double max = 0;
            Node best = children.get(0);
            for (Node child : children) {
                double uct = UCT(child);
                if (max < uct) {
                    best = child;
                    max = uct;
                }
            }
            return best;
        }

        public int[] getPoints() {
            return points;
        }

        public int getAll() {
            return all;
        }

        public void incrementAll(int all) {
            this.all += 1;
        }

        public int getWins() {
            return wins;
        }

        public void incrementWins(int wins) {
            this.wins += 1;
        }

        public List<Node> getChildren() {
            return children;
        }

    }

}