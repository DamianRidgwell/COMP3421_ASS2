package ass2.spec;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import static java.awt.event.KeyEvent.*;


/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class Game extends JFrame implements GLEventListener, KeyListener {

    private Terrain myTerrain;
    public static boolean wireframeMode, renderTerrain, renderTrees, renderRoads, renderAvatar, quickTime, nightMode, movingSun;
    private Camera myCamera;
    private Avatar myAvatar;
    private float[] sun;
    private static Game instance;
    public static final int CHESSBOARD_TEX = 0;
    public static final int ASPHALT_TEX = 1;
    public static final int JADE_TEX = 2;
    public static final int STONE_TEX = 3;
    public static final int MARBLE_TEX = 4;
    private long time;
    private long dayLength = 60 * 1000; // number of millis in a game 'day'
    private double sunDistance = 50.0;

    private int[] myTextures;

    public static String workingDir = System.getProperty("user.dir");
    public static String fileSeparator = System.getProperty("file.separator");
    public static String vertexShaderName = "phongVertexShader.glsl";
    public static String fragmentShaderName = "phongFragmentShader.glsl";
    private String texturesDir = workingDir + fileSeparator + "Textures" + fileSeparator;
    private String chessboardImageName =  texturesDir + "chessboard.png";
    private String asphaltImageName = texturesDir + "cobbles.png";
    private String jadeImageName = texturesDir + "jade.png";
    private String stoneImageName = texturesDir + "stone.png";
    private String marbleImageName = texturesDir + "marble.png";

    private String textureExtName = "png";

    private float[] sunsetRed = new float[] {0.4f, 0.0f, 0.0f, 1.0f};
    private float[] sunsetOrange = new float[] {0.8f, 0.4f, 0.0f, 1.0f};
    private float[] fullSun = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
    private double sunsetStage1Angle = Math.PI * 0.15;
    private double sunsetStage2Angle = Math.PI * 0.05;



    public Game(Terrain terrain) {
    	super("Assignment 2");

        myTerrain = terrain;

        //paths for shader source
        String path = workingDir + fileSeparator + "src" + fileSeparator + "ass2" + fileSeparator + "spec" + fileSeparator;

        myAvatar = new Avatar(0.0, 0.0, path + vertexShaderName, path + fragmentShaderName);
        myCamera = new Camera(myTerrain, 60.0, 0.1, 100.0, myAvatar);

        sun = myTerrain.getSunlight();

        wireframeMode = false;
        renderTerrain = renderTrees = renderRoads = renderAvatar = true;
        quickTime = nightMode = movingSun = false;

        time = System.currentTimeMillis();
    }

    public boolean isMovingSun() {
        return movingSun;
    }

    /**
     * Run the game.
     *
     */
    public void run() {
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLJPanel panel = new GLJPanel();
        panel.addGLEventListener(this);
        panel.addKeyListener(this);
        panel.setFocusable(true);

        // Add an animator to call 'display' at 60fps
        FPSAnimator animator = new FPSAnimator(60);
        animator.add(panel);
        animator.start();

        getContentPane().add(panel);
        setSize(800, 600);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Load a level file and display it.
     *
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File(args[0]));
        Game game = new Game(terrain);
        setInstance(game);
        game.run();
    }

    @Override
	public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glClearColor(0.7f, 0.7f, 0.7f, 1.0f);
        if (nightMode && movingSun) {
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        }
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);

        myCamera.update();

        // enable lighting
        enableLighting(gl);
        renderSun(gl);
        setMaterials(gl); // sets standard materials
        renderTerrain(gl);
        if (myCamera.isThirdPerson()) {
            renderAvatar(gl);
        }

    }

    private void renderSun(GL2 gl) {
        float matEmi[] = getSunColour();
        if (nightMode && movingSun) {
            matEmi = new float[] {0.9f, 0.9f, 1.0f, 1.0f};
        }
        float oldMatEmi[] = new float[4];
        gl.glGetMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, oldMatEmi, 0);
        double[] pos = new double[] {sun[0] * sunDistance, sun[1] * sunDistance, sun[2] * sunDistance};
        GLUT glut = new GLUT();
        gl.glPushMatrix();
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, matEmi, 0);
        gl.glTranslated(pos[0], pos[1], pos[2]);
        glut.glutSolidSphere(1.0, 10, 10);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, oldMatEmi, 0);
        gl.glPopMatrix();
    }

    private void renderAvatar(GL2 gl) {
        gl.glPushMatrix();
        myAvatar.render(gl);
        gl.glPopMatrix();
    }

    private void setMaterials(GL2 gl) {
        // Material property vectors.
        float matAmbAndDif2[] = {0.0f, 0.9f, 0.0f, 1.0f};
        float matSpec[] = { 1.0f, 1.0f, 1.0f, 1.0f };
        float matShine[] = { 50.0f };

        // Material property vectors.
        float matDif1[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float matAmb1[] = {0.5f, 0.5f, 0.5f, 1.0f};;

        // Material properties.
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, matDif1,0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, matAmb1,0);
        gl.glMaterialfv(GL2.GL_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, matAmbAndDif2,0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, matSpec,0);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, matShine,0);
    }

    private void enableLighting(GL2 gl) {
        gl.glShadeModel(GL2.GL_SMOOTH);

        float[] globAmb = {0.1f, 0.1f, 0.1f, 1.0f};
        float[] amb = {0.1f, 0.1f, 0.1f, 1.0f};
        float[] spec = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] dif = getSunColour();

        //enable one light source
        gl.glEnable(GL2.GL_LIGHT0);

        //update sun position
        updateSun();

        //light0 properties
        float[] sun4 = new float[4];
        System.arraycopy(sun, 0, sun4, 0, 3);
        sun4[3] = 0;
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, sun, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, amb, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, dif, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, spec, 0);

        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globAmb, 0);  // global ambient lighting

        if (nightMode && movingSun) {
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, dif, 0);
            enableTorch(gl);
        } else {
            gl.glDisable(GL2.GL_LIGHT1);
        }
    }

    private void enableTorch(GL2 gl) {
        float[] torchDif = {0.93f, 1.0f, 0.47f, 1.0f};
        float[] torchSpec = {1.0f, 1.0f, 1.0f, 1.0f};
        double[] heading = myAvatar.getMyHeading();
        double[] position = myAvatar.getMyPosition();
        float[] torchDir = {(float) heading[0], 0.0f, (float) heading[1], 1.0f};
        float[] torchPos = {(float) position[0], (float) (getAltitude(position[0], position[1]) + (myAvatar.height / 2)), (float) position[1], 1.0f};

        gl.glEnable(GL2.GL_LIGHT1);
        gl.glLightf(GL2.GL_LIGHT1, GL2.GL_SPOT_CUTOFF, 45);
        gl.glLightf(GL2.GL_LIGHT1, GL2.GL_SPOT_EXPONENT, 4);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPOT_DIRECTION, torchDir, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, torchPos, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, torchDif, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, torchSpec, 0);
        gl.glLightf(GL2.GL_LIGHT1, GL2.GL_LINEAR_ATTENUATION, 0.3f);
    }

    private float[] getSunColour() {
        float[] dif = new float[4];
        System.arraycopy(fullSun, 0, dif, 0, dif.length);
        if (movingSun) {
            float sunAngle = (float) Math.atan2(sun[1], sun[0]);
            if (sunAngle < sunsetStage2Angle) {
                sunAngle /= sunsetStage2Angle;
                for (int i = 0; i < 3; i++) {
                    dif[i] = sunsetRed[i] * (1.0f - sunAngle) + sunsetOrange[i] * (sunAngle);
                }
            } else if (sunAngle < sunsetStage1Angle) {
                sunAngle = (float) ((sunAngle - sunsetStage2Angle) / (sunsetStage1Angle - sunsetStage2Angle));
                for (int i = 0; i < 3; i++) {
                    dif[i] = sunsetOrange[i] * (1.0f - sunAngle) + fullSun[i] * (sunAngle);
                }
            } else if (sunAngle > Math.PI - sunsetStage2Angle) {
                sunAngle = (float) (Math.PI - sunAngle);
                sunAngle /= sunsetStage2Angle;
                for (int i = 0; i < 3; i++) {
                    dif[i] = sunsetRed[i] * (1.0f - sunAngle) + sunsetOrange[i] * (sunAngle);
                }
            } else if (sunAngle > Math.PI - sunsetStage1Angle) {
                sunAngle = (float) (Math.PI - sunAngle);
                sunAngle = (float) ((sunAngle - sunsetStage2Angle) / (sunsetStage1Angle - sunsetStage2Angle));
                for (int i = 0; i < 3; i++) {
                    dif[i] = sunsetOrange[i] * (1.0f - sunAngle) + fullSun[i] * (sunAngle);
                }
            }

            if (nightMode) {
                dif = new float[] {0.1f, 0.1f, 0.2f, 1.0f};
            }
        }
        return dif;
    }

    private void updateSun() {
        long newTime = System.currentTimeMillis();
        double delta = newTime - time;
        time = newTime;

        if (movingSun) {
            if (quickTime) {
                delta *= 5;
            }
            double dayFraction = delta / dayLength; //fraction of a day that this delta equals
            double sunAngle = dayFraction * Math.PI;

            double[] sun2 = MatrixMath.transform(MatrixMath.rotationMat3(0.0, 0.0, sunAngle), new double[]{sun[0], sun[1], sun[2]});
            sun = new float[]{(float) sun2[0], (float) sun2[1], (float) sun2[2]};

            //if sun has dropped below horizon
            if (sun[1] < 0) {
                nightMode = !nightMode;
                sun[0] = 0 - sun[0];
                sun[1] = 0 - sun[1];
            }
        }
    }

    /**
     *
     * @param gl
     */
    private void renderTerrain(GL2 gl) {
        gl.glPushMatrix();
        Dimension dim = myTerrain.size();
        gl.glColor4d(1, 0, 0, 1);
        if (wireframeMode) {
            gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_LINE);
        } else {
            gl.glPolygonMode(gl.GL_FRONT, gl.GL_FILL);
        }

        myTerrain.render(gl);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        gl.glPopMatrix();
    }

    @Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL2.GL_DEPTH_TEST);

        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glCullFace(GL2.GL_BACK);

        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_NORMALIZE);

        gl.glEnable(GL2.GL_TEXTURE_2D);

        gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);

        myTextures = new int[5];
        gl.glGenTextures(5, myTextures, 0);
        myTextures[0] = new MyTexture(gl, chessboardImageName, textureExtName, true, myTextures[0]).getTextureId();
        myTextures[1] = new MyTexture(gl, asphaltImageName, textureExtName, true, myTextures[1]).getTextureId();
        myTextures[2] = new MyTexture(gl, jadeImageName, textureExtName, true, myTextures[2]).getTextureId();
        myTextures[3] = new MyTexture(gl, stoneImageName, textureExtName, true, myTextures[3]).getTextureId();
        myTextures[4] = new MyTexture(gl, marbleImageName, textureExtName, true, myTextures[4]).getTextureId();

        myAvatar.initShader(gl);
        VBOGameObject.generateVBO(gl);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
        myCamera.init(drawable.getGL().getGL2(), width, height);
	}

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case VK_LEFT:
                myAvatar.rotateHeading(0.1);
                break;
            case VK_RIGHT:
                myAvatar.rotateHeading(-0.1);
                break;
            case VK_UP:
                myAvatar.forward(0.1);
                break;
            case VK_DOWN:
                myAvatar.backward(0.1);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case VK_1:
                renderTerrain = !renderTerrain;
                break;
            case VK_2:
                renderTrees = !renderTrees;
                break;
            case VK_3:
                renderAvatar = !renderAvatar;
                break;
            case VK_4:
                renderRoads = !renderRoads;
                break;
            case VK_Q:
                quickTime = !quickTime;
                break;
            case VK_SPACE:
                myCamera.toggleMode();
                break;
            case VK_S:
                movingSun = !movingSun;
                break;
        }
    }

    public static double vectorLength(double[] v) {
        double sum = 0.0;
        for (int i = 0; i < v.length; i++) {
            sum += v[i] * v[i];
        }
        return Math.sqrt(sum);
    }

    public static double[] normaliseVector(double[] v) {
        double length = vectorLength(v);
        for (int i = 0; i < v.length; i++) {
            v[i] /= length;
        }
        return v;
    }

    public static double lerp(double x, double x1, double x2, double y1, double y2) {
        return ((x2 - x)/(x2 - x1) * y1) + ((x - x1)/(x2 - x1) * y2);
    }

    public static double vectorLength(double d1, double d2) {
        return Math.sqrt(d1*d1 + d2*d2);
    }

    public static boolean isInsideTri(double px, double pz, double ax, double az, double bx, double bz, double cx, double cz) {
        double[] w = bccCoords(px, pz, ax, az, bx, bz, cx, cz);
        double u = w[0];
        double v = w[1];

        // Check if point is in triangle include points on the line of the triangle
        return (u >= 0) && (v >= 0) && (u + v <= 1);
    }

    public static Double vecDot(double[] v0, double[] v1) {
        if (v0.length != v1.length) {
            return null;
        }
        Double sum = 0.0;
        for (int i = 0; i < v0.length; i++) {
            sum += v0[i] * v1[i];
        }
        return sum;
    }

    //given three points defining a triange and an arbitrary point, calculate u and v such that
    //u * v0 + v* v1 = P where v0 and v1 are two sides of the triangle.
    public static double[] bccCoords(double px, double pz, double ax, double az, double bx, double bz, double cx, double cz) {
        // Compute vectors
        // v0 = C - A
        double[] v0 = {cx - ax, cz - az};
        // v1 = B - A
        double[] v1 = {bx - ax, bz - az};
        // v2 = P - A
        double[] v2 = {px - ax, pz - az};

        // Compute dot products
        double dot00 = vecDot(v0, v0);
        double dot01 = vecDot(v0, v1);
        double dot02 = vecDot(v0, v2);
        double dot11 = vecDot(v1, v1);
        double dot12 = vecDot(v1, v2);

        // Compute barycentric coordinates
        double invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
        double u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        double v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        return new double[]{u, v};
    }

    public static double[] rotateVector(double[] v, double rX, double rY, double rZ) {
        double length = vectorLength(v);
        if (rX != 0.0) {
            v[1] = v[1] * Math.cos(rX) - v[2] * Math.sin(rX);
            v[2] = v[1] * Math.sin(rX) + v[2] * Math.cos(rX);
        }
        if (rY != 0.0) {
            v[0] = v[0] * Math.cos(rY) + v[2] * Math.sin(rY);
            v[2] = v[2] * Math.cos(rY) - v[0] * Math.sin(rY);
        }
        if (rZ != 0.0) {
            v[0] = v[0] * Math.cos(rZ) - v[1] * Math.sin(rZ);
            v[1] = v[0] * Math.sin(rZ) + v[1] * Math.cos(rZ);
        }

        if (vectorLength(v) != length) {
            v = normaliseVector(v);
            v[0] = v[0] * length;
            v[1] = v[1] * length;
            v[2] = v[2] * length;
        }
        return v;
    }

    public static double[] createVector(double[] a, double[] b) {
        double[] ab = new double[3];
        ab[0] = b[0] - a[0];
        ab[1] = b[1] - a[1];
        ab[2] = b[2] - a[2];

        return ab;
    }

    public static double[] createNormal(double[] a, double[] b) {
        //AxB = (AyBz − AzBy, AzBx − AxBz, AxBy − AyBx)
        double[] normal = new double[3];
        normal[0] = a[1] * b[2] - a[2] * b[1];
        normal[1] = a[2] * b[0] - a[0] * b[2];
        normal[2] = a[0] * b[1] - a[1] * b[0];

        return Game.normaliseVector(normal);
    }

    public int getTexture(int id) {
        return myTextures[id];
    }

    public static Game getInstance() {
        return instance;
    }

    private static void setInstance(Game instance) {
        Game.instance = instance;
    }

    public double getAltitude(double x, double z) {
        return myTerrain.altitude(x, z);
    }

    public boolean isNightMode() {
        return nightMode;
    }
}
