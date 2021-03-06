package reschu.game.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

import javax.swing.*;
import javax.swing.event.*;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.awt.Toolkit;

import reschu.constants.*;
import reschu.game.algorithm.CollisionMap;
import reschu.game.algorithm.CollisionZone;
import reschu.game.algorithm.Geo;
import reschu.game.controller.GUI_Listener;
import reschu.game.controller.Reschu;
import reschu.game.model.Game;
import reschu.game.model.Map;
import reschu.game.model.StructSelectedPoint;
import reschu.game.model.Vehicle;
import reschu.game.model.VehicleList;
import reschu.game.utils.Utils;

class CollisionZoneDiff {
    public static ArrayList<CollisionZone> App = new ArrayList<CollisionZone>();
    public static ArrayList<CollisionZone> Disapp = new ArrayList<CollisionZone>();

    public static boolean IsApp(Integer[] zoneVehicles) {
	for (CollisionZone zone : App) {
	    if (IsSameArray(zone.involvedVehicles, zoneVehicles)) {
		return true;
	    }
	}
	return false;
    }

    public static boolean IsDisapp(Integer[] zoneVehicles) {
	for (CollisionZone zone : Disapp) {
	    if (IsSameArray(zone.involvedVehicles, zoneVehicles)) {
		return true;
	    }
	}
	return false;
    }

    private static boolean IsSameArray(Integer[] left, Integer[] right) {
	Integer[] cLeft = left.clone();
	Integer[] cRight = right.clone();
	Arrays.sort(cLeft);
	Arrays.sort(cRight);

	return Arrays.equals(cLeft, cRight);
    }

    public static void refresh(ArrayList<CollisionZone> prev,
	    ArrayList<CollisionZone> next) {
	boolean found = false;
	App = new ArrayList<CollisionZone>();
	Disapp = new ArrayList<CollisionZone>();

	// Disappeared pairs
	for (CollisionZone pcz : prev) {
	    for (CollisionZone ncz : next) {
		if (IsSameArray(pcz.involvedVehicles, ncz.involvedVehicles)) {
		    if (!(pcz.isFaked ^ ncz.isFaked)) {
			found = true;
			break;
		    }
		}
	    }
	    if (!found) {
		Disapp.add(pcz);
	    }
	    found = false;
	}

	// Appeared pairs
	found = false;
	for (CollisionZone ncz : next) {
	    for (CollisionZone pcz : prev) {
		if (IsSameArray(pcz.involvedVehicles, ncz.involvedVehicles)) {
		    // if same pair but t->f or f->t, treated as newly appeared
		    if (!(pcz.isFaked ^ ncz.isFaked)) {
			found = true;
			break;
		    }
		}
	    }
	    if (!found) {
		App.add(ncz);
	    }
	    found = false;
	}
    }
}

public class PanelMap extends JPanel implements ActionListener, MouseListener,
	MouseMotionListener, PopupMenuListener {
    private static final long serialVersionUID = -4987595764448267113L;

    private boolean TABLETOP = true;

    private Map map;
    private GUI_Listener lsnr;
    private JPopupMenu popMenu;
    private JMenuItem mnuItemSetGoal, mnuItemAddWP, mnuItemDelWP,
	    mnuItemSubmit, mnuItemCancel, mnuItemPrev, mnuItemNext,
	    mnuItemInstantDelWP, mnuItemEngage;
    private Vehicle selectedVehicle;
    private boolean mapSettingMode, vehicleGoalMode, vehicleWPAddMode,
	    vehicleWPDelMode, vehicleWPChangeMode, vehicleGoalChangeMode,
	    WPRightClickedMode;
    public boolean vehicleWPAddPrevMode, vehicleWPAddNextMode;
    private boolean dragGPMode, dragWPMode;
    private int ex_WP_x, ex_WP_y, new_WP_x, new_WP_y, ex_GP_x, ex_GP_y,
	    new_GP_x, new_GP_y;
    private int clicked_pos_x, clicked_pos_y;
    private int[] just_added_WP;
    private int[] drag_from, drag_to, drag_to_prev, drag_next, region;

    // private Image backbuffer;
    // private Graphics2D backg;
    private static Image img;
    public double cGoodAuto = 0;
    public double cMalAuto = 1;

    public boolean IsMalfunctionalAutoRequired() {
	return (cGoodAuto / (cGoodAuto + cMalAuto)) > MyGame.AUTO_RELIABILITY;
    }

    public String CollisionZonesToString() {
	String result = "Collisions:\n";
	for (CollisionZone cz : collisionZones) {
	    result += cz.toString();
	}
	return result;
    }

    public static void setImg(Image newImage) {
	img = newImage;
    }

    private Game game;
    private JButton btnEmpty;
    private PaintComponent p;
    private StructSelectedPoint gp, wp; // goal_point, way_point
    private boolean eventDisabled;

    // variables for textOnTop
    private int _durationTextOnTop = 0;
    private String _msgTextOnTop = "";
    private boolean _isTextOnTop = false;
    //

    private final int cellsize = MySize.SIZE_CELL;
    private final int halfcell = MySize.SIZE_HALF_CELL;
    private final int rulersize = MySize.SIZE_RULER;
    private final int mapWidth = MySize.MAP_WIDTH_PXL;
    private final int mapHeight = MySize.MAP_HEIGHT_PXL;
    private final int wpsize = MySize.SIZE_WAYPOINT_PXL;
    private final int vWidth = MySize.SIZE_VEHICLE_WIDTH_PXL;
    private final int vHeight = MySize.SIZE_VEHICLE_HEIGHT_PXL;
    private final int targetsize = MySize.SIZE_TARGET_PXL;

    private static byte[][] collisionMap;
    public static ArrayList<CollisionZone> collisionZones;
    public ArrayList<CollisionZone> fakeCollisionZones;

    public static int GetVHCollisionCount() {
	int count = 0;
	for (int i = 0; i < collisionMap.length; i++) {
	    for (int j = 0; j < collisionMap[0].length; j++) {
		if (collisionMap[i][j] != 0) {
		    count++;
		}
	    }
	}
	return count;
    }

    private synchronized Vehicle getV() {
	return selectedVehicle;
    }

    private synchronized void setV(Vehicle v) {
	selectedVehicle = v;
    }

    public PanelMap(GUI_Listener l, Game g, String strTitle) {
	lsnr = l;
	game = g;
	map = game.map;
	popMenu = new JPopupMenu();
	p = new PaintComponent();
	mapSettingMode = false;
	selectedVehicle = null;
	vehicleGoalMode = false;
	vehicleGoalChangeMode = false;
	vehicleWPAddMode = false;
	vehicleWPDelMode = false;
	vehicleWPChangeMode = false;
	vehicleWPAddPrevMode = false;
	vehicleWPAddNextMode = false;
	WPRightClickedMode = false;
	dragGPMode = false;
	dragWPMode = false;
	eventDisabled = false;
	drag_to_prev = new int[] { 0, 0 }; // to optimize repainting when drag.
	// saves the previous mouse point.
	region = new int[] { 0, 0, 0, 0 }; // bogus value

	btnEmpty = new JButton();
	btnEmpty.setEnabled(false);

	this.setSize(mapWidth, mapHeight);
	this.collisionZones = new ArrayList<CollisionZone>();
	this.fakeCollisionZones = new ArrayList<CollisionZone>();
	try {
	    if (g.getWorkload() == MyGameMode.HIGH_WORKLOAD) {
		img = Toolkit.getDefaultToolkit().getImage(
			new URL(MyURL.URL_PREFIX + "map.jpg"));
	    } else if (g.getWorkload() == MyGameMode.LOW_WORKLOAD) {
		img = Toolkit.getDefaultToolkit().getImage(
			new URL(MyURL.URL_PREFIX + "darkmap.jpg"));
	    }
	} catch (MalformedURLException urle) {
	    // define img in the other way
	    urle.printStackTrace();
	}
	this.add(btnEmpty);
	this.addMouseListener(this);
	this.addMouseMotionListener(this);
    }

    public boolean isUniquePoint(ArrayList<int[]> list, int[] pos)
    {
	double[] pTarget = new double[]{pos[0], pos[1], pos[2]};
	for(int[] p : list)
	{
	    double[] p1 = new double[]{p[0], p[1], p[2]};
	    //System.out.println(Geo.Distance(p1, pTarget) + "----" + p1[2] + "," + pTarget[2]);
	    if((Geo.Distance(p1, pTarget) < 12) && (p1[2] == pTarget[2]))
	    {
		return false;
	    }
	}
	return true;
    }
    
    public String VHHighlightToString()
    {
	String result = "VH Collision: ";
	ArrayList<int[]> activePointsList = new ArrayList<int[]>();
	
	for(int i = 0; i < this.collisionMap.length; i++ )
	{
	    for(int j = 0; j < this.collisionMap[0].length; j++)
	    {
		if(this.collisionMap[i][j] > 0)
		{
		    if(this.isUniquePoint(activePointsList, new int[]{i,j, collisionMap[i][j]}))
		    {
			activePointsList.add(new int[]{i, j, collisionMap[i][j]});
			Vehicle v = game.getVehicleList().getVehicleByIndex(this.collisionMap[i][j]);
			result += String.format("[%d, %d, %d, %d, %d],", i, j, this.collisionMap[i][j], (int)v.movingStatus[0], (int)v.movingStatus[1]);
		    }
		   
		}
	    }
	}
	
	return result;
    }
    /**
     * For DEBUG
     */
    // private void printCoord(String msg, int x1, int y1, int x2, int y2) {
    // System.err.println("COORD: (" + x1 + "," + y1 +") - (" + x2 +"," + y2 +
    // ") when " + msg);
    // }

    @Override
    public void update(Graphics g) {
	Graphics2D g2d = (Graphics2D) g.create();
	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);

	paintMap(g2d);
	paintHazardArea(g2d);
	// paintCriticalZones(g2d);
	paintTarget(g2d);
	paintVehicles(g2d);
	paintDrag(g2d);
	paintText(g2d);
	paintStatistics(g2d);
	updateCollisionMap();
	lsnr.UpdateMapLogData(this.CollisionZonesToString() + map.toString() + VHHighlightToString());
	lsnr.Update_Collision_Info(this.collisionZones);

	if (Game.collisionHighlightEnabled) {
	    paintCollision(g2d);
	}

	// Juntao: Don't display blue boarder when setting points.
	// if (mapSettingMode)
	// paintBorder(g2d, Color.blue);
	// Juntao: Don't display red border when doing payload.
	// if (eventDisabled)
	// paintBorder(g2d, Color.red);
    }

    @Override
    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D) g.create();
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
	if (mapSettingMode) {
	    Graphics2D g2d = (Graphics2D) g;
	    g2d.setComposite(makeComposite(.8f));
	}
	if (eventDisabled) {
	    Graphics2D g2d = (Graphics2D) g;
	    g2d.setComposite(makeComposite(.5f));
	}
	g.clearRect(0, 0, getWidth(), getHeight());
	update(g);
    }

    @Override
    public void setEnabled(boolean enabled) {
	if (enabled) {
	    eventDisabled = false;
	} // setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));}
	else {
	    eventDisabled = true;
	} // setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));}
    }

    /**
     * Creates a back buffer.
     */
    // public void init_buffer() {
    // super.addNotify();
    // assert ( !super.isDisplayable() || GraphicsEnvironment.isHeadless())
    // : "Map is not displayable. Please contact nehme@mit.edu";
    // backbuffer = createImage(mapWidth , mapHeight);
    // backg = (Graphics2D)backbuffer.getGraphics();
    // backg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    // RenderingHints.VALUE_ANTIALIAS_ON);
    // paintMap();
    // }

    /**
     * Displays a message on the top of the map (maybe at the bottom?-subject to
     * change) The display will last for a certain duration of time as given
     * (duration)
     * 
     * @param msg
     *            the message to be displayed
     * @param duration
     *            the duration. msg will be display for (duration) seconds
     */
    public void setTextOnTop(String msg, int duration) {
	_msgTextOnTop = msg;
	_durationTextOnTop = duration;
	_isTextOnTop = true;
    }

    /**
     * If _isTextOnTop is true, decrease _durationTextOnTop by one second, and
     * if the duration reaches zero, sets _isTextOnTop = false
     */
    public void decreaseTextOnTopTime() {
	if (_isTextOnTop) {
	    if (_durationTextOnTop == 1) {
		_isTextOnTop = false;
		_msgTextOnTop = "";
	    }
	    _durationTextOnTop--;
	}
    }

    /**
     * Displays a message on the top of the map.
     * 
     * @param msg
     *            message to be displayed
     * @param size
     *            the size font
     * @param c
     *            the color of the message
     * @param x
     *            width
     * @param y
     *            height
     */
    private void paintTextOnTop(Graphics2D g, String msg, int size, Color c,
	    int x, int y) {
	g.setFont(new Font("Helvetica", Font.ITALIC, size));
	g.setColor(c);
	g.drawString(msg, x, y);
    }

    private void paintMap(Graphics2D g) {
	g.drawImage(img, 0, 0, this);

	int i;
	g.setColor(new Color(0, 0, 0, 50));
	g.setStroke(MyStroke.STROKE_NARROW);
	for (i = 0; i <= getHeight(); i = i + rulersize) {
	    g.drawLine(0, i, mapWidth, i);
	}
	for (i = 0; i <= mapWidth; i = i + rulersize) {
	    g.drawLine(i, 0, i, getHeight());
	}
	g.setStroke(MyStroke.STROKE_BASIC);

	// FOR GRID MEASUREMENT. SHOULD BE REMOVED
	/*
	 * for( i=0; i<MySize.width; i++ ) for( int j=0; j<MySize.height; j++ )
	 * if( map.getCellType(i,j) == MyGame.SEASHORE ) { g.setColor(new
	 * Color(250,50,50,250)); g.fillRect(i*cellsize,
	 * j*cellsize,cellsize,cellsize); }
	 */
    }

    private void paintHazardArea(Graphics2D g) {
	for (int i = 0; i < map.getListHazard().size(); i++) {
	    int[] pos = map.getListHazard().get(i);
	    p.paintOval(g, pos[0], pos[1], cellsize, MySize.SIZE_HAZARD_3_PXL,
		    new Color(255, 255, 128, 80));
	    p.paintOval(g, pos[0], pos[1], cellsize, MySize.SIZE_HAZARD_2_PXL,
		    new Color(255, 255, 128, 100));
	    p.paintOval(g, pos[0], pos[1], cellsize, MySize.SIZE_HAZARD_1_PXL,
		    new Color(255, 255, 128, 150));
	}
    }

    private void paintTarget(Graphics2D g) {
	Color clrTarget;

	for (int i = 0; i < map.getListAssignedTarget().size(); i++) {
	    int[] pos = map.getListAssignedTarget().get(i).getPos();
	    if (map.getListAssignedTarget().get(i).isDone()) {
		p.paintPolygon(g, pos[0], pos[1], cellsize, targetsize,
			new Color(0, 0, 0, 250), MyColor.COLOR_TARGET_DONE);
		p.paintString(g, pos[0] - 2, pos[1] + 2, cellsize, Color.white,
			MyFont.fontBold, map.getListAssignedTarget().get(i)
				.getName());
	    } else {
		if (!map.getListAssignedTarget().get(i).isVisible())
		    clrTarget = MyColor.COLOR_TARGET_INVISIBLE_OCCUPIED;
		else
		    clrTarget = MyColor.COLOR_TARGET_OCCUPIED;
		p.paintPolygon(g, pos[0], pos[1], cellsize, targetsize,
			new Color(0, 0, 0, 250), clrTarget);
		p.paintString(g, pos[0] - 2, pos[1] + 2, cellsize, Color.white,
			MyFont.fontBold, map.getListAssignedTarget().get(i)
				.getName());
	    }
	}

	for (int i = 0; i < map.getListUnassignedTarget().size(); i++) {
	    int[] pos = map.getListUnassignedTarget().get(i).getPos();
	    if (!map.getListUnassignedTarget().get(i).isVisible())
		clrTarget = MyColor.COLOR_TARGET_INVISIBLE;
	    else
		clrTarget = MyColor.COLOR_TARGET_VACANT;
	    p.paintPolygon(g, pos[0], pos[1], cellsize, targetsize, new Color(
		    0, 0, 0, 250), clrTarget);
	    p.paintString(g, pos[0] - 2, pos[1] + 2, cellsize, Color.white,
		    MyFont.fontBold, map.getListUnassignedTarget().get(i)
			    .getName());
	}
    }

    public void paintCollision(Graphics2D g) {

	for (int i = 0; i < this.collisionMap.length; i++) {
	    for (int j = 0; j < this.collisionMap[0].length; j++) {
		//if (this.collisionMap[i][j] == CollisionMap.HAZARD_COLLISION) {
		if (this.collisionMap[i][j] != 0) {
		    p.paintOval(g, i, j, cellsize, MySize.SIZE_COLLISION,
			    MyColor.COLOR_COLLISION_TRACK);
		}
	    }
	}

	for (int i = 0; i < this.collisionZones.size(); i++) {
	    // int[] rect = this.collisionZones.get(i).collisionArea;
	    // Integer[][] collisionArea =
	    // this.collisionZones.get(i).collisionArea;
	    CollisionZone cz = collisionZones.get(i);
	    Rectangle2D r = cz.collisionArea;
	    p.paintRect(g, (int) r.getX(), (int) r.getY(), (int) r.getX()
		    + (int) r.getWidth(), (int) r.getY() + (int) r.getHeight(),
		    cellsize, cz.isFaked);
	    // p.paintRect(g, rect[0], rect[1], rect[2], rect[3], cellsize);
	    // p.fillPolygon(g, collisionArea[0], collisionArea[1], cellsize);
	}
    }

    // Painting vehicle logo and vehicle Path
    public void paintVehicles(Graphics2D g) {
	VehicleList vList = game.getVehicleList();
	Vehicle v;

	for (int i = 0; i < vList.size(); i++) {
	    v = vList.getVehicle(i);
	    Color clrVehicle;
	    if (v.getStatus() == MyGame.STATUS_VEHICLE_PENDING)
		clrVehicle = MyColor.COLOR_VEHICLE_PENDING;
	    else
		clrVehicle = MyColor.COLOR_VEHICLE;

	    if (selectedVehicle == v) {
		p.paintHighlight(g, v.getX(), v.getY(), cellsize, halfcell,
			MySize.SIZE_HIGHLIGHT_PXL, rulersize / 3,
			MyColor.COLOR_HIGHLIGHT, MyStroke.STROKE_BASIC,
			MyStroke.STROKE_WIDE);
	    }

	    if (v.getType() == Vehicle.TYPE_UAV) {
		p.paintArc(g, v.getX(), v.getY(), cellsize, halfcell,
			rulersize, vWidth, vHeight, new Color(0, 0, 0, 250),
			clrVehicle, Vehicle.TYPE_UAV);
		p.paintString(g, v.getX() - 1, v.getY() + 2, cellsize,
			new Color(255, 255, 255, 255), MyFont.fontBold,
			Integer.toString(v.getIndex()));
		if (v.getPayload() == Vehicle.PAYLOAD_COM)
		    p.paintOval(g, v.getX() + 1, v.getY() + 2, cellsize,
			    MySize.SIZE_UAV_COMM_PXL,
			    MyColor.COLOR_VEHICLE_COMM_BOUNDARY);
	    } else if (v.getType() == Vehicle.TYPE_UUV) {
		p.paintArc(g, v.getX(), v.getY(), cellsize, halfcell,
			rulersize, vWidth, vHeight, new Color(0, 0, 0, 250),
			clrVehicle, Vehicle.TYPE_UUV);
		p.paintString(g, v.getX() - 1, v.getY() + 3, cellsize,
			new Color(255, 255, 255, 255), MyFont.fontBold,
			Integer.toString(v.getIndex()));
	    }

	    if (v.hasGoal()) {
		if (v.hasWaypoint()) {
		    if (selectedVehicle == v)
			g.setColor(MyColor.COLOR_HIGHLIGHT);
		    else
			g.setColor(MyColor.COLOR_LINE);

		    for (double[] d : v.getTrack()) {
			g.drawLine((int) (d[0] * cellsize + halfcell),
				(int) (d[1] * cellsize + halfcell), (int) (d[0]
					* cellsize + halfcell) + 1, (int) (d[1]
					* cellsize + halfcell) + 1);
		    }

		    if (selectedVehicle == v)
			g.setColor(MyColor.COLOR_HIGHLIGHT);
		    else
			g.setColor(MyColor.COLOR_LINE);
		    g.fillOval((v.getPath().get(0)[0] - wpsize / cellsize / 2)
			    * cellsize, (v.getPath().get(0)[1] - wpsize
			    / cellsize / 2)
			    * cellsize, wpsize, wpsize);

		    for (int j = 0; j < v.getPath().size() - 1; j++) {
			if (selectedVehicle == v)
			    g.setColor(MyColor.COLOR_HIGHLIGHT);
			else
			    g.setColor(MyColor.COLOR_LINE);

			/*
			 * for (double[] d : v.getTrack()) { g.drawLine((int)
			 * (d[0] * cellsize + halfcell), (int) (d[1] * cellsize
			 * + halfcell), (int) (d[0] * cellsize + halfcell) + 1,
			 * (int) (d[1] * cellsize + halfcell) + 1); }
			 */

			if (j == v.getPath().size() - 2)
			    if (selectedVehicle == v)
				g.setColor(MyColor.COLOR_HIGHLIGHT);
			    else
				g.setColor(MyColor.COLOR_LINE);

			g.fillOval((v.getPath().get(j + 1)[0] - wpsize
				/ cellsize / 2)
				* cellsize, (v.getPath().get(j + 1)[1] - wpsize
				/ cellsize / 2)
				* cellsize, wpsize, wpsize);
		    }
		} else {
		    if (selectedVehicle == v)
			g.setColor(MyColor.COLOR_HIGHLIGHT);
		    else
			g.setColor(MyColor.COLOR_LINE);

		    for (double[] d : v.getTrack()) {
			g.drawLine((int) (d[0] * cellsize + halfcell),
				(int) (d[1] * cellsize + halfcell), (int) (d[0]
					* cellsize + halfcell) + 1, (int) (d[1]
					* cellsize + halfcell) + 1);
		    }

		    if (selectedVehicle == v)
			g.setColor(MyColor.COLOR_HIGHLIGHT);
		    else
			g.setColor(MyColor.COLOR_LINE);

		    g.fillOval((v.getPath().getLast()[0] - wpsize / cellsize
			    / 2)
			    * cellsize, (v.getPath().getLast()[1] - wpsize
			    / cellsize / 2)
			    * cellsize, wpsize, wpsize);
		}
	    }
	}
    }

    public void paintBorder(Graphics2D g, Color c) {
	g.setColor(c);
	g.fillRect(0, 0, getWidth(), 5); // top
	g.fillRect(0, 0, 5, getHeight()); // left
	g.fillRect(getWidth() - 5, 0, 5, getHeight()); // right
	g.fillRect(0, getHeight() - 5, getWidth(), 5); // bottom
    }

    public void paintDrag(Graphics2D g) {
	if (dragGPMode) {
	    g.setColor(Color.gray);
	    g.fillOval(drag_to[0] - wpsize / 2, drag_to[1] - wpsize / 2,
		    wpsize, wpsize);
	    g.drawLine(drag_from[0] * cellsize, drag_from[1] * cellsize,
		    drag_to[0], drag_to[1]);
	}
	if (dragWPMode) {
	    g.setColor(Color.gray);
	    g.fillOval(drag_to[0] - wpsize / 2, drag_to[1] - wpsize / 2,
		    wpsize, wpsize);
	    g.drawLine(drag_from[0] * cellsize, drag_from[1] * cellsize,
		    drag_to[0], drag_to[1]);
	    g.drawLine(drag_to[0], drag_to[1], drag_next[0] * cellsize,
		    drag_next[1] * cellsize);
	}
    }

    private void paintText(Graphics2D g) {
	if (Reschu.tutorial() || Reschu.extraTutorial())
	    paintTextOnTop(g, "Tutorial Mode", 40, new Color(1.0f, 1.0f, 1.0f,
		    0.5f), 200, 50);
	if (Reschu.train())
	    paintTextOnTop(g, "Training Mode", 40, new Color(1.0f, 1.0f, 1.0f,
		    0.5f), 70, 50);
	// Required resolution check for Java WebStart
	// if( getHeight() < mapHeight || getWidth() < mapWidth ) {
	// g.setColor(new Color(0.8f, 0.8f, 0.8f, 0.4f));
	// g.fillRect(10,getHeight()-55, getWidth()-20, 35);
	// Composite backup = g.getComposite();
	// g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
	// paintTextOnTop(g,
	// "You need a minimum screen resolution of 1280 x 1024. Please try again.",
	// 20, new Color(1.0f, 1.0f, 0.0f, 0.8f), 20, getHeight()-30);
	// g.setComposite(backup);
	// }
	if (_isTextOnTop) {
	    g.setColor(new Color(0.8f, 0.8f, 0.8f, 0.4f));
	    g.fillRect(10, getHeight() - 55, getWidth() - 20, 35);
	    Composite backup = g.getComposite();
	    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
	    paintTextOnTop(g, _msgTextOnTop + " (" + _durationTextOnTop + ")",
		    20, new Color(1.0f, 1.0f, 0.0f, 0.8f), 20, getHeight() - 30);
	    g.setComposite(backup);
	}
    }

    private void paintStatistics(Graphics2D g) {
	String message1 = String.format("Correct: %d", game.getCorrectHit());
	paintTextOnTop(g, message1, 20, Color.yellow, getWidth() - 190, 30);

	String message2 = String
		.format("Incorrect: %d", game.getInCorrectHit());
	paintTextOnTop(g, message2, 20, Color.yellow, getWidth() - 190, 50);

	String message3 = String.format("V-V Damage: %.1f", game.getVVDamage());
	paintTextOnTop(g, message3, 20, Color.yellow, getWidth() - 190, 70);

	String message4 = String.format("V-H Damage: %.1f", game.getVHDamage());
	paintTextOnTop(g, message4, 20, Color.yellow, getWidth() - 190, 90);

    }

    // POPUP
    private void showPopup(Component invoker, int x, int y, Vehicle v) {
	setPopupMenu();

	if (!mapSettingMode && !vehicleWPAddMode) {
	    if (!v.hasGoal()) {
		mnuItemSetGoal.setEnabled(true);
		popMenu.remove(mnuItemAddWP);
		popMenu.remove(mnuItemDelWP);
	    } else {
		mnuItemSetGoal.setText("Change the goal");
		mnuItemSetGoal.setEnabled(true);
		mnuItemAddWP.setEnabled(true);
		if (v.hasWaypoint())
		    mnuItemDelWP.setEnabled(true);
		else
		    popMenu.remove(mnuItemDelWP);
	    }
	}
	popMenu.show(this, x, y);
	repaint(x, y, popMenu.getWidth(), popMenu.getHeight());
    }

    public void HidePopup(Vehicle v) {
	if (popMenu.isShowing() && selectedVehicle == v)
	    popMenu.setVisible(false);
    }

    private void setPopupMenu() {
	if (mapSettingMode && vehicleWPAddMode) {
	    popMenu.removeAll();
	    mnuItemSubmit = new JMenuItem("Submit");
	    mnuItemCancel = new JMenuItem("Cancel");
	    mnuItemSubmit.addActionListener(this);
	    mnuItemCancel.addActionListener(this);
	    popMenu.add(mnuItemSubmit);
	    popMenu.add(mnuItemCancel);
	    /*
	     * if (vehicleWPAddPrevMode) mnuItemPrev = new JMenuItem("Prev"); if
	     * (vehicleWPAddNextMode) mnuItemNext = new JMenuItem("Next");
	     * 
	     * 
	     * if (vehicleWPAddPrevMode) mnuItemPrev.addActionListener(this); if
	     * (vehicleWPAddNextMode) mnuItemNext.addActionListener(this);
	     * 
	     * 
	     * if (vehicleWPAddPrevMode) popMenu.add(mnuItemPrev); if
	     * (vehicleWPAddNextMode) popMenu.add(mnuItemNext);
	     */
	} else if (WPRightClickedMode) { // When the user clicked a waypoint
					 // with the mouse-RIGHT-button
	    popMenu.removeAll();
	    mnuItemInstantDelWP = new JMenuItem("Delete waypoint");
	    mnuItemInstantDelWP.addActionListener(this);
	    popMenu.add(mnuItemInstantDelWP);
	} else { // if(mapSettingMode && !vehicleWPAddMode)
	    popMenu.removeAll();
	    mnuItemSetGoal = new JMenuItem("Set the goal");
	    mnuItemAddWP = new JMenuItem("Add waypoint");
	    mnuItemDelWP = new JMenuItem("Delete waypoint");

	    mnuItemSetGoal.addActionListener(this);
	    mnuItemAddWP.addActionListener(this);
	    mnuItemDelWP.addActionListener(this);

	    // Juntao: Put add waypoint in the first line.
	    popMenu.add(mnuItemAddWP);
	    if (selectedVehicle.getStatus() != MyGame.STATUS_VEHICLE_PENDING)
		popMenu.add(mnuItemSetGoal);
	    // popMenu.add(mnuItemDelWP); DELETED THIS AFTER A DISCUSSION WITH
	    // CARL (MAR 3rd)

	    if (selectedVehicle.getStatus() == MyGame.STATUS_VEHICLE_PENDING) {
		mnuItemEngage = new JMenuItem("Engage");
		mnuItemEngage.addActionListener(this);
		popMenu.add(mnuItemEngage);
	    }

	}
	popMenu.addPopupMenuListener(this);
    }

    public boolean chkEngage() {
	return true;
    }

    public boolean chkSwitch() {
	return true;
    }

    // Set alpha composite.
    private AlphaComposite makeComposite(float alpha) {
	int type = AlphaComposite.SRC_OVER;
	return (AlphaComposite.getInstance(type, alpha));
    }

    public void setSelectedVehicle(Vehicle v) {
	selectedVehicle = v;
    }

    private void setWPNextPrev(int idx) {
	if (idx == 0)
	    vehicleWPAddPrevMode = false;
	else
	    vehicleWPAddPrevMode = true;

	if (idx == getV().getPath().size() - 2)
	    vehicleWPAddNextMode = false;
	else
	    vehicleWPAddNextMode = true;
    }

    public synchronized void setClear() {
	// selectedVehicle = null;
	mapSettingMode = false;
	vehicleGoalMode = false;
	vehicleGoalChangeMode = false;
	vehicleWPAddMode = false;
	vehicleWPDelMode = false;
	vehicleWPAddPrevMode = false;
	vehicleWPAddNextMode = false;
	vehicleWPChangeMode = false;
    }

    public synchronized void setGoal(Vehicle v) {
	selectedVehicle = v;
	mapSettingMode = true;
	setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	vehicleGoalMode = true;
	// TODO: pass (v.getX(), v.getY(), goal_x, goal_y)
	// repaint();

    }

    public synchronized void addWP(Vehicle v) {
	selectedVehicle = v;
	mapSettingMode = true;
	setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	vehicleWPAddMode = true;
	// repaint();
    }

    public synchronized void delWP(Vehicle v) {
	selectedVehicle = v;
	mapSettingMode = true;
	setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	vehicleWPDelMode = true;
	// repaint();
    }

    // ActionListener interface
    public void actionPerformed(ActionEvent evt) {
	if (evt.getSource() == mnuItemSetGoal) {
	    Vehicle v = getV();
	    setClear();
	    setGoal(v);
	    lsnr.EVT_GP_SetGP_Start(v.getIndex());
	}
	if (evt.getSource() == mnuItemAddWP) {
	    Vehicle v = getV();
	    if (v != null) {
		setClear();
		addWP(v);
		lsnr.EVT_WP_AddWP_Start(v.getIndex());
	    }
	}
	if (evt.getSource() == mnuItemDelWP) {
	    Vehicle v = getV();
	    setClear();
	    delWP(v);
	    lsnr.EVT_WP_DeleteWP_Start(v.getIndex());
	}
	if (evt.getSource() == mnuItemSubmit) {
	    repaint();
	    if (vehicleWPAddMode) {
		// This is the exact point where the user finishes to add a
		// waypoint.
		lsnr.EVT_WP_AddWP_End(selectedVehicle.getIndex(),
			clicked_pos_x, clicked_pos_y);
	    }
	    setClear();
	    // System.out.println("Vehicle deselected by adding waypoint");
	}
	if (evt.getSource() == mnuItemCancel) {
	    repaint();
	    getV().delWaypoint(just_added_WP[0], just_added_WP[1]);
	    if (vehicleWPAddMode) {
		// This is the exact point where the user finishes to add a
		// waypoint.
		lsnr.EVT_WP_AddWP_Cancel(selectedVehicle.getIndex());
	    }
	    setClear();
	    // System.out.println("Vehicle deselected by adding waypoint");
	}
	if (evt.getSource() == mnuItemPrev) {
	    int idx;
	    getV().delWaypoint(just_added_WP[0], just_added_WP[1]);
	    idx = getV().addWaypoint(just_added_WP[0], just_added_WP[1],
		    --just_added_WP[2]);
	    setWPNextPrev(idx);
	    if (!TABLETOP) {
		clicked_pos_x = (int) getMousePosition().getX() / cellsize - 10;
		clicked_pos_y = (int) getMousePosition().getY() / cellsize - 30;
	    }
	    showPopup(
		    this,
		    clicked_pos_x * cellsize,
		    clicked_pos_y * cellsize,
		    game.getVehicleList().getVehicle(clicked_pos_x,
			    clicked_pos_y));
	}
	if (evt.getSource() == mnuItemNext) {
	    int idx;
	    getV().delWaypoint(just_added_WP[0], just_added_WP[1]);
	    idx = getV().addWaypoint(just_added_WP[0], just_added_WP[1],
		    ++just_added_WP[2]);
	    setWPNextPrev(idx);
	    if (!TABLETOP) {
		clicked_pos_x = (int) getMousePosition().getX() / cellsize - 10;
		clicked_pos_y = (int) getMousePosition().getY() / cellsize - 30;
	    }
	    showPopup(
		    this,
		    clicked_pos_x * cellsize,
		    clicked_pos_y * cellsize,
		    game.getVehicleList().getVehicle(clicked_pos_x,
			    clicked_pos_y));
	}
	if (evt.getSource() == mnuItemInstantDelWP) {
	    Vehicle v = getV();
	    if (v != null) {
		v.delWaypoint(wp.getX(), wp.getY());
		lsnr.EVT_WP_DeleteWP_Start(selectedVehicle.getIndex());
		lsnr.EVT_WP_DeleteWP_End(selectedVehicle.getIndex(), wp.getX(),
			wp.getY());
	    }
	}
	if (evt.getSource() == mnuItemEngage) {
	    if (selectedVehicle.getPayload() == Vehicle.PAYLOAD_COM)
		selectedVehicle.COM_Payload();
	    else
		lsnr.Vehicle_Engage_From_pnlMap(selectedVehicle);
	}
	repaint();
    }

    // MouseListener interface
    public void mouseClicked(MouseEvent m_ev) {
	if (eventDisabled)
	    return;

	clicked_pos_x = m_ev.getX() / cellsize;
	clicked_pos_y = m_ev.getY() / cellsize;

	Vehicle v = game.Vechicle_Location_Check(clicked_pos_x, clicked_pos_y);

	// Vehicle selected
	if (v != null && !mapSettingMode && !vehicleWPAddMode
		&& !vehicleWPDelMode) {
	    setV(v);
	    lsnr.Vehicle_Selected_From_pnlMap(v.getIndex());
	    if (Utils.isLeftClick(m_ev)) {
		lsnr.EVT_VSelect_Map_LBtn(v.getIndex());
	    } else if (Utils.isRightClick(m_ev)) {
		lsnr.EVT_VSelect_Map_RBtn(v.getIndex());
		showPopup(this, m_ev.getX(), m_ev.getY(), v);
	    }
	    repaint();
	}

	if (v == null && Utils.isLeftClick(m_ev) && selectedVehicle != null
		&& !mapSettingMode && !vehicleWPAddMode && !vehicleWPDelMode) {
	    lsnr.Vehicle_Unselected_From_pnlMap();
	}

	// Add goal
	if (Utils.isLeftClick(m_ev) && mapSettingMode && vehicleGoalMode) {
	    if (getV().getPath().size() == 0)
		getV().addGoal(clicked_pos_x, clicked_pos_y);
	    else
		getV().changeGoal(getV().getPath().getLast(), clicked_pos_x,
			clicked_pos_y);
	    setClear();
	    // setV(null);
	    mapSettingMode = false;
	    vehicleGoalMode = false;
	}

	if (!TABLETOP) {
	    // Add a waypoint
	    if (Utils.isLeftClick(m_ev) && mapSettingMode && vehicleWPAddMode) {
		int idx = getV().addWaypoint(clicked_pos_x, clicked_pos_y);
		just_added_WP = new int[] { clicked_pos_x, clicked_pos_y, idx };
		setWPNextPrev(idx);
		showPopup(this, m_ev.getX(), m_ev.getY(), game.getVehicleList()
			.getVehicle(clicked_pos_x, clicked_pos_y));
	    }
	}

	// Delete a waypoint
	if (Utils.isLeftClick(m_ev) && mapSettingMode && vehicleWPDelMode
		&& wp != null) {
	    getV().delWaypoint(wp.getX(), wp.getY());
	    setClear();
	    // System.out.println("Vehicle deselected by deleting waypoint");
	}
	repaint();

	if (Game.collisionHighlightEnabled && Game.automationEnabled) {
	    Integer[] vIds = this.getCollisionZoneVIds(new int[] {
		    clicked_pos_x, clicked_pos_y });
	    if (Utils.isLeftClick(m_ev) && vIds.length > 0) {
		String info = "Apply automation for vehicle "
			+ Arrays.toString(vIds) + "?";
		if (JOptionPane.showConfirmDialog(this, info, "Automation",
			JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
		    lsnr.Automation_Applied_From_pnlMap(vIds);
		} else {
		    lsnr.Automation_Application_Canceled(vIds);
		}
	    }
	}

    }

    public void mousePressed(MouseEvent m_ev) {
	if (eventDisabled)
	    return;
	int clicked_pos_x, clicked_pos_y;
	clicked_pos_x = m_ev.getX() / cellsize;
	clicked_pos_y = m_ev.getY() / cellsize;

	if (Utils.isLeftClick(m_ev)) {
	    // TODO: Very inefficient way of checking whether the clicked point
	    // is a waypoint.
	    // Should I do it this way, or change the waypoint as a component
	    // model?
	    // Sep 4, 2008
	    wp = game.Vehicle_Waypoint_Check(clicked_pos_x, clicked_pos_y);
	    if (wp == null)
		gp = game.Vehicle_Goal_Check(clicked_pos_x, clicked_pos_y);

	    // MOVE - GP
	    if (Utils.isLeftClick(m_ev) && gp != null) {
		setV(gp.getV());
		repaint();
		// System.out.println("[mousePressed]Vehicle(" +
		// getV().getName() + ") selected.(gp)");
		vehicleGoalChangeMode = true;
		lsnr.Vehicle_Selected_From_pnlMap(getV().getIndex());
		ex_GP_x = gp.getX();
		ex_GP_y = gp.getY();

		String targetName;
		if (selectedVehicle.getTarget() == null)
		    targetName = "NULL";
		else
		    targetName = selectedVehicle.getTarget().getName();
		lsnr.EVT_GP_ChangeGP_Start(selectedVehicle.getIndex(),
			clicked_pos_x, clicked_pos_y, targetName);
	    }

	    // MOVE - WP
	    if (wp != null && Utils.isLeftClick(m_ev) && !vehicleWPDelMode) {
		setV(wp.getV());
		repaint();
		// System.out.println("[mousePressed]Vehicle(" +
		// getV().getName() + ") selected.(wp)");
		vehicleWPChangeMode = true;
		lsnr.Vehicle_Selected_From_pnlMap(getV().getIndex());
		ex_WP_x = wp.getX();
		ex_WP_y = wp.getY();
		lsnr.EVT_WP_MoveWP_Start(selectedVehicle.getIndex(),
			clicked_pos_x, clicked_pos_y);
	    }
	}
    }

    public void mouseReleased(MouseEvent m_ev) {
	if (eventDisabled)
	    return;
	int clicked_pos_x, clicked_pos_y;
	clicked_pos_x = m_ev.getX() / cellsize;
	clicked_pos_y = m_ev.getY() / cellsize;

	new_WP_x = new_GP_x = clicked_pos_x;
	new_WP_y = new_GP_y = clicked_pos_y;

	// Waypoint MOUSE_RIGHT_BUTTON Click
	if (Utils.isRightClick(m_ev) && !mapSettingMode) {
	    wp = game.Vehicle_Waypoint_Check(clicked_pos_x, clicked_pos_y);
	    if (wp != null) {
		setV(wp.getV());
		WPRightClickedMode = true;
		showPopup(this, m_ev.getX(), m_ev.getY(), getV());
		WPRightClickedMode = false;
	    }
	}

	// Menu - Goal point set (either adding or changing)
	if (Utils.isLeftClick(m_ev) && mapSettingMode && vehicleGoalMode
		&& !vehicleWPAddMode && !vehicleWPChangeMode
		&& !vehicleWPDelMode) {
	    if (getV().isAssignededTarget(clicked_pos_x, clicked_pos_y)) {
		lsnr.showMessageOnTopOfMap(
			"You cannot assign a vehicle to a target that is already assigned to another vehicle",
			10);
	    } else if (getV().getPath().size() == 0) {
		getV().addGoal(clicked_pos_x, clicked_pos_y);
	    } else {
		getV().changeGoal(getV().getPath().getLast(), clicked_pos_x,
			clicked_pos_y);
	    }
	    setClear();
	}

	// DRAG - GOAL CHANGE
	if (Utils.isLeftClick(m_ev) && !vehicleWPAddMode
		&& vehicleGoalChangeMode && !vehicleWPDelMode) {
	    if (getV().isAssignededTarget(clicked_pos_x, clicked_pos_y)) {
		lsnr.showMessageOnTopOfMap(
			"You cannot assign a vehicle to a target that is already assigned to another vehicle",
			10);
	    } else if (getV().getPath().size() == 0)
		getV().addGoal(new_GP_x, new_GP_y);
	    else
		getV().changeGoal(new int[] { ex_GP_x, ex_GP_y }, new_GP_x,
			new_GP_y);
	    setClear();

	    // System.out.println("Vehicle deselected by drag changing goal.");
	    vehicleGoalChangeMode = false;
	    gp = null;
	}

	// Menu - Add WP
	if (Utils.isLeftClick(m_ev) && mapSettingMode && !vehicleGoalMode
		&& vehicleWPAddMode && !vehicleWPChangeMode
		&& !vehicleWPDelMode) {
	    int idx = getV().addWaypoint(clicked_pos_x, clicked_pos_y);
	    just_added_WP = new int[] { clicked_pos_x, clicked_pos_y, idx };
	    setWPNextPrev(idx);
	    showPopup(this, m_ev.getX(), m_ev.getY(), game.getVehicleList()
		    .getVehicle(clicked_pos_x, clicked_pos_y));
	}

	// DRAG - WayPoint CHANGE
	if (Utils.isLeftClick(m_ev) && !vehicleWPAddMode && vehicleWPChangeMode
		&& !vehicleWPDelMode) {
	    getV().changeWaypoint(ex_WP_x, ex_WP_y, new_WP_x, new_WP_y);
	    // System.out.println("Vehicle deselected by changing waypoint.");
	    vehicleWPChangeMode = false;
	    wp = null;
	    lsnr.EVT_WP_MoveWP_End(selectedVehicle.getIndex(), clicked_pos_x,
		    clicked_pos_y);
	}

	// Delete WP
	if (Utils.isLeftClick(m_ev) && mapSettingMode && !vehicleGoalMode
		&& !vehicleWPAddMode && !vehicleWPChangeMode
		&& vehicleWPDelMode) {
	    if (wp == null) {
		setClear();
		return;
	    } else {
		selectedVehicle.delWaypoint(wp.getX(), wp.getY());
		wp = null;
		lsnr.EVT_WP_DeleteWP_End(selectedVehicle.getIndex(), wp.getX(),
			wp.getY());
		setClear();
		// System.out.println("Vehicle deselected by deleting waypoint");
	    }
	}
	repaint();

	dragGPMode = dragWPMode = false;
	setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void mouseEntered(MouseEvent m_ev) {
    }

    public void mouseExited(MouseEvent m_ev) {
    }

    // MouseMotionListener Interface
    public void mouseMoved(MouseEvent mm_ev) {
	if (Game.collisionHighlightEnabled && Game.automationEnabled) {
	    if (this.isCursorOnCollisionArea(new int[] {
		    mm_ev.getX() / cellsize, mm_ev.getY() / cellsize })) {
		Cursor normalCursor = new Cursor(Cursor.HAND_CURSOR);
		setCursor(normalCursor);
	    } else {
		Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		setCursor(normalCursor);
	    }
	}
    }

    public void mouseDragged(MouseEvent mm_ev) {
	if (selectedVehicle == null)
	    return;
	if (gp != null) {
	    dragGPMode = true;
	    if (selectedVehicle.hasWaypoint())
		drag_from = selectedVehicle.getPathAt(selectedVehicle
			.getPathSize() - 2); // coord
	    else
		drag_from = new int[] { selectedVehicle.getX(),
			selectedVehicle.getY() }; // coord
	    drag_to = new int[] { mm_ev.getX(), mm_ev.getY() }; // pxl

	    int w = MySize.SIZE_CELL;
	    region = getClip(drag_from[0] * w, drag_from[1] * w, drag_to[0],
		    drag_to[1], drag_to_prev[0], drag_to_prev[1]);
	    // printCoord("GP", region[0], region[1], region[2], region[3]);
	    repaint(region[0], region[1], region[2], region[3]);
	    drag_to_prev = drag_to;
	    // System.out.println("MOUSE (" + drag_to[0] +","+drag_to[1]+")");
	    // repaint();
	} else if (wp != null && selectedVehicle != null) {
	    dragWPMode = true;
	    // to solve the problem from the tabletop.
	    if (wp.getIdx() + 1 >= selectedVehicle.getPathSize())
		return;
	    if (wp.getIdx() > 0)
		drag_from = selectedVehicle.getPathAt(wp.getIdx() - 1); // coord
	    else
		drag_from = new int[] { selectedVehicle.getX(),
			selectedVehicle.getY() }; // coord
	    drag_to = new int[] { mm_ev.getX(), mm_ev.getY() }; // pxl
	    drag_next = selectedVehicle.getPathAt(wp.getIdx() + 1); // coord
	    int[] wp_before_drag = selectedVehicle.getPathAt(wp.getIdx()); // coord

	    int w = MySize.SIZE_CELL;
	    region = getClip(wp_before_drag[0], wp_before_drag[1], drag_from[0]
		    * w, drag_from[1] * w, drag_to[0], drag_to[1],
		    drag_to_prev[0] * w, drag_to_prev[1] * w, drag_next[0] * w,
		    drag_next[1] * w);
	    // printCoord("WP drag", region[0], region[1], region[2],
	    // region[3]);
	    repaint(region[0], region[1], region[2], region[3]);
	    drag_to_prev = drag_to;
	    // repaint();
	}
    }

    // PopupMenuListener interface
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
	if (mapSettingMode && vehicleWPAddMode) {
	    getV().delWaypoint(just_added_WP[0], just_added_WP[1]);
	    lsnr.EVT_WP_AddWP_Cancel(selectedVehicle.getIndex());
	}
	setClear();
	repaint();
    }

    private int[] getClip(int x1, int y1, int x2, int y2, int x3, int y3) {
	int w = MySize.SIZE_WAYPOINT_PXL * 2;
	int[] ret = new int[] { Utils.min(x1, x2, x3) - w,
		Utils.min(y1, y2, y3) - w, Utils.max(x1, x2, x3) + w,
		Utils.max(y1, y2, y3) + w };
	return ret;
    }

    private int[] getClip(int x1, int y1, int x2, int y2, int x3, int y3,
	    int x4, int y4, int x5, int y5) {
	int w = MySize.SIZE_WAYPOINT_PXL * 2;
	int[] ret = new int[] { Utils.min(x1, x2, x3, x4, x5) - w,
		Utils.min(y1, y2, y3, y4, y5) - w,
		Utils.max(x1, x2, x3, x4, x5) + w,
		Utils.max(y1, y2, y3, y4, y5) + w };
	return ret;
    }

    private void updateCollisionMap() {
	collisionMap = CollisionMap.GetVHCollision(this.getHeight(),
		this.getWidth(), map.getListHazard(), game.getVehicleList());

	ArrayList<CollisionZone> newCollisionZone = CollisionMap
		.GetVVCollision(this.getHeight(), this.getHeight(),
			game.getVehicleList());

	if (IsMalfunctionalAutoRequired()) {
	    CollisionZone fakeCZ = CollisionMap.FakeCollisionZone(
		    this.getHeight(), this.getWidth(), game.getVehicleList(),
		    newCollisionZone, this.fakeCollisionZones);

	    if (fakeCZ != null) {
		this.fakeCollisionZones.add(fakeCZ);
	    }
	}

	if (this.fakeCollisionZones.size() > 0) {
	    newCollisionZone.addAll(fakeCollisionZones);
	}

	CollisionZoneDiff.refresh(collisionZones, newCollisionZone);
	for (CollisionZone cz : CollisionZoneDiff.App) {
	    if (cz.isFaked) {
		this.cMalAuto++;
		System.out.println(this.cMalAuto + "," + this.cGoodAuto);
	    } else {
		this.cGoodAuto++;
		System.out.println(this.cMalAuto + "," + this.cGoodAuto);
	    }
	}

	this.collisionZones = newCollisionZone;
    }

    // when cursor is on collision zone, return the involved vehicle of pointed
    // zone. like {1,2,3}
    public synchronized Integer[] getCollisionZoneVIds(int[] cursorPos) {
	HashSet<Integer> vIds = new HashSet<Integer>();
	for (CollisionZone cz : this.collisionZones) {
	    // if (cursorPos[0] < cz.collisionArea[2] && cursorPos[0] >
	    // cz.collisionArea[0] && cursorPos[1] > cz.collisionArea[1]
	    // && cursorPos[1] < cz.collisionArea[3])
	    // if(CollisionMap.pointInPolygon(cz.collisionArea[0],
	    // cz.collisionArea[1], cursorPos[0], cursorPos[1]))
	    if (cz.collisionArea.contains(cursorPos[0], cursorPos[1])) {
		for (int vId : cz.involvedVehicles) {
		    vIds.add(vId);
		}
	    }
	}

	return (Integer[]) vIds.toArray(new Integer[vIds.size()]);
    }

    public synchronized boolean isCursorOnCollisionArea(int[] cursorPos) {
	for (CollisionZone cz : this.collisionZones) {
	    // if (cursorPos[0] < cz.collisionArea[2] && cursorPos[0] >
	    // cz.collisionArea[0] && cursorPos[1] > cz.collisionArea[1]
	    // && cursorPos[1] < cz.collisionArea[3])
	    // if(CollisionMap.pointInPolygon(cz.collisionArea[0],
	    // cz.collisionArea[1], cursorPos[0], cursorPos[1]))
	    if (cz.collisionArea.contains(cursorPos[0], cursorPos[1])) {
		return true;
	    }
	}
	return false;
    }
}

class PaintComponent {
    public void fillPolygon(Graphics2D g, Integer[] polyX, Integer[] polyY,
	    int SIZE_CELL) {
	g.setColor(MyColor.COLOR_COLLISION_TRACK);

	int[] polyXPrimitive = new int[polyX.length];
	for (int i = 0; i < polyX.length; i++) {
	    polyXPrimitive[i] = polyX[i] * SIZE_CELL;
	}

	int[] polyYPrimitive = new int[polyY.length];
	for (int i = 0; i < polyY.length; i++) {
	    polyYPrimitive[i] = polyY[i] * SIZE_CELL;
	}

	g.fillPolygon(polyXPrimitive, polyYPrimitive, polyYPrimitive.length);
    }

    public void paintRect(Graphics2D g, int x1, int y1, int x2, int y2,
	    int SIZE_CELL, boolean isFaked) {

	g.setColor(new Color(255, 0, 0, 65));
	g.fillRect(x1 * SIZE_CELL, y1 * SIZE_CELL, (x2 - x1) * SIZE_CELL,
		(y2 - y1) * SIZE_CELL);
    }

    public void paintOval(Graphics2D g, int x, int y, int SIZE_CELL,
	    int object_size, Color fill_color) {
	g.setColor(fill_color);
	g.fillOval((x - Math.round(object_size / SIZE_CELL / 2)) * SIZE_CELL,
		(y - Math.round(object_size / SIZE_CELL / 2)) * SIZE_CELL,
		object_size, object_size);
    }

    public void paintPolygon(Graphics2D g, int x, int y, int SIZE_CELL,
	    int object_size, Color draw_color, Color fill_color) {
	int[] px, py;
	px = new int[] { x * SIZE_CELL - (int) (object_size / 1.414),
		x * SIZE_CELL, x * SIZE_CELL + (int) (object_size / 1.414),
		x * SIZE_CELL };
	py = new int[] { y * SIZE_CELL,
		y * SIZE_CELL - (int) (object_size / 1.414), y * SIZE_CELL,
		y * SIZE_CELL + (int) (object_size / 1.414) };

	g.setColor(draw_color);
	g.drawPolygon(px, py, 4);
	g.setColor(fill_color);
	g.fillPolygon(px, py, 4);
    }

    public void paintHighlight(Graphics2D g, int x, int y, int SIZE_CELL,
	    int half_SIZE_CELL, int object_size, int ruler_size,
	    Color highlight_color, BasicStroke stroke, BasicStroke wide_stroke) {

	g.setColor(highlight_color);
	g.drawOval((x - Math.round(object_size / SIZE_CELL / 2)) * SIZE_CELL,
		(y - Math.round(object_size / SIZE_CELL / 2)) * SIZE_CELL,
		object_size, object_size);

	g.setStroke(wide_stroke);
	g.drawLine((x - Math.round(object_size / SIZE_CELL / 2) - ruler_size)
		* SIZE_CELL + half_SIZE_CELL, (y) * SIZE_CELL + half_SIZE_CELL,
		(x - Math.round(object_size / SIZE_CELL / 2) + ruler_size)
			* SIZE_CELL + half_SIZE_CELL, (y) * SIZE_CELL
			+ half_SIZE_CELL);
	g.drawLine((x + Math.round(object_size / SIZE_CELL / 2) - ruler_size)
		* SIZE_CELL + half_SIZE_CELL, (y) * SIZE_CELL + half_SIZE_CELL,
		(x + Math.round(object_size / SIZE_CELL / 2) + ruler_size)
			* SIZE_CELL + half_SIZE_CELL, (y) * SIZE_CELL
			+ half_SIZE_CELL);
	g.drawLine((x) * SIZE_CELL + half_SIZE_CELL,
		(y - Math.round(object_size / SIZE_CELL / 2) - ruler_size)
			* SIZE_CELL + half_SIZE_CELL, (x) * SIZE_CELL
			+ half_SIZE_CELL,
		(y - Math.round(object_size / SIZE_CELL / 2) + ruler_size)
			* SIZE_CELL + half_SIZE_CELL);
	g.drawLine((x) * SIZE_CELL + half_SIZE_CELL,
		(y + Math.round(object_size / SIZE_CELL / 2) - ruler_size)
			* SIZE_CELL + half_SIZE_CELL, (x) * SIZE_CELL
			+ half_SIZE_CELL,
		(y + Math.round(object_size / SIZE_CELL / 2) + ruler_size)
			* SIZE_CELL + half_SIZE_CELL);
	g.setStroke(stroke);
    }

    public void paintString(Graphics2D g, int x, int y, int SIZE_CELL,
	    Color color, Font font, String str) {
	g.setColor(color);
	g.setFont(font);
	g.drawString("" + str, x * SIZE_CELL, y * SIZE_CELL);
    }

    public void paintArc(Graphics2D g, int x, int y, int SIZE_CELL,
	    int half_SIZE_CELL, int ruler_size, int object_width_size,
	    int object_height_size, Color draw_color, Color fill_color,
	    String type) {
	if (type == Vehicle.TYPE_UAV) {
	    g.setColor(draw_color);
	    g.drawArc((x - Math.round(object_width_size / SIZE_CELL / 2))
		    * SIZE_CELL + half_SIZE_CELL, y * SIZE_CELL - ruler_size
		    - half_SIZE_CELL, object_width_size, object_height_size, 0,
		    180);

	    g.setColor(fill_color);
	    g.fillArc((x - Math.round(object_width_size / SIZE_CELL / 2))
		    * SIZE_CELL + half_SIZE_CELL, y * SIZE_CELL - ruler_size
		    - half_SIZE_CELL, object_width_size, object_height_size, 0,
		    180);
	} else if (type == Vehicle.TYPE_UUV) {
	    g.setColor(draw_color);
	    g.drawArc((x - Math.round(object_width_size / SIZE_CELL / 2))
		    * SIZE_CELL,
		    y * SIZE_CELL - Math.round(object_height_size / 2)
			    - ruler_size, object_width_size,
		    object_height_size, 180, 180);

	    g.setColor(fill_color);
	    g.fillArc((x - Math.round(object_width_size / SIZE_CELL / 2))
		    * SIZE_CELL,
		    y * SIZE_CELL - Math.round(object_height_size / 2)
			    - ruler_size, object_width_size,
		    object_height_size, 180, 180);
	}
    }

}