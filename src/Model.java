import java.awt.Color;

public class Model {


    // Board seems to be the standard name,
    // but we can change to field
    private int[] board;

    private Player player1;
    private Player player2;

    public Model() {

        /**
         * Board is a list of integers of length 24, with positive numbers
         * representing number of black tokens and same for white ones.
         */
        int[] board = {2,0,0,0,0,-5,0,-3,0,0,0,5,-5,0,0,0,3,0,5,0,0,0,0,-2};
    }

    public int[] getBoard() {
        return board;
    }

    public void setBoard(int[] board) {
        this.board = board;
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }
}

class Player {

    private String name;

    private boolean isComputer;

    private Color tokenColor;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isComputer() {
        return isComputer;
    }

    public void setComputer(boolean isComputer) {
        this.isComputer = isComputer;
    }

    public Color getTokenColor() {
        return tokenColor;
    }

    public void setTokenColor(Color tokenColor) {
        this.tokenColor = tokenColor;
    }
}
