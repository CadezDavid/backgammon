import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

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
public class View extends JFrame {

	public static void main(String[] args) {
		View view = new View();
		view.pack();
		view.setVisible(true);
	}

	// MARK: - Constructor

	public View() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		int[] pips = { 2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0, 0, 0, -2 };

		BoardView board = new BoardView(pips);

		this.add(board);
		this.setPreferredSize(board.getPreferredSize());
	}
}

@SuppressWarnings("serial")
class BoardView extends JPanel {
	// Offset from the edge of the screen.
	private static int PADDING = 15;

	// Board padding or the border of the board.
	private static int BOARD_BORDER = 30;
	// The widht of the bar in the center.
	private static int BAR_WIDTH = 2 * BOARD_BORDER;
	// How wide should a pip preferrably be.
	private static int PREFERRED_PIP_WIDTH = 60;

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

	// MARK: - State

	/**
	 * We express stones on the pips as positive and negative numbers.
	 */
	private int[] pips;

	// MARK: - Constructor

	public BoardView(int[] pips) {
		this.pips = pips;
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
		Color[] colors = { Color.RED, Color.ORANGE, Color.BLUE, Color.YELLOW, Color.GREEN, Color.WHITE };

		int index = (int) Math.floor(Math.random() * (float) colors.length);
		Color color = colors[index];

		this.setBackground(BOARD_BACKGROUND);

		// -----------------------------------------------

		// Pain the board with the bar.
		this.paintBoard(g);

		// Pain each of the pips and stones on it.
		for (int i = 0; i < 24; i++) {
			this.paintPip(g, i);

			int stones = this.pips[i];
			this.paintStones(g, i, stones);
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
		int width = this.getWidth();
		int height = this.getHeight();

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
		int n = index % 6;

		int x1 = 0, y1 = 0, x2 = 0, y2 = 0, x3 = 0, y3 = 0;

		// bottom side of the board
		if (0 <= index && index < 12) {
			y1 = height - BOARD_BORDER;

			if (odd)
				y3 = y1 - (4 * this.pipHeight() / 5);
			else
				y3 = y1 - this.pipHeight();

			// left
			if (index < 6) {
				x1 = BOARD_BORDER + n * this.pipWidth();
				// right
			} else {
				x1 = width - BOARD_BORDER - (6 - n) * this.pipWidth();
			}

			// Set the color
			if (odd)
				g.setColor(WHITE_PIP);
			else
				g.setColor(BLACK_PIP);

		}

		// top side of the board
		if (12 <= index && index < 24) {
			y1 = BOARD_BORDER;

			if (odd)
				y3 = y1 + this.pipHeight();
			else
				y3 = y1 + (4 * this.pipHeight() / 5);

			// left
			if (index < 18) {
				x1 = BOARD_BORDER + n * this.pipWidth();
				// right
			} else {
				x1 = width - BOARD_BORDER - (6 - n) * this.pipWidth();
			}

			// Set the pip color
			if (odd)
				g.setColor(BLACK_PIP);
			else
				g.setColor(WHITE_PIP);

		}

		// Set remaining points that depend on the already set ones.
		x2 = x1 + this.pipWidth();
		x3 = x1 + this.pipWidth() / 2;
		y2 = y1;

		// Each pip has an internal color and a light shading over the border.
		g.fillPolygon(new int[] { x1, x2, x3 }, new int[] { y1, y2, y3 }, 3);

		g.setColor(BOARD_EDGES);
		g.drawPolygon(new int[] { x1, x2, x3 }, new int[] { y1, y2, y3 }, 3);
	}

	/**
	 * Returns the width of a single pip in the board.
	 * 
	 * @return
	 */
	private int pipWidth() {
		int width = this.getWidth();
		int space = width - 2 * BOARD_BORDER - BAR_WIDTH;

		return space / 12;
	}

	/**
	 * Returns the height of a pip based on the height of the board.
	 * 
	 * @return
	 */
	private int pipHeight() {
		int height = this.getHeight();
		int space = height - 2 * BOARD_BORDER - BOARD_BORDER;

		return space / 2;
	}

	/**
	 * Draws stones on a given pip.
	 * 
	 * @param g
	 * @param index  Index of the given pip.
	 * @param stones Number of stones on that pip.
	 */
	private void paintStones(Graphics g, int index, int stones) {
		int height = this.getHeight();
		int width = this.getWidth();

		int size = this.pipWidth() * 4 / 5;
		int n = Math.abs(stones);

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
			x = BOARD_BORDER + (2 * i + 1) * this.pipWidth() / 2;
		}
		
		// right side of the board
		if (6 <= position && position < 12) {
			x = width - BOARD_BORDER - (11 - 2 * i) * this.pipWidth() / 2;
		}

		Color stone = WHITE_STONE;
		if (stones < 0) stone = BLACK_STONE;
		
		for (int j = 0; j < n; j++) {
			int sx = x;
			int sy = y + j * direction * size;
			this.paintStone(g, sx, sy, size / 2, stone);
		}
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
			Color pattern = colors[c];

			p = 0.8 * p;
			precission = precission * 4 / 5;
			double step = 2 * Math.PI / (double) precission;

			g.setColor(pattern);

			for (int i = 0; i < precission; i++) {
				double fi = step * i;
				double pr = Math.sin(leaves * fi + offset * c) * p;

				Point2D point = new Point2D(fi, pr);

				int px = x + point.getX();
				int py = y + point.getY();

				g.drawOval(px, py, 1, 1);
			}

		}

	}
}

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