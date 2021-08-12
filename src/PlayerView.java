import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

class PlayerView extends JPanel implements PointView.Delegate, CheckerView.Delegate, KeyListener, ItemListener {

    // MARK: - Delegate

    interface Delegate {
        Player player(PlayerView source);
        /**
         * Event that we trigger once the player changes.
         */
        void onPlayerChange(PlayerChangedEvent event);
    }

    /**
     * Event triggered when the name changes.
     */
    static class PlayerChangedEvent extends EventObject {
        public final Player player;

        public PlayerChangedEvent(Object source, Player player) {
            super(source);
            this.player = player;
        }
    }


    // MARK: - State

    private final Delegate delegate;

    private final JTextField name;
    private final CheckerView checker;
    private final PointView point;
    private final JCheckBox computer;

    // MARK: - Constructors

    PlayerView(Player player, Delegate delegate) {
        this.delegate = delegate;

        // Layout
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        this.name = new JTextField(player.name);
        this.name.setHorizontalAlignment(JTextField.CENTER);
        this.name.setFont(new Font("Arial", Font.BOLD, 24));
        this.name.setBorder(null);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0.2;
        this.add(this.name, c);

        this.checker = new CheckerView(this);
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.weightx = 0.5;
        c.weighty = 0.7;
        c.insets = new Insets(10, 10, 10, 10);
        this.add(this.checker, c);

        this.point = new PointView(this);
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.weightx = 0.5;
        c.weighty = 0.7;
        c.insets = new Insets(10, 10, 10, 10);
        this.add(this.point, c);

        this.computer = new JCheckBox("Computer", player.type == Player.Type.COMPUTER);
        this.computer.setFont(new Font("Arial", Font.BOLD, 16));
        c.gridx = 0;
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 2;
        c.weightx = 1;
        c.weighty = 0.1;
        c.insets = new Insets(10, 10, 10, 10);
        this.add(this.computer, c);

        // Events
        this.name.addKeyListener(this);
        this.computer.addItemListener(this);
    }

    // MARK: - Accessors

    @Override
    public Color color(CheckerView source) {
        if (source == this.checker) return this.delegate.player(this).checker;
        return null;
    }

    @Override
    public Color color(PointView source) {
        if (source == this.point) return this.delegate.player(this).point;
        return null;
    }

    // MARK: - Events

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == this.computer) {
            Player player = this.delegate.player(this);
            player.type = e.getStateChange() == 1 ? Player.Type.COMPUTER : Player.Type.HUMAN;

            PlayerChangedEvent event = new PlayerChangedEvent(this, player);
            this.delegate.onPlayerChange(event);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getSource() == this.name) {
            Player player = this.delegate.player(this);
            player.name = this.name.getText();

            PlayerChangedEvent event = new PlayerChangedEvent(this, player);
            this.delegate.onPlayerChange(event);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }


    @Override
    public void onColorChanged(CheckerView.ColorChangedEvent e) {
        if (e.getSource() == this.checker) {
            Player player = this.delegate.player(this);
            player.checker = e.color;

            PlayerChangedEvent event = new PlayerChangedEvent(this, player);
            this.delegate.onPlayerChange(event);
        }
    }

    @Override
    public void onColorChanged(PointView.ColorChangedEvent e) {
        if (e.getSource() == this.point) {
            Player player = this.delegate.player(this);
            player.point = e.color;

            PlayerChangedEvent event = new PlayerChangedEvent(this, player);
            this.delegate.onPlayerChange(event);
        }
    }
}

