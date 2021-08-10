import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;

import javax.swing.*;

/**
 * This file contains the getting started view.
 */

class SettingsView extends JPanel implements ActionListener, PlayerView.Delegate {
    // Offset from the edge of the screen.
    private static final int PADDING = 75;



    // MARK: - Delegate

    public interface Delegate {
        /**
         * Triggered when the game should start.
         */
        void onStart();
    }

    // MARK: - Properties

    /**
     * The delegate class to communicate with the outer world.
     */
    private final Delegate delegate;

    // MARK: - State

    private final PlayerView white;
    private final PlayerView black;
    private final JButton start;

    // MARK: - Constructor

    public SettingsView(Delegate delegate) {
        this.delegate = delegate;

        this.white = new PlayerView(this);
        this.black = new PlayerView(this);
        this.start = new JButton("Start");

        // Events

        this.start.addActionListener(this);

        // Draw

        this.add(this.white);
        this.add(this.black);
        this.add(this.start);
    }

    // MARK: - Accessors

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(500, 500);
    }

    // MARK: - View

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

//        int width = this.getWidth();
//        int height = this.getHeight();
//
//        this.white.setSize(new Dimension(width / 2, height));
//        this.black.setSize(new Dimension(width / 2, height));
    }

    // MARK: - Events
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.start) this.delegate.onStart();
    }

    @Override
    public String name(PlayerView source) {
        return null;
    }

    @Override
    public Color checker(PlayerView source) {
        return null;
    }

    @Override
    public Color point(PlayerView source) {
        return null;
    }

    @Override
    public void onNameChanged(PlayerView.NameChangedEvent event) {

    }

    @Override
    public void onCheckerChanged(PlayerView.ColorChangedEvent event) {

    }

    @Override
    public void onPointChanged(PlayerView.ColorChangedEvent event) {

    }

}

// MARK: - SubComponents

class PlayerView extends JPanel implements PointView.Delegate, CheckerView.Delegate, ActionListener {

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

    PlayerView(Delegate delegate) {
        this.delegate = delegate;

        this.name = new JTextField(this.delegate.name(this));
        this.name.addActionListener(this);

        this.checker = new CheckerView(this);
        this.point = new PointView(this);

        this.checker.setSize(100, 100);
        this.point.setSize(50, 100);


        // Draw
        this.add(this.name);
        this.add(this.point);
        this.add(this.checker);
    }

    // MARK: - Name events

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.name) {
            NameChangedEvent event = new NameChangedEvent(this, this.name.getName());
            this.delegate.onNameChanged(event);
        }
    }

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

/**
 * Checker view that lets us pick the checker color.
 */
class PointView extends JPanel implements MouseListener {

    public interface Delegate {
        /**
         * Tells the current color of the checker.
         */
        Color color(PointView source);

        /**
         * Handles the new color event.
         */
        void onColorChanged(ColorChangedEvent event);
    }

    /**
     * Event triggered when the color changes.
     */
    public static class ColorChangedEvent extends EventObject {
        public final Color color;

        private ColorChangedEvent(Object source, Color color) {
            super(source);

            this.color = color;
        }
    }

    // MARK: - State

    private final Delegate delegate;

    /**
     * Tells whether user is focusing on this component.
     */
    private boolean focused;

    // MARK: - Constructor

    public PointView(Delegate delegate) {
        this.focused = false;
        this.delegate = delegate;

        this.addMouseListener(this);
    }

    // MARK: - View

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Color color = this.delegate.color(this);

        g.setColor(color);
        int width = this.getWidth();
        int height = this.getHeight();

        g.fillPolygon(new int[]{0, width / 2, width}, new int[]{0, height, 0}, 3);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!this.focused)
            return;

        Color color = JColorChooser.showDialog(this, "Point Color", this.delegate.color(this));

        ColorChangedEvent event = new ColorChangedEvent(this, color);
        this.delegate.onColorChanged(event);

        this.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        this.focused = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        this.focused = false;
    }
}

/**
 * Checker view that lets us pick the checker color.
 */
class CheckerView extends JPanel implements MouseListener {

    public interface Delegate {
        /**
         * Tells the current color of the checker.
         */
        Color color(CheckerView source);

        /**
         * Handles the new color event.
         */
        void onColorChanged(ColorChangedEvent event);
    }

    /**
     * Event triggered when the color changes.
     */
    public static class ColorChangedEvent extends EventObject {
        public final Color color;

        private ColorChangedEvent(Object source, Color color) {
            super(source);

            this.color = color;
        }
    }

    // MARK: - State

    private final Delegate delegate;

    private boolean focused;

    // MARK: - Constructor

    public CheckerView(Delegate delegate) {
        this.focused = false;
        this.delegate = delegate;

        this.addMouseListener(this);
    }

    // MARK: - View

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Color color = this.delegate.color(this);

        int width = this.getWidth();
        int height = this.getHeight();

        int size = Math.min(width / 2, height / 2);

        BoardView.paintChecker(g, width / 2, height / 2, size, color);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!this.focused)
            return;

        Color color = JColorChooser.showDialog(this, "Checker Color", this.delegate.color(this));

        ColorChangedEvent event = new ColorChangedEvent(this, color);
        this.delegate.onColorChanged(event);

        this.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        this.focused = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        this.focused = false;
    }
}
