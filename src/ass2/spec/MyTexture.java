package ass2.spec;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

/**
 * Created by Administrator on 10/6/2016.
 */
public class MyTexture {
    private int textureID;
    private boolean mipMapEnabled;

    public MyTexture(GL2 gl, String filename, String extension, boolean mipmaps, int id) {
        textureID = id;
        mipMapEnabled = true;
        TextureData data = null;
        try {
            File file = new File(filename);
            BufferedImage img = ImageIO.read(file);
            ImageUtil.flipImageVertically(img);

            data = AWTTextureIO.newTextureData(GLProfile.getDefault(), img, false);

        } catch (IOException e) {
            System.err.println(filename);
            e.printStackTrace();
            System.exit(1);
        }


        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        gl.glBindTexture(GL.GL_TEXTURE_2D, id);

        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0,
                data.getInternalFormat(),
                data.getWidth(),
                data.getHeight(),
                0,
                data.getPixelFormat(),
                data.getPixelType(),
                data.getBuffer());

        setFilters(gl);
    }

    private void setFilters(GL2 gl){
        // Build the texture from data.
        if (mipMapEnabled) {
            // Set texture parameters to enable automatic mipmap generation and bilinear/trilinear filtering
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);

            float fLargest[] = new float[1];
            gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, fLargest,0);
            gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, fLargest[0]);
            gl.glGenerateMipmap(GL2.GL_TEXTURE_2D);
        } else {
            // Set texture parameters to enable bilinear filtering.
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        }
    }

    public int getTextureId() {
        return textureID;
    }
}
