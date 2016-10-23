package ass2.spec;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES2;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Created by Administrator on 10/22/2016.
 */
public abstract class ShaderGameObject extends GameObject {
    private int shaderProgramID;

    private String[] vertShaderSource = new String[1];
    private String[] fragShaderSource = new String[1];
    public ShaderGameObject(double x, double z, String vertShaderPath, String fragShaderPath) {
        super(x, z);

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader( new FileInputStream(vertShaderPath)));
            StringWriter writer = new StringWriter();
            String line = reader.readLine();
            while (line != null) {
                writer.write(line);
                writer.write("\n");
                line = reader.readLine();
            }
            reader.close();
            vertShaderSource[0] = writer.getBuffer().toString();

            reader = new BufferedReader(new InputStreamReader( new FileInputStream(fragShaderPath)));
            writer = new StringWriter();
            line = reader.readLine();
            while (line != null) {
                writer.write(line);
                writer.write("\n");
                line = reader.readLine();
            }
            reader.close();
            fragShaderSource[0] = writer.getBuffer().toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getShaderProgramID() {
        return shaderProgramID;
    }

    public void setShaderProgramID(int shaderProgramID) {
        this.shaderProgramID = shaderProgramID;
    }

    public int initShader(GL2 gl) {
        int vertShader = gl.glCreateShader(GL2.GL_VERTEX_SHADER);
        int fragShader = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);

        gl.glShaderSource(vertShader, 1, vertShaderSource, new int[]{vertShaderSource[0].length()}, 0);
        gl.glShaderSource(fragShader, 1, fragShaderSource, new int[]{fragShaderSource[0].length()}, 0);

        gl.glCompileShader(vertShader);
        gl.glCompileShader(fragShader);

        int[] compiled = new int[1];
        gl.glGetShaderiv(vertShader, GL2ES2.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == GL.GL_FALSE) {
            checkLogInfo(gl, vertShader);

            System.err.println("Error compiling vertex shader: " + vertShader);
            System.exit(0);
        }

        gl.glGetShaderiv(fragShader, GL2ES2.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == GL.GL_FALSE) {
            checkLogInfo(gl, fragShader);

            System.err.println("Error compiling fragment shader: " + fragShader);
            System.exit(0);
        }

        shaderProgramID = gl.glCreateProgram();

        gl.glAttachShader(shaderProgramID, vertShader);
        gl.glAttachShader(shaderProgramID, fragShader);
        gl.glLinkProgram(shaderProgramID);
        gl.glValidateProgram(shaderProgramID);

        return shaderProgramID;
    }

    private void checkLogInfo(GL2 gl, int programObject) {
        IntBuffer intValue = Buffers.newDirectIntBuffer(1);
        gl.glGetObjectParameterivARB(programObject, GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB, intValue);

        int lengthWithNull = intValue.get();

        if (lengthWithNull <= 1) {
            return;
        }

        ByteBuffer infoLog = Buffers.newDirectByteBuffer(lengthWithNull);

        intValue.flip();
        gl.glGetInfoLogARB(programObject, lengthWithNull, intValue, infoLog);

        int actualLength = intValue.get();

        byte[] infoBytes = new byte[actualLength];
        infoLog.get(infoBytes);
        System.out.println("GLSL Validation >> " + new String(infoBytes));
    }

    public void enableShader(GL2 gl) {
        gl.glUseProgram(shaderProgramID);
    }

    public void disableShader(GL2 gl) {
        gl.glUseProgram(0);
    }
}
