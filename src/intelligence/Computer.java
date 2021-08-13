package intelligence;

import java.util.*;

import javax.swing.SwingWorker;

import model.Game;

public class Computer {

    /**
     * How many searches down the game tree should Computer make, with every tenth
     * being preSearch which expands one Node.
     */
    private static final int ITER = 10000;

    // MARK: - Delegate

    public interface Delegate {
        /**
         * Triggered when computation finishes.
         */
        void onMoves(int[] points, Move moves);
    }

    // MARK: - State

    private static Random r;
    private Node tree;
    private final Delegate delegate;

    // MARK: - Constructor

    public Computer(Delegate delegate) {
        r = new Random();
        this.delegate = delegate;
    }

    // MARK: - Accessors

    /**
     * Starts a computation of a move and returns the result to delegate.
     */
    public void getMoves(int[] _points, int direction, ArrayList<Integer> dice) {
        // Save local values.
        this.tree = new Node(new ArrayList<Move>());
        Delegate delegate = this.delegate;

        int[] points = Game.clonePoints(_points);

        // Create a worker to carry out the computation.
        SwingWorker<ArrayList<Move>, Void> worker = new SwingWorker<>() {
            @Override
            protected ArrayList<Move> doInBackground() {
                ArrayList<ArrayList<Move>> allMoves = allMovesFromDice(points, direction, dice);
                System.out.print("AllMoves from dice: ");
                System.out.println(allMoves.size());
                for (ArrayList<Move> moves : allMoves) {
                    tree.addChild(new Node(moves));
                }

                int k = ITER;
                while (k > 0) {
                    // if (tree.preSearch(points, -1 * direction, k % 100 == 0).wins == 0) {
                    if (tree.preSearch(points, -1 * direction, false).wins == 0) {
                        System.out.println("We won");
                    }
                    k--;
                }

                return tree.mostVisited().getMoves();
            }

            @Override
            protected void done() {
                ArrayList<Move> moves = new ArrayList<>();

                try {
                    moves = get();
                } catch (Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                }

                if (!moves.isEmpty()) {
                    delegate.onMoves(points, moves.get(0));
                } else {
                    System.out.println("Prazna poteza v intelligence.Computer:84");
                }
            }
        };

        // Start executing.
        worker.execute();
    }

    /**
     * Method that simulates a random play, but simplifies game logic for speed.
     */
    private static ArrayList<Move> makeRandomMoves(int[] points, int direction) {
        ArrayList<Move> moves = new ArrayList<Move>();
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
        return moves;
    }

    private static ArrayList<ArrayList<Move>> allMovesFromDice(int[] points, int direction, ArrayList<Integer> dice) {
        int bar = 25 * (1 - direction) / 2;
        int opponentsBar = 25 * (1 + direction) / 2;
        ArrayList<ArrayList<Move>> allMoves = new ArrayList<ArrayList<Move>>();

        for (int start = bar; start * direction < opponentsBar; start += direction) {
            for (Integer end : Game.getMoves(points, direction, dice, start)) {
                ArrayList<Move> currMove = new ArrayList<Move>();
                currMove.add(new Move(start, end));
                allMoves.add(currMove);
            }
        }
        if (allMoves.isEmpty()) {
            allMoves.add(new ArrayList<Move>());
        }

        return allMoves;
    }

    /**
     * Returns children that come from current node. expand doesnt check all
     * possible Nodes it could come to in two moves, but rather a tenth of them,
     * otherwise the tree would grow too quickly.
     */
    private static Set<Node> expand(int[] points, int direction) {
        int bar = 25 * (1 - direction) / 2;
        int opponentsBar = 25 * (1 + direction) / 2;
        Set<Node> children = new HashSet<Node>();

        for (int start1 = bar; start1 * direction < opponentsBar; start1 += direction) {
            for (int throw1 = 1; throw1 < 7; throw1 += 1) {
                int end1 = start1 + direction * throw1;
                if (r.nextInt(10) > 8 && Game.isMoveValid(points, start1, end1)) {
                    int[] points1 = Game.move(Game.clonePoints(points), start1, end1);
                    for (int start2 = bar; start2 * direction < opponentsBar; start2 += direction) {
                        for (int throw2 = 1; throw2 < 7; throw2 += 1) {
                            int end2 = start2 + direction * throw2;
                            if (Game.isMoveValid(points1, start2, end2)) {
                                ArrayList<Move> moves = new ArrayList<Move>();
                                moves.add(new Move(start1, end1));
                                moves.add(new Move(start2, end2));
                                children.add(new Node(moves));
                            }
                        }
                    }
                }
            }
        }

        return children;
    }

    private static boolean result(int[] points, int direction) {
        for (int i = 0; i < 26; i++) {
            if (points[i] * direction > 0) {
                return false;
            }
        }
        return true;
    }

    private static int[] move(int[] points, ArrayList<Move> moves) {
        for (Move move : moves) {
            points = Game.move(points, move.start, move.end);
        }
        return points;
    }

    /**
     * Tree structure which computer uses for MCTS.
     */
    static class Node {

        /**
         * Represents moves that were made from parent node to this node.
         */
        private final ArrayList<Move> moves;

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
        private Set<Node> children;

        public static int c = 2;

        public Node(ArrayList<Move> moves) {
            this.moves = moves;
            this.children = new HashSet<Node>();
        }

        public void addChild(Node child) {
            this.children.add(child);
        }

        public static double UCT(Node root, Node child) {
            if (child.getAll() == 0) {
                return 100;
            }
            return ((float) child.getWins() / (float) child.getAll()
                    + c * Math.sqrt(Math.log(root.getAll()) / child.getAll()));
        }

        public Node bestChild() {
            Node best = new Node(null);
            double max = 0;
            for (Node child : children) {
                double uct = UCT(this, child);
                if (max < uct) {
                    best = child;
                    max = uct;
                }
            }
            return best;
        }

        public Node mostVisited() {
            Iterator<Node> iter = children.iterator();
            Node best = iter.next();
            double max = best.getAll();
            while (iter.hasNext()) {
                Node child = iter.next();
                double curr = child.getAll();
                if (max < curr) {
                    best = child;
                    max = curr;
                }
            }
            return best;
        }

        public boolean search(int[] points, int direction) {
            int[] currPoints = move(points, this.moves);

            // Checks if this is a winning Node. Has to check only for player
            // that was last on the move.
            if (result(currPoints, direction)) {
                if (direction == 1) {
                    System.out.println("Black won");
                } else {
                    System.out.println("White won");
                }
                // return false, because for his parent node, this isnt good
                return false;
            }

            ArrayList<Move> moves = makeRandomMoves(points, direction);
            Node child = new Node(moves);

            return !child.search(currPoints, direction * -1);
        }

        public Yield preSearch(int[] points, int direction, boolean expand) {
            int[] currPoints = move(points, this.moves);
            if (result(currPoints, direction)) {
                wins++;
                all++;
                return new Yield(0, 1);
            } else if (children.isEmpty() && expand) {
                this.children = Computer.expand(points, -1 * direction);
                int currAll = 0;
                int currWins = 0;
                for (Node child : this.children) {
                    if (child.search(currPoints, -1 * direction)) {
                        child.wins++;
                    } else {
                        currWins++;
                    }
                    child.all++;
                    currAll++;
                }
                all += currAll;
                wins += currWins;
                return new Yield(currAll - currWins, currAll);
            } else if (children.isEmpty()) {
                if (search(points, direction)) {
                    this.wins++;
                    this.all++;
                    return new Yield(0, 1);
                }
                this.all++;
                return new Yield(1, 1);
            } else {
                Node child = bestChild();
                Yield yield = child.preSearch(currPoints, -1 * direction, expand);
                wins += yield.wins;
                all += yield.all;
                return new Yield(yield.all - yield.wins, yield.all);
            }
        }

        public int getAll() {
            return all;
        }

        public int getWins() {
            return wins;
        }

        public Set<Node> getChildren() {
            return children;
        }

        public ArrayList<Move> getMoves() {
            return this.moves;
        }

    }

    public static class Move {
        public final int start;
        public final int end;

        public Move(int start, int end) {
            this.start = start;
            this.end = end;
        }

    }

    static class Yield {
        public final int all;
        public final int wins;

        public Yield(int wins, int all) {
            this.wins = wins;
            this.all = all;
        }
    }

}
