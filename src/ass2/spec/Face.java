package ass2.spec;

/**
 * Created by Administrator on 9/27/2016.
 */
public class Face {
    private int[] verts;
    private int[] normals;

    public Face() {
        verts = new int[0];
        normals = new int[0];
    }

    public int[] getNormals() {
        return normals;
    }

    public void setNormals(int[] normals) {
        this.normals = normals;
    }

    public int[] getVerts() {
        return verts;
    }

    public void setVerts(int[] verts) {
        this.verts = verts;
    }
}
