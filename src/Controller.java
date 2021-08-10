import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Controller extends JFrame implements BoardViewDelegate, SettingsViewDelegate {

    // MARK: - State

    private Model model;
    /**
     * View may be anything that conforms to a JPanel. Ideally, controller doesn't
     * know much about the view itself besides the rerendering options and size.
     */
    private JPanel view;

    // MARK: - Constructor

    public Controller() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.model = new Model();
        this.view = new BoardView(this);

        this.add(this.view);
        this.setPreferredSize(this.view.getPreferredSize());
    }

    // MARK: - Methods

    @Override
    public Set<Integer> draggable(int start) {
        Model model = this.model;
        return model.getAllPlays(start);
    }

    @Override
    public void onDragged(int start, int end) {
        Model model = this.model;
        model.move(new Move(start, end));
    }

    @Override
    public int[] dice() {
        Model model = this.model;
        int[] dice = new int[model.getDice().size()];
        for (int i = 0; i < model.getDice().size(); i++) {
            dice[i] = model.getDice().get(i);
        }
        return dice;
    }

    @Override
    public int[] board() {
        Model model = this.model;
        return model.getBoard();
    }
}

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
