package ass2.spec;

import com.jogamp.opengl.GL2;

import java.awt.*;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by Administrator on 9/27/2016.
 */
public class TerrainMesh extends Mesh {
    private Dimension mySize;

    public TerrainMesh(Terrain theTerrain) {
        super();
        mySize = theTerrain.size();

        int[][] vertIndices = new int[mySize.width][mySize.height];
        int counter = 0;
        for (int x = 0; x < mySize.width; x++) {
            for (int z = 0; z < mySize.height; z++) {
                vertList.add(new double[]{x, theTerrain.getGridAltitude(x, z), z});
                vertIndices[x][z] = counter;
                counter++;
            }
        }
        //create faces of terrain mesh
        for (int x = 0; x < mySize.width - 1; x++) {
            for (int z = 0; z < mySize.height - 1; z++) {
                //      +------+
                //      | 2   /|
                //      |   /  |
                //      | /  1 |
                //      +------+

                //region Triangle 1
                Face newFace = new Face();
                int[] newVerts = new int[3];
                newVerts[0] = vertIndices[x][z];
                newVerts[1] = vertIndices[x + 1][z + 1];
                newVerts[2] = vertIndices[x + 1][z];

                double[] a = createVector(vertList.get(vertIndices[x][z]), vertList.get(vertIndices[x+1][z]));
                double[] b = createVector(vertList.get(vertIndices[x+1][z]), vertList.get(vertIndices[x+1][z+1]));

                newFace.setNormals(new int[]{normList.size(), normList.size(), normList.size()});
                normList.add(createNormal(b, a));

                newFace.setVerts(newVerts);

                faceList.add(newFace);
                //endregion

                //region Triangle 2
                newFace = new Face();
                newVerts = new int[3];
                newVerts[0] = vertIndices[x][z];
                newVerts[1] = vertIndices[x][z + 1];
                newVerts[2] = vertIndices[x + 1][z + 1];

                a = createVector(vertList.get(vertIndices[x][z]), vertList.get(vertIndices[x+1][z+1]));
                b = createVector(vertList.get(vertIndices[x+1][z+1]), vertList.get(vertIndices[x][z+1]));

                newFace.setNormals(new int[]{normList.size(), normList.size(), normList.size()});
                normList.add(createNormal(b, a));

                newFace.setVerts(newVerts);

                faceList.add(newFace);
                //endregion
            }
        }
    }

    public void render(GL2 gl) {
        float[] greenDiff = {0.2f, 0.8f, 0.2f, 1.0f};
        float[] greenAmb = {0.0f, 0.5f, 0.0f, 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, greenDiff, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, greenAmb, 0);

        Face nextFace = null;
        Iterator<Face> iter = faceList.iterator();
        gl.glBegin(gl.GL_TRIANGLES);
            while (iter.hasNext()) {
                nextFace = iter.next();
                int[] verts = nextFace.getVerts();
                for (int i = 0; i < verts.length; i++) {
                    double[] normal = normList.get(nextFace.getNormals()[i]);
                    gl.glNormal3d(normal[0], normal[1], normal[2]);
                    double[] vertex = vertList.get(nextFace.getVerts()[i]);
                    gl.glVertex3d(vertex[0], vertex[1], vertex[2]);
                }
            }
        gl.glEnd();
    }

    private static double[] createVector(double[] a, double[] b) {
        double[] ab = new double[3];
        ab[0] = b[0] - a[0];
        ab[1] = b[1] - a[1];
        ab[2] = b[2] - a[2];

        return ab;
    }

    private static double[] createNormal(double[] a, double[] b) {
        //AxB = (AyBz − AzBy, AzBx − AxBz, AxBy − AyBx)
        double[] normal = new double[3];
        normal[0] = a[1] * b[2] - a[2] * b[1];
        normal[1] = a[2] * b[0] - a[0] * b[2];
        normal[2] = a[0] * b[1] - a[1] * b[0];

        return Game.normaliseVector(normal);
    }

    /**
     * Get the altitude of the mesh at an arbitrary point.
     * Non-integer points should be interpolated from neighbouring grid points
     *
     * @param x
     * @param z
     * @return
     */
    public double altitude(double x, double z) {
        if (x < 0 || z < 0 || x > mySize.width - 1 || z > mySize.height - 1) {
            return 0;
        }
        Face theFace = findFace(x, z);

        double[] p0 = vertList.get(theFace.getVerts()[0]);
        double[] p1 = vertList.get(theFace.getVerts()[1]);
        double[] p2 = vertList.get(theFace.getVerts()[2]);

        return calcTriangleY(x, z, p0, p1, p2);
    }

    /**
     * Calculate the Y value of a given x,z coordinate in a 3D triangle
     * @return
     */
    public static double calcTriangleY(double x, double z, double[] p0, double[] p1, double[] p2) {
        //if the face is parallel with the xz plane, then just return the y value of one of the verts
        if (p0[1] == p1[1] && p1[1] == p2[1]) {
            return p0[2];
        } else {    //calculate the equation of the plane the triangle is on, then calculate y from x and z
            double[] v1 = createVector(p0, p1);
            double[] v2 = createVector(p0, p2);
            double[] n = createNormal(v1, v2);
            double d = (n[0] * p0[0] + n[1] * p0[1] + n[2] * p0[2]) * -1;
            double y = (d + x * n[0] + z * n[2]) * (-1 / n[1]);

            return y;
        }
    }

    private Face findFace(double x, double z) {
        Iterator<Face> iter = faceList.iterator();
        Face theFace = null;
        while (iter.hasNext()) {
            theFace = iter.next();
            double[] v0 = vertList.get(theFace.getVerts()[0]);
            double[] v1 = vertList.get(theFace.getVerts()[1]);
            double[] v2 = vertList.get(theFace.getVerts()[2]);
            if (Game.isInsideTri(x, z, v0[0], v0[2], v1[0], v1[2], v2[0], v2[2])) {
                return theFace;
            }
        }
        return null;
    }
}
