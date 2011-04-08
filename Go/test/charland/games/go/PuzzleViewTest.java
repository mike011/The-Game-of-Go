/**
 * 
 */
package charland.games.go;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.graphics.Canvas;

import com.xtremelabs.robolectric.RobolectricTestRunner;

/**
 * @author mcharland
 * 
 */
@RunWith(RobolectricTestRunner.class)
public class PuzzleViewTest {

	/**
	 * Test method for {@link charland.games.go.PuzzleView#onSizeChanged(int, int, int, int)}.
	 */
	@Test
	public void testOnSizeChanged() {
		PuzzleView pv = new PuzzleView(null);
		int w = 100;
		int h = 10;
		int oldw = 234;
		int oldh = 421;
		pv.onSizeChanged(w, h, oldw, oldh);
		assertEquals("Width should the smaller of the height and width ", (float) h, pv.getWidthAmount(), 0.1);

		float cellWidth = (float) h / 10;
		assertEquals("Cell Width should be one tenth the smaller width height", cellWidth, pv.getCellWidthAmount(), 0.1);
		assertEquals("Left should be the cellWidth", cellWidth, pv.getLeftAmount(), 0.1);
		assertEquals("Top should be the cellWidth", cellWidth, pv.getTopAmount(), 0.1);
		assertEquals("Right should be the cellWidth * Board Width", cellWidth * Board.SIZE, pv.getRightAmount(), 0.1);
		assertEquals("Bottom should be the cellWidth * Board Width", cellWidth * Board.SIZE, pv.getBottomAmount(), 0.1);

		// Assert.assertEquals(h, pv.getHeightAmount());
	}
}
