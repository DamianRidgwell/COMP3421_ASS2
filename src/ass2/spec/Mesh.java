package ass2.spec;

import com.jogamp.opengl.GL2;

import java.util.*;

/**
 * Created by Administrator on 10/4/2016.
 */
public abstract class Mesh {
    protected Vector<double[]> vertList;
    protected Vector<double[]> normList;
    protected Vector<Face> faceList;

    public Mesh() {
        vertList = new Vector<>();
        normList = new Vector<>();
        faceList = new Vector<>();
    }

    public Vector<Face> getFaceList() {
        return faceList;
    }

    public void setFaceList(Vector<Face> faceList) {
        this.faceList = faceList;
    }

    public Vector<double[]> getVertList() {
        return vertList;
    }

    public void setVertList(Vector<double[]> vertList) {
        this.vertList = vertList;
    }

    public Vector<double[]> getNormList() {
        return normList;
    }

    public void setNormList(Vector<double[]> normList) {
        this.normList = normList;
    }

    public abstract void render(GL2 gl);
}
