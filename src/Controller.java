import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class Controller extends JFrame implements BoardView.Delegate, SettingsView.Delegate {

    // MARK: - State

    private final Model model;
    /**
     * View may be anything that conforms to a JPanel. Ideally,
     * controller doesn't know much about the view itself besides the
     * re-rendering options and size.
     */
    private JPanel view;

    // MARK: - Constructor

    public Controller() {
        super("Backgammon");

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.model = new Model();
        this.view = new SettingsView(this);

        this.add(this.view);
    }

    // MARK: - Methods

    /**
     * Starts a new game.
     */
    public void onStart() {
        this.rerender(new BoardView(this));
    }

    public void onStop() {
        this.rerender(new SettingsView(this));
    }

    /**
     * Recreates the window to present the current view.
     */
    private void rerender(JPanel view) {
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
        return game.getDice();
    }

    @Override
    public int[] board() {
        Game game = this.model.getGame();
        return game.getBoard();
    }
}