import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class Utils {
    /**
     * Performs a given move and returns a new board.
     */
    public static int[] move(int[] points, Utils.Move move) {
        // Make sure we are not writing to the original board.
        points = points.clone();

        // Check that we are performing a move.
        if (move.getStartPoint() == move.getEndPoint())
            return points;

        // Figure out which checker are we trying to move.
        int direction = (move.getStartPoint() < move.getEndPoint() ? 1 : -1);

        // Remove the checker from the start point.
        points[move.getStartPoint()] -= direction;

        // Check that we are still on the board when making a move.
        if (0 < move.getEndPoint() && move.getEndPoint() < 25) {
            // Regularly move the checker if we are not beating.
            if (points[move.getEndPoint()] * direction >= 0) {
                points[move.getEndPoint()] += direction;
            } else {
                // If it is white player's move, black should be beat and vice-versa.
                // That is, when it's white, we should "add" one to the other player's
                // bench.
                points[(1 + direction) / 2 * 25] -= direction;
                points[move.getEndPoint()] = direction;
            }
        }

        return points;
    }

    /**
     * Tells whether a player can make a move.
     */
    public static boolean isMoveValid(int[] points, int direction, Utils.Move move) {
        // if player has a checker on the start point
        if (points[move.getStartPoint()] * direction <= 0) {
            return false;
        }

        // if player has checkers on the bar and isn't moving those
        if (points[(25 - 25 * direction) / 2] != 0 && move.getStartPoint() != (25 - 25 * direction) / 2) {
            return false;
        }

        // if player is bearing off
        if (move.getEndPoint() <= 0 || 25 <= move.getEndPoint()) {
            if (!Utils.canBearOff(points, direction)) {
                return false;
            } else if (move.getEndPoint() < 0) {
                for (int i = 6; i < move.getStartPoint(); i--) {
                    if (points[i] < 0) {
                        return false;
                    }
                }
            } else if (25 < move.getEndPoint()) {
                for (int i = 18; i < move.getStartPoint(); i++) {
                    if (points[i] > 0) {
                        return false;
                    }
                }
            }
            return true;
        }

        // if checkers on the board make sense
        if (direction * points[move.getEndPoint()] < -1) {
            return false;
        }

        // if the direction of moving is correct
        if ((move.getEndPoint() - move.getStartPoint()) * direction <= 0) {
            return false;
        }

        return true;
    }

    public static List<Utils.Move> getValidMoves(int[] points, int direction, int die) {
        List<Utils.Move> moves = new ArrayList<>();

        for (int i = 0; i < 26; i++) {
            int end = i + direction * die;
            Utils.Move move = new Utils.Move(i, end);
            if (isMoveValid(points, direction, move)) {
                moves.add(move);
            }
        }
        return moves;
    }

    public static List<LinkedList<Utils.Move>> getAllPlays(int[] points, int direction, List<Integer> dice) {
        List<LinkedList<Utils.Move>> plays = new ArrayList<LinkedList<Utils.Move>>();

        if (dice.size() == 1) {
            for (int i = 0; i < 26; i++) {
                int end = i + direction * dice.get(0);
                Utils.Move move = new Utils.Move(i, end);
                LinkedList<Utils.Move> curr = new LinkedList<Utils.Move>();
                if (isMoveValid(points, direction, move)) {
                    curr.push(move);
                }
                plays.add(curr);
            }
            return plays;
        }

        for (int i = 0; i < dice.size(); i++) {

            List<Utils.Move> moves = getValidMoves(points, direction, dice.get(i));
            List<Integer> clone = new ArrayList<Integer>();
            for (int j = 0; j < dice.size(); j++) {
                clone.add(dice.get(j));
            }
            clone.remove(dice.get(i));

            List<LinkedList<Utils.Move>> playsRec = getAllPlays(
                    move(points, new Utils.Move(i, i + direction * dice.get(0))), direction, clone);
            for (LinkedList<Utils.Move> play : playsRec) {
                LinkedList<Utils.Move> tmp = (LinkedList<Utils.Move>) play.clone();
                for (Utils.Move move : moves) {
                    tmp.push(move);
                }
                plays.add(tmp);
            }
        }

        // removes duplicates and ensures all plays will be the same length
        plays.remove(new LinkedList<Utils.Move>());
        if (plays.isEmpty()) {
            plays.add(new LinkedList<Utils.Move>());
        }
        return plays;
    }

    public static boolean canBearOff(int[] points, int direction) {
        int c = 7 * (1 - direction) / 2;
        for (int i = c; i < 19 + c; i++) {
            if (points[i] * direction > 0) {
                return false;
            }
        }

        return true;
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
}
