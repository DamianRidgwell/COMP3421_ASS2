package ass2.spec;

import com.jogamp.opengl.*;
import com.jogamp.opengl.glu.GLU;

/**
 * Created by Administrator on 9/21/2016.
 */
public class Camera {
    private double fov = 60.0;
    private double near, far;
    private double[] position;
    private double[] target;
    Terrain theTerrain;

    private Camera() {
    }

    public Camera(Terrain t, double fov, double n, double f) {
        this.fov = fov;
        near = n;
        far = f;
        position = new double[]{0, 0, 0};
        target = new double[]{0, 0, 0};
        theTerrain = t;
    }

    public void init(GL2 gl, double width, double height) {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        GLU glu = new GLU();

        glu.gluPerspective(fov, (float)width/(float)height, near, far);
    }

    public void update() {
        double altitude = theTerrain.altitude(position[0], position[2]);
        GLU glu = new GLU();
        glu.gluLookAt(position[0], position[1] + altitude, position[2], position[0] + target[0], position[1] + target[1] + altitude, position[2] + target[2], 0, 1, 0);
    }

    public double[] getPosition() {
        return position;
    }

    public void setPosition(double[] position) {
        this.position = position;
    }

    public double[] getTarget() {
        return target;
    }

    public void setTarget(double[] target) {
        this.target = target;
    }

    public double getFov() {
        return fov;
    }

    public void setFov(double fov) {
        this.fov = fov;
    }

    public double getNear() {
        return near;
    }

    public void setNear(double near) {
        this.near = near;
    }

    public double getFar() {
        return far;
    }

    public void setFar(double far) {
        this.far = far;
    }

    public double getX() {
        return position[0];
    }

    public void setX(double x) {
        position[0] = x;
    }

    public double getY() {
        return position[1];
    }

    public void setY(double y) {
        position[1] = y;
    }

    public double getZ() {
        return position[2];
    }

    public void setZ(double z) {
        position[2] = z;
    }

    public void forward(double v) {
        double[] heading = getHeading();
        position[0] += heading[0] * v;
        position[2] += heading[1] * v;
    }

    public void back(double v) {
        double[] heading = getHeading();
        position[0] += heading[0] * v * -1;
        position[2] += heading[1] * v * -1;
    }

    private double[] getHeading() {
        double[] heading = new double[]{target[0], target[2]};
        double vLength = Game.vectorLength(heading[0], heading[1]);
        heading[0] = heading[0] / vLength;
        heading[1] = heading[1] / vLength;

        return heading;
    }

    public void rotate(double rX, double rY, double rZ) {
        target = Game.rotateVector(target, rX, rY, rZ);
    }
}
