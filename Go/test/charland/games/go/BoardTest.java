/**
 * 
 */
package charland.games.go;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.RobolectricTestRunner;

/**
 * @author Michael
 * 
 */
@RunWith(RobolectricTestRunner.class)
public class BoardTest {

    /**
     * Make sure everything is empty to start.
     */
    @Test
    public void testIsOccupiedStart() {
        Board b = new Board();
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                Assert.assertEquals("Cell x:" + x + " y:" + y + " Should not be occupied", Board.EMPTY, b.isOccupied(x, y));
            }
        }
    }

    /**
     * Occupy a cell with black.
     */
    @Test
    public void testIsOccupiedBlack() {
        Board b = new Board();
        b.occupyBlack(4, 3);
        Assert.assertEquals("Should be occupied", Board.BLACK, b.isOccupied(4, 3));
    }

    /**
     * Occupy a cell with white.
     */
    @Test
    public void testIsOccupiedWhite() {
        Board b = new Board();
        b.occupyWhite(2, 7);
        Assert.assertEquals("Should be occupied", Board.WHITE, b.isOccupied(2, 7));
    }

    /**
     * Tests the short[] constructor to make sure the parsing is working as expected.
     */
    @Test
    public void testBoardShortArrayConstructor() {

        short[] array = new short[Board.SIZE * Board.SIZE];
        for (int x = 0; x < Board.SIZE * Board.SIZE; x++) {
            array[x] = Board.EMPTY;
        }

        Board b = new Board(array);
        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                Assert.assertEquals("Cell x:" + x + " y:" + y + " Should not be occupied", Board.EMPTY, b.isOccupied(x, y));
            }
        }
    }

    /**
     * Tests that you can convert from a double array to a single array.
     */
    @Test
    public void testGetShortArray() {
        Board b = new Board();
        short[] shortArray = b.getShortArray();
        for (int y = 0; y < Board.SIZE * Board.SIZE; y++) {
            Assert.assertEquals("Cell " + y + " should not be occupied", Board.EMPTY, shortArray[y]);
        }
    }

    /**
     * Test an empty board which shouldn't have any cells occupied.
     */
    @Test
    public void testLibertiesCheck() {
        Board b = new Board();
        Assert.assertEquals("Cell should be onoccupied", -1, b.checkLiberties(0, 0, Board.BLACK));
    }

    /**
     * Test a spot which should have the max amount of liberties. <br>
     * <code>
     * | | | | | |
     * -----------
     * | | |*| | |
     * ----------- 
     * | |*|S|*| |
     * ----------- 
     * | | |*| | | 
     * ----------- 
     * | | | | | | 
     * </code>
     */
    @Test
    public void testLibertiesCheck_Four() {
        Board b = new Board();
        int x = 1, y = 1;
        b.occupyBlack(x, y);
        Assert.assertEquals("Amount of liberties wrong", 4, b.checkLiberties(x, y, Board.BLACK));
    }

    /**
     * Test a spot which should have the max amount of liberties and a stone to the left adding those liberties to the total. <br>
     * <code>
     * | | | | | | |
     * -------------
     * | | |*|*| | |
     * ------------- 
     * | |*|2|1|*| |
     * ------------- 
     * | | |*|*| | | 
     * ------------- 
     * | | | | | | | 
     * </code>
     */
    @Test
    public void testLibertiesCheck_Multi_LeftStone() {
        Board b = new Board();
        int x = 2, y = 1;
        b.occupyBlack(x - 1, y);
        b.occupyBlack(x, y);
        Assert.assertEquals("Amount of liberties wrong", 6, b.checkLiberties(x, y, Board.BLACK));
    }

    /**
     * Test a spot which should have the max amount of liberties and a stone to the right adding those liberties to the
     * total. <br>
     * <code>
     * | | | | | | |
     * -------------
     * | | |*|*| | |
     * ------------- 
     * | |*|1|2|*| |
     * ------------- 
     * | | |*|*| | | 
     * ------------- 
     * | | | | | | | 
     * </code>
     */
    @Test
    public void testLibertiesCheck_Multi_RightStone() {
        Board b = new Board();
        int x = 1, y = 1;
        b.occupyBlack(x, y);
        b.occupyBlack(x + 1, y);
        Assert.assertEquals("Amount of liberties wrong", 6, b.checkLiberties(x, y, Board.BLACK));
    }

    /**
     * Test a spot which should have the max amount of liberties and a stone to the below adding those liberties to the
     * total. <br>
     * <code>
     * | | | | | |
     * -----------
     * | | |*| | |
     * ----------- 
     * | |*|1|*| |
     * ----------- 
     * | |*|2|*| | 
     * ----------- 
     * | | |*| | |
     * ----------- 
     * | | | | | |
     * </code>
     */
    @Test
    public void testLibertiesCheck_Multi_DownStone() {
        Board b = new Board();
        int x = 1, y = 1;
        b.occupyBlack(x, y);
        b.occupyBlack(x, y + 1);
        Assert.assertEquals("Amount of liberties wrong", 6, b.checkLiberties(x, y, Board.BLACK));
    }

    /**
     * Test a spot which should have the max amount of liberties and a stone to the above adding those liberties to the
     * total. <br>
     * <code>
     * | | | | | |
     * -----------
     * | | |*| | |
     * ----------- 
     * | |*|2|*| |
     * ----------- 
     * | |*|1|*| | 
     * ----------- 
     * | | |*| | | 
     * ----------- 
     * | | | | | |
     * </code>
     */
    @Test
    public void testLibertiesCheck_Multi_UpStone() {
        Board b = new Board();
        int x = 1, y = 2;
        b.occupyBlack(x, y - 1);
        b.occupyBlack(x, y);
        Assert.assertEquals("Amount of liberties wrong", 6, b.checkLiberties(x, y, Board.BLACK));
    }

    /**
     * Test the top left corner. <br>
     * <code>
     * ___________
     * ||S|*| | 
     * ----------- 
     * ||*| | | 
     * </code>
     */
    @Test
    public void testLibertiesCheck_TopLeft() {
        Board b = new Board();
        int x = 0, y = 0;
        b.occupyBlack(x, y);
        Assert.assertEquals("Amount of liberties wrong", 2, b.checkLiberties(x, y, Board.BLACK));
    }

    /**
     * Test the top side. <br>
     * <code>
     * ___________ 
     * | |*|S|*| | 
     * ----------- 
     * | | |*| | | 
     * </code>
     */
    @Test
    public void testLibertiesCheck_TopSide() {
        Board b = new Board();
        int x = 5, y = 0;
        b.occupyBlack(x, y);
        Assert.assertEquals("Amount of liberties wrong", 3, b.checkLiberties(x, y, Board.BLACK));
    }

    /**
     * Test the left side. <br>
     * <code>
     * ----------- 
     * ||*| | |  
     * -----------
     * ||S|*| | 
     * ----------- 
     * ||*| | | 
     * </code>
     */
    @Test
    public void testLibertiesCheck_LeftSide() {
        Board b = new Board();
        int x = 0, y = 3;
        b.occupyBlack(x, y);
        Assert.assertEquals("Amount of liberties wrong", 3, b.checkLiberties(x, y, Board.BLACK));
    }

    /**
     * Test the top right corner. <br>
     * <code>
     * ________ 
     * | |*|S||
     * -------- 
     * | | |*||
     * </code>
     */
    @Test
    public void testLibertiesCheck_TopRight() {
        Board b = new Board();
        int x = Board.SIZE - 1, y = 0;
        b.occupyBlack(x, y);
        Assert.assertEquals("Amount of liberties wrong", 2, b.checkLiberties(x, y, Board.BLACK));
    }

    /**
     * Test the right side. <br>
     * <code>
     * | | ||
     * | |*||
     * |*|S||
     * | |*||
     * | | ||
     * </code>
     */
    @Test
    public void testLibertiesCheck_RightSide() {
        Board b = new Board();
        int x = Board.SIZE - 1, y = 6;
        b.occupyBlack(x, y);
        Assert.assertEquals("Amount of liberties wrong", 3, b.checkLiberties(x, y, Board.BLACK));
    }

    /**
     * Test the bottom left corner. <br>
     * <code>
     * ||*| | |
     * -------- 
     * ||S|*| |
     * ________ 
     * </code>
     */
    @Test
    public void testLibertiesCheck_BottomLeft() {
        Board b = new Board();
        int x = 0, y = Board.SIZE - 1;
        b.occupyBlack(x, y);
        Assert.assertEquals("Amount of liberties wrong", 2, b.checkLiberties(x, y, Board.BLACK));
    }

    /**
     * Test the bottom side. <br>
     * <code>
     * | | |*| | |
     * -----------
     * | |*|S|*| |
     * ___________
     * </code>
     */
    @Test
    public void testLibertiesCheck_BottomSide() {
        Board b = new Board();
        int x = 2, y = Board.SIZE - 1;
        b.occupyBlack(x, y);
        Assert.assertEquals("Amount of liberties wrong", 3, b.checkLiberties(x, y, Board.BLACK));
    }

    /**
     * Test the bottom right corner. <br>
     * <code>
     * | | |*||
     * -------- 
     * | |*|S||
     * ________ 
     * </code>
     */
    @Test
    public void testLibertiesCheck_BottomRight() {
        Board b = new Board();
        int x = Board.SIZE - 1, y = Board.SIZE - 1;
        b.occupyBlack(x, y);
        Assert.assertEquals("Amount of liberties wrong", 2, b.checkLiberties(x, y, Board.BLACK));
    }

    /**
     * Test a spot which should has a different coloured stone beside it. <br>
     * <code>
     * | | | | | | |
     * -------------
     * | | |*|$| | |
     * ------------- 
     * | |*|B|W|$| |
     * ------------- 
     * | | |*|$| | | 
     * ------------- 
     * | | | | | | | 
     * </code>
     */
    @Test
    public void testLibertiesCheck_Two_LeftStone() {
        Board b = new Board();
        int x = 2, y = 1;
        b.occupyBlack(x, y);
        b.occupyWhite(x + 1, y);
        Assert.assertEquals("Amount of liberties wrong", 3, b.checkLiberties(x, y, Board.BLACK));
        Assert.assertEquals("Amount of liberties wrong", 3, b.checkLiberties(x + 1, y, Board.WHITE));
    }
}