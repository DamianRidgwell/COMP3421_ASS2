package ass2.spec;

import com.jogamp.opengl.GL2;

import java.util.Iterator;
import java.util.Vector;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree extends Mesh {
    private double[] myPos;
    private double trunkRadius;
    private double trunkHeight;
    private double leavesRadius;
    private int trunkFaces = 7;
    private int leavesSlices = 10;
    private int leavesVertIndex = 0; // the index of the start of the leaves verts
    private int leavesNormIndex = 0; // the index of the start of the leaves normals
    private int leavesFaceIndex = 0;

    private Vector<Face> leavesFaces;
    
    public Tree(double x, double y, double z) {
        super();
        myPos = new double[3];
        myPos[0] = x;
        myPos[1] = y;
        myPos[2] = z;

        trunkHeight = 1.0;
        trunkRadius = 0.2;
        leavesRadius = 1.0;

        generateMesh();
    }

    private void generateMesh() {
        generateTrunkMesh();
        generateLeavesMesh();
    }

    private void generateLeavesMesh() {
        leavesVertIndex = vertList.size();
        leavesNormIndex = normList.size();
        leavesFaceIndex = faceList.size();

        //add the top point of the sphere. Normal is the same as the vertex.
        double[] topVert = {0.0, 1.0, 0.0};
        vertList.add(topVert);
        normList.add(topVert);

        for (int i = 1; i < leavesSlices; i++) {    // horizontal slices
            for (int j = 0; j < leavesSlices + 1; j++) {    //
                double x = Math.sin(Math.PI * i / leavesSlices) *  Math.cos(2 * Math.PI * j / leavesSlices) * leavesRadius;
                double y = Math.cos(Math.PI * i / leavesSlices) * leavesRadius;
                double z = Math.sin(Math.PI * i / leavesSlices) * Math.sin(2 * Math.PI * j / leavesSlices) * leavesRadius;
                double[] newVert = {x, y, z};
                vertList.add(newVert);
                normList.add(newVert);
            }
        }

        //add the bottom point of the sphere. Normal is the same as the vertex.
        double[] bottomVert = {0.0, -1.0, 0.0};
        vertList.add(bottomVert);
        normList.add(bottomVert);

        //now generate faces
        //start with the top and bottom slices, which will be triangles connecting the polar
        // points to the next level of verts. Normals for all verts are the same as the vector for the verts.
        for (int i = 0; i < leavesSlices; i++) {
            Face topFace = new Face();
            topFace.setVerts(new int[]{leavesVertIndex, leavesVertIndex+2+i, leavesVertIndex+1+i});
            topFace.setNormals(new int[]{leavesNormIndex, leavesNormIndex+2+i, leavesNormIndex+1+i});

            Face bottomFace = new Face();
            int lastIndex = vertList.size() - 1;
            int lastIndexN = normList.size() - 1;
            bottomFace.setVerts(new int[]{lastIndex, lastIndex-1-i, lastIndex-2-i});
            bottomFace.setNormals(new int[]{lastIndexN, lastIndexN-1-i, lastIndexN-2-i});

            faceList.add(topFace);
            faceList.add(bottomFace);
        }

        //the intermediate layers, create triangles from adjacent verts
        int offset = leavesVertIndex + 1; //skip the top polar vert
        int offsetN = leavesNormIndex + 1;
        for (int i = 0; i < leavesSlices - 2; i++) {
            for (int j = 0; j < leavesSlices; j++) {
                int indexV1 = offset + i * (leavesSlices + 1); // there are (leavesSlices + 1) verts in a longitudinal line
                int indexV2 = offset + (i + 1) * (leavesSlices + 1);
                int indexN1 = offsetN + i * (leavesSlices + 1);
                int indexN2 = offsetN + (i + 1) * (leavesSlices + 1);

                Face face0 = new Face();
                face0.setVerts(new int[] {indexV1+j, indexV2+j+1, indexV2+j});
                face0.setNormals(new int[] {indexN1+j, indexN2+j+1, indexN2+j});

                Face face1 = new Face();
                face1.setVerts(new int[] {indexV1+j, indexV1+j+1, indexV2+j+1});
                face1.setNormals(new int[] {indexN1+j, indexN1+j+1, indexN2+j+1});

                faceList.add(face0);
                faceList.add(face1);
            }
        }
    }

    private void generateTrunkMesh() {
        double r = 2 * Math.PI / trunkFaces;
        for (int i = 0; i <= trunkFaces; i++) {
            double x = trunkRadius * Math.cos(i* r);
            double z = trunkRadius * Math.sin(i * r);
            double[] v0 = {x, 0.0, z};
            double[] v1 = {x, trunkHeight, z};
            vertList.add(v0);
            vertList.add(v1);
            double[] n = {Math.cos(i * r), 0.0, Math.sin(i * r)};
            normList.add(n);
        }

        for (int i = 3; i < vertList.size(); i += 2) {
            Face face0 = new Face();
            face0.setVerts(new int[] {i-3, i-2, i-1});
            int n = i / 2;
            face0.setNormals(new int[] {n-1, n-1, n});

            Face face1 = new Face();
            face1.setVerts(new int[] {i-2, i, i-1});
            face1.setNormals(new int[] {n-1, n, n});

            faceList.add(face0);
            faceList.add(face1);
        }
    }

    public double[] getPosition() {
        return myPos;
    }

    public void render(GL2 gl) {
        gl.glTranslated(myPos[0], myPos[1], myPos[2]);

        gl.glPushMatrix();
        renderTrunk(gl);
        gl.glPopMatrix();

        gl.glPushMatrix();
        renderLeaves(gl);
        gl.glPopMatrix();
    }

    private void renderTrunk(GL2 gl) {
        float[] brownDiff = {0.64f, 0.16f, 0.16f};
        float[] brownAmb = {0.64f, 0.16f, 0.16f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, brownDiff, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, brownAmb, 0);

        Face nextFace = null;
        Iterator<Face> faceIter = faceList.iterator();
        gl.glBegin(gl.GL_TRIANGLES);
            for (int i = 0; i < leavesVertIndex; i++) {
                nextFace = faceIter.next();
                int[] verts = nextFace.getVerts();
                for (int j = 0; j < verts.length; j++) {
                    double[] normal = normList.get(nextFace.getNormals()[j]);
                    gl.glNormal3d(normal[0], normal[1], normal[2]);
                    double[] vertex = vertList.get(nextFace.getVerts()[j]);
                    gl.glVertex3d(vertex[0], vertex[1], vertex[2]);
                }
            }
        gl.glEnd();
    }

    private void renderLeaves(GL2 gl) {
        float[] greenDiff = {0.2f, 0.8f, 0.2f, 1.0f};
        float[] greenAmb = {0.0f, 0.5f, 0.0f, 1.0f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, greenDiff, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, greenAmb, 0);

        gl.glTranslated(0.0, trunkHeight + leavesRadius - (1 - Math.cos(Math.PI * 1 / leavesSlices)), 0.0);

        Face nextFace = null;
        Iterator<Face> faceIter = faceList.listIterator(leavesFaceIndex);
        gl.glBegin(gl.GL_TRIANGLES);
        while(faceIter.hasNext()) {
            nextFace = faceIter.next();
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
}
