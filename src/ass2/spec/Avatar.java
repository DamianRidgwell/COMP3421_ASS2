package ass2.spec;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * Created by Administrator on 10/7/2016.
 */
public class Avatar extends GameObject {

    public Avatar(double x, double z) {
        super(x, z);
        height = 0.25;
    }

    @Override
    public void render(GL2 gl) {
        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
        gl.glTranslated(0.0, height, 0.0); // to make the teapot sit flat on a flat surface.
        double y = Game.getInstance().getAltitude(myPosition[0], myPosition[1]);
        gl.glTranslated(myPosition[0], y, myPosition[1]);
        double angle = Math.atan2(myHeading[1], myHeading[0]);
        gl.glRotated(angle/Math.PI*-180, 0.0, 1.0, 0.0);
        GLUT glut = new GLUT();
        glut.glutSolidTeapot(height);
    }

    @Override
    public void generateMesh() {

    }
}
