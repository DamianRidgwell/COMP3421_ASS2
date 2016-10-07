package ass2.spec;

import com.jogamp.opengl.GL2;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */
public class Terrain extends Mesh {

    private Dimension mySize;
    private double[][] myAltitude;
    private List<Tree> myTrees;
    private List<Road> myRoads;
    private float[] mySunlight;




    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth) {
        mySize = new Dimension(width, depth);
        myAltitude = new double[width][depth];
        myTrees = new ArrayList<Tree>();
        myRoads = new ArrayList<Road>();
        mySunlight = new float[3];
    }

    public void generateMesh() {
        int[][] vertIndices = new int[mySize.width][mySize.height];
        int counter = 0;
        for (int x = 0; x < mySize.width; x++) {
            for (int z = 0; z < mySize.height; z++) {
                vertList.add(new double[]{x, getGridAltitude(x, z), z});
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

                double[] a = Game.createVector(vertList.get(vertIndices[x][z]), vertList.get(vertIndices[x+1][z]));
                double[] b = Game.createVector(vertList.get(vertIndices[x+1][z]), vertList.get(vertIndices[x+1][z+1]));

                newFace.setNormals(new int[]{normList.size(), normList.size(), normList.size()});
                normList.add(Game.createNormal(b, a));

                newFace.setVerts(newVerts);

                faceList.add(newFace);
                //endregion

                //region Triangle 2
                newFace = new Face();
                newVerts = new int[3];
                newVerts[0] = vertIndices[x][z];
                newVerts[1] = vertIndices[x][z + 1];
                newVerts[2] = vertIndices[x + 1][z + 1];

                a = Game.createVector(vertList.get(vertIndices[x][z]), vertList.get(vertIndices[x+1][z+1]));
                b = Game.createVector(vertList.get(vertIndices[x+1][z+1]), vertList.get(vertIndices[x][z+1]));

                newFace.setNormals(new int[]{normList.size(), normList.size(), normList.size()});
                normList.add(Game.createNormal(b, a));

                newFace.setVerts(newVerts);

                faceList.add(newFace);
                //endregion
            }
        }
    }

    public Terrain(Dimension size) {
        this(size.width, size.height);
    }

    public Dimension size() {
        return mySize;
    }

    public List<Tree> trees() {
        return myTrees;
    }

    public List<Road> roads() {
        return myRoads;
    }

    public float[] getSunlight() {
        return mySunlight;
    }

    /**
     * Set the sunlight direction. 
     * 
     * Note: the sun should be treated as a directional light, without a position
     * 
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        mySunlight[0] = dx;
        mySunlight[1] = dy;
        mySunlight[2] = dz;        
    }
    
    /**
     * Resize the terrain, copying any old altitudes. 
     * 
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        mySize = new Dimension(width, height);
        double[][] oldAlt = myAltitude;
        myAltitude = new double[width][height];
        
        for (int i = 0; i < width && i < oldAlt.length; i++) {
            for (int j = 0; j < height && j < oldAlt[i].length; j++) {
                myAltitude[i][j] = oldAlt[i][j];
            }
        }
    }

    /**
     * Get the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        return myAltitude[x][z];
    }

    /**
     * Set the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, double h) {
        myAltitude[x][z] = h;
    }

    /**
     * Get the altitude at an arbitrary point. 
     * Non-integer points should be interpolated from neighbouring grid points
     * 
     * TO BE COMPLETED
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
            return p0[1];
        } else {    //calculate the equation of the plane the triangle is on, then calculate y from x and z
            double[] v1 = Game.createVector(p0, p1);
            double[] v2 = Game.createVector(p0, p2);
            double[] n = Game.createNormal(v1, v2);
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

    private double max(double a, double b) {
        if (a > b) {
            return a;
        }
        return b;
    }

    private double min(double a, double b) {
        if (a < b) {
            return a;
        }
        return b;
    }

    /**
     * Add a tree at the specified (x,z) point. 
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     * 
     * @param x
     * @param z
     */
    public void addTree(double x, double z) {
        double y = altitude(x, z);
        Tree tree = new Tree(x, y, z);
        myTrees.add(tree);
    }

    /**
     * Add a road. 
     * 
     * @param width
     * @param spine
     */
    public void addRoad(double width, double[] spine) {
        Road road = new Road(width, spine);
        myRoads.add(road);
        road.generateMesh();
    }

    @Override
    public void render(GL2 gl) {
        int chessboardTexID = Game.getInstance().getTexture(0);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, chessboardTexID);

        float[] whiteDiff = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] whiteAmb = {0.25f, 0.25f, 0.25f, 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, whiteDiff, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, whiteAmb, 0);

        Face nextFace = null;
        Tree nextTree = null;
        Iterator<Face> faceIter = faceList.iterator();
        Iterator<Tree> treeIter = myTrees.iterator();
        gl.glBegin(gl.GL_TRIANGLES);
            while (faceIter.hasNext()) {
                nextFace = faceIter.next();
                int[] verts = nextFace.getVerts();
                for (int i = 0; i < verts.length; i++) {
                    double[] normal = normList.get(nextFace.getNormals()[i]);
                    gl.glNormal3d(normal[0], normal[1], normal[2]);
                    double[] vertex = vertList.get(nextFace.getVerts()[i]);
                    gl.glTexCoord2d(vertex[0] / 8, vertex[2] / 8);
                    gl.glVertex3d(vertex[0], vertex[1], vertex[2]);
                }
            }
        gl.glEnd();

        while (treeIter.hasNext()) {
            nextTree = treeIter.next();
            gl.glPushMatrix();
            nextTree.render(gl);
            gl.glPopMatrix();
        }
    }
}
