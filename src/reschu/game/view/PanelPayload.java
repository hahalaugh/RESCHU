package reschu.game.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.RenderingHints;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;

import static javax.media.opengl.GL.*;

import com.sun.opengl.util.j2d.*;
import com.sun.opengl.util.texture.*;

import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.timing.interpolation.*;

import reschu.constants.MyGame;
import reschu.constants.MyURL;
import reschu.game.controller.GUI_Listener;
import reschu.game.controller.Reschu;
import reschu.game.model.Game;
import reschu.game.model.Payload;
import reschu.game.model.Vehicle;
import reschu.game.utils.FileReader;
import reschu.game.utils.Utils;
import reschu.game.view.PanelMsgBoard.MessageType;

/**
 * @author Carl Nehme Code modified by yale.
 */
public class PanelPayload extends MyCanvas implements GLEventListener {

    private static final long serialVersionUID = -6487171440210682586L;
    private static final boolean GL_DEBUG = false;
    private static final boolean USE_POPUP = false;

    private static final GLU glu = new GLU();
    private GLJPanel glCanvas;
    private TextureRenderer animRenderer;
    private Animator changing_view;
    private Animator changing_x;
    private Animator changing_y;

    private boolean initialLoading = false;

    private TextRenderer trB24;

    // rivate Random rnd = new Random();
    private BufferedImage img;
    private Game g;
    private GUI_Listener lsnr;
    // private PayloadList payload_list;
    private java.awt.event.MouseEvent mouseEvt;
    private int GL_width, GL_height;

    private final int bogus_pxl_width_and_height = 600;
    private int pxl_width = bogus_pxl_width_and_height; // the width of payload
							// image
    private int pxl_height = bogus_pxl_width_and_height;// the height of payload
							// image

    private float bezierAlpha = 1f;
    private float imageX;
    private float imageY;
    private ArrayList<Integer> taskSequenceList;
    private static int taskCursor;
    public boolean imageReplaced = false;

    public synchronized float getImageX() {
	return imageX;
    }

    public synchronized void setImageX(float imageX) {
	this.imageX = imageX;
    }

    public synchronized float getImageY() {
	return imageY;
    }

    public synchronized void setImageY(float imageY) {
	this.imageY = imageY;
    }

    private float new_x_off;
    private float new_y_off;
    private float x_dist;
    private float y_dist;
    private float camera_x;
    private float camera_y;
    private float camera_height;

    private double zoom_angle_off;
    private double rotate_angle;
    private double CAMERA_ANGLE;

    private int zoom_count;
    private Vehicle v;

    public synchronized Vehicle getVehicle() {
	return v;
    }

    public Payload curPayload;
    // private float x_limit = (float) rnd.nextInt(10);
    // private float y_limit = (float) rnd.nextInt(10);
    // private boolean penalize;
    private boolean enabled = false;
    private boolean isControlEnabled = false;
    private boolean correct = false;
    private boolean screenBlackedAfterPayloadDone;
    private JPopupMenu popMenu;
    private JMenuItem mnuSubmit, mnuCancel;

    private double min_x, max_x, min_y, max_y;
    private boolean rbtnClicked = false;
    private boolean preSelectionRect = true;
    private int clickedX, clickedY;

    private int viewport[] = new int[4];
    private double mvmatrix[] = new double[16];
    private double projmatrix[] = new double[16];
    private double wcoord[] = new double[4];
    private double wcoord1[] = new double[4];
    private double wcoord2[] = new double[4];
    private double wcoord3[] = new double[4];

    // private int x_direction = 2;
    // private int y_direction = 2;

    private FloatBuffer frameBuffer = ByteBuffer.allocateDirect(4)
	    .order(ByteOrder.nativeOrder()).asFloatBuffer();
    private Boolean Image_Loading;
    public Boolean Image_Checking;
    private float flash;

    private JButton btnSubmit, btnCancel;

    public PanelPayload(GUI_Listener e, String strTitle,
	    GLJPanel payload_canvas, Game g) {
	if (GL_DEBUG)
	    System.out.println("GL: PanelPayload created");

	lsnr = e;
	glCanvas = payload_canvas;

	// payload_list = g.getPayloadList();
	Image_Loading = false;
	Image_Checking = false;
	flash = 0;

	glEnabled(false);
	glControlEnabled(false);

	setPopup();
	initTextRenenders();
	makeVibrateThread();

	initializeTaskSequence();

	glCanvas.setLayout(null);
	this.g = g;

    }

    private void initializeTaskSequence() {

	FileReader fr = new FileReader(MyURL.URL_TASK_SEQUENCE);
	String aLine = "";
	fr.openFile();

	if ((aLine = fr.readLineByLine()) != null) {
	    String a[] = aLine.split(",");
	    this.taskSequenceList = new ArrayList<Integer>();
	    for (String s : a) {
		taskSequenceList.add(Integer.parseInt(s));
	    }
	}

	fr.closeFile();
    }

    private void initTextRenenders() {
	new TextRenderer(new Font("SansSerif", Font.PLAIN, 14), true, false);
	new TextRenderer(new Font("SansSerif", Font.BOLD, 12), true, false);
	new TextRenderer(new Font("SansSerif", Font.BOLD, 17), true, false);
	new TextRenderer(new Font("SansSerif", Font.BOLD, 20), true, false);
	trB24 = new TextRenderer(new Font("SansSerif", Font.BOLD, 24), true,
		false);
    }

    // A thread for making the screen vibrate
    private Random rnd = new Random();
    private float x_limit = (float) rnd.nextInt(10);
    private float y_limit = (float) rnd.nextInt(10);
    private int x_direction = 2;
    private int y_direction = 2;

    private void makeVibrateThread() {
	new Thread(new Runnable() {
	    public void run() {
		while (true) {
		    try {
			Thread.sleep(50);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }

		    x_dist += x_direction * ((float) rnd.nextGaussian() + 2);
		    y_dist += y_direction * ((float) rnd.nextGaussian() + 2);

		    if (x_direction > 0) {
			if (x_dist > x_limit) {
			    x_limit = 6 * (float) rnd.nextInt(3);
			    x_direction = -x_direction;
			}
		    } else {
			if (x_dist < -x_limit) {
			    x_limit = 6 * (float) rnd.nextInt(3);
			    x_direction = -x_direction;
			}
		    }

		    if (y_direction > 0) {
			if (y_dist > y_limit) {
			    y_limit = 6 * (float) rnd.nextInt(3);
			    y_direction = -y_direction;
			}
		    } else {
			if (y_dist < -y_limit) {
			    y_limit = 6 * (float) rnd.nextInt(3);
			    y_direction = -y_direction;
			}
		    }
		    flash = (float) (flash + 0.1) % 2;
		    // rotate_angle = rotate_angle + 1;
		    glCanvas.display();
		}
	    }
	}).start();
    }

    /**
     * Called by the drawable immediately after the OpenGL context is
     * initialized.
     */
    public void init(GLAutoDrawable drawable) {
	if (GL_DEBUG)
	    System.out.println("GL: init called");
	GL gl = drawable.getGL();
	gl.setSwapInterval(0);
	gl.glEnable(GL_DEPTH_TEST);
	gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

	initAnimRenderer();
	updateAnimRenderer();
    }

    /**
     * Called by the drawable to initiate OpenGL rendering by the client.
     */
    int selectionBlink = 0;

    public synchronized void display(GLAutoDrawable drawable) {
	if (GL_DEBUG)
	    System.out.println("GL: display called");
	if (!isEnabled() && screenBlackedAfterPayloadDone)
	    return;

	GL gl = drawable.getGL();
	gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	gl.glMatrixMode(GL_MODELVIEW);
	gl.glLoadIdentity();

	gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
	gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, mvmatrix, 0);
	gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projmatrix, 0);

	glu.gluLookAt(camera_x + getImageX() + x_dist, // eyeX
		camera_y + getImageY() + y_dist, // eyeY
		camera_height, // eyeZ
		getImageX() + x_dist, // centerX
		getImageY() + y_dist, // centerY
		0f, // centerZ
		(float) (Math.sin(Utils.degreesToRadians(rotate_angle))), // upX
		(float) (Math.cos(Utils.degreesToRadians(rotate_angle))), // upY
		0.0f); // upZ
	// System.out.println("@@@@@@@@@@@@" + getImageX() + "," + getImageY());
	displayAnimRenderer(drawable);
	displayText(drawable);

	unproj(gl, (int) (viewport[2] / 2), (int) (viewport[3] / 2), wcoord1);
	unproj(gl, 1, 1, wcoord2);

	double half_width = wcoord1[0] - wcoord2[0];
	min_x = -(pxl_width - 50) + half_width;
	max_x = (pxl_width - 50) - half_width;

	double half_height = wcoord2[1] - wcoord1[1];
	max_y = (pxl_height - 50) - half_height;
	unproj(gl, 1, viewport[3] - 1, wcoord3);
	min_y = -(pxl_height - 50) - (wcoord3[1] - wcoord1[1]);

	if (initialLoading && curPayload != null) {
	    initialLoading = false;

	    int time_factor = 2 * (int) Math.sqrt(curPayload.getLocation()[0]
		    * curPayload.getLocation()[0] + curPayload.getLocation()[1]
		    * curPayload.getLocation()[1]);

	    pan((float) curPayload.getLocation()[0],
		    (float) curPayload.getLocation()[1], time_factor);
	}

	if (changing_x != null && changing_y != null && curPayload != null) {
	    if (changing_x.isRunning() || changing_y.isRunning()) {
		setImageX(getPanX());
		setImageY(getPanY());
		PanelPayloadControls.btnCheck.setEnabled(false);
		PanelPayloadControls.btnSafe.setEnabled(false);
		PanelPayloadControls.btnHit.setEnabled(false);
	    } else {
		PanelPayloadControls.btnCheck.setEnabled(true);
		PanelPayloadControls.btnSafe.setEnabled(true);
		if (curPayload.getTaskType() != MyGame.TASK_NO_HINT) {
		    PanelPayloadControls.btnHit.setEnabled(true);
		} else {
		    if (this.rbtnClicked) {
			PanelPayloadControls.btnHit.setEnabled(true);
		    } else {
			PanelPayloadControls.btnHit.setEnabled(false);
		    }
		}
	    }
	}
	// calibrate if the image is off the screen

	if (getImageX() < min_x)
	    setImageX((float) min_x);
	if (getImageX() > max_x)
	    setImageX((float) max_x);
	if (getImageY() < min_y)
	    setImageY((float) min_y);
	if (getImageY() > max_y)
	    setImageY((float) max_y);

	if (mouseEvt != null) {
	    // Move to the mouse clicked position
	    if (this.isControlEnabled) {
		// Juntao: Do not move around with mouse clicking. Only provides
		// fixed view in all kinds of payload tasks.
		System.out.println("enabled");
		unproj(gl, mouseEvt.getX(), mouseEvt.getY(), wcoord);
	    }
	    // Check if the clicked position is the correct target position
	    setCorrect();

	    // Juntao: Do not respond to any left clicking.
	    /*
	     * if (Utils.isLeftClick(mouseEvt)) { hidePopup();
	     * 
	     * if (wcoord[0] < min_x) wcoord[0] = min_x; if (wcoord[0] > max_x)
	     * wcoord[0] = max_x; if (wcoord[1] < min_y) wcoord[1] = min_y; if
	     * (wcoord[1] > max_y) wcoord[1] = max_y;
	     * 
	     * int time_factor = 2 * (int) Math.sqrt((wcoord[0] - getImageX())
	     * (wcoord[0] - getImageX()) + (wcoord[1] - getImageY()) (wcoord[1]
	     * - getImageY()));
	     * 
	     * if (this.isControlEnabled) { //Juntao. Do not pan on any mode.
	     * View is always fixed. //pan((float) wcoord[0], (float) wcoord[1],
	     * time_factor); } }
	     */
	    mouseEvt = null;
	}

	camera_pers(gl);
	lsnr.Payload_Graphics_Update();
	// Indicate the GL that display doesn't have to be called again and
	// again
	// because it is already blacked out.
	if (!isEnabled())
	    screenBlackedAfterPayloadDone = true;
    }

    /**
     * Called by the drawable during the first repaint after the component has
     * been resized.
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width,
	    int height) {
	if (GL_DEBUG)
	    System.out.println("GL: reshape called");
	GL_width = width;
	GL_height = height;
	GL gl = drawable.getGL();
	gl.glViewport(0, 0, GL_width, GL_height);
	gl.glMatrixMode(GL_PROJECTION);
	gl.glLoadIdentity();
	double aspectRatio = (double) GL_width / (double) GL_height;
	glu.gluPerspective(CAMERA_ANGLE + zoom_angle_off, aspectRatio, 100,
		2500.0);
	gl.glMatrixMode(GL_MODELVIEW);
    }

    /**
     * Called by the drawable when the display mode or the display device
     * associated with the GLAutoDrawable has changed.
     */
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged,
	    boolean deviceChanged) {
	if (GL_DEBUG)
	    System.out.println("GL: displayChanged called");
    }

    private void initAnimRenderer() {
	animRenderer = new TextureRenderer(pxl_width, pxl_height, false);
	animRenderer.setSize(10, 10);
    }

    private void updateAnimRenderer() {
	if (GL_DEBUG)
	    System.out.println("GL: updateAnimRenderer called");
	int w = animRenderer.getWidth();
	int h = animRenderer.getHeight();
	Graphics2D g2d = animRenderer.createGraphics();
	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
	g2d.drawImage(img, null, null);
	g2d.dispose();
	animRenderer.markDirty(0, 0, w, h); // to be automatically synchronized
					    // with the underlying Texture
    }

    /**
     * Called by MyCanvas to check if this panel is enabled, that is, if there
     * is any payload that is currently shown in the panel
     */
    public synchronized boolean isEnabled() {
	return enabled;
    }

    private synchronized void glEnabled(boolean b) {
	enabled = b;
    }

    public synchronized boolean isControlEnabled() {
	return this.isControlEnabled;
    }

    public synchronized void ReplaceImage() {
	if (!imageReplaced) {

	    if (img != null)
		img.flush();

	    img = new BufferedImage(img.getWidth(), img.getHeight(),
		    img.getType());
	    // img.flush();
	    updateAnimRenderer();

	    Runnable runnable = new Runnable() {
		public synchronized void run() {
		    try {
			Thread.sleep(4000);
			img = ImageIO.read(new URL(curPayload
				.getClearFilename()));
			updateAnimRenderer();
			imageReplaced = true;
			Image_Checking = false;
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    };

	    Thread thread = new Thread(runnable);
	    thread.start();
	}
    }

    public synchronized void glControlEnabled(boolean b) {
	this.isControlEnabled = b;
    }

    // }

    /**
     * Called by MyCanvas Sets the local variable rbtnClicked.
     */
    public void setClicked(boolean c) {
	rbtnClicked = c;
    }

    public void reset_variables() {
	camera_x = 0;
	// image_x = image_y = 0;
	setImageX(0);
	setImageY(0);
	new_x_off = new_y_off = 0;
	x_dist = y_dist = 0;
	rotate_angle = 0;
	zoom_count = 0;
	min_x = max_x = 0;
	min_y = max_y = 0;
	// penalize = false;
	rbtnClicked = false;
    }

    /**
     * Called by PanelMap when the user engages a target.
     * 
     * @param v
     */
    public synchronized void set_payload(Vehicle v) {
	this.v = v;
	final String type = v.getType();
	final String mission = v.getTarget().getMission();

	// if the previous image is not flushed yet,
	if (img != null)
	    img.flush();

	// get the current payload of this vehicle
	// INCREMENTING CURSOR
	if (Reschu.tutorial() || Reschu.extraTutorial() || Reschu.train()) {
	    curPayload = g.getTutorialPayloadList().getPayload(type, mission);
	} else {

	    switch (taskSequenceList.get(taskCursor++
		    % (taskSequenceList.size()))) {
	    case MyGame.TASK_HINT:
		curPayload = g.getHintedPayloadList().getPayload(type, mission);
		break;
	    case MyGame.TASK_MAYBE:
		curPayload = g.getMaybePayloadList().getPayload(type, mission);
		break;
	    case MyGame.TASK_NO_HINT:
		curPayload = g.getNoHintPayloadList().getPayload(type, mission);
		break;
	    default:
		break;
	    }
	}
	Image_Loading = true;

	new Thread(new Runnable() {
	    public synchronized void run() {
		try {
		    img = ImageIO.read(new URL(curPayload.getFilename()));
		    // img = BlurPayloadImage(img);
		} catch (IOException ex) {
		    ex.printStackTrace();
		    System.exit(0);
		}
		pxl_width = (type == Vehicle.TYPE_UUV) ? 2000 : img.getWidth();
		pxl_height = img.getHeight();

		// updates animation renderer with this image's size
		animRenderer.setSize(pxl_width, pxl_height);
		updateAnimRenderer();

		// set camera
		camera_height = (type == Vehicle.TYPE_UUV) ? 1300 : 2000;
		CAMERA_ANGLE = 30.0;
		zoom_angle_off = 10;
		camera_y = (type == Vehicle.TYPE_UUV) ? 0 : -400;

		// resets all the variables that should be cleaned and display
		// it
		reset_variables();

		initialLoading = true;
		glCanvas.display();

		Image_Loading = false;
	    }
	}).start();

	glEnabled(true);
	glControlEnabled(true);

	correct = false;
	lsnr.Payload_Assigned_From_pnlPayload(v, curPayload);
    }

    private void displayText(GLAutoDrawable drawable) {
	if (GL_DEBUG)
	    System.out.println("GL: displayText called");
	if (Image_Loading) {
	    trB24.beginRendering(drawable.getWidth(), drawable.getHeight());
	    trB24.setColor(0.9f, 0.9f, 0.9f, flash);
	    trB24.draw("INITIATING VIDEO FEED", drawable.getWidth() / 2 - 120,
		    drawable.getHeight() / 2);
	    trB24.endRendering();
	}
	// Juntao: Disable all the marks in the payload screen
	if (isEnabled()) {
	    /*
	     * trB24.beginRendering(drawable.getWidth(), drawable.getHeight());
	     * trB24.setColor(0.9f, 0.9f, 0.9f, 0.9f); trB24.draw("_",
	     * drawable.getWidth() / 2 - 25, drawable.getHeight() / 2 + 5);
	     * trB24.draw("_", drawable.getWidth() / 2 + 18,
	     * drawable.getHeight() / 2 + 5); trB24.draw("|",
	     * drawable.getWidth() / 2 - 1, drawable.getHeight() / 2 - 25);
	     * trB24.draw("|", drawable.getWidth() / 2 - 1, drawable.getHeight()
	     * / 2 + 18); trB24.endRendering();
	     * 
	     * trP14.beginRendering(drawable.getWidth(), drawable.getHeight());
	     * trP14.setColor(0.9f, 0.9f, 0.9f, 0.9f);
	     * trP14.draw("|      |     |     |     |     |     |     |      |",
	     * drawable.getWidth() / 4 - 5, 10 + drawable.getHeight() * 4 / 5);
	     * trP14.endRendering();
	     * 
	     * trB20.beginRendering(drawable.getWidth(), drawable.getHeight());
	     * trB20.setColor(0.8f, 0.1f, 0.1f, 1f); trB20.draw( "|", (int) (-8
	     * + drawable.getWidth() / 2 + (drawable.getWidth() / 4)
	     * ((getImageX() + x_dist) / max_x)), 5 + drawable.getHeight() * 4 /
	     * 5); trB20.endRendering();
	     * 
	     * trB12.beginRendering(drawable.getWidth(), drawable.getHeight());
	     * trB12.setColor(0.9f, 0.9f, 0.9f, 0.9f); trB12.draw("__",
	     * drawable.getWidth() * 9 / 10, drawable.getHeight() / 4 - 12);
	     * trB12.draw("__", drawable.getWidth() * 9 / 10,
	     * drawable.getHeight() / 4 + 13); trB12.draw("__",
	     * drawable.getWidth() * 9 / 10, drawable.getHeight() / 4 + 40);
	     * trB12.draw("__", drawable.getWidth() * 9 / 10,
	     * drawable.getHeight() / 4 + 61); trB12.draw("__",
	     * drawable.getWidth() * 9 / 10, drawable.getHeight() / 4 + 83);
	     * trB12.draw("__", drawable.getWidth() * 9 / 10,
	     * drawable.getHeight() / 4 + 105); trB12.draw("__",
	     * drawable.getWidth() * 9 / 10, drawable.getHeight() / 4 + 128);
	     * trB12.draw("__", drawable.getWidth() * 9 / 10,
	     * drawable.getHeight() / 4 + 151); trB12.draw("__",
	     * drawable.getWidth() * 9 / 10, drawable.getHeight() / 4 + 172);
	     * trB12.endRendering();
	     * 
	     * trB20.beginRendering(drawable.getWidth(), drawable.getHeight());
	     * trB20.setColor(0.8f, 0.1f, 0.1f, 1f); trB20.draw( "__", 17 +
	     * drawable.getWidth() * 5 / 6, (int) (drawable.getHeight() / 2 +
	     * (drawable.getHeight() / 4) ((getImageY() + y_dist) / max_y)));
	     * trB20.endRendering();
	     * 
	     * trB17.beginRendering(drawable.getWidth(), drawable.getHeight());
	     * trB17.setColor(0.9f, 0.9f, 0.9f, 0.9f); trB17.draw("[+]",
	     * (drawable.getWidth() * 1 / 10) + 22, drawable.getHeight() / 4 +
	     * 140); trB17.draw("_", (drawable.getWidth() * 1 / 10) + 28,
	     * drawable.getHeight() / 4 + 133); trB17.draw("|",
	     * (drawable.getWidth() * 1 / 10) + 25, drawable.getHeight() / 4 +
	     * 120); trB17.draw("|", (drawable.getWidth() * 1 / 10) + 25,
	     * drawable.getHeight() / 4 + 105); trB17.draw("|",
	     * (drawable.getWidth() * 1 / 10) + 25, drawable.getHeight() / 4 +
	     * 90); trB17.draw("|", (drawable.getWidth() * 1 / 10) + 25,
	     * drawable.getHeight() / 4 + 75);
	     * 
	     * trB17.draw("|", (drawable.getWidth() * 1 / 10) + 35,
	     * drawable.getHeight() / 4 + 120); trB17.draw("|",
	     * (drawable.getWidth() * 1 / 10) + 35, drawable.getHeight() / 4 +
	     * 105); trB17.draw("|", (drawable.getWidth() * 1 / 10) + 35,
	     * drawable.getHeight() / 4 + 90); trB17.draw("|",
	     * (drawable.getWidth() * 1 / 10) + 35, drawable.getHeight() / 4 +
	     * 75); trB17.draw("_", (drawable.getWidth() * 1 / 10) + 28,
	     * drawable.getHeight() / 4 + 74); trB17.draw("[-]",
	     * (drawable.getWidth() * 1 / 10) + 22, drawable.getHeight() / 4 +
	     * 50); trB17.endRendering();
	     * 
	     * trB20.beginRendering(drawable.getWidth(), drawable.getHeight());
	     * trB20.setColor(0.8f, 0.1f, 0.1f, 1f); trB20.draw("__",
	     * (drawable.getWidth() * 1 / 10) + 20, drawable.getHeight() / 4 +
	     * 75 + (int) (60 / 3) * zoom_count); trB20.endRendering();
	     */

	    if (this.selectionBlink > 50) {
		if (rbtnClicked
			&& curPayload.getTaskType() == MyGame.TASK_NO_HINT) {
		    drawSelectionRect(drawable, clickedX, clickedY);
		}

		if (preSelectionRect && curPayload != null
			&& curPayload.getIsPreSelected() == 1) {
		    GL gl = drawable.getGL();
		    double[] co = new double[4];
		    proj(gl, curPayload.getLocation()[0],
			    curPayload.getLocation()[1], co);
		    drawSelectionRect(drawable, (int) co[0], (int) co[1]);

		}

		if (selectionBlink == 100) {
		    selectionBlink = 0;
		}
	    }
	    selectionBlink++;
	}
    }

    private void drawSelectionRect(GLAutoDrawable drawable, int x, int y) {
	trB24.beginRendering(drawable.getWidth(), drawable.getHeight());

	// Make outer highlight and inside highligh color consistent.
	switch (this.curPayload.getTaskType()) {
	case MyGame.TASK_HINT:
	    trB24.setColor(Color.RED);
	    break;
	case MyGame.TASK_MAYBE:
	    trB24.setColor(Color.ORANGE);
	    break;
	case MyGame.TASK_NO_HINT:
	default:
	    trB24.setColor(Color.GREEN);
	    break;
	}
	// trB24.setColor(0.1f, 0.1f, 1.0f, 0.9f);

	double box_center_x = x - (x_dist / (pxl_width * 2))
		* drawable.getWidth();
	double box_center_y = drawable.getHeight() - y
		- (y_dist / (pxl_height * 2)) * drawable.getHeight();

	trB24.draw("|", (int) box_center_x - 15, (int) box_center_y - 7);
	trB24.draw("|", (int) box_center_x + 9, (int) box_center_y - 7);
	trB24.draw("__", (int) box_center_x - 12, (int) box_center_y + 15);
	trB24.draw("__", (int) box_center_x - 12, (int) box_center_y - 10);

	trB24.endRendering();
    }

    private void displayAnimRenderer(GLAutoDrawable drawable) {
	if (GL_DEBUG)
	    System.out.println("GL: displayAnimRenderer called");
	if (bezierAlpha == 0f)
	    return;

	GL gl = drawable.getGL();
	Texture tex = animRenderer.getTexture();
	TextureCoords tc = tex.getImageTexCoords();

	float tx1 = tc.left();
	float ty1 = tc.top();
	float tx2 = tc.right();
	float ty2 = tc.bottom();

	gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
	tex.bind();
	tex.enable();
	gl.glBegin(GL.GL_QUADS);

	float rgb = bezierAlpha;
	float corner_x = pxl_width;
	float corner_y = pxl_height;
	gl.glColor4f(rgb, rgb, rgb, rgb);
	gl.glTexCoord2f(tx1, ty1);
	gl.glVertex3f(-corner_x, corner_y, 0f);
	gl.glTexCoord2f(tx2, ty1);
	gl.glVertex3f(corner_x, corner_y, 0f);
	gl.glTexCoord2f(tx2, ty2);
	gl.glVertex3f(corner_x, -corner_y, 0f);
	gl.glTexCoord2f(tx1, ty2);
	gl.glVertex3f(-corner_x, -corner_y, 0f);
	gl.glEnd();

	tex.disable();
    }

    private void camera_pers(GL gl) {
	if (GL_DEBUG)
	    System.out.println("GL: camera_pers called");
	gl.glViewport(0, 0, GL_width, GL_height);
	gl.glMatrixMode(GL_PROJECTION);
	gl.glLoadIdentity();
	double aspectRatio = (double) GL_width / (double) GL_height;
	glu.gluPerspective(CAMERA_ANGLE + zoom_angle_off, aspectRatio, 100,
		2500.0);
	gl.glMatrixMode(GL_MODELVIEW);
	lsnr.Payload_Graphics_Update();
    }

    // Screen(mouse) to real
    private void unproj(GL gl, int x, int y, double[] wcoord) {
	if (GL_DEBUG)
	    System.out.println("GL: unproj called");
	gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
	gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, mvmatrix, 0);
	gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projmatrix, 0);

	/* note viewport[3] is height of window in pixels */
	int realx = (int) x; // GL x coord pos
	int realy = viewport[3] - (int) y; // GL y coord pos

	gl.glReadPixels(realx, realy, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT,
		frameBuffer);
	frameBuffer.rewind();
	glu.gluUnProject((double) realx, (double) realy,
		(double) frameBuffer.get(0), // winX,winY,winZ
		mvmatrix, 0, projmatrix, 0, viewport, 0, wcoord, 0);

    }

    // real to mouse(screen)
    private void proj(GL gl, int x, int y, double[] wcoord) {
	if (GL_DEBUG)
	    System.out.println("GL: proj called");
	gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
	gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, mvmatrix, 0);
	gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projmatrix, 0);

	/* note viewport[3] is height of window in pixels */
	int realx = (int) x; // GL x coord pos
	int realy = (int) y; // GL y coord pos

	gl.glReadPixels(realx, realy, 1, 1, GL_DEPTH_COMPONENT, GL_FLOAT,
		frameBuffer);
	frameBuffer.rewind();
	glu.gluProject((double) realx, (double) realy,
		(double) frameBuffer.get(0), // winX,winY,winZ
		mvmatrix, 0, projmatrix, 0, viewport, 0, wcoord, 0);

	wcoord[1] = viewport[3] - wcoord[1];
    }

    public void mouse_click(java.awt.event.MouseEvent m_ev) {
	if (GL_DEBUG)
	    System.out.println("GL(Mouse): " + m_ev.toString());
	if (Utils.isRightClick(m_ev)
		&& curPayload.getTaskType() == MyGame.TASK_NO_HINT) {
	    clickedX = m_ev.getX();
	    clickedY = m_ev.getY();
	    rbtnClicked = true;
	    // showPopup(getParent(), m_ev.getXOnScreen()+10,
	    // m_ev.getYOnScreen()+10);
	    showPopup(getParent(), m_ev.getX() + 10, m_ev.getY() + 10);
	    lsnr.Payload_Submit(true); // T3

	}
	// Juntao: Respond to left clicking only when under task_no_hint mode.
	else if (Utils.isLeftClick(m_ev)
		&& curPayload.getTaskType() == MyGame.TASK_NO_HINT) {
	    rbtnClicked = false;
	}
	mouseEvt = m_ev;
    }

    /**
     * Check if the mouse clicked position is correct place provided by the
     * current payload. This function is to be called in display() function.
     */
    private void setCorrect() {
	int px = curPayload.getLocation()[0];
	int py = curPayload.getLocation()[1];
	int offset = 150;
	if ((wcoord[0] <= px + offset && wcoord[0] >= px - offset)
		&& (wcoord[1] <= py + offset && wcoord[1] >= py - offset)) {
	    correct = true;
	} else {
	    correct = false;
	}

	System.out.println(wcoord[0] + "," + wcoord[1]);
	System.out.println("GL: setCorrect(" + correct + ") called");
    }

    /**
     * Called when submit in the popup menu is clicked or the submit button is
     * clicked.
     */
    public void checkCorrect(int selection) {
	// Only when expecting result is not reassign, check correct in
	// different logic

	if (curPayload.getExpectingAction() == MyGame.EXPECTING_RESIGN) {
	    if (selection == MyGame.SAFE_OPTION) {
		correct = false;
	    }
	}

	if (curPayload.getExpectingAction() != MyGame.EXPECTING_RESIGN) {
	    correct = (selection == curPayload.getExpectingAction());
	}

	if (correct) {
	    PanelMsgBoard.Msg("CORRECT!, SCORE!", MessageType.PayloadCorrect);
	    g.payloadCorrect();
	    lsnr.EVT_Payload_Finished_Correct(v.getIndex(), v.getTarget()
		    .getName());
	} else {
	    PanelMsgBoard.Msg("INCORRECT!, NO SCORE!",
		    MessageType.PayloadInCorrect);
	    g.payloadIncorrect();
	    lsnr.EVT_Payload_Finished_Incorrect(v.getIndex(), v.getTarget()
		    .getName());
	}

	lsnr.Payload_Finished_From_pnlPayload(v);
	lsnr.Payload_Submit(false); // T3
	initAnimRenderer();
	glEnabled(false);
	glControlEnabled(false);

	this.imageReplaced = false;
	screenBlackedAfterPayloadDone = false;

	PanelPayloadControls.btnHit.setEnabled(false);
	PanelPayloadControls.btnCheck.setEnabled(false);
	PanelPayloadControls.btnSafe.setEnabled(false);

	Border bdrTitle = BorderFactory.createTitledBorder("Payload");
	Reschu.pnlPayloadContainer.setBorder(bdrTitle);

	// Juntao: Clear curpayload
	curPayload = null;
	this.initialLoading = false;
    }

    // PAYLOAD CAMERA CONTROL
    public double getRotating() {
	return rotate_angle;
    }

    public void setRotating(double alpha) {
	rotate_angle = alpha;
    }

    public synchronized float getPanX() {
	return new_x_off;
    }

    public synchronized void setPanX(float alpha) {
	new_x_off = alpha;
    }

    public synchronized float getPanY() {
	return new_y_off;
    }

    public synchronized void setPanY(float alpha) {
	new_y_off = alpha;
    }

    public double getZoom() {
	return zoom_angle_off;
    }

    public void setZoom(double alpha) {
	zoom_angle_off = alpha;
    }

    public void r_c_2() {
	if (changing_view != null && changing_view.isRunning())
	    return;

	if (v.getType() == Vehicle.TYPE_UAV) {
	    changing_view = PropertySetter.createAnimator(1000, this,
		    "Rotating", rotate_angle, rotate_angle + 30);
	}
	changing_view.setAcceleration(0.4f);
	changing_view.start();
    }

    public void r_c_c_2() {
	if (changing_view != null && changing_view.isRunning())
	    return;

	if (v.getType() == Vehicle.TYPE_UAV) {
	    changing_view = PropertySetter.createAnimator(1000, this,
		    "Rotating", rotate_angle, rotate_angle - 30);
	}
	changing_view.setAcceleration(0.4f);
	changing_view.start();
    }

    public void pan(float x, float y, int time) {
	if (GL_DEBUG)
	    System.out.println("GL: pan called");
	changing_x = PropertySetter.createAnimator(time, this, "PanX",
		getImageX(), x);
	changing_y = PropertySetter.createAnimator(time, this, "PanY",
		getImageY(), y);
	changing_x.start();
	changing_y.start();
    }

    public void zoom_in() {
	if (!isControlEnabled() || !isEnabled() || zoom_count == 3
		|| (changing_view != null && changing_view.isRunning())) {
	    return;
	}
	zoom_count = zoom_count + 1;
	if (v.getType() == Vehicle.TYPE_UAV) {
	    changing_view = PropertySetter.createAnimator(500, this, "Zoom",
		    zoom_angle_off, zoom_angle_off - 5);
	} else if (v.getType() == Vehicle.TYPE_UUV) {
	    changing_view = PropertySetter.createAnimator(500, this, "Zoom",
		    zoom_angle_off, zoom_angle_off - 5);
	}
	changing_view.setAcceleration(0.4f);
	changing_view.start();
    }

    public void zoom_out() {
	if (!isControlEnabled() || !isEnabled() || zoom_count == 0
		|| (changing_view != null && changing_view.isRunning())) {
	    return;
	}
	zoom_count = zoom_count - 1;
	if (v.getType() == Vehicle.TYPE_UAV) {
	    changing_view = PropertySetter.createAnimator(500, this, "Zoom",
		    zoom_angle_off, zoom_angle_off + 5);
	} else if (v.getType() == Vehicle.TYPE_UUV) {
	    changing_view = PropertySetter.createAnimator(500, this, "Zoom",
		    zoom_angle_off, zoom_angle_off + 5);
	}
	changing_view.setAcceleration(0.4f);
	changing_view.start();
    }

    // PopupMenu implementation
    public JPopupMenu getPopMenu() {
	return popMenu;
    }

    private void setPopup() {
	if (USE_POPUP) {
	    popMenu = new JPopupMenu();
	    mnuSubmit = new JMenuItem("Submit");
	    mnuCancel = new JMenuItem("Cancel");
	    mnuSubmit.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    rbtnClicked = false;
		    hidePopup();
		    // checkCorrect(0);
		    Border bdrTitle = BorderFactory
			    .createTitledBorder("Payload");
		    Reschu.pnlPayloadContainer.setBorder(bdrTitle);
		}
	    });
	    mnuCancel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    rbtnClicked = false;
		    hidePopup();
		    Border bdrTitle = BorderFactory
			    .createTitledBorder("Payload");
		    Reschu.pnlPayloadContainer.setBorder(bdrTitle);
		}
	    });
	    popMenu.add(mnuSubmit);
	    popMenu.add(mnuCancel);
	} else {
	    btnSubmit = new JButton("SUBMIT");
	    btnSubmit.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    rbtnClicked = false;
		    glCanvas.remove(btnSubmit);
		    glCanvas.remove(btnCancel);
		    checkCorrect(MyGame.HIT_OPTION);
		}
	    });

	    btnCancel = new JButton("CANCEL");
	    btnCancel.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
		    rbtnClicked = false;
		    glCanvas.remove(btnSubmit);
		    glCanvas.remove(btnCancel);
		    checkCorrect(MyGame.SAFE_OPTION);
		    Border bdrTitle = BorderFactory
			    .createTitledBorder("Payload");
		    Reschu.pnlPayloadContainer.setBorder(bdrTitle);
		}
	    });
	}

    }

    private void showPopup(Component invoker, int x, int y) {
	if (USE_POPUP) {
	    popMenu.show(invoker, x, y);
	} else {
	    PanelPayloadControls.btnHit.setEnabled(true);
	}
    }

    private void hidePopup() {
	if (USE_POPUP) {
	    popMenu.setVisible(false);
	} else {
	    PanelPayloadControls.btnHit.setEnabled(false);
	}
    }

}
