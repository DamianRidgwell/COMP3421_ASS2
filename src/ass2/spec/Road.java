package ass2.spec;

import com.jogamp.opengl.GL2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * COMMENT: Comment Road 
 *
 * @author malcolmr
 */
public class Road extends Mesh {
    private List<Double> myPoints;
    private double myWidth;
    private int numPoints = 10; // number of points on the curve.
    
    /** 
     * Create a new road starting at the specified point
     */
    public Road(double width, double x0, double y0) {
        myWidth = width;
        myPoints = new ArrayList<Double>();
        myPoints.add(x0);
        myPoints.add(y0);
    }

    /**
     * Create a new road with the specified spine 
     *
     * @param width
     * @param spine
     */
    public Road(double width, double[] spine) {
        myWidth = width;
        myPoints = new ArrayList<Double>();
        for (int i = 0; i < spine.length; i++) {
            myPoints.add(spine[i]);
        }
    }

    /**
     * The width of the road.
     * 
     * @return
     */
    public double width() {
        return myWidth;
    }

    /**
     * Add a new segment of road, beginning at the last point added and ending at (x3, y3).
     * (x1, y1) and (x2, y2) are interpolated as bezier control points.
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     */
    public void addSegment(double x1, double y1, double x2, double y2, double x3, double y3) {
        myPoints.add(x1);
        myPoints.add(y1);
        myPoints.add(x2);
        myPoints.add(y2);
        myPoints.add(x3);
        myPoints.add(y3);        
    }
    
    /**
     * Get the number of segments in the curve
     * 
     * @return
     */
    public int size() {
        return myPoints.size() / 6;
    }

    /**
     * Get the specified control point.
     * 
     * @param i
     * @return
     */
    public double[] controlPoint(int i) {
        double[] p = new double[2];
        p[0] = myPoints.get(i*2);
        p[1] = myPoints.get(i*2+1);
        return p;
    }
    
    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     * 
     * @param t
     * @return
     */
    public double[] point(double t) {
        int i = (int)Math.floor(t);
        t = t - i;
        
        i *= 6;
        
        double x0 = myPoints.get(i++);
        double y0 = myPoints.get(i++);
        double x1 = myPoints.get(i++);
        double y1 = myPoints.get(i++);
        double x2 = myPoints.get(i++);
        double y2 = myPoints.get(i++);
        double x3 = myPoints.get(i++);
        double y3 = myPoints.get(i++);
        
        double[] p = new double[2];

        p[0] = b(0, t) * x0 + b(1, t) * x1 + b(2, t) * x2 + b(3, t) * x3;
        p[1] = b(0, t) * y0 + b(1, t) * y1 + b(2, t) * y2 + b(3, t) * y3;        
        
        return p;
    }
    
    /**
     * Calculate the Bezier coefficients
     * 
     * @param i
     * @param t
     * @return
     */
    private double b(int i, double t) {
        
        switch(i) {
        
        case 0:
            return (1-t) * (1-t) * (1-t);

        case 1:
            return 3 * (1-t) * (1-t) * t;
            
        case 2:
            return 3 * (1-t) * t * t;

        case 3:
            return t * t * t;
        }
        
        // this should never happen
        throw new IllegalArgumentException("" + i);
    }


    @Override
    public void render(GL2 gl) {
        if (Game.renderRoads) {
            int asphaltTexID = Game.getInstance().getTexture(Game.ASPHALT_TEX);
            gl.glBindTexture(GL2.GL_TEXTURE_2D, asphaltTexID);

            gl.glPolygonOffset(-1.0f, -1.0f);

            Face nextFace = null;
            Iterator<Face> faceIter = faceList.iterator();
            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
            gl.glBegin(gl.GL_TRIANGLE_STRIP);
                while (faceIter.hasNext()) {
                    nextFace = faceIter.next();
                    int[] verts = nextFace.getVerts();
                    int[] norms = nextFace.getNormals();
                    for (int j = 0; j < verts.length; j++) {
                        double[] normal = normList.get(norms[j]);
                        gl.glNormal3d(normal[0], normal[1], normal[2]);
                        double[] vertex = vertList.get(nextFace.getVerts()[j]);
                        gl.glTexCoord2d(vertex[0] / 3.0, vertex[2] / 3.0);
                        gl.glVertex3d(vertex[0], vertex[1], vertex[2]);
                    }
                }
            gl.glEnd();

            gl.glPolygonOffset(0.0f, 0.0f);

            gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
        }
    }

    @Override
    public void generateMesh(GL2 gl) {
        //the vertices of the cross section of the road.
        double[][] verts = {{0.0, -0.5 * width()},
                            {0.0, 0.5 * width()}};
        double[] n0 = {0.0, 1.0, 0.0};
        getNormList().add(n0);

        double tIncrement = 1.0/numPoints;
        double[][] points = new double[size()*numPoints + 1][2];
        points[0] = controlPoint(0);
        points[size()*numPoints] = controlPoint(size()*3);

        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < numPoints; j++) {
                double t = i + j * tIncrement;
                points[i * numPoints + j] = point(t);
            }
        }

        double[][] vectors = new double[points.length][2]; // an array of the vectors of point[a] -> point[a+1]
        int length = points.length - 1;
        for (int i = 0; i < length; i++) {
            vectors[i][0] = points[i + 1][0] - points[i][0];
            vectors[i][1] = points[i + 1][1] - points[i][1]; // vector i = the difference between points[i+1] and points [i]
        }
        vectors[length] = vectors[length - 1];

        double[][] matTrans = MatrixMath.identity(3);
        double[][] matRot = MatrixMath.identity(3);
        double[] translation = new double[2];
        for (int i = 0; i < points.length; i++) {
            double[] heading = new double[2];
            if (i == 0 || i == length) { // first or last point
                heading = vectors[i];
            } else {
                heading[0] = vectors[i][0] + vectors[i-1][0];
                heading[1] = vectors[i][1] + vectors[i-1][1];
            }
            translation = points[i];

            matTrans = MatrixMath.translationMat2(translation);
            matRot = MatrixMath.rotationMat2(0 - Math.atan2(heading[1], heading[0]));

            for (int j = 0; j < verts.length; j++) {
                double[] vertex = MatrixMath.transform(matRot, verts[j]);
                vertex = MatrixMath.transform(matTrans, vertex);
                vertList.add(new double[]{vertex[0], Terrain.getInstance().altitude(points[i][0], points[i][1]), vertex[1]});
            }
        }

        Iterator<double[]> iter = vertList.iterator();

        int counter = 0;
        double[] v0 = iter.next();
        double[] v1 = iter.next();
        double[] v2;
        while (iter.hasNext()) {
            v2 = iter.next();
            Face face = new Face();
            face.setNormals(new int[]{0, 0, 0});
            face.setVerts(new int[]{counter, counter + 1, counter + 2});
            counter++;
            faceList.add(face);
        }
    }
}