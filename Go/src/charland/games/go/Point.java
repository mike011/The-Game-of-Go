package charland.games.go;

/**
 * Represents a point on the board.
 * 
 * @author Michael
 * 
 */
public class Point {

    public int x;
    public int y;
    public boolean alive;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
        alive = true;
    }

    public String toString() {
        return "[" + x + "][" + y + "]";
    }
}
