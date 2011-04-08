/**
 * 
 */
package charland.games.go;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author mcharland
 *
 */
public class PointTest extends TestCase {

    /**
     * Test method for {@link charland.games.go.Point#Point(int, int)}.
     */
    public void testPoint() {
        int y =1;
		int x =-34;
		Point p = new Point(x,y);
		Assert.assertEquals("bad x", x, p.x);
		Assert.assertEquals("bad y", y, p.y);
		Assert.assertTrue("Should be alive", p.alive);
    }

    /**
     * Test method for {@link charland.games.go.Point#toString()}.
     */
    public void testToString() {
        Assert.assertEquals("bad string", "[1][2]", new Point(1,2).toString());
    }

}
