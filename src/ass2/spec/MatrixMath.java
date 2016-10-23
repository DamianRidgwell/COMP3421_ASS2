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

    /**
     * Produces a 3D rotation matrix. Only supply one angle for an axis. Don't try anything fancy.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static double[][] rotationMat3(double x, double y, double z) throws IllegalArgumentException {
        double[][] matrix = identity(4);
        if (x != 0.0 && y == z && z == 0.0) {
            matrix[1][1] = Math.cos(x);
            matrix[1][2] = 0 - Math.sin(x);
            matrix[2][1] = Math.sin(x);
            matrix[2][2] = Math.cos(x);
        } else if (y != 0.0 && x == z && z == 0.0) {
            matrix[0][0] = Math.cos(y);
            matrix[0][2] = Math.sin(y);
            matrix[2][0] = 0 - Math.sin(y);
            matrix[2][2] = Math.cos(y);
        } else if (z != 0.0 && x == y && y == 0.0) {
            matrix[0][0] = Math.cos(z);
            matrix[0][1] = 0 - Math.sin(z);
            matrix[1][0] = Math.sin(z);
            matrix[1][1] = Math.cos(z);
        } else if (x == y && y == z && z == 0.0) {
            return matrix;
        } else {
            throw new IllegalArgumentException("Angles supplied for more than one axis");
        }

        return matrix;
    }

    public static double[][] rotationMat2(double a) {
        double[][] matrix = identity(3);
        matrix[0][0] = Math.cos(a);
        matrix[0][1] = Math.sin(a); // -sin(a)
        matrix[1][0] = Math.sin(0 - a);
        matrix[1][1] = Math.cos(a);

        return matrix;
    }

    public static double[][] translationMat2(double[] newVector) {
        double[][] matrix = identity(3);
        for (int i = 0; i < newVector.length; i++) {
            matrix[i][2] = newVector[i];
        }
        return matrix;
    }

    /**
     *
     * @param transMat
     * @param v0
     * @return
     */
    public static double[] transform(double[][] transMat, double[] v0) throws IllegalArgumentException {
        double[] v1 = new double[v0.length + 1];
        if (v1.length != transMat.length) {
            throw new IllegalArgumentException("Size mismatch in MathMatrix.transform " + transMat.length + ", " + v1.length);
        }
        System.arraycopy(v0, 0, v1, 0, v0.length);
        v1[v1.length - 1] = 1;

        v1 = multiply(transMat, v1);
        double[] v2 = new double[v0.length];
        System.arraycopy(v1, 0, v2, 0, v2.length);
        return v2;
    }

    public static double[][] scaleMat3(double v) {
        double[][] matrix = identity(4);
        for (int i = 0; i < 3; i++) {
            matrix[i][i] = v;
        }
        return matrix;
    }
}
