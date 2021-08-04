public class Model {

    private Board board;

    private Player blackPlayer;
    private Player whitePlayer;

    private State state;

    public Model(String nameBP, String nameWP, Type typeBP, Type typeWP) {

        setBoard(new Board());

        setBlackPlayer(new Player(nameWP, typeWP));
        setWhitePlayer(new Player(nameBP, typeBP));

        setState(State.STARTING);
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public void setBlackPlayer(Player blackPlayer) {
        this.blackPlayer = blackPlayer;
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public void setWhitePlayer(Player whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

}

class Board {

    private int[] points;

    private Bar bar;

    public Board() {
        // positive numbers represent black checkers and negative numbers
        // represent white checkers, with black going right (towards 23) and white
        // going left (towards 0)
        points = new int[] { 2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0, 0, 0, -2 };

        bar = new Bar();
    }

    public void move(Checker checker, int startingPoint, int endPoint) {
        int c = (checker == Checker.BLACK ? 1 : -1);

        // case in which the opposing player has been hit
        if (c * points[endPoint] == -1) {
            points[startingPoint] -= c;
            points[endPoint] = c;
        }
        // usual case
        else {
            points[startingPoint] -= c;
            points[endPoint] += c;
        }
    }

    public boolean isMoveValid(Checker checker, int startingPoint, int endPoint) {
        int c = (checker == Checker.BLACK ? 1 : -1);

        // if checkers on board make sense
        if ((c * points[startingPoint] > 0) && (c * points[endPoint] > -2)) {
            return false;
        }

        // if player has no checkers on the bar
        if ((checker == Checker.BLACK && bar.getBlack() > 0) || (checker == Checker.WHITE && bar.getWhite() > 0)) {
            return false;
        }

        // if the direction of moving is correct
        if ((endPoint - startingPoint) * c <= 0) {
            return false;
        }

        return true;
    }

    public void moveFromBar(Checker checker, int endPoint) {
        int c = (checker == Checker.BLACK ? 1 : -1);

        // case in which the opposing player has been hit
        if (c * points[endPoint] == -1) {
            if (checker == Checker.BLACK) {
                bar.removeBlack();
            } else {
                bar.removeWhite();
            }
            points[endPoint] = c;
        }
        // usual case
        else {
            if (checker == Checker.BLACK) {
                bar.removeBlack();
            } else {
                bar.removeWhite();
            }
            points[endPoint] += c;
        }
    }

    public boolean isMoveFromBarValid(Checker checker, int endPoint) {
        int c = (checker == Checker.BLACK ? 1 : -1);

        // if player has at least one checker on bar
        if ((checker == Checker.BLACK && bar.getBlack() == 0) || (checker == Checker.WHITE && bar.getWhite() == 0)) {
            return false;
        }

        // if checkers on board make sense
        if (c * points[endPoint] > -2) {
            return false;
        }

        // if the direction of moving is correct
        if ((endPoint + (23 * c - 23) / 2) * c > 0) {
            return false;
        }

        return true;
    }

    public boolean canBearOff(Checker checker) {
        // if player has at least one checker on bar
        if ((checker == Checker.BLACK && bar.getBlack() > 0) || (checker == Checker.WHITE && bar.getWhite() > 0)) {
            return false;
        }

        // if all checkers are in home board
        int c = (checker == Checker.BLACK ? 0 : 6);
        for (int i = c; i < 18 + c; i++) {
            if (points[i] * c > 0) {
                return false;
            }
        }

        return true;
    }

    public int[] getpoints() {
        return points;
    }

    public void setpoints(int[] points) {
        this.points = points;
    }

    public int[] getPoints() {
        return points;
    }

    public void setPoints(int[] points) {
        this.points = points;
    }
}

class Bar {
    private int white;
    private int black;

    public Bar() {
        setWhite(0);
        setBlack(0);
    }

    public void addWhite() {
        white += 1;
    }

    public void addBlack() {
        black += 1;
    }

    public void removeWhite() {
        white -= 1;
    }

    public void removeBlack() {
        black -= 1;
    }

    public int getWhite() {
        return white;
    }

    public void setWhite(int white) {
        this.white = white;
    }

    public int getBlack() {
        return black;
    }

    public void setBlack(int black) {
        this.black = black;
    }
}

class Player {

    private String name;

    private Type type;

    public Player(String name, Type type) {
        setName(name);
        setType(type);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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
