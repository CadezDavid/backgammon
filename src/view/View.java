package view;

import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

/**
 * The board size should follow dimensions 4/3.
 * 
 * - https://www.bkgm.com/rgb/rgb.cgi?view+842 -
 * https://www.getwoodworking.com/sites/5/documents/Backgammon.pdf
 */

@SuppressWarnings("serial")
public class View extends JFrame implements BoardViewDelegate {

	public static void main(String[] args) {
		View view = new View();
		view.pack();
		view.setVisible(true);
	}

	private int[] board;

	// MARK: - Constructor

	public View() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		this.board = new int[] { 5, 2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 5, -5, 0, 0, 0, 3, 0, 5, 0, 0, 0, 0, -2, -5 };

		BoardView board = new BoardView(this);

		this.add(board);
		this.setPreferredSize(board.getPreferredSize());
	}

	@Override
	public Set<Integer> draggable(int start) {
		Set<Integer> drops = new HashSet<Integer>();

		for (int i = 1; i < board.length - 1; i++) {
			int j = board[i];
			if (j * board[start] >= 0)
				drops.add(i);

		}

		return drops;
	}

	@Override
	public void onDragged(int start, int end) {
		int direction = this.board[start] / Math.abs(this.board[start]);

		this.board[start] -= direction;
		this.board[end] += direction;
	}

	public int[] board() {
		return this.board;
	}
}

