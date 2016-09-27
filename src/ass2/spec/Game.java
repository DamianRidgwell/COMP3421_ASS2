package ass2.spec;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.JFrame;

import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

import static java.awt.event.KeyEvent.*;


/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class Game extends JFrame implements GLEventListener, KeyListener{

    private Terrain myTerrain;
    private boolean wireframe = true;
    private Camera myCamera;

    public Game(Terrain terrain) {
    	super("Assignment 2");
        myTerrain = terrain;
        myCamera = new Camera(myTerrain, 60.0, 1, 40);
        double[] pos = new double[]{0, 3, -5};
        myCamera.setPosition(pos);
        myCamera.setTarget(new double[]{0, -3, 5});
   
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
        renderTerrain(gl);
	}

    /**
     *
     * @param gl
     */
    private void renderTerrain(GL2 gl) {
        Dimension dim = myTerrain.size();
        gl.glColor4d(1, 0, 0, 1);
        if (wireframe) {
            gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_LINE);
        } else {
            gl.glPolygonMode(gl.GL_FRONT, gl.GL_FILL);
        }

        for (int x = 0; x < dim.getWidth() - 1; x++) {
            gl.glBegin(gl.GL_TRIANGLE_STRIP);
                gl.glVertex3d(x, myTerrain.getGridAltitude(x, 0), 0);
                gl.glVertex3d(x + 1, myTerrain.getGridAltitude(x + 1, 0), 0);
                for (int z = 1; z < dim.getHeight(); z++) {
                    gl.glVertex3d(x + 1, myTerrain.getGridAltitude(x + 1, z), z);
                    gl.glVertex3d(x, myTerrain.getGridAltitude(x, z), z);
                }
            gl.glEnd();
        }
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
    }

    @Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL2.GL_DEPTH_TEST);

        // enable lighting
        //gl.glEnable(GL2.GL_LIGHTING);
        //gl.glEnable(GL2.GL_LIGHT0);

        //gl.glEnable(GL2.GL_NORMALIZE);
		
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
                myCamera.rotateY(-0.1);
                break;
            case VK_RIGHT:
                myCamera.rotateY(0.1);
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
}
