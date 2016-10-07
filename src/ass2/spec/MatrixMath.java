package ass2.spec;

/**
 * Created by Administrator on 10/5/2016.
 */
public class MatrixMath {
    /**
     * Multiply two matrices of the same order
     * @param a
     * @param b
     * @return
     */
    public static double[][] multiply(double[][] a, double[][] b) {
        double[][] out = new double[a.length][a.length];
        if (a.length > 0 && a.length == b.length) {
            for (int i = 0; i < a.length; i++) {
                for (int j = 0; j < a[0].length; j++) {
                    out[i][j] = 0;
                    for (int k = 0; k < out.length; k++) {
                        out[i][j] += a[i][k] * b[k][j];
                    }
                }
            }
        } else {
            return null;
        }
        return out;
    }

    public static double[][] identity(int order) {
        double[][] out = new double[order][order];
        for (int i = 0; i < out.length; i++) {
            for (int j = 0; j < out[0].length; j++) {
                if (i == j) {
                    out[i][j] = 1;
                } else {
                    out[i][j] = 0;
                }
            }
        }
        return out;
    }

    public static double[] multiply(double[][] a, double[] b) {
        double[] out = new double[b.length];
        if (a.length == b.length) {
            for (int i = 0; i < a.length; i++) {
                for (int j = 0; j < a.length; j++) {
                        out[i] += a[i][j] * b[j];
                }
            }
        }
        return out;
    }
}
