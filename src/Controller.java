import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

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
        Model model = this.model;
        return model.getAllPlays(start);
    }

    @Override
    public void onDragged(int start, int end) {
        Model model = this.model;
        model.move(new Move(start, end));
    }

    @Override
    public int[] dice() {
        Model model = this.model;
        int[] dice = new int[model.getDice().size()];
        for (int i = 0; i <  model.getDice().size(); i++){
            dice[i] = model.getDice().get(i);
        }
        return dice;
    }

    @Override
    public int[] board() {
        Model model = this.model;
        return model.getBoard();
    }
}
