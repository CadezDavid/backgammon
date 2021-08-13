package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;

/**
 * Checker view that lets us pick the checker color.
 */
public class PointView extends JPanel implements MouseListener {

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

        int[] xs = new int[]{0, width / 2, width};
        int[] ys = new int[]{0, height, 0};

        g.fillPolygon(xs, ys, 3);
        g.setColor(new Color(167, 100, 84));
        g.drawPolygon(xs, ys, 3);
    }

    // MARK: - Accessors



    // MARK: - Events

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
