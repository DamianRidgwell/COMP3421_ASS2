package ass2.spec;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Vector;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;

import static java.awt.event.KeyEvent.*;


/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class Game extends JFrame implements GLEventListener, KeyListener {

    private Terrain myTerrain;
    private Vector<Tree> myTrees;
    private boolean wireframe = false;
    private Camera myCamera;
    private float[] sun;
    private double mouseX, mouseY;

    public Game(Terrain terrain) {
    	super("Assignment 2");
        myTerrain = terrain;
        myCamera = new Camera(myTerrain, 60.0, 1, 40);
        double[] pos = new double[]{0, 3, -5};
        myCamera.setPosition(pos);
        myCamera.setTarget(new double[]{0, -1, 5});
        sun = myTerrain.getSunlight();
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
        game.run();
    }

    @Override
	public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glClearColor(1,1,1,1);
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);

        myCamera.update();

        // enable lighting
        enableLighting(gl);
        renderTerrain(gl);

    }

    private void enableLighting(GL2 gl) {
        float[] globAmb = {0.3f, 0.3f, 0.3f, 1.0f};
        float[] amb = {0.0f, 0.0f, 0.0f, 1.0f};
        float[] difAndSPec = {1.0f, 1.0f, 1.0f, 1.0f};

        //enable one light source
        gl.glEnable(GL2.GL_LIGHT0);

        //light0 properties
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, sun, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, amb, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, difAndSPec, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, difAndSPec, 0);

        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globAmb, 0);  // global ambient lighting
    }

    /**
     *
     * @param gl
     */
    private void renderTerrain(GL2 gl) {
        gl.glPushMatrix();
        Dimension dim = myTerrain.size();
        gl.glColor4d(1, 0, 0, 1);
        if (wireframe) {
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
                myCamera.rotate(0.0, 0.1, 0.0);
                break;
            case VK_RIGHT:
                myCamera.rotate(0.0, -0.1, 0.0);
                break;
            case VK_UP:
                myCamera.forward(0.1);
                break;
            case VK_DOWN:
                myCamera.back(0.1);
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

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
}
