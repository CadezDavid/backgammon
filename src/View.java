import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

/**
 * @author maticzavadlal
 * 
 *         The board size should follow dimensions 4/3.
 * 
 *         - https://www.bkgm.com/rgb/rgb.cgi?view+842 -
 *         https://www.getwoodworking.com/sites/5/documents/Backgammon.pdf
 *
 */

@SuppressWarnings("serial")
public class View extends JFrame implements BoardViewDelegate {

	public static void main(String[] args) {
		View view = new View();
		view.pack();
		view.setVisible(true);
	}

	// MARK: - Constructor

	public View() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		int[] pips = { 12, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0, 0, 0, -2 };

		BoardView board = new BoardView(this, pips);

		this.add(board);
		this.setPreferredSize(board.getPreferredSize());
	}

	@Override
	public Set<Integer> draggable(Stone stone) {
		Integer[] pips = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, stone.getPip() };
		return new HashSet<Integer>(Arrays.asList(pips));
	}
}

interface BoardViewDelegate {
	/**
	 * Tells whether a stone may be dragged or not.
	 * 
	 * @param stone
	 * @return Returns drop locations.
	 */
	Set<Integer> draggable(Stone stone);
}

@SuppressWarnings("serial")
class BoardView extends JPanel implements MouseListener, MouseMotionListener {
	// Offset from the edge of the screen.
	private static int PADDING = 15;

	// Board padding or the border of the board.
	private static int BOARD_BORDER = 30;
	// The widht of the bar in the center.
	private static int BAR_WIDTH = 2 * BOARD_BORDER;
	// How wide should a pip preferrably be.
	private static int PREFERRED_PIP_WIDTH = 60;
	// The offset of stone over the other stone.
	private static int STONE_OFFSET = 5;

	// Board color settings.
	private static Color BOARD_COLOR = new Color(117, 60, 24);
	private static Color BOARD_BACKGROUND = new Color(245, 126, 51);
	private static Color BOARD_EDGES = new Color(167, 100, 84);
	private static Color WHITE_PIP = new Color(245, 223, 213);
	private static Color BLACK_PIP = new Color(74, 47, 31);
	private static Color WHITE_STONE = new Color(255, 232, 222);
	private static Color BLACK_STONE = new Color(51, 46, 44);
	private static Color STONE_EDGE = new Color(128, 116, 111);
	private static Color STONE_BACKGROUND = new Color(233, 241, 223);
	private static Color DROP_COLOR = Color.GRAY;
	private static Color TARGET_COLOR = Color.BLACK;

	// MARK: - Properties

	/**
	 * We express stones on the pips as positive and negative numbers.
	 */
	private int[] pips;

	/**
	 * The delegate class that we use to communicate with the outter world.
	 */
	private BoardViewDelegate delegate;

	// MARK: - State

	/**
	 * The stone that is being dragged.
	 */
	private Stone dragged;
	/**
	 * Tells what kind of stone we are dragging.
	 */
	private int direction;
	/**
	 * The current position of mouse on the screen.
	 */
	private Point mouse;
	/**
	 * An array or pip indexes where the dragged stone may be dropped.
	 */
	private Set<Integer> drops;
	/**
	 * The pip that we may want to drop the stone on. You may assume that it is a
	 * pip that is also in drops variable.
	 */
	private Integer target;

	// MARK: - Constructor

	public BoardView(BoardViewDelegate delegate, int[] pips) {
		this.pips = pips;
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
		int width = 2 * BOARD_BORDER + BAR_WIDTH + 2 * 6 * PREFERRED_PIP_WIDTH;
		int height = 3 * width / 4;

		return new Dimension(width, height);
	}

	// MARK: - View

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Redraw visualizer.
//		Color[] colors = { Color.RED, Color.ORANGE, Color.BLUE, Color.YELLOW, Color.GREEN, Color.WHITE };
//
//		int index = (int) Math.floor(Math.random() * (float) colors.length);
//		Color color = colors[index];

		this.setBackground(BOARD_BACKGROUND);

		// ----------------------------------------------

		// Pain the board with the bar.
		this.paintBoard(g);

		// ----------------------------------------------

		int size = this.getStoneSize();

		// Pain each of the pips and stones on it.
		for (int i = 0; i < 24; i++) {
			this.paintPip(g, i);

			int stones = this.pips[i];
			Color color = this.getStoneColor(stones);

			// Draw the stones.
			for (int j = 0; j < Math.abs(this.pips[i]); j++) {
				Point coord = this.getStonePosition(i, j);
				this.paintStone(g, coord.x, coord.y, size / 2, color);
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

		// Paint the dragged stone.
		if (this.dragged != null) {
			Color color = this.getStoneColor(this.direction);
			this.paintStone(g, this.mouse.x, this.mouse.y, size / 2, color);
		}
	}

	/**
	 * Paints the outer edeges of the board.
	 */
	private void paintBoard(Graphics g) {
		int width = this.getWidth();
		int height = this.getHeight();

		// Draw the outer edges.
		g.setColor(BOARD_COLOR);
		g.fillRect(0, 0, width, height);

		// Draw the inner board.
		g.setColor(this.getBackground());
		g.fillRect(BOARD_BORDER, BOARD_BORDER, width - 2 * BOARD_BORDER, height - 2 * BOARD_BORDER);
		g.setColor(BOARD_EDGES);
		g.drawRect(BOARD_BORDER, BOARD_BORDER, width - 2 * BOARD_BORDER, height - 2 * BOARD_BORDER);

		// Draw the bench.
		g.setColor(BOARD_COLOR);
		g.fillRect(width / 2 - BAR_WIDTH / 2, BOARD_BORDER, BAR_WIDTH, height - 2 * BOARD_BORDER);
		g.setColor(BOARD_EDGES);
		g.drawLine(width / 2, BOARD_BORDER, width / 2, height - BOARD_BORDER);
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

		// bottom side of the board
		if (0 <= index && index < 12) {

			if (odd) {
				y3 = base.y - (4 * this.getPipHeight() / 5);
				g.setColor(WHITE_PIP);
			} else {
				y3 = base.y - this.getPipHeight();
				g.setColor(BLACK_PIP);
			}

		}

		// top side of the board
		if (12 <= index && index < 24) {
			if (odd) {
				y3 = base.y + this.getPipHeight();
				g.setColor(BLACK_PIP);
			} else {
				y3 = base.y + (4 * this.getPipHeight() / 5);
				g.setColor(WHITE_PIP);
			}
		}

		// Each pip has an internal color and a light shading over the border.
		g.fillPolygon(new int[] { x1, x2, x3 }, new int[] { y1, y2, y3 }, 3);

		g.setColor(BOARD_EDGES);
		g.drawPolygon(new int[] { x1, x2, x3 }, new int[] { y1, y2, y3 }, 3);
	}

	/**
	 * Returns where on the screen the pip should start.
	 * 
	 * @param index
	 * @return
	 */
	private Point getPipBase(int index) {
		int width = this.getWidth();
		int height = this.getHeight();

		/**
		 * We use modulo indexes to calculate the relative position of the stone in a
		 * given group.
		 * 
		 * - n tells the position in a single block of six pips - i tells relative
		 * position on a given side of the board
		 */
		int n = index % 6;
		int i = index % 12;

		int x = 0, y = 0;

		// bottom
		if (0 <= index && index < 12) {
			y = height - BOARD_BORDER;
		}

		// top
		if (12 <= index && index < 24) {
			y = BOARD_BORDER;
		}

		// left
		if (0 <= i && i < 6) {
			x = BOARD_BORDER + (1 + 2 * n) * this.getPipWidth() / 2;
		}
		// right
		if (6 <= i && i < 12) {
			x = width - BOARD_BORDER - (11 - 2 * n) * this.getPipWidth() / 2;
		}

		return new Point(x, y);
	}

	/**
	 * Returns the width of a single pip in the board.
	 * 
	 * @return
	 */
	private int getPipWidth() {
		int width = this.getWidth();
		int space = width - 2 * BOARD_BORDER - BAR_WIDTH;

		return space / 12;
	}

	/**
	 * Returns the height of a pip based on the height of the board.
	 * 
	 * @return
	 */
	private int getPipHeight() {
		int height = this.getHeight();
		int space = height - 2 * BOARD_BORDER - BOARD_BORDER;

		return space / 2;
	}

	/**
	 * Returns the position of the stone on the board given the index of the pip and
	 * the stone position.
	 * 
	 * @param index
	 * @param stone
	 * @return
	 */
	private Point getStonePosition(int index, int stone) {
		int height = this.getHeight();
		int width = this.getWidth();

		// Calculate the size of each stone and the number of stones in each row.
		int size = this.getStoneSize();
		int row = this.getPipHeight() / size;

		// Direction tells how we should add next stones to the pip.
		int direction = 0;
		int x = 0, y = 0;

		// bottom side of the board
		if (0 <= index && index < 12) {
			y = height - BOARD_BORDER - size / 2;
			direction = -1;
		}

		// top side of the board
		if (12 <= index && index < 24) {
			y = BOARD_BORDER + size / 2;
			direction = 1;
		}

		int position = index % 12;
		int i = index % 6;

		// left side of the board
		if (0 <= position && position < 6) {
			x = BOARD_BORDER + (2 * i + 1) * this.getPipWidth() / 2;
		}

		// right side of the board
		if (6 <= position && position < 12) {
			x = width - BOARD_BORDER - (11 - 2 * i) * this.getPipWidth() / 2;
		}

		// Coordinates calculation.
		int offset = (stone / row) * STONE_OFFSET;
		int margin = (stone % row) * size;
		int sx = x;
		int sy = y + direction * (margin + offset);

		return new Point(sx, sy);
	}

	/**
	 * Returns the size of the stone based on the size of the board.
	 * 
	 * @return
	 */
	private int getStoneSize() {
		return this.getPipWidth() * 4 / 5;
	}

	/**
	 * Returns the color of a stone on a given pip.
	 * 
	 * @param pip
	 * @return
	 */
	private Color getStoneColor(int stones) {
		Color color = WHITE_STONE;
		if (stones < 0)
			color = BLACK_STONE;

		return color;
	}

	/**
	 * Paints a stone at a given location and a given radius.
	 */
	private void paintStone(Graphics g, int x, int y, int r, Color color) {
		// We start by drawing the outer and inner edges of the stone.
		g.setColor(color);
		g.fillOval(x - r, y - r, 2 * r, 2 * r);
		g.setColor(STONE_EDGE);
		g.drawOval(x - r, y - r, 2 * r, 2 * r);

		/**
		 * To draw the pattern we switch to the polar system as it's easier to process
		 * values there.
		 */
		Color[] colors = { new Color(242, 56, 90), new Color(245, 165, 3), new Color(54, 177, 191, 75) };
		int precission = 420;
		int leaves = 6;

		double p = (double) r * 0.8;
		double offset = 2 * Math.PI / (colors.length);

		g.setColor(STONE_BACKGROUND);
		g.fillOval(x - (int) p, y - (int) p, 2 * (int) p, 2 * (int) p);

		for (int c = 0; c < colors.length; c++) {
			p = 0.8 * p;
			double step = 2 * Math.PI / (double) precission;

			int[] xs = new int[precission];
			int[] ys = new int[precission];

			for (int i = 0; i < precission; i++) {
				double fi = step * i;
				double pr = Math.sin(leaves * fi + offset * c) * p;

				Point2D point = new Point2D(fi, pr);

				int px = x + point.getX();
				int py = y + point.getY();

				xs[i] = px;
				ys[i] = py;
			}

			Color pattern = colors[c];
			g.setColor(pattern);

			Graphics2D g2D = (Graphics2D) g;
			g2D.setStroke(new BasicStroke(1));

			g.drawPolyline(xs, ys, precission);
		}

	}

	/**
	 * Paints a possible target where the user may drop the stone.
	 * 
	 * @param pip
	 * @param targeted
	 */
	private void paintDrop(Graphics g, int pip, boolean targeted) {
		int size = this.getStoneSize() / 2;
		int stone = Math.abs(this.pips[pip]);

		Point base = this.getStonePosition(pip, stone);

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

//		System.out.println("x:" + x + ", y:" + y);
		int size = this.getStoneSize();

		int index = -1, n = 0;

		// Paint each of the pips and stones on it.
		for (int i = 0; i < 24; i++) {
			int stones = Math.abs(this.pips[i]);

			for (int j = 0; j < stones; j++) {
				Point coord = this.getStonePosition(i, j);
//				System.out.println("(" + i + ", " + j + "): " + coord.x + ", " + coord.y);

				double diff = Math.sqrt(Math.pow(x - coord.x, 2) + Math.pow(y - coord.y, 2));

				if (diff <= size) {
					index = i;
					n = j;
				}
			}
		}

		// Makes sure we actually clicked on a stone.
		if (index >= 0) {
			Stone stone = new Stone(index, n);

			this.dragged = stone;
			this.mouse = e.getPoint();
			this.drops = this.delegate.draggable(stone);
			this.direction = this.pips[index] / Math.abs(this.pips[index]);

			this.pips[index] -= direction;
		}

		// Rerender the screen.
		this.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (this.dragged == null) return;
		
		if (this.target == null)
			this.pips[this.dragged.getPip()] += this.direction;
		else
			this.pips[this.target] += this.direction;
		
		this.dragged = null;
		this.drops = new HashSet<Integer>();
		this.direction = 0;

		this.repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {

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

		// Find the closest pip to the mouse.
		for (int i = 0; i < 24; i++) {
			Point coord = this.getPipBase(i);
//				System.out.println("(" + i + ", " + j + "): " + coord.x + ", " + coord.y);

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

		if (this.drops.contains(pip)) this.target = pip;
		else this.target = null;
		
		this.repaint();
	}

	//

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

// MARK: - Utilities

class Point2D {
	private double x;
	private double y;

	// MARK: - Constructor

	Point2D(int x, int y) {
		this.x = x;
		this.y = y;
	}

	Point2D(double fi, double r) {
		this.x = Math.cos(fi) * r;
		this.y = Math.sin(fi) * r;
	}

	// MARK: - Accessors

	int getX() {
		return (int) this.x;
	}

	int getY() {
		return (int) this.y;
	}

	double getR() {
		return Math.sqrt(x * x + y * y);
	}

	double getFi() {
		return Math.atan(y / x);
	}
}

class Stone {
	/**
	 * Index of the pip.
	 */
	private int pip;
	/**
	 * Index of a stone on the given pip.
	 */
	private int stone;

	// MARK: - Constructor

	Stone(int pip, int stone) {
		this.pip = pip;
		this.stone = stone;
	}

	// MARK: - Accessors

	int getPip() {
		return this.pip;
	}

	int getStone() {
		return this.stone;
	}
}