package ass2.tests;

import ass2.spec.MatrixMath;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by Administrator on 10/5/2016.
 */
public class MatrixMathTest extends TestCase {
    private static final double EPSILON = 0.001;

    @Test
    public void testMultiply0() {
        double[][] a = MatrixMath.identity(3);
        double[][] b = MatrixMath.identity(3);

        double[][] c = MatrixMath.multiply(a, b);
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a.length; j++) {
                assertEquals(a[i][j], c[i][j], EPSILON);
            }
        }
    }

    @Test
    public void testMultiply1() {
        double[][] a = {{3, -1, 5},
                        {2, 6, -3},
                        {0, 0, 1}};
        double[][] b = {{1, 8, 3},
                        {5, 8, 1},
                        {6, 8, 4}};

        double[][] c = {{28, 56, 28},
                        {14, 40, 0},
                        {6, 8, 4}};

        double[][] r = MatrixMath.multiply(a, b);

        for (int i = 0; i < r.length; i++) {
            for (int j = 0; j < r.length; j++) {
                assertEquals(c[i][j], r[i][j], EPSILON);
            }
        }
    }

    @Test
    public void testMultiply2() {
        double[][] a = MatrixMath.identity(3);
        double[] b = {3, 5, 7};
        double[] r = MatrixMath.multiply(a, b);

        for (int i = 0; i < b.length; i++) {
            assertEquals(b[i], r[i], EPSILON);
        }
    }

    @Test
    public void testMultiply3() {
        double[][] a = {{1, 2, 3},
                        {4, 5, 6},
                        {7, 8, 9}};
        double[] b = {10, 11, 12};
        double[] r = MatrixMath.multiply(a, b);
        assertEquals(r[0], 68, EPSILON);
        assertEquals(r[1], 167, EPSILON);
        assertEquals(r[2], 266, EPSILON);
    }
}
