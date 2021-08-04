import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.*;

/**
 * 
 * @author maticzavadlal
 * 
 *         The board size should follow dimensions 4/3.
 * 
 *         - https://www.bkgm.com/rgb/rgb.cgi?view+842 -
 *         https://www.getwoodworking.com/sites/5/documents/Backgammon.pdf
 *
 */

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

class BoardView extends JPanel {

	// Board padding or the border of the board.
	private static int PADDING = 20;
	// The widht of the bar in the center.
	private static int BAR_WIDTH = 40;
	// How wide should a pip preferrably be.
	private static int PREFERRED_PIP_WIDTH = 20;

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
		int width = 2 * PADDING + BAR_WIDTH + 2 * 6 * PREFERRED_PIP_WIDTH;
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

		this.setBackground(color);

		// Pain the border.

		// Pain each of the sides one by one.
		for (int i = 0; i < 24; i++) {
			boolean odd = i % 2 == 1;
			
			if (odd) g.setColor(Color.WHITE);
			else g.setColor(Color.BLACK);
			
			this.paintPip(g, i);
		}
	}

	/**
	 * Draws a single pip on the board.
	 */
	void paintPip(Graphics g, int index) {
		int height = this.getHeight();
		int width = this.getWidth();

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

		int x1 = 0, y1 = 0, x2 = 0, y2 = 0, x3 = 0, y3 = 0;
		int n = index % 6;

		// bottom-left part of the board
		if (0 <= index && index < 6) {
			x1 = PADDING + n * this.pipWidth();

			y1 = height - PADDING;
			y3 = y1 - this.pipHeight();

		}

		// bottom-right
		if (6 <= index && index < 12) {
			x1 = PADDING + 6 * this.pipWidth() + BAR_WIDTH + n * this.pipWidth();
			
			y1 = height - PADDING;
			y3 = y1 - this.pipHeight();
		}

		if (12 <= index && index < 18) {
			x1 = PADDING + n * this.pipWidth();

			y1 = PADDING;
			y3 = y1 + this.pipHeight();
		}

		if (18 <= index && index < 24) {
			x1 = PADDING + 6 * this.pipWidth() + BAR_WIDTH + n * this.pipWidth();

			y1 = PADDING;
			y3 = y1 + this.pipHeight();
		}
		
		// Set remaining points that depend on the already set ones.
		x2 = x1 + this.pipWidth();
		x3 = x1 + this.pipWidth() / 2;
		y2 = y1;
		
		

		g.fillPolygon(new int[] { x1, x2, x3 }, new int[] { y1, y2, y3 }, 3);
	}

	/**
	 * Returns the width of a single pip in the board.
	 * 
	 * @return
	 */
	private int pipWidth() {
		int width = this.getWidth();
		int space = width - 2 * PADDING - BAR_WIDTH;

		return space / 12;
	}

	/**
	 * Returns the height of a pip based on the height of the board.
	 * 
	 * @return
	 */
	private int pipHeight() {
		int height = this.getHeight();
		int space = height - 2 * PADDING - PADDING;

		return space / 2;
	}

	/**
	 * Paints a stone on the board.
	 * 
	 * @param g
	 */
	void paintStone(Graphics g) {
		g.fillOval(1, 1, 1, 1);
	}
}