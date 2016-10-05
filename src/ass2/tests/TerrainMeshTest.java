package ass2.tests;
import junit.framework.*;
import ass2.spec.Terrain;
import org.junit.Test;

/**
 * Created by Administrator on 10/3/2016.
 */
public class TerrainMeshTest extends TestCase {
    private static final double EPSILON = 0.001;

    @Test
    public void testCalcTriangleY0() {
        double[] p0 = {0.0, 0.0, 0.0};
        double[] p1 = {1.0, 1.0, 0.0};
        double[] p2 = {1.0, 1.0, 1.0};

        double y = Terrain.calcTriangleY(0.5, 0.5, p0, p1, p2);
        assertEquals(0.5, y, EPSILON);
    }

    @Test
    public void testCalcTriangleY1() {
        double[] p0 = {1.0, 1.0, 1.0};
        double[] p1 = {-1.0, 1.0, 0.0};
        double[] p2 = {2.0, 0.0, 3.0};

        double y = Terrain.calcTriangleY(0.8, 0.8, p0, p1, p2);
        System.out.println(y);
        assertEquals(1.066666, y, EPSILON);
    }

    @Test
    public void testCalcTriangleY2() {
        double[] p0 = {2.0, 1.0, -1.0};
        double[] p1 = {0.0, -2.0, 0.0};
        double[] p2 = {1.0, -1.0, 2.0};

        double y = Terrain.calcTriangleY(0.0, 1.0, p0, p1, p2);
        assertEquals(-2.2, y, EPSILON);
    }

    @Test
    public void testCalcTriangleY3() {
        double[] p0 = {2.0, 1.0, -1.0};
        double[] p1 = {0.0, -2.0, 0.0};
        double[] p2 = {1.0, -1.0, 2.0};

        double y = Terrain.calcTriangleY(1.5, 0.5, p0, p1, p2);
        assertEquals(0.0, y, EPSILON);
    }
}
