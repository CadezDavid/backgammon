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
        tree = new Node(points, null, 0);

        Set<Set<Move>> allMoves = allMovesFromDice(points, direction, dice);
        for (Set<Move> moves : allMoves) {
            int[] newPoints = points.clone();
            for (Move move : moves) {
                Game.move(newPoints, move.getStartPoint(), move.getEndPoint());
            }
            tree.addChild(new Node(newPoints, moves, 1));
        }

        int k = 10000;
        while (k > 0) {
            tree.search();
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

    /**
     * Tree structure which intelligence uses for MCTS.
     */
    static class Node {

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
        private Set<Node> children;

        private int depth;

        public static int c = 1;

        public Node(int[] points, Set<Move> moves, int depth) {
            this.points = points;
            this.moves = moves;
            this.children = new HashSet<Node>();
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

        public boolean search() {
            if (this.depth > 500) {
                return false;
            }
            Node child;
            Set<Move> moves = makeRandomMoves(points, direction);
            for (Move move : moves) {
                points = Game.move(points, move.getStartPoint(), move.getEndPoint());
            }
            child = new Node(points, moves, this.depth + 1);
            boolean yield = child.search();
            if (yield) {
                wins++;
            }
            all++;
            return yield;
        }

        public boolean preSearch() {
            if (children.isEmpty()) {
                expand();
                return search();
            } else {
                Node child = bestChild();
                return child.preSearch();
            }
        }

        private void expand() {
            int bar = 25 * (1 - direction) / 2;
            int opponentsBar = 25 * (1 + direction) / 2;
            Set<Node> children = new HashSet<Node>();

            for (int start1 = bar; start1 * direction < opponentsBar; start1 += direction) {
                for (int end1 = start1; end1 * direction < opponentsBar + 5; end1 += direction) {
                    if (Game.isMoveValid(points, start1, end1)) {
                        int[] newPoints = Game.move(points.clone(), start1, end1);
                        for (int start2 = bar; start2 * direction < opponentsBar; start2 += direction) {
                            for (int end2 = start2; end2 * direction < opponentsBar + 5; end2 += direction) {
                                if (Game.isMoveValid(newPoints, start2, end2)) {
                                    int[] p = Game.move(newPoints.clone(), start2, end2);
                                    Set<Move> moves = new HashSet<Move>();
                                    moves.add(new Move(start1, end1));
                                    moves.add(new Move(start2, end2));
                                    children.add(new Node(p, moves, this.depth + 1));
                                }
                            }
                        }
                    }

                }
            }
            this.children = children;
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
