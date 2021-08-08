import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

/**
 * This file contains everything related to drawing the board.
 */

interface BoardViewDelegate {
    /**
     * Returns the current state on the board.
     *
     * @return
     */
    int[] board();

    /**
     * Tells whether a stone may be dragged or not by telling where it may be
     * dropped.
     *
     * @param start
     * @return Returns drop locations.
     */
    Set<Integer> draggable(int start);

    /**
     * Event that's triggered when the stone has been dragged to a new location.
     *
     * @param start
     * @param end
     */
    void onDragged(int start, int end);
}


@SuppressWarnings("serial")
class BoardView extends JPanel implements MouseListener, MouseMotionListener {
    // Offset from the edge of the screen.
    private static int PADDING = 50;

    // Board padding or the border of the board.
    private static int BOARD_BORDER = 30;
    // The widht of the bar in the center.
    private static int BAR_WIDTH = 2 * BOARD_BORDER;
    // How wide should a pip preferrably be.
    private static int PREFERRED_PIP_WIDTH = 60;
    // The offset of checker over the other checker.
    private static int CHECKER_OFFSET = 5;

    // Board color settings.
    private static Color BOARD_COLOR = new Color(117, 60, 24);
    private static Color BOARD_BACKGROUND = new Color(245, 126, 51);
    private static Color BOARD_EDGES = new Color(167, 100, 84);
    private static Color WHITE_PIP = new Color(245, 223, 213);
    private static Color BLACK_PIP = new Color(74, 47, 31);
    private static Color WHITE_CHECKER = new Color(255, 232, 222);
    private static Color BLACK_CHECKER = new Color(51, 46, 44);
    private static Color CHECKER_EDGE = new Color(128, 116, 111);
    private static Color CHECKER_BACKGROUND = new Color(233, 241, 223);
    private static Color DROP_COLOR = new Color(75, 75, 75);
    private static Color TARGET_COLOR = new Color(0, 0, 0);

    // MARK: - Properties

    /**
     * The delegate class that we use to communicate with the outter world.
     */
    private BoardViewDelegate delegate;

    // MARK: - State

    /**
     * The checker that is being dragged.
     */
    private Integer dragged;
    /**
     * Tells what kind of checker we are dragging.
     */
    private int direction;
    /**
     * The current position of mouse on the screen.
     */
    private Point mouse;
    /**
     * An array or pip indexes where the dragged checker may be dropped.
     */
    private Set<Integer> drops;
    /**
     * The pip that we may want to drop the checker on. You may assume that it is a
     * pip that is also in drops variable.
     */
    private Integer target;

    // MARK: - Constructor

    public BoardView(BoardViewDelegate delegate) {
        this.delegate = delegate;

        this.mouse = this.getMousePosition();
        this.drops = new HashSet<Integer>();
        this.direction = 0;
        this.dragged = null;
        this.target = null;

        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    // MARK: - Accessors

    @Override
    public Dimension getPreferredSize() {
        // Board
        int boardWidth = 2 * BOARD_BORDER + BAR_WIDTH + 2 * 6 * PREFERRED_PIP_WIDTH;
        int boardHeight = 3 * boardWidth / 4;

        // Screen
        int width = boardWidth + 2 * PADDING;
        int height = boardHeight + 2 * PADDING;

        return new Dimension(width, height);
    }

    // MARK: - View

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the background so it matches bearing off.
        if (this.target == null) {
            this.setBackground(BOARD_BACKGROUND);
        } else {
            // White is trying to get to 0 and black to 26.
            if (this.target == 0) {
                this.setBackground(WHITE_PIP);
            }

            if (this.target == 26) {
                this.setBackground(BLACK_PIP);
            }
        }


        // ----------------------------------------------

        // Pain the board with the bar.
        this.paintBoard(g);

        // ----------------------------------------------

        int size = this.getCheckerSize();

        // Paint each of the pips.
        for (int i = 1; i < 25; i++) {
            this.paintPip(g, i);
        }

        // Paint checkers.
        for (int i = 0; i < 26; i++) {
            int checkers = this.delegate.board()[i];
            Color color = this.getCheckerColor(checkers);

            // Get the number of checkers and account for the dragging.
            if (this.dragged != null && this.dragged == i)
                checkers -= this.direction;

            for (int j = 0; j < Math.abs(checkers); j++) {
                Point coord = this.getCheckerPosition(i, j);
                this.paintChecker(g, coord.x, coord.y, size / 2, color);
            }
        }


        // ----------------------------------------------

        // Paint the droppable pips and the target.
        if (this.drops != null) {
            for (Integer drop : drops) {
                boolean targeted = this.target != null && drop.equals(this.target);
                this.paintDrop(g, drop, targeted);
            }
        }

        // Paint the dragged checker.
        if (this.dragged != null) {
            Color color = this.getCheckerColor(this.direction);
            this.paintChecker(g, this.mouse.x, this.mouse.y, size / 2, color);
        }
    }

    /**
     * Paints the outer edeges of the board.
     */
    private void paintBoard(Graphics g) {
        // Size of the board.
        int width = this.getWidth() - 2 * PADDING;
        int height = this.getHeight() - 2 * PADDING;

        int margin = BOARD_BORDER + PADDING;

        // Draw the outer edges.
        g.setColor(BOARD_COLOR);
        g.fillRect(PADDING, PADDING, width, height);

        // Draw the inner board.
        g.setColor(BOARD_BACKGROUND);
        g.fillRect(margin, margin, width - 2 * BOARD_BORDER, height - 2 * BOARD_BORDER);
        g.setColor(BOARD_EDGES);
        g.drawRect(margin, margin, width - 2 * BOARD_BORDER, height - 2 * BOARD_BORDER);

        int center = this.getWidth() / 2;
        int bottom = this.getHeight() - margin;

        // Draw the bench.
        g.setColor(BOARD_COLOR);
        g.fillRect(center - BAR_WIDTH / 2, margin, BAR_WIDTH, height - 2 * BOARD_BORDER);
        g.setColor(BOARD_EDGES);
        g.drawRect(center - BAR_WIDTH / 2, margin, BAR_WIDTH, height - 2 * BOARD_BORDER);
        g.drawLine(center, margin, center, bottom);
    }

    /**
     * Draws a single pip on the board.
     */
    private void paintPip(Graphics g, int index) {
        /**
         * The coordinates as named as shown in the picture regardless of the pip
         * orientation.
         *
         * @formatter:off
         *
         * (x1, y1) - (x2, y2)
         * 	\               /
         * 	 \             /
         *    \           /
         *     \         /
         *      \       /
         *      (x3, y3)
         *
         * @formatter:on
         */

        /**
         * It's important to notice that the top part of the board isn't zero indexed.
         * Because of that the top part has "inverted" odd and even pips.
         */
        boolean odd = index % 2 == 1;

        Point base = this.getPipBase(index);

        // Set remaining points that depend on the already set ones.
        int x1 = base.x - this.getPipWidth() / 2;
        int y1 = base.y;
        int x2 = base.x + this.getPipWidth() / 2;
        int y2 = base.y;
        int x3 = base.x;
        int y3 = 0;

        if (odd) {
            y3 = base.y + this.getPipOrientation(index) * this.getPipHeight();
            g.setColor(BLACK_PIP);
        } else {
            y3 = base.y + this.getPipOrientation(index) * (4 * this.getPipHeight() / 5);
            g.setColor(WHITE_PIP);
        }

        // Each pip has an internal color and a light shading over the border.
        g.fillPolygon(new int[]{x1, x2, x3}, new int[]{y1, y2, y3}, 3);

        g.setColor(BOARD_EDGES);
        g.drawPolygon(new int[]{x1, x2, x3}, new int[]{y1, y2, y3}, 3);
    }

    /**
     * Returns where on the screen the pip should start.
     *
     * @param index
     * @return
     */
    private Point getPipBase(int index) {
        // Screen sizes
        int width = this.getWidth();
        int height = this.getHeight();

        int margin = BOARD_BORDER + PADDING;

        /**
         * We use modulo indexes to calculate the relative position of the checker in a
         * given group.
         *
         * - n tells the position in a single block of six pips - i tells relative
         * position on a given side of the board
         */
        int n = (index - 1) % 6;
        int i = (index - 1) % 12;

        int x = 0, y = 0;

        // black bar
        if (index == 0) {
            x = width / 2;
            y = height / 2 + BOARD_BORDER / 3;
        }

        // bottom
        if (1 <= index && index < 13) {
            y = height - margin;

            // left
            if (0 <= i && i < 6) {
                x = width - margin - (1 + 2 * n) * this.getPipWidth() / 2;
            }
            // right
            if (6 <= i && i < 12) {
                x = margin + (11 - 2 * n) * this.getPipWidth() / 2;
            }
        }

        // top
        if (13 <= index && index < 25) {
            y = margin;

            // left
            if (0 <= i && i < 6) {
                x = margin + (1 + 2 * n) * this.getPipWidth() / 2;
            }
            // right
            if (6 <= i && i < 12) {
                x = width - margin - (11 - 2 * n) * this.getPipWidth() / 2;
            }
        }

        // white bar
        if (index == 25) {
            x = width / 2;
            y = height / 2 - BOARD_BORDER / 3;
        }

        return new Point(x, y);
    }

    /**
     * Tells the direction of the pip based on the side of the board it is on.
     *
     * @param index
     * @return
     */
    private int getPipOrientation(int index) {

        if (1 <= index && index < 13)
            return -1;

        if (index <= 13 && index < 25)
            return 1;

        // bar
        if (index == 0)
            return 1;

        if (index == 25)
            return -1;

        return 1;
    }

    /**
     * Returns the width of a single pip in the board.
     *
     * @return
     */
    private int getPipWidth() {
        int width = this.getWidth();
        int margin = PADDING + BOARD_BORDER;
        int space = width - 2 * margin - BAR_WIDTH;

        return space / 12;
    }

    /**
     * Returns the height of a pip based on the height of the board.
     *
     * @return
     */
    private int getPipHeight() {
        int height = this.getHeight();
        int margin = BOARD_BORDER + PADDING;
        int space = height - 2 * margin - BOARD_BORDER;

        return space / 2;
    }

    /**
     * Returns the position of the checker on the board given the index of the pip
     * and the checker position.
     *
     * @param index
     * @param checker
     * @return
     */
    private Point getCheckerPosition(int index, int checker) {
        // Calculate the size of each checker and the number of checkers in each row.
        int size = this.getCheckerSize();
        int row = this.getPipHeight() / size;

        // Direction tells how we should add next checkers to the pip.
        int direction = this.getPipOrientation(index);
        Point base = this.getPipBase(index);

        int x = base.x;
        int y = base.y + this.getPipOrientation(index) * (size / 2);

        // Coordinates calculation.
        int offset = (checker / row) * CHECKER_OFFSET;
        int margin = (checker % row) * size;
        int sx = x;
        int sy = y + direction * (margin + offset);

        return new Point(sx, sy);
    }

    /**
     * Returns the size of the checker based on the size of the board.
     *
     * @return
     */
    private int getCheckerSize() {
        return this.getPipWidth() * 4 / 5;
    }

    /**
     * Returns the color of a checker on a given pip.
     *
     * @param checkers
     * @return
     */
    private Color getCheckerColor(int checkers) {
        Color color = WHITE_CHECKER;
        if (checkers > 0)
            color = BLACK_CHECKER;

        return color;
    }

    /**
     * Paints a checker at a given location and a given radius.
     */
    private void paintChecker(Graphics g, int x, int y, int r, Color color) {
        // We start by drawing the outer and inner edges of the checker.
        g.setColor(color);
        g.fillOval(x - r, y - r, 2 * r, 2 * r);
        g.setColor(CHECKER_EDGE);
        g.drawOval(x - r, y - r, 2 * r, 2 * r);

        /**
         * To draw the pattern we switch to the polar system as it's easier to process
         * values there.
         */
        Color[] colors = {new Color(242, 56, 90), new Color(245, 165, 3), new Color(54, 177, 191, 75)};
        int precision = 420;
        int leaves = 6;

        double p = (double) r * 0.8;
        double offset = 2 * Math.PI / (colors.length);

        g.setColor(CHECKER_BACKGROUND);
        g.fillOval(x - (int) p, y - (int) p, 2 * (int) p, 2 * (int) p);

        for (int c = 0; c < colors.length; c++) {
            p = 0.8 * p;
            double step = 2 * Math.PI / (double) precision;

            int[] xs = new int[precision];
            int[] ys = new int[precision];

            for (int i = 0; i < precision; i++) {
                double fi = step * i;
                double pr = Math.sin(leaves * fi + offset * c) * p;

                int px = x + (int) (Math.cos(fi) * pr);
                int py = y + (int) (Math.sin(fi) * pr);

                xs[i] = px;
                ys[i] = py;
            }

            Color pattern = colors[c];
            g.setColor(pattern);

            Graphics2D g2D = (Graphics2D) g;
            g2D.setStroke(new BasicStroke(1));

            g.drawPolyline(xs, ys, precision);
        }

    }

    /**
     * Paints a possible target where the user may drop the checker.
     *
     * @param pip
     * @param targeted
     */
    private void paintDrop(Graphics g, int pip, boolean targeted) {
        // Make sure we are on the board when drawing.
        if (pip < 1 || 25 < pip) return;

        int size = this.getCheckerSize() / 2;

        // Calculate the position of the next checker.
        int checker = Math.abs(this.delegate.board()[pip]);
        if (this.dragged != null && this.dragged == pip)
            checker--;

        Point base = this.getCheckerPosition(pip, checker);

        // Draw the target.
        g.setColor(DROP_COLOR);
        if (targeted)
            g.setColor(TARGET_COLOR);

        g.fillOval(base.x - size / 2, base.y - size / 2, size, size);
    }

    // MARK: - Events

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        int size = this.getCheckerSize();

        // Find the index of the pip of the clicked checker.
        int index = -1;

        for (int i = 0; i < 26; i++) {
            int checkers = Math.abs(this.delegate.board()[i]);

            for (int j = 0; j < checkers; j++) {
                Point coord = this.getCheckerPosition(i, j);

                double diff = Math.sqrt(Math.pow(x - coord.x, 2) + Math.pow(y - coord.y, 2));

                if (diff <= size)
                    index = i;
            }
        }

        // Makes sure we actually clicked on a checker.
        if (index >= 0) {
            this.dragged = index;
            this.mouse = e.getPoint();
            this.drops = this.delegate.draggable(index);

            int checkers = this.delegate.board()[index];
            this.direction = checkers / Math.abs(checkers);
        }

        // Rerender the screen.
        this.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        this.mouse = e.getPoint();

        int width = this.getWidth();
        int height = this.getHeight();


        /**
         * The distance of the closes droppable pip.
         */
        Integer pip = null;
        double closest = width + height;

        /**
         * Check if mouse is out of board bounds (i.e. dragging out) and consider the
         * direction of the dragged stone to see which side is pulling out.
         *
         * If white is bearing, it should drag to 0. Conversely, black should drag to 26.
         * Since black's direction is positive (1), we can get to 26 as 1 * 26. For white
         * we want (1 - 1) * 26.
         */
        if (mouse.x < PADDING || mouse.y < PADDING || mouse.x > width - PADDING || mouse.y > height - PADDING) {
            pip = (1 + direction) / 2 * 26;
            closest = 0;
        }

        // Find the closest pip to the mouse.
        for (int i = 1; i < 25; i++) {
            Point coord = this.getPipBase(i);

            int dx = this.mouse.x - coord.x;
            int dy = this.mouse.y - coord.y;
            double diff = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

            /**
             * We always fix the diff, but only make a pip selection if it can actually be
             * selected.
             */
            if (diff <= closest) {
                closest = diff;
                pip = i;
            }
        }

        if (this.drops.contains(pip))
            this.target = pip;
        else
            this.target = null;

        this.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (this.dragged == null)
            return;

        // Calculate the movement of the checker.
        int start = this.dragged;
        int end = start;

        if (this.target != null)
            end = this.target;

        // Reset dragging values.
        this.dragged = null;
        this.drops = new HashSet<Integer>();
        this.direction = 0;
        this.target = null;

        // Trigger event.
        this.delegate.onDragged(start, end);

        this.repaint();
    }

    //

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}