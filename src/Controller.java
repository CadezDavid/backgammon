import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

class Controller extends JFrame implements ActionListener, BoardView.Delegate, SettingsView.Delegate {

    // MARK: - State

    private final Model model;
    /**
     * View may be anything that conforms to a JPanel. Ideally,
     * controller doesn't know much about the view itself besides the
     * re-rendering options and size.
     */
    private JPanel view;

    private final JMenuItem menuItemStartGame;

    // MARK: - Constructor

    public Controller() {
        super("Backgammon");

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // State
        this.model = new Model();
        this.view = new SettingsView(this);

        // View
        this.add(this.view);

        // MenuBar
        JMenuBar menu_bar = new JMenuBar();
        this.setJMenuBar(menu_bar);

        JMenu igra_menu = new JMenu("Nova igra");
        menu_bar.add(igra_menu);

        this.menuItemStartGame = new JMenuItem("Začni novo igro.");
        igra_menu.add(this.menuItemStartGame);

        this.menuItemStartGame.addActionListener(this);
    }

    // MARK: - Methods

    /**
     * Starts a new game.
     */
    public void onStart() {
        this.model.startGame();
        this.render(new BoardView(this));
    }

    public void onStop() {
        this.render(new SettingsView(this));

    }

    @Override
    public Player white() {
        return this.model.white;
    }

    @Override
    public void onWhiteChanged(Player white) {
        this.model.white = white;
    }

    @Override
    public Player black() {
        return this.model.black;
    }

    @Override
    public void onBlackChanged(Player black) {
        this.model.black = black;
    }


    /**
     * Recreates the window to present the current view.
     */
    private void render(JPanel view) {
        this.getContentPane().remove(this.view);

        this.view = view;

        this.getContentPane().add(this.view);
        this.setSize(this.view.getPreferredSize());

        this.revalidate();
        this.repaint();
    }

    @Override
    public Set<Integer> draggable(int start) {
        Game game = this.model.getGame();
        return game.getMoves(start);
    }

    @Override
    public Set<Integer> movable() {
        HashSet<Integer> points = new HashSet<Integer>();
        Game game = this.model.getGame();

        int[] checkers = game.getMovableCheckers();
        for (int i = 0; i < checkers.length; i++) {
            if (checkers[i] > 0) points.add(i);
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

        if (event.getSource() == this.view) {
            game.move(event.start, event.end);
        }
    }

    @Override
    public int[] dice() {
        Game game = this.model.getGame();
        int[] dice = new int[game.getDice().size()];

        for (int i = 0; i < game.getDice().size(); i++) {
            dice[i] = game.getDice().get(i);
        }

        return dice;
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
    }
}