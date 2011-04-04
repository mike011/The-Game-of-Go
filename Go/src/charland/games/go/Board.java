package charland.games.go;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;

public class Board {

    /**
     * The cell is empty.
     */
    static final short EMPTY = -1;

    /**
     * The cell is occupied by black.
     */
    static final short BLACK = 0;

    /**
     * The cell is occupied by white.
     */
    static final short WHITE = 1;

    /**
     * The size of one side of the game board.
     */
    public static final short SIZE = 9;

    /**
     * The game board.
     */
    private short gameBoard[][];

    /**
     * Creates an empty game board.
     */
    Board() {
        this((short[][]) null);
    }

    /**
     * Create a game board with a board set up.
     * 
     * @param newBoard
     *            The board. If the board is a null an empty board is created instead.
     */
    Board(short[][] newBoard) {
        if (newBoard == null) {
            gameBoard = createEmptyBoard();
        } else {
            gameBoard = newBoard;
        }
    }

    /**
     * Creates an empty game board.
     * 
     * @return An empty game board.
     */
    private static short[][] createEmptyBoard() {
        // Initialize the game board.
        short gameBoard[][] = new short[SIZE][SIZE];
        for (int a = 0; a < SIZE; a++) {
            gameBoard[a] = new short[SIZE];
            for (int b = 0; b < SIZE; b++) {
                gameBoard[a][b] = EMPTY;
            }
        }
        return gameBoard;
    }

    /**
     * Creates a new game board base off a single array of shorts. It is expected that each array is Board.Size long and the
     * array is of size Board.SIZE.
     * 
     * @param string
     *            The board in string form.
     */
    Board(short[] shortBoard) {

        gameBoard = new short[Board.SIZE][Board.SIZE];
        int x = 0, y = 0;
        for (short s : shortBoard) {
            gameBoard[x][y] = (short) s;
            ++x;
            if (x == Board.SIZE) {
                ++y;
                x = 0;
            }
        }
    }

    /**
     * @return Returns a single array of shorts of the board.
     */
    public short[] getShortArray() {
        short[] array = new short[Board.SIZE * Board.SIZE];
        int x = 0, y = 0;
        for (int a = 0; a < array.length; a++) {
            array[a] = gameBoard[y][x];
            ++x;
            if (x == Board.SIZE) {
                ++y;
                x = 0;
            }
        }
        return array;
    }

    /**
     * Is the cell occupied?
     * 
     * @param x
     *            The x location.
     * @param y
     *            The y location.
     * @return Who occupies the cell?
     */
    public int isOccupied(int x, int y) {
        return gameBoard[x][y];
    }

    /**
     * Occupy the cell for Black.
     * 
     * @param x
     *            The x location.
     * @param y
     *            The y location.
     * @return Did a spot become occupied?
     */
    boolean occupyBlack(int x, int y) {
        return occupy(x, y, BLACK);
    }

    /**
     * Occupy the cell for White.
     * 
     * @param x
     *            The x location.
     * @param y
     *            The y location.
     * @return Did a spot become occupied?
     */
    boolean occupyWhite(int x, int y) {
        return occupy(x, y, WHITE);
    }

    /**
     * Occupy the cell for Black.
     * 
     * @param x
     *            The x location.
     * @param y
     *            The y location.
     * @param colour
     *            The colour playing.
     * @return Did a spot become occupied?
     */
    private boolean occupy(int x, int y, short colour) {
        // Check if you are trying to play on top of another stone.
        if (gameBoard[x][y] == EMPTY) {
            int right = lookBeSide(x + 1, y, colour == BLACK ? WHITE : BLACK, createEmptyBoard());
            int left = lookBeSide(x - 1, y, colour == BLACK ? WHITE : BLACK, createEmptyBoard());
            int down = lookBeSide(x, y + 1, colour == BLACK ? WHITE : BLACK, createEmptyBoard());
            int up = lookBeSide(x, y - 1, colour == BLACK ? WHITE : BLACK, createEmptyBoard());

            gameBoard[x][y] = colour;

            // Make sure you aren't trying to commit suicide.
            if (checkLiberties(x, y, colour) == 0) {

                // Check the liberties around you to see if any spots of the other colour are in atari.
                if (right == 1 || left == 1 || down == 1 || up == 1) {
                    if (right == 1) {
                        gameBoard[x + 1][y] = EMPTY;
                    }
                    if (left == 1) {
                        gameBoard[x - 1][y] = EMPTY;
                    }
                    if (down == 1) {
                        gameBoard[x][y + 1] = EMPTY;
                    }
                    if (up == 1) {
                        gameBoard[x][y - 1] = EMPTY;
                    }
                    return true;
                } else {
                    gameBoard[x][y] = EMPTY;
                    return false;
                }

            }
            return true;
        }

        // Cell is occupied.
        return false;
    }

    /**
     * Special method that return negative one to indicate the cell is out of bounds.
     * 
     * @param x
     * @param y
     * @param colour
     * @param locationslookedAt
     * @return
     */
    private int lookBeSide(int x, int y, short colour, short[][] locationslookedAt) {
        if (x < 0 || x > SIZE && y < 0 && y > SIZE) {
            return -1;
        }
        return look(x, y, colour, locationslookedAt);
    }

    /**
     * @return Gets the game board.
     */
    public short[][] getBoard() {
        return gameBoard;
    }

    /**
     * Checks out how many liberties the stones have for the colour passed in.
     * 
     * @param colour
     *            Look at this persons liberties.
     * 
     * @return If any stones were captured.
     */
    public boolean checkAllLibertiesFor(short colour) {
        List<Point> capture = new ArrayList<Point>();
        // First go over the whole board looking for points that have been captured.
        for (int y = 0; y < SIZE; ++y) {
            for (int x = 0; x < SIZE; ++x) {
                if (checkLiberties(x, y, colour) == 0) {
                    capture.add(new Point(x, y));
                }
            }
        }

        // Now remove all the captured points.
        for (Point p : capture) {
            gameBoard[p.x][p.y] = EMPTY;
        }

        // Were any stones captured?
        return capture.isEmpty();
    }

    /**
     * Checks the liberties for one location.
     * 
     * @param x
     *            The x location.
     * @param y
     *            The y location.
     * @param colour
     *            Look at this persons liberties.
     * @return How many liberties the location has, -1 if the cell is unoccupied.
     */
    int checkLiberties(int x, int y, short colour) {
        return checkEachLiberty(x, y, colour, createEmptyBoard());
    }

    /**
     * Checks the liberties for one location.
     * 
     * @param x
     *            The x location.
     * @param y
     *            The y location.
     * @param colour
     *            Look at this persons liberties.
     * @param locationslookedAt
     *            All the spots that have been covered to prevent infinite recursions.
     * 
     * @return How many liberties the location has, -1 if the cell is unoccupied.
     */
    private int checkEachLiberty(int x, int y, short colour, short[][] locationslookedAt) {
        int liberties = 0;
        if (locationslookedAt[x][y] == EMPTY) {
            locationslookedAt[x][y] = 0;

            if (isOccupied(x, y) != Board.EMPTY) {
                liberties = lookAround(x, y, colour, locationslookedAt);
            } else {
                liberties = -1;
            }
        }
        return liberties;
    }

    /**
     * Look for liberties.
     * 
     * @param x
     *            The x location.
     * @param y
     *            The y location.
     * @param colour
     *            Look at this persons liberties.
     * @param locationslookedAt
     *            All the spots that have been covered to prevent infinite recursions.
     * @return How many liberties the location has, 0 if none found.
     */
    private int lookAround(int x, int y, short colour, short[][] locationslookedAt) {
        int liberties;
        // look left
        liberties = look(x - 1, y, colour, locationslookedAt);

        // look right
        liberties += look(x + 1, y, colour, locationslookedAt);

        // look up
        liberties += look(x, y - 1, colour, locationslookedAt);

        // look down
        liberties += look(x, y + 1, colour, locationslookedAt);
        return liberties;
    }

    /**
     * Look in a direction for a liberties.
     * 
     * @param x
     *            The x location.
     * @param y
     *            The y location.
     * @param colour
     *            Look at this persons liberties.
     * @param locationslookedAt
     *            Where you've looked.
     * @return The amount of liberties found,
     */
    private int look(int x, int y, short colour, short[][] locationslookedAt) {
        int liberties = 0;
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
            if (isOccupied(x, y) == EMPTY) {
                ++liberties;
            } else if (isOccupied(x, y) == colour) {
                liberties += checkEachLiberty(x, y, colour, locationslookedAt);
            }
        }
        return liberties;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        // Print numbers across the top.
        buffer.append('\n').append("---");
        for (int x = 0; x < SIZE; ++x) {
            buffer.append("---").append(x).append("---");
        }
        buffer.append('\n');

        for (int y = 0; y < SIZE; ++y) {

            // Draw Row
            buffer.append(y).append("--");
            for (int x = 0; x < SIZE; ++x) {
                if (gameBoard[x][y] == EMPTY) {
                    buffer.append("---");
                    buffer.append("*");
                    buffer.append("---");
                } else if (gameBoard[x][y] == BLACK) {
                    buffer.append("---");
                    buffer.append("B");
                    buffer.append("---");
                } else if (gameBoard[x][y] == WHITE) {
                    buffer.append("--");
                    buffer.append("W");
                    buffer.append("--");
                }
            }
            buffer.append('\n');
        }

        return buffer.toString();
    }
}
