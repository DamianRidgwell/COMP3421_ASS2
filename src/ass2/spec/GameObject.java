package ass2.spec;

import com.jogamp.opengl.GL2;

/**
 * Created by Administrator on 10/7/2016.
 */
public abstract class GameObject extends Mesh{
    protected double[] myPosition;
    protected double[] myHeading;

    protected double height = 0.25;

    public GameObject(double x, double z) {
        super();
        myPosition = new double[]{x, z};
        myHeading = new double[]{0.0, 1.0};
    }

    public double[] getMyHeading() {
        return myHeading;
    }

    public void setMyHeading(double[] myHeading) {
        this.myHeading = myHeading;
    }

    public double[] getMyPosition() {

        return myPosition;
    }

    public void setMyPosition(double[] myPosition) {
        this.myPosition = myPosition;
    }

    /**
     * Rotate the GameObject's heading by the angle provided.
     * @param r angle in degrees
     */
    public void rotateHeading(double r) {
        double currentAngle = Math.atan2(myHeading[0], myHeading[1]);
        currentAngle += r;
        myHeading[0] = Math.sin(currentAngle);
        myHeading[1] = Math.cos(currentAngle);
    }

    public void forward(double d) {
        myPosition[0] = myPosition[0] + myHeading[0] * d;
        myPosition[1] = myPosition[1] + myHeading[1] * d;
    }

    public void backward(double d) {
        forward(d * -1);
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
