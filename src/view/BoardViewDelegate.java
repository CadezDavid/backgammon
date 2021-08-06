package view;

import java.util.Set;

public interface BoardViewDelegate {
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