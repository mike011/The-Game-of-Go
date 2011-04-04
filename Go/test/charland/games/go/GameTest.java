/**
 * 
 */
package charland.games.go;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.RobolectricTestRunner;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author Michael
 * 
 */
@RunWith(RobolectricTestRunner.class)
public class GameTest extends TestCase {

    /**
     * Test trying to capture a white stone. <br>
     * <code>
     * |  |  |B1|  |  |W2|
     * -------------------
     * |  |B3|W4|B5|  |W6|
     * ------------------- 
     * |  |  |B7|  |  |  | 
     * </code>
     */
    @Test
    public void testPlayTurn() {

        // Setup
        Game g = new Game();
        g.createBoard(null);
        g.playTurn(3, 0); // B1
        g.playTurn(6, 0); // W2

        g.playTurn(2, 1); // B3
        g.playTurn(3, 1); // W4
        g.playTurn(4, 1); // B5
        g.playTurn(6, 1); // W6

        // Test
        Assert.assertFalse(g.getGameBoard().toString(), g.playTurn(3, 2)); // B7 which should capture W4.
    }
}