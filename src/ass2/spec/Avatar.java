package ass2.spec;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * Created by Administrator on 10/7/2016.
 */
public class Avatar extends ShaderGameObject {

    public Avatar(double x, double z, String vertShaderPath, String fragShaderPath) {
        super(x, z, vertShaderPath, fragShaderPath);
        height = 0.25;
    }

    @Override
    public void render(GL2 gl) {
        if (Game.renderAvatar) {
            //enableShader(gl);

            gl.glPushAttrib(GL2.GL_COLOR_BUFFER_BIT);
            gl.glPushAttrib(GL2.GL_ENABLE_BIT);

            gl.glPushMatrix();

            gl.glBindTexture(GL2.GL_TEXTURE_2D, Game.getInstance().getTexture(Game.JADE_TEX));
            gl.glTranslated(0.0, height, 0.0); // to make the teapot sit flat on a flat surface.
            double y = Game.getInstance().getAltitude(myPosition[0], myPosition[1]);
            gl.glTranslated(myPosition[0], y, myPosition[1]);
            double angle = Math.atan2(myHeading[1], myHeading[0]);
            gl.glRotated(angle/Math.PI*-180, 0.0, 1.0, 0.0);

            GLUT glut = new GLUT();
            gl.glFrontFace(GL2.GL_CW);
            glut.glutSolidTeapot(height);
            gl.glFrontFace(GL2.GL_CCW);

            gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);

            gl.glPopMatrix();

            gl.glPopAttrib();
            gl.glPopAttrib();

            //disableShader(gl);
        }
    }

    @Override
    public void generateMesh(GL2 gl) {

    }
}
