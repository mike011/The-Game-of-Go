package charland.games.go;

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
     */
    void occupyBlack(int x, int y) {
        if (gameBoard[x][y] == EMPTY) {
            gameBoard[x][y] = BLACK;
        }
    }

    /**
     * Occupy the cell for White.
     * 
     * @param x
     *            The x location.
     * @param y
     *            The y location.
     */
    void occupyWhite(int x, int y) {
        if (gameBoard[x][y] == EMPTY) {
            gameBoard[x][y] = WHITE;
        }
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
        boolean captured = false;
        for (int y = 0; y < SIZE; ++y) {
            for (int x = 0; x < SIZE; ++x) {
                if (checkLiberties(x, y, colour) == 0) {
                    gameBoard[x][y] = EMPTY;
                    captured = true;
                }
            }
        }
        return captured;
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
                // look left
                liberties = look(x - 1, y, colour, locationslookedAt);

                // look right
                liberties += look(x + 1, y, colour, locationslookedAt);

                // look up
                liberties += look(x, y - 1, colour, locationslookedAt);

                // look down
                liberties += look(x, y + 1, colour, locationslookedAt);
            } else {
                liberties = -1;
            }
        }
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
     * @return
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
        buffer.append('\n').append("  ");

        for (int y = 0; y < SIZE; ++y) {
            buffer.append(y).append(' ');
        }
        buffer.append('\n');
        
        int row = 0;
        for (int y = 0; y < SIZE / 2; ++y) {

            // Draw Row
            buffer.append(row++).append(' ');
            for (int x = 0; x < SIZE; ++x) {
                buffer.append("--");                
            }
            buffer.append('\n');

            // Draw Columns
            if (y < SIZE - 1) {
                buffer.append(row++);
                for (int x = 0; x < SIZE; ++x) {
                    buffer.append("|  ");
                }
                buffer.append('\n');
            }
        }

        return buffer.toString();
    }
}
