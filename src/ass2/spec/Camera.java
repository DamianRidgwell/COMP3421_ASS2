package ass2.spec;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;

/**
 * Created by Administrator on 9/21/2016.
 */
public class Camera {
    private double fov = 60.0;
    private double near, far;
    Terrain theTerrain;

    private boolean thirdPerson = true;

    private GameObject target;
    private final double[] thirdPersonOffset = {0.0, 1.5, 0.0};

    private Camera() {
    }

    public Camera(Terrain t, double fov, double n, double f, GameObject target) {
        this.fov = fov;
        near = n;
        far = f;
        this.target = target;
        theTerrain = t;
    }

    public void init(GL2 gl, double width, double height) {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        GLU glu = new GLU();

        glu.gluPerspective(fov, (float)width/(float)height, near, far);
    }

    public void update() {
        double[] at = target.getMyHeading();
        double[] pos = new double[3];
        double[] up = new double[]{0.0, 1.0, 0.0};
        double[] targetPos = target.getMyPosition();
        double altitude = Game.getInstance().getAltitude(targetPos[0], targetPos[1]);
        pos[0] = targetPos[0];
        pos[1] = altitude;
        pos[2] = targetPos[1];
        if (thirdPerson) {
            pos[0] += thirdPersonOffset[0] - at[0] * 3.0;
            pos[1] += thirdPersonOffset[1];
            pos[2] += thirdPersonOffset[2] - at[1] * 3.0;
        } else {
            pos[1] += target.getHeight();
        }
        GLU glu = new GLU();
        glu.gluLookAt(pos[0], pos[1], pos[2], pos[0] + at[0], pos[1], pos[2] + at[1], up[0], up[1], up[2]);
    }

    public GameObject getTarget() {
        return target;
    }

    public void setTarget(GameObject object) {
        this.target = object;
    }

    private double[] getHeading() {
        return target.getMyHeading();
    }

    public boolean isThirdPerson() {
        return thirdPerson;
    }

    public void setThirdPerson(boolean thirdPerson) {
        this.thirdPerson = thirdPerson;
    }

    public void toggleMode() {
        thirdPerson = !thirdPerson;
    }
}
