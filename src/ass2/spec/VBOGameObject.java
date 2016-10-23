package ass2.spec;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.io.*;

import javax.swing.JFrame;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;

/**
 * Created by Administrator on 10/14/2016.
 */
public class VBOGameObject extends ShaderGameObject implements GLEventListener {

    private final double rotatePeriod = 5.0; // number of seconds to complete a rotation for debugging
    private int tick = 0;
    private static int[] bufferIDs = new int[2]; // one buffer for verts/colour/normals one for index
    private static int[] seams = new int[8];

    private static float[] positions;
    private static float[] colours;
    private static float[] normals;
    private static short[] indexes;

    private static int segments = 10;

    private int shaderProgramID;
    private String[] vertShaderSource = new String[1];
    private String[] fragShaderSource = new String[1];


    static ArrayList<Float> outline = new ArrayList<>();
    static ArrayList<Float> outlineNormals = new ArrayList<>();

    public VBOGameObject(double x, double z, String vertShaderPath, String fragShaderPath) {
        super(x, z, vertShaderPath, fragShaderPath);
    }

    private static void createColours() {
        colours = new float[positions.length];
        for (int i = 0; i < colours.length / 3; i++) {
            colours[i] = 1.0f;
            colours[i + 1] = 0.0f;
            colours[i + 2] = 0.0f;
        }
    }

    private static void createIndexes() {
        ArrayList<Short> indexList = new ArrayList<>();
        int segmentLength = outline.size() / 3;
        int vertLength = positions.length / 3;
        for (int i = 0; i < segments; i++) {
            //the base of the piece is a triangle fan connecting the first vert to the surrounding verts.
            short index = (short) ((i * segmentLength) + 1);
            indexList.add(index);
            indexList.add((short) 0);
            index = (short) ((i + 1) * segmentLength + 1);
            if (index > (vertLength - 2)) {
                index = (short) (index - vertLength + 2);
            }
            indexList.add(index);

            for (int j = 1; j < seams.length; j++) {
                for (int k = (i * segmentLength) + seams[j-1] + 1; k < (i * segmentLength) + seams[j]; k++) {
                    index = (short) (k + segmentLength);
                    if (index > vertLength) {
                        index = (short) (index - vertLength + 2);
                    }
                    indexList.add((short) k);
                    indexList.add(index);
                    indexList.add((short) (k + 1));

                    indexList.add(index);
                    indexList.add((short) (index + 1));
                    indexList.add((short) (k + 1));
                }
            }

            // top faces of the piece are also a triangle fan
            indexList.add((short) (vertLength - 1));
            indexList.add((short)(((i + 1) * segmentLength)));
            index = (short) ((i + 2) * segmentLength);
            if (index > vertLength) {
                index = (short) (index - vertLength + 2);
            }
            indexList.add(index);
        }

        indexes = new short[indexList.size()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = indexList.get(i);
        }
    }

    private static void createVerts() {
        //generate the verts, colours, normals and indexes.
        outline.add(3.2071068f);
        outline.add(0.0f);
        outline.add(0.0f);

        outlineNormals.add(0.0f);
        outlineNormals.add(-1.0f);
        outlineNormals.add(0.0f);

        seams[0] = outline.size() / 3;

        float height = 0.5f;
        int numVerts = 3;
        for (int i = 0; i < numVerts + 1; i++) {
            float y = i * height / numVerts;
            float y2 = y * y;
            float x = (float)Math.sqrt(2 * (0.25 - y2)) + 2.5f;
            outline.add(x);
            outline.add(y);
            outline.add(0.0f);

            //calculate normal
            x = (x - 2.5f) / 2;
            float length = (float)Math.sqrt(x * x + y2);
            outlineNormals.add(x / length);
            outlineNormals.add(y / length);
            outlineNormals.add(0.0f);
        }
        seams[1] = outline.size() / 3;

        height = 1.0f;
        numVerts = 4;
        for (int i = 0; i < numVerts + 1; i++) {
            float y = -0.5f + i / (float)numVerts;
            float y2 = y * y;
            float x = (float)Math.sqrt((0.25 - y2));
            outline.add(x + 2.5f);
            outline.add(y + 1.0f);
            outline.add(0.0f);

            float length = (float)Math.sqrt(x * x + y2);
            outlineNormals.add(x / length);
            outlineNormals.add(y / length);
            outlineNormals.add(0.0f);
        }
        seams[2] = outline.size() / 3;

        height = 4.5f;
        numVerts = 5;
        float[] lastNormal = new float[]{0.0f, 0.0f};
        float[] lastPos = new float[]{0.0f, 0.0f};
        for (int i = 0; i < numVerts + 1; i++) {
            float y = 1.5f + i * height / numVerts;
            float y2 = y * y;
            float x = (16f / 105f) * y2 - (31f / 21f) * y + (153f / 35f);
            outline.add(x);
            outline.add(y);
            outline.add(0.0f);

            if (i != 0) {
                float[] v = new float[]{x - lastPos[0], y - lastPos[1]};
                float[] n = new float[]{v[1], 0 - v[0]};
                float length = (float)Math.sqrt(n[0] * n[0] + n[1] * n[1]);
                n[0] /= length;
                n[1] /= length;

                float length2 = (float)(Math.sqrt(lastNormal[0] * lastNormal[0] + lastNormal[1] * lastNormal[1])) + 1.0f;
                outlineNormals.add((n[0] + lastNormal[0]) / length2);
                outlineNormals.add((n[1] + lastNormal[1]) / length2);
                outlineNormals.add(0.0f);

                if (i == numVerts) {
                    outlineNormals.add(n[0]);
                    outlineNormals.add(n[1]);
                    outlineNormals.add(0.0f);
                }
                lastNormal = n;
            }

            lastPos = new float[]{x, y};
        }
        seams[3] = outline.size() / 3;

        outline.add(1.0f);
        outline.add(6.0f);
        outline.add(0.0f);

        outlineNormals.add(0.0f);
        outlineNormals.add(-1.0f);
        outlineNormals.add(0.0f);

        outline.add(1.5f);
        outline.add(6.0f);
        outline.add(0.0f);

        outlineNormals.add(0.0f);
        outlineNormals.add(-1.0f);
        outlineNormals.add(0.0f);

        seams[4] = outline.size() / 3;

        outline.add(1.5f);
        outline.add(6.0f);
        outline.add(0.0f);

        outlineNormals.add(1.0f);
        outlineNormals.add(0.0f);
        outlineNormals.add(0.0f);

        outline.add(1.5f);
        outline.add(6.5f);
        outline.add(0.0f);

        outlineNormals.add(1.0f);
        outlineNormals.add(0.0f);
        outlineNormals.add(0.0f);

        seams[5] = outline.size() / 3;

        outline.add(1.5f);
        outline.add(6.5f);
        outline.add(0.0f);

        //just trust me, it's the normal
        outlineNormals.add(0.581f);
        outlineNormals.add(0.814f);
        outlineNormals.add(0.0f);

        outline.add(0.8f);
        outline.add(7.0f);
        outline.add(0.0f);

        outlineNormals.add(0.581f);
        outlineNormals.add(0.814f);
        outlineNormals.add(0.0f);

        seams[6] = outline.size() / 3;

        height = 3.0f;
        numVerts = 7;
        for (int i = 0; i < numVerts; i++) {
            float y = 7.0f + i * height / (float)numVerts;
            float y2 = (y - 8.29f) * (y - 8.29f);
            float x = (float)Math.sqrt(2.89 - y2);
            outline.add(x);
            outline.add(y);
            outline.add(0.0f);

            float length = (float)Math.sqrt(x * x + y2);
            outlineNormals.add(x / length);
            outlineNormals.add((y - 7.0f) / length);
            outlineNormals.add(0.0f);
        }
        seams[7] = outline.size() / 3;

        //now transform each vert in the outline by a rotation matrix to create the 3D object.
        positions = new float[outline.size() * segments + 6];
        normals = new float[outline.size() * segments + 6];
        int counter = 0;
        int counterN = 0;
        positions[counter++] = 0.0f;
        positions[counter++] = 0.0f;
        positions[counter++] = 0.0f;

        normals[counterN++] = 0.0f;
        normals[counterN++] = -1.0f;
        normals[counterN++] = 0.0f;

        double[][] scale = MatrixMath.scaleMat3(1.0 / 3.207168);

        for (int i = 0; i < segments; i++) {
            double[][] rot = MatrixMath.rotationMat3(0.0, i * 2 * Math.PI / segments, 0.0);
            double[][] rotAndScale = MatrixMath.multiply(rot, scale);
            for (int j = 0; j < outline.size() / 3; j++) {
                double p[] = new double[]{outline.get(j * 3), outline.get(j * 3 + 1), outline.get(j * 3 + 2)};
                double n[] = new double[]{outlineNormals.get(j * 3), outlineNormals.get(j * 3 + 1), outlineNormals.get(j * 3 + 2)};
                p = MatrixMath.transform(rotAndScale, p);
                n = MatrixMath.transform(rot, n);
                positions[counter++] = (float)p[0];
                positions[counter++] = (float)p[1];
                positions[counter++] = (float)p[2];

                normals[counterN++] = (float)n[0];
                normals[counterN++] = (float)n[1];
                normals[counterN++] = (float)n[2];
            }
        }

        //
        double p[] = new double[]{0.0, 10.0, 0.0};
        p = MatrixMath.transform(scale, p);

        positions[counter++] = (float) p[0];
        positions[counter++] = (float) p[1];
        positions[counter++] = (float) p[2];

        normals[counterN++] = 0.0f;
        normals[counterN++] = 1.0f;
        normals[counterN++] = 0.0f;
    }

    public static void main(String [] args) {
        // Initialise OpenGL
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);

        // Create a panel to draw on
        GLJPanel panel = new GLJPanel(caps);

        final JFrame jframe = new JFrame("Triangle");
        jframe.setSize(300, 300);
        jframe.add(panel);
        jframe.setVisible(true);

        // Catch window closing events and quit
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String workingDirectory = System.getProperty("user.dir");
        System.out.println(workingDirectory);
        String fileSeparator = System.getProperty("file.separator");
        String path = workingDirectory + fileSeparator + "src" + fileSeparator + "ass2" + fileSeparator + "spec" + fileSeparator;
        VBOGameObject object = new VBOGameObject(0.0, 0.0, path + "phongVertex.glsl", path + "phongFragment.glsl");

        // add a GL Event listener to handle rendering
        panel.addGLEventListener(object);

        // NEW: add an animator to create display events at 60 FPS
        FPSAnimator animator = new FPSAnimator(60);
        animator.add(panel);
        animator.start();
    }

    public static void generateVBO(GL2 gl) {
        createVerts();
        createIndexes();
        createColours();

        FloatBuffer posBuffer = Buffers.newDirectFloatBuffer(positions);
        FloatBuffer colBuffer = Buffers.newDirectFloatBuffer(colours);
        FloatBuffer norBuffer = Buffers.newDirectFloatBuffer(normals);
        ShortBuffer indBuffer = Buffers.newDirectShortBuffer(indexes);

        gl.glGenBuffers(2, bufferIDs, 0);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferIDs[0]);

        gl.glBufferData(GL2.GL_ARRAY_BUFFER,
                ( positions.length + colours.length + normals.length ) * Float.SIZE / 8,
                null,
                GL2.GL_STATIC_DRAW);

        gl.glBufferSubData(GL2.GL_ARRAY_BUFFER,
                0,
                positions.length * Float.SIZE / 8,
                posBuffer);
        gl.glBufferSubData(GL2.GL_ARRAY_BUFFER,
                positions.length * Float.SIZE / 8,
                colours.length * Float.SIZE / 8,
                colBuffer);
        gl.glBufferSubData(GL2.GL_ARRAY_BUFFER,
                (positions.length + colours.length) * Float.SIZE / 8,
                normals.length * Float.SIZE / 8,
                norBuffer);

        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferIDs[1]);

        gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER,
                indexes.length * Short.SIZE / 8,
                indBuffer,
                GL2.GL_STATIC_DRAW);

    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glCullFace(GL2.GL_BACK);

        this.setShaderProgramID(initShader(gl));

        generateVBO(gl);
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

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glDeleteBuffers(1,bufferIDs,0);
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();


        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);


        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        enableLighting(gl);

        gl.glRotated(tick++ * 10/rotatePeriod, 1.0, 0.0, 0.0);

        render(gl);

    }

    private void enableLighting(GL2 gl) {
        float[] globAmb = {0.1f, 0.1f, 0.1f, 1.0f};
        float[] amb = {0.0f, 0.0f, 0.0f, 1.0f};
        float[] dif = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] spec = {1.0f, 1.0f, 1.0f, 1.0f};

        //enable one light source
        gl.glEnable(GL2.GL_LIGHT0);



        //light0 properties
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, new float[]{1.0f, 0.0f, 0.0f}, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, amb, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, dif, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, spec, 0);

        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globAmb, 0);  // global ambient lighting
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        //Sometimes good to use glOrtho for developing and debugging
        gl.glOrtho(-4, 4, -4, 4, -5, 20);
    }

    @Override
    public void render(GL2 gl) {
        double y = Game.getInstance().getAltitude(myPosition[0], myPosition[1]);
        gl.glTranslated(myPosition[0], y, myPosition[1]);

        gl.glBindTexture(GL.GL_TEXTURE_2D, Game.getInstance().getTexture(Game.MARBLE_TEX));

        //enableShader(gl);
        // Material property vectors.

        float matAmbAndDif2[] = {0.0f, 0.9f, 0.0f, 1.0f};
        float matSpec[] = { 1.0f, 1.0f, 1.0f, 1.0f };
        float matShine[] = { 50.0f };

        // Material property vectors.
        float matAmbAndDif1[] = {1.0f, 1.0f, 1.0f, 1.0f};

        // Material properties.
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, matAmbAndDif1,0);
        gl.glMaterialfv(GL2.GL_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, matAmbAndDif2,0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, matSpec,0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, matShine,0);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferIDs[0]);

        // Enable three vertex arrays: coordinates, color and normals
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);

        gl.glVertexPointer(3,
                GL.GL_FLOAT,
                0,
                0);
        gl.glColorPointer(3, GL.GL_FLOAT, 0,
                positions.length*Float.SIZE / 8);
        gl.glNormalPointer(GL.GL_FLOAT, 0,
                (positions.length + colours.length) * Float.SIZE / 8);

        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferIDs[1]);

        //Draw using your indexes
        gl.glDrawElements(GL2.GL_TRIANGLES, indexes.length, GL2.GL_UNSIGNED_SHORT, 0);

        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER,0);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER,0);

        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        //disableShader(gl);
    }

    @Override
    public void generateMesh(GL2 gl) {
    }
}
