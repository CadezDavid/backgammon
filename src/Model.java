import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Model {

    private Board board;

    private Player blackPlayer;
    private Player whitePlayer;

    private State state;

    public Model(String nameBP, String nameWP, Type typeBP, Type typeWP) {

        this.board = new Board();

        this.blackPlayer = new Player(nameWP, typeWP);
        this.whitePlayer = new Player(nameBP, typeBP);

        this.state = State.STARTING;
    }

    public Board getBoard() {
        return board;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public State getState() {
        return state;
    }

}

class Board {

    private int[] pips;

    public Board() {
        // positive numbers represent black checkers and negative numbers
        // represent white checkers, with black going right (towards 24) and white
        // going left (towards 1)
        // 0 - black player's bar (value n means n checkers on bar)
        // 25 - white player's bar (value -n means n checkers on bar)
        pips = new int[] { 0, 2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0, 0, 0, -2, 0 };
    }

    public List<LinkedList<Move>> getAllPlays(Checker checker, int[] dice) {
        List<LinkedList<Move>> plays = new ArrayList<LinkedList<Move>>();

        for (int i = 0; i < dice.length; i++) {
            List<Move> moves = getValidMoves(checker, dice[i]);
            int[] clone = new int[dice.length - 1];
            for (int j = 0; j < dice.length && j != i; j++) {
                clone[j - (j < i ? 1 : 0)] = dice[i];
            }
            for (LinkedList<Move> play : getAllPlays(checker, clone)) {
                for (Move move : moves) {
                    play.push(move);
                    plays.add(play);
                }
            }
        }

        // removes duplicates and ensures all plays will be the same length
        List<LinkedList<Move>> playsNoDup = plays.stream().distinct().collect(Collectors.toList());
        playsNoDup.remove(new LinkedList<Move>());
        if (playsNoDup.isEmpty()) {
            playsNoDup.add(new LinkedList<Move>());
        }
        return playsNoDup;
    }

    public List<Move> getValidMoves(Checker checker, int die) {
        int c = (checker == Checker.BLACK ? 1 : -1);
        List<Move> moves = new ArrayList<>();

        if (c * pips[(25 - 25 * c) / 2] > 0) {
            Move move = new Move((25 - 25 * c) / 2, (25 - 25 * c) / 2 + c * die);
            if (isMoveValid(checker, move)) {
                moves.add(move);
            } else {
            }
        } else {
            for (int i = 1; i < 25; i++) {
                Move move = new Move(i, i + c * die);
                if (isMoveValid(checker, move)) {
                    moves.add(move);
                }
            }
        }
        return moves;
    }

    public void move(Checker checker, Move move) {
        int c = (checker == Checker.BLACK ? 1 : -1);

        // case in which the opposing player has been hit
        if (c * pips[move.getEndPip()] == -1) {
            pips[move.getStartPip()] -= c;
            pips[move.getEndPip()] = c;
            pips[(25 + 25 * c) / 2] -= c;
        }
        // usual case
        else {
            pips[move.getStartPip()] -= c;
            pips[move.getEndPip()] += c;
        }
    }

    private boolean isMoveValid(Checker checker, Move move) {
        int c = (checker == Checker.BLACK ? 1 : -1);

        // if player is move from the bar
        if (move.getStartPip() == 0 || move.getStartPip() == 25) {
            // if checkers on the board make sense
            if ((pips[move.getStartPip()] > 0) && (c * pips[move.getEndPip()] > -2)) {
                return false;
            }

            // if player has no checkers on the bar
            if (pips[(25 - 25 * c) / 2] == 0) {
                return false;
            }
        }
        // usual case
        else {
            // if checkers on the board make sense
            if ((c * pips[move.getStartPip()] > 0) && (c * pips[move.getEndPip()] > -2)) {
                return false;
            }

            // if player has checkers on the bar
            if (pips[(25 - 25 * c) / 2] != 0) {
                return false;
            }
        }

        // if the direction of moving is correct
        if ((move.getEndPip() - move.getStartPip()) * c <= 0) {
            return false;
        }

        return true;
    }

    private boolean canBearOff(Checker checker) {
        int c = (checker == Checker.BLACK ? 0 : 7);

        for (int i = c; i < 19 + c; i++) {
            if (pips[i] * c > 0) {
                return false;
            }
        }

        return true;
    }

}

class Player {

    private String name;

    private Type type;

    public Player(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

}

class Move {
    private int startPip;
    private int endPip;

    public Move(int startPip, int endPip) {
        this.startPip = startPip;
        this.endPip = endPip;
    }

    public int getStartPip() {
        return startPip;
    }

    public int getEndPip() {
        return endPip;
    }

}

enum Type {
    COMPUTER, HUMAN
}

enum Checker {
    WHITE, BLACK
}

enum State {
    // meaning of states:
    // STARTING - before the start, when players roll the die to determine who
    // starts first
    // MOVE_WHITE - white player on the move
    // MOVE_BLACK - black player on the move
    // DRAW - game ended in a draw
    // WIN_BLACK - black player won
    // WIN_WHITE - white player won
    STARTING, MOVE_WHITE, MOVE_BLACK, DRAW, WIN_BLACK, WIN_WHITE
}
