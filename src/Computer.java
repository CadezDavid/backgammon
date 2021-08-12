import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

class Computer {

    private static Random r;
    private Node tree;

    // MARK: - Constructor

    public Computer() {
        r = new Random();
    }

    // MARK: - Accessors

    public Set<Move> getMoves(int[] points, int direction, ArrayList<Integer> dice) {
        tree = new Node(null);

        Set<Set<Move>> allMoves = allMovesFromDice(points, direction, dice);
        for (Set<Move> moves : allMoves) {
            int[] newPoints = points.clone();
            for (Move move : moves) {
                Game.move(newPoints, move.getStartPoint(), move.getEndPoint());
            }
            tree.addChild(new Node(moves));
        }

        int k = 10000;
        while (k > 0) {
            tree.search(points, direction);
            k--;
        }

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
        return moves;
    }

    private static Set<Set<Move>> allMovesFromDice(int[] points, int direction, ArrayList<Integer> dice) {
        int bar = 25 * (1 - direction) / 2;
        int opponentsBar = 25 * (1 + direction) / 2;
        Set<Set<Move>> allMoves = new HashSet<Set<Move>>();
        allMoves.add(new HashSet<Move>());
        if (dice.isEmpty()) {
            return allMoves;
        }

        for (int start = bar; start * direction < opponentsBar; start += direction) {
            for (Integer end : Game.getMoves(points, direction, dice, start)) {

                int[] rpoints = Game.move(points, start, end);

                dice.remove((Integer) Math.abs(end - start));
                Set<Set<Move>> rAllMoves = allMovesFromDice(rpoints, direction, dice);
                dice.add((Integer) Math.abs(end - start));

                for (Set<Move> set : rAllMoves) {
                    set.add(new Move(start, end));
                    allMoves.add(set);
                }
            }
        }
        return allMoves;
    }

    public static Set<Node> expand(int[] points, int direction) {
        Set<Set<Move>> allMoves = new HashSet<Set<Move>>();

        for (int a = 1; a < 7; a++) {
            for (int b = a; b < 7; b++) {
                ArrayList<Integer> dice = new ArrayList<Integer>();
                dice.add(a);
                dice.add(b);
                if (a == b) {
                    dice.add(a);
                    dice.add(a);
                }
                allMoves.addAll(allMovesFromDice(points, direction, dice));
            }
        }

        Set<Node> children = new HashSet<Node>();
        for (Set<Move> moves : allMoves) {
            children.add(new Node(moves));
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

    private static int[] move(int[] points, Set<Move> moves) {
        for (Move move : moves) {
            points = Game.move(points, move.getStartPoint(), move.getEndPoint());
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
        private Set<Node> children;

        public static int c = 1;

        public Node(Set<Move> moves) {
            this.moves = moves;
            this.children = new HashSet<Node>();
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

        public boolean search(int[] points, int direction) {
            if (result(points, direction)) {
                return true;
            }
            Node child;
            Set<Move> moves = makeRandomMoves(points, direction);
            points = move(points, moves);
            child = new Node(moves);
            boolean yield = child.search(points, direction * -1);
            return yield;
        }

        public boolean preSearch(int[] points, int direction) {
            if (children.isEmpty()) {
                children = Computer.expand(points, direction);
                return search(points, -1 * direction);
            } else {
                Node child = bestChild();
                return child.preSearch(move(points, child.getMoves()), -1 * direction);
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

        public Set<Move> getMoves() {
            return this.moves;
        }

    }

    static class Move {
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

}
