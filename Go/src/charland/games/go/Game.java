package charland.games.go;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * The logic of the game is housed in this class.
 * 
 * @author Michael
 * 
 */
public class Game extends Activity {

    private static final String GAME_BOARD = "GAME_BOARD";

    /**
     * The graphical end of things.
     */
    private PuzzleView puzzle;

    /**
     * The only instance of the game board.
     */
    private Board board;

    /** The amount of black stones that have been captured. */
    private int blackStonesCaptured;

    /** The amount of white stones that have been captured. */
    private int whiteStonesCaptured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(Go.TAG, "onCreate");
        createBoard(savedInstanceState);
        setPuzzleView(createPuzzleView());
    }

    protected void setPuzzleView(PuzzleView pv) {
        puzzle = pv;
    }

    /**
     * Creates the puzzle view.
     */
    private PuzzleView createPuzzleView() {
        PuzzleView puzzle = new PuzzleView(this);
        setContentView(puzzle);
        puzzle.requestFocus();
        return puzzle;
    }

    /**
     * Creates the board.
     * 
     * @param savedInstanceState
     */
    void createBoard(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(GAME_BOARD)) {
            board = new Board(savedInstanceState.getShortArray(GAME_BOARD));
        } else {
            board = new Board();
        }
    }

    /** Who's turn is it? */
    private int turn;

    /**
     * The current player executes there turn.
     * 
     * @param x
     *            The x location.
     * @param y
     *            The y location.
     * @return Did the spot get occupied?
     */
    public boolean playTurn(int x, int y) {
        boolean turnPlayed = false;
        int occupied = board.isOccupied(x, y);
        if (occupied != Board.EMPTY) {
            Log.d(Go.TAG, "Empty spot not found");
            return false;
        }
        boolean whosTurn = turn % 2 == 0;
        if (whosTurn) {
            turnPlayed = board.occupyBlack(x, y);
        } else {
            turnPlayed = board.occupyWhite(x, y);
        }

        // System.out.println(board.toString());
        if (turnPlayed) {
            int captured = board.checkAllLibertiesFor(!whosTurn ? Board.BLACK : Board.WHITE);
            if (captured != 0) {
                if (whosTurn) {
                    whiteStonesCaptured += captured;
                } else {
                    blackStonesCaptured += captured;
                }

                if (puzzle != null) {
                    // TODO: This call is inefficient and should be looked at later to just redraw the stones removed.
                    puzzle.invalidate();
                }
            }
            ++turn;
        }
        return turnPlayed;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Store the game board into the bundle so it is not lost when for example the device is rotated.
        savedInstanceState.putShortArray(GAME_BOARD, board.getShortArray());
    }

    /**
     * @return The game board.
     */
    public Board getGameBoard() {
        return board;
    }

    public int getWhiteStonesCaptured() {
        return whiteStonesCaptured;
    }

    public int getBlackStonesCaptured() {
        return blackStonesCaptured;
    }

    /**
     * @return Indicate who's turn it is.
     */
    public String getWhosTurnItIs() {
        return turn % 2 == 0 ? "Black" : "White";
    }

}