import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

class Controller extends JFrame implements ActionListener, BoardView.Delegate, SettingsView.Delegate, Computer.Delegate {

    // MARK: - State

    /**
     * Model holds all data we use in the app.
     */
    private final Model model;

    /**
     * Class that we use to compute moves.
     */
    private final Computer computer;

    /**
     * Different views of the app.
     */
    private final SettingsView settings;
    private final BoardView board;

    /**
     * Items in the menu bar. We use this for source distinction in events.
     */
    private final JMenuItem menuItemStartGame;
    private final JMenuItem menuItemUndo;

    // MARK: - Constructor

    public Controller() {
        super("Backgammon");

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // State
        this.model = new Model();
        this.settings = new SettingsView(this);
        this.board = new BoardView(this);
        this.computer = new Computer(this);

        // MenuBar
        JMenuBar menu_bar = new JMenuBar();
        this.setJMenuBar(menu_bar);

        JMenu igra_menu = new JMenu("Igra");
        menu_bar.add(igra_menu);

        this.menuItemStartGame = new JMenuItem("Začni novo igro.");
        igra_menu.add(this.menuItemStartGame);

        this.menuItemUndo = new JMenuItem("Razveljavi zadnjo potezo.");
        igra_menu.add(this.menuItemUndo);

        this.menuItemStartGame.addActionListener(this);
        this.menuItemUndo.addActionListener(this);

        // Render the settings view.
        this.onStop();
    }

    // MARK: - Accessors

    @Override
    public Player white() {
        return this.model.white;
    }

    @Override
    public Player black() {
        return this.model.black;
    }

    // MARK: - Methods

    /**
     * Starts a new game.
     */
    public void onStart() {
        this.model.startGame();
        this.render(this.board);

        this.tick();
    }

    public void onStop() {
        this.render(this.settings);
    }


    @Override
    public void onWhiteChanged(Player white) {
        this.model.white = white;
    }

    @Override
    public void onBlackChanged(Player black) {
        this.model.black = black;
    }


    /**
     * Recreates the window to present the current view.
     */
    private void render(JPanel view) {
        this.getContentPane().removeAll();

        this.getContentPane().add(view);
        this.setSize(view.getPreferredSize());

        this.revalidate();
        this.repaint();
    }

    // MARK: - Methods

    @Override
    public Set<Integer> draggable(int start) {
        Game game = this.model.getGame();
        return game.getMoves(start);
    }

    @Override
    public Set<Integer> movable() {
        HashSet<Integer> points = new HashSet<>();
        Game game = this.model.getGame();

        int[] checkers = game.getMovableCheckers();
        for (int i = 0; i < checkers.length; i++) {
            if (checkers[i] > 0)
                points.add(i);
        }

        return points;
    }

    public Game.State state() {
        Game game = this.model.getGame();
        return game.getState();
    }

    @Override
    public void onDragged(BoardView.DraggedEvent event) {
        Game game = this.model.getGame();

        if (event.getSource() == this.board) {
            game.move(event.start, event.end);
            this.repaint();

            // Check if the next move is computer move.
            this.tick();
        }
    }

    /**
     * Checks if it has to perform a move.
     */
    private void tick() {
        Game game = this.model.getGame();

        int turn = game.getTurn();
        boolean wcpu = turn == -1 && this.white().type == Player.Type.COMPUTER;
        boolean bcpu = turn == 1 && this.black().type == Player.Type.COMPUTER;

        if (wcpu || bcpu) {
            System.out.println("Calculating moves!");
            this.computer.getMoves(game.getPoints(), turn, game.getDice());
        }
    }

    @Override
    public void onMoves(int[] points, List<Computer.Move> moves) {
        System.out.println("CALCULATED");
        Game game = this.model.getGame();

        if (game.getPoints() == points) {
            for (Computer.Move move : moves) {
                this.board.animate(move.start, move.end);
                game.move(move.start, move.end);
                this.repaint();
            }

            // Check if the next move is also computer move.
            this.tick();
        }
    }

    @Override
    public int[] dice() {
        Game game = this.model.getGame();
        ArrayList<Integer> dice = game.getDice();
        int[] clone = new int[dice.size()];

        for (int i = 0; i < dice.size(); i++) {
            clone[i] = dice.get(i);
        }

        return clone;
    }

    @Override
    public int[] board() {
        Game game = this.model.getGame();
        return game.getPoints();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.menuItemStartGame) {
            this.onStop();
        }

        if (e.getSource() == this.menuItemUndo) {
            Game game = this.model.getGame();
            game.undo();
        }

        this.repaint();
        tick();
    }

}
