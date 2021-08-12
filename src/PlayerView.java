import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EventObject;

class PlayerView extends JPanel implements PointView.Delegate, CheckerView.Delegate, KeyListener {

    // MARK: - Delegate

    interface Delegate {
        /**
         * Tells the current name of the player.
         */
        String name(PlayerView source);

        /**
         * Tells the color of a checker.
         */
        Color checker(PlayerView source);

        /**
         * Tells the current color of the point.
         */
        Color point(PlayerView source);

        /**
         * Event triggered when the name changed.
         */
        void onNameChanged(NameChangedEvent event);

        /**
         * Event that we trigger once the color of a checker changes.
         */
        void onCheckerChanged(ColorChangedEvent event);

        /**
         * Event that we trigger once the color of the pip changes.
         */
        void onPointChanged(ColorChangedEvent event);
    }

    /**
     * Event triggered when the name changes.
     */
    static class NameChangedEvent extends EventObject {
        public final String name;

        public NameChangedEvent(Object source, String name) {
            super(source);
            this.name = name;
        }
    }

    /**
     * Event triggered when the color changes.
     */
    static class ColorChangedEvent extends EventObject {
        public final Color color;

        public ColorChangedEvent(Object source, Color color) {
            super(source);
            this.color = color;
        }
    }

    // MARK: - State

    private final Delegate delegate;

    private final JTextField name;
    private final CheckerView checker;
    private final PointView point;

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
        c.weighty = 0.8;
        c.insets = new Insets(10, 10, 10, 10);
        this.add(this.checker, c);

        this.point = new PointView(this);
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.weightx = 0.5;
        c.weighty = 0.8;
        c.insets = new Insets(10, 10, 10, 10);
        this.add(this.point, c);


        // Events
        this.name.addKeyListener(this);
    }

    // MARK: - Events

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getSource() == this.name) {
            NameChangedEvent event = new NameChangedEvent(this, this.name.getText());
            this.delegate.onNameChanged(event);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { }

    // MARK: - Checker Events

    @Override
    public Color color(CheckerView source) {
        if (source == this.checker) return this.delegate.checker(this);
        return null;
    }

    @Override
    public void onColorChanged(CheckerView.ColorChangedEvent e) {
        if (e.getSource() == this.checker) {
            ColorChangedEvent event = new ColorChangedEvent(this, e.color);
            this.delegate.onCheckerChanged(event);
        }
    }
    // MARK: - Point events

    @Override
    public Color color(PointView source) {
        if (source == this.point) return this.delegate.point(this);
        return null;
    }

    @Override
    public void onColorChanged(PointView.ColorChangedEvent e) {
        if (e.getSource() == this.point) {
            ColorChangedEvent event = new ColorChangedEvent(this, e.color);
            this.delegate.onPointChanged(event);
        }
    }
}

