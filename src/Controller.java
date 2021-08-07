import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class Controller extends JFrame implements BoardViewDelegate, SettingsViewDelegate {

    // MARK: - State

    private Model model;
    /**
     * View may be anything that conforms to a JPanel. Ideally,
     * controller doesn't know much about the view itself besides the
     * rerendering options and size.
     */
    private JPanel view;

    // MARK: - Constructor

    public Controller() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.model = new Model();
        this.view = new BoardView(this);

        this.add(this.view);
        this.setPreferredSize(this.view.getPreferredSize());
    }

    // MARK: - Methods

    @Override
    public Set<Integer> draggable(int start) {
        Game game = this.model.getGame();
        return game.getMoves(start);
    }

    @Override
    public void onDragged(int start, int end) {
        Game game = this.model.getGame();
        game.move(start, end);
    }

    @Override
    public int[] board() {
        Game game = this.model.getGame();
        return game.getBoard();
    }
}