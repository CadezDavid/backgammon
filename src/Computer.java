import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.SwingWorker;

class Computer {

    // MARK: - Delegate

    interface Delegate {
        /**
         * Triggered when computation finishes.
         */
        void onMoves(int[] points, List<Move> moves);
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
        this.tree = new Node(null);
        Delegate delegate = this.delegate;

        int[] points = _points.clone();

        // Create a worker to carry out the computation.
        SwingWorker<ArrayList<Move>, Void> worker = new SwingWorker<>() {
            @Override
            protected ArrayList<Move> doInBackground() {
                ArrayList<ArrayList<Move>> allMoves = allMovesFromDice(points, direction, dice);
                for (ArrayList<Move> moves : allMoves) {
                    int[] newPoints = points.clone();
                    for (Move move : moves) {
                        Game.move(newPoints, move.start, move.end);
                    }
                    tree.addChild(new Node(moves));
                }

                int k = 50;
                while (k > 0) {
                    tree.preSearch(points, direction);
                    System.out.println(k);
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

                }

                delegate.onMoves(_points, moves);
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

                int[] rpoints = Game.move(points, start, end);

                dice.remove((Integer) Math.abs(end - start));
                ArrayList<ArrayList<Move>> rAllMoves = allMovesFromDice(rpoints, direction, dice);
                dice.add((Integer) Math.abs(end - start));

                for (ArrayList<Move> moves : rAllMoves) {
                    moves.add(new Move(start, end));
                    allMoves.add(moves);
                }
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
            for (int throw1 = 1; throw1 < 7; throw1 += direction) {
                int end1 = start1 + direction * throw1;
                if (r.nextInt(10) > 8 && Game.isMoveValid(points, start1, end1)) {
                    int[] points1 = Game.move(points.clone(), start1, end1);
                    for (int start2 = bar; start2 * direction < opponentsBar; start2 += direction) {
                        for (int throw2 = 1; throw2 < 7; throw2 += direction) {
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
            if (points[i] * direction > 0)
                return false;
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
        private ArrayList<Move> moves;

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

        public static int c = 1;

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
            return ((float) child.getWins() / (float) child.getAll() + c * Math.sqrt(Math.log(root.getAll()) / child.getAll()));
        }

        public Node bestChild() {
            Iterator<Node> iter = children.iterator();
            Node best = iter.next();
            double max = UCT(this, best);
            while (iter.hasNext()) {
                Node child = iter.next();
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
            ArrayList<Move> moves = makeRandomMoves(points, direction);
            points = move(points, moves);

            if (result(points, direction)) return true;


            Node child = new Node(moves);
            return child.search(points, direction * -1);
        }

        public Yield preSearch(int[] points, int direction) {
            if (children.isEmpty()) {
                children = Computer.expand(points, direction);
                int currAll = 0;
                int currWins = 0;
                for (Node child : children) {
                    if (child.search(points, -1 * direction)) {
                        child.wins++;
                        currWins++;
                    }
                    child.all++;
                    currAll++;
                }
                all += currAll;
                wins += currWins;
                return new Yield(currWins, currAll);
            } else {
                Node child = bestChild();
                Yield yield = child.preSearch(move(points, child.getMoves()), -1 * direction);
                wins += yield.wins;
                all += yield.all;
                return yield;
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

    static class Move {
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
