/**
 * 
 */
package charland.games.go;

import static org.junit.Assert.assertTrue;
import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.os.Bundle;

import com.xtremelabs.robolectric.RobolectricTestRunner;

/**
 * @author Michael
 * 
 */
@RunWith(RobolectricTestRunner.class)
public class GameTest {

	/**
	 * Test trying to capture a white stone (W4). <br>
	 * <code>
	 * ------0------1------2------3------4------5------6------7------8---
	 * 0-----*------*------*------B1-----*------*------W2-----*------*---
	 * 1-----*------*------B3-----W4-----B5-----*------W6-----*------*---
	 * 2-----*------*------*------B7-----*------*------W8-----*------*---
	 * 3-----*------*------*------*------*------*------*------*------*---
	 * </code>
	 */
	@Test
	public void testCaptureSingleStone() {

		// Setup
		Game g = new Game();
		g.createBoard(null);
		setupCapture(g);

		// Test capturing.
		assertTrue(g.getGameBoard().toString(), g.playTurn(3, 2)); // B7 which should capture W4.

		assertSpotsEquals(g.getGameBoard(), "Spot should be empty", Board.EMPTY, 3, 1);
	}

	/**
	 * Make the necessary plays to get capture ready.
	 * 
	 * @param g
	 *            The game being played.
	 */
	private void setupCapture(Game g) {
		assertTrue(g.playTurn(3, 0)); // B1
		assertTrue(g.playTurn(6, 0)); // W2

		assertTrue(g.playTurn(2, 1)); // B3
		assertTrue(g.playTurn(3, 1)); // W4
		assertTrue(g.playTurn(4, 1)); // B5
		assertTrue(g.playTurn(6, 1)); // W6
	}

	/**
	 * Test trying to capture two vertical white stones (W4 & W8). <br>
	 * <code>
	 * ------0------1------2------3------4------5------6------7------8---
	 * 0-----*------*------*------B1-----*------*------W2-----*------*---
	 * 1-----*------*------B3-----W4-----B5-----*------W6-----*------*---
	 * 2-----*------*------B7-----W8-----B9-----*------W10----*------*---
	 * 3-----*------*------*------B11----*------*------W12----*------*---
	 * </code>
	 */
	@Test
	public void testCaptureTwoStonesVertically() {

		// Setup
		Game g = new Game();
		g.createBoard(null);
		setupCapture(g);
		assertTrue(g.playTurn(2, 2)); // B7
		assertTrue(g.playTurn(3, 2)); // W8
		assertTrue(g.playTurn(4, 2)); // B9
		assertTrue(g.playTurn(6, 2)); // W10

		// Test capturing.
		assertTrue(g.getGameBoard().toString(), g.playTurn(3, 3)); // B11 which should capture W4 & W8.

		assertSpotsEquals(g.getGameBoard(), "Spot should be empty", Board.EMPTY, 3, 1);
		assertSpotsEquals(g.getGameBoard(), "Spot should be empty", Board.EMPTY, 3, 2);
	}

	/**
	 * Test trying to capture two horizontal white stones (W4 & W8). <br>
	 * <code>
	 * ------0------1------2------3------4------5------6------7------8---
	 * 0-----*------*------*------B1-----B11----*------*------*------W2---
	 * 1-----*------*------B3-----W4-----W8-----B7-----*------*------W6---
	 * 2-----*------*------*------B5-----B9-----*------*------*------W10--
	 * 3-----*------*------*------B11----*------*------*------*------*---
	 * </code>
	 */
	@Test
	public void testCaptureTwoStonesHorizontally() {

		// Setup
		Game g = new Game();
		g.createBoard(null);
		assertTrue(g.playTurn(3, 0)); // B1
		assertTrue(g.playTurn(8, 0)); // W2

		assertTrue(g.playTurn(2, 1)); // B3
		assertTrue(g.playTurn(3, 1)); // W4
		assertTrue(g.playTurn(3, 2)); // B5
		assertTrue(g.playTurn(8, 1)); // W6
		assertTrue(g.playTurn(5, 1)); // B7
		assertTrue(g.playTurn(4, 1)); // W8
		assertTrue(g.playTurn(4, 2)); // B9
		assertTrue(g.playTurn(8, 2)); // W10

		// Test capturing.
		assertTrue(g.getGameBoard().toString(), g.playTurn(4, 0)); // B11 which should capture W4 & W8.

		assertSpotsEquals(g.getGameBoard(), "Spot should be empty", Board.EMPTY, 3, 1);
		assertSpotsEquals(g.getGameBoard(), "Spot should be empty", Board.EMPTY, 4, 1);
	}

	/**
	 * Test trying to capture B1 & B3 with W8. <br>
	 * <code>
	 * ------0------1------2------3------4------5------6------7------8---
	 * 0-----W8-----B1----W2------*------*------*------*------*------B5--
	 * 1-----B3-----W4-----*------*------*------*------*------*------B7--
	 * 2-----W6-----*------*------*------*------*------*------*------*---
	 * 3-----*------*------*------*------*------*------*------*------*---
	 * </code>
	 */
	@Test
	public void testCornerCaptureTwoStones() {

		// Setup
		Game g = new Game();
		g.createBoard(null);
		assertTrue(g.playTurn(1, 0)); // B1
		assertTrue(g.playTurn(2, 0)); // W2

		assertTrue(g.playTurn(0, 1)); // B3
		assertTrue(g.playTurn(1, 1)); // W4
		assertTrue(g.playTurn(8, 0)); // B5
		assertTrue(g.playTurn(0, 2)); // W6
		assertTrue(g.playTurn(8, 1)); // B7

		boolean playTurn = g.playTurn(0, 0); // W8
		Board gameBoard = g.getGameBoard();
		String string = "White is capturing B1 & B3." + gameBoard.toString();
		Assert.assertTrue(string, playTurn);

		assertSpotsEquals(gameBoard, "", Board.WHITE, 0, 0);
		assertSpotsEquals(gameBoard, "", Board.EMPTY, 0, 1);
		assertSpotsEquals(gameBoard, "", Board.EMPTY, 1, 0);
	}

	/**
	 * Test trying to capture B1 with W6. <br>
	 * <code>
	 * ------0------1------2------3------4------5------6------7------8---
	 * 0-----W6-----B1----W2------*------*------*------*------*------B5--
	 * 1-----*------W4-----*------*------*------*------*------*------B3--
	 * 2-----*------*------*------*------*------*------*------*------*---
	 * 3-----*------*------*------*------*------*------*------*------*---
	 * </code>
	 */
	@Test
	public void testCaptureTop() {

		// Setup
		Game g = new Game();
		g.createBoard(null);
		assertTrue(g.playTurn(1, 0)); // B1
		assertTrue(g.playTurn(2, 0)); // W2

		assertTrue(g.playTurn(8, 1)); // B3
		assertTrue(g.playTurn(1, 1)); // W4
		assertTrue(g.playTurn(8, 0)); // B5

		boolean playTurn = g.playTurn(0, 0); // W6
		Board gameBoard = g.getGameBoard();
		String string = "White is capturing B1." + gameBoard.toString();
		Assert.assertTrue(string, playTurn);

		Assert.assertEquals("Spot should be White" + gameBoard.toString(), Board.WHITE, gameBoard.getBoard()[0][0]);
		Assert.assertEquals("Spot should be empty" + gameBoard.toString(), Board.EMPTY, gameBoard.getBoard()[1][0]);
	}

	/**
	 * Test trying to capture B1 with W6. <br>
	 * <code>
	 * ------0------1------2------3------4------5------6------7------8---
	 * 0-----W6-----*------*------*------*------*------*------*------B5--
	 * 1-----B1-----W4-----*------*------*------*------*------*------B3--
	 * 2-----W2-----*------*------*------*------*------*------*------*---
	 * 3-----*------*------*------*------*------*------*------*------*---
	 * </code>
	 */
	@Test
	public void testCaptureSide() {

		// Setup
		Game g = new Game();
		g.createBoard(null);
		assertTrue(g.playTurn(0, 1)); // B1
		assertTrue(g.playTurn(0, 2)); // W2

		assertTrue(g.playTurn(8, 1)); // B3
		assertTrue(g.playTurn(1, 1)); // W4
		assertTrue(g.playTurn(8, 0)); // B5

		boolean playTurn = g.playTurn(0, 0); // W6
		Board gameBoard = g.getGameBoard();
		String string = "White is capturing B1." + gameBoard.toString();
		Assert.assertTrue(string, playTurn);

		Assert.assertEquals("Spot should be White" + gameBoard.toString(), Board.WHITE, gameBoard.getBoard()[0][0]);
		Assert.assertEquals("Spot should be empty" + gameBoard.toString(), Board.EMPTY, gameBoard.getBoard()[1][0]);
	}

	/**
	 * Test capturing multiple stones by killing B5, B1, and B3. <br>
	 * <code>
	 * ------0------1------2------3------4------5------6------7------8---
	 * 0-----B5-----B1----W2------*------*------*------*------*------*---
	 * 1-----B3-----W4-----*------*------*------*------*------*------*--
	 * 2-----W6-----*------*------*------*------*------*------*------*---
	 * 3-----*------*------*------*------*------*------*------*------*---
	 * </code>
	 */
	@Test
	public void testCornerCaptureThreeStones() {

		// Setup
		Game g = new Game();
		g.createBoard(null);
		assertTrue(g.playTurn(1, 0)); // B1
		assertTrue(g.playTurn(2, 0)); // W2
		assertTrue(g.playTurn(0, 1)); // B3
		assertTrue(g.playTurn(1, 1)); // W4
		assertTrue(g.playTurn(0, 0)); // B5

		boolean playTurn = g.playTurn(0, 2); // W6
		Board gameBoard = g.getGameBoard();
		String message = "White is capturing B1, B3, & B5.";
		String string = gameBoard.toString() + message;
		Assert.assertTrue(string, playTurn);

		assertSpotsEquals(gameBoard, message, Board.EMPTY, 0, 0);
		assertSpotsEquals(gameBoard, message, Board.EMPTY, 0, 1);
		assertSpotsEquals(gameBoard, message, Board.EMPTY, 1, 0);
	}

	/**
	 * Test trying to capture four white stones (W2, W4, W6, W10). <br>
	 * <code>
	 * ------0------1------2------3------4------5------6------7------8---
	 * 0-----*------*------*------B1-----B5-----*------*------*------W8--
	 * 1-----*------*------B3-----W2-----W4-----B9-----*------*------W12-
	 * 2-----*------*------B7-----W6-----W10----B11----*------*------W14-
	 * 3-----*------*------*------B13----B15----*------*------*------*---
	 * </code>
	 */
	@Test
	public void testCaptureFourStones() {

		// Setup
		Game g = new Game();
		g.createBoard(null);
		assertTrue(g.playTurn(3, 0)); // B1
		assertTrue(g.playTurn(3, 1)); // W2

		assertTrue(g.playTurn(2, 1)); // B3
		assertTrue(g.playTurn(4, 1)); // W4
		assertTrue(g.playTurn(4, 0)); // B5
		assertTrue(g.playTurn(3, 2)); // W6
		assertTrue(g.playTurn(2, 2)); // B7
		assertTrue(g.playTurn(8, 0)); // W8
		assertTrue(g.playTurn(5, 1)); // B9
		assertTrue(g.playTurn(4, 2)); // W10
		assertTrue(g.playTurn(5, 2)); // B11
		assertTrue(g.playTurn(8, 1)); // W12
		assertTrue(g.playTurn(3, 3)); // B13
		assertTrue(g.playTurn(8, 2)); // W14

		// Test capturing.
		assertTrue(g.getGameBoard().toString(), g.playTurn(4, 3)); // B15 which should capture W2, W4, W6, W10.

		assertSpotsEquals(g.getGameBoard(), "Spot should be empty", Board.EMPTY, 3, 1);
		assertSpotsEquals(g.getGameBoard(), "Spot should be empty", Board.EMPTY, 3, 2);
		assertSpotsEquals(g.getGameBoard(), "Spot should be empty", Board.EMPTY, 4, 1);
		assertSpotsEquals(g.getGameBoard(), "Spot should be empty", Board.EMPTY, 4, 2);
		assertSpotsEquals(g.getGameBoard(), "Spot should be Black", Board.BLACK, 2, 1);
	}

	/**
	 * Test capturing multiple stones by killing B5, B1, and B3. <br>
	 * <code>
	 * ------0------1------2------3------4------5------6------7------8---
	 * 0-----B1-----W2-----*------*------*------*------*------*------B3--
	 * 1-----W4-----*------*------*------*------*------*------*------*--
	 * 2-----*------*------*------*------*------*------*------*------*---
	 * 3-----*------*------*------*------*------*------*------*------*---
	 * </code>
	 */
	@Test
	public void testCornerCaptureSimple() {

		// Setup
		Game g = new Game();
		g.createBoard(null);
		assertTrue(g.playTurn(0, 0)); // B1
		assertTrue(g.playTurn(1, 0)); // W2

		assertTrue(g.playTurn(8, 0)); // B3

		boolean playTurn = g.playTurn(0, 1); // W4
		Board gameBoard = g.getGameBoard();
		String message = "White is capturing B1";
		String string = gameBoard.toString() + message;
		Assert.assertTrue(string, playTurn);

		assertSpotsEquals(gameBoard, message, Board.EMPTY, 0, 0);
	}

	/**
	 * Helper method to make it more obvious what is wrong when looking at locations for stones.
	 * 
	 * @param board
	 *            The game board.
	 * @param message
	 *            Optional additional error message.
	 * @param expected
	 *            What was expected.
	 * @param x
	 *            The x location.
	 * @param y
	 *            The y location.
	 */
	private void assertSpotsEquals(Board board, String message, int expected, int x, int y) {
		Assert.assertEquals(board.toString() + message + " at [" + x + "][" + y + "]", getColour(expected),
				getColour(board.getBoard()[x][y]));
	}

	/**
	 * Gets the colour as a string.
	 * 
	 * @param colour
	 *            The colour as an int.
	 * @return The colour.
	 */
	private String getColour(final int colour) {
		String result = "EMPTY";
		switch (colour) {
		case Board.BLACK:
			result = "BLACK";
			break;
		case Board.WHITE:
			result = "WHITE";
			break;
		}

		return result;

	}

	/**
	 * Test trying to commit suicide at B7 as black. <br>
	 * <code>
	 * ------0------1------2------3------4------5------6------7------8---
	 * 0-----B7-----B1----W2------*------*------*------*------*------B5--
	 * 1-----B3-----W4-----*------*------*------*------*------*------*---
	 * 2-----W6-----*------*------*------*------*------*------*------*---
	 * 3-----*------*------*------*------*------*------*------*------*---
	 * </code>
	 */
	@Test
	public void testSuicide() {

		// Setup
		Game g = new Game();
		g.createBoard(null);
		assertTrue(g.playTurn(1, 0)); // B1
		assertTrue(g.playTurn(2, 0)); // W2

		assertTrue(g.playTurn(0, 1)); // B3
		assertTrue(g.playTurn(1, 1)); // W4
		assertTrue(g.playTurn(8, 0)); // B5
		assertTrue(g.playTurn(0, 2)); // W6

		boolean playTurn = g.playTurn(0, 0);
		Assert.assertFalse("black is committing suicide." + g.getGameBoard().toString(), playTurn); // B7
	}

	/**
	 * To start the game make sure black goes first.
	 */
	@Test
	public void getWhosTurnItIs() {

		Game g = new Game();
		Assert.assertEquals("Black should always play first", g.getWhosTurnItIs(), "Black");
	}

	/**
	 * To start the game make sure white goes second.
	 */
	@Test
	public void getWhosTurnItIs_White() {

		Game g = new Game();
		g.createBoard(null);
		g.playTurn(0, 0);
		Assert.assertEquals("White should always play second", g.getWhosTurnItIs(), "White");
	}

	/**
	 * To start the game make sure white has no stones captured.
	 */
	@Test
	public void getWhiteStonesCaptured() {
		Game g = new Game();
		Assert.assertEquals("No stones should be captured", 0, g.getWhiteStonesCaptured());
	}

	/**
	 * To start the game make sure black has no stones captured.
	 */
	@Test
	public void getBlackStonesCaptured() {
		Game g = new Game();
		Assert.assertEquals("No stones should be captured", 0, g.getBlackStonesCaptured());
	}

	/**
	 * Trying to play on top of another stone is not allowed.
	 */
	@Test
	public void playTurn_OnTop() {
		Game g = new Game();
		g.createBoard(null);
		g.playTurn(0, 0);
		Assert.assertFalse("Can't play on top of another stone", g.playTurn(0, 0));
	}

	private static class MyPuzzle extends PuzzleView {

		boolean invalidated;

		public MyPuzzle(Context context) {
			super(context);
		}

		public void invalidate() {
			invalidated = true;
		}

	}

	/**
	 * Make sure invalidate is called when a stone is captured.
	 */
	@Test
	public void invalidate() {
		Game g = new Game();
		MyPuzzle pv = new MyPuzzle(null);
		g.setPuzzleView(pv);
		g.createBoard(null);
		setupCapture(g);
		g.playTurn(3, 2); // B7 which should capture W4.

		Assert.assertTrue("Invalidate not called", pv.invalidated);
	}

	/**
	 * Make sure the board is saved.
	 */
	@Test
	public void onSaveInstanceState() {
		Bundle b = new Bundle();

		Game g = new Game();
		g.createBoard(null);

		g.onSaveInstanceState(b);

		Assert.assertNull("Board not saved", b.getShortArray("GAME_BOARD"));

	}
}