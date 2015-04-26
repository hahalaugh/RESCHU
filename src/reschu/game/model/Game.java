package reschu.game.model;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import reschu.app.ScenarioConfig;
import reschu.constants.*;
import reschu.data.DataRecorder;
import reschu.database.DBWriter;
import reschu.game.algorithm.Geo;
import reschu.game.controller.GUI_Listener;
import reschu.game.controller.Reschu;
import reschu.game.utils.FileReader;
import reschu.game.view.FrameEnd;
import reschu.game.view.FrameStart;
import reschu.game.view.PanelMsgBoard;
import reschu.game.view.PanelMsgBoard.MessageType;

public class Game implements Runnable, ActionListener {
    static public int TIME_TOTAL_GAME = ScenarioConfig.GetInstance()
	    .get_gameTime() * 60 * MySpeed.SPEED_CLOCK;
    static public int HALF_TOTAL_GAME = TIME_TOTAL_GAME / 2;

    private double PROBABILITY_TARGET_VISIBILITY; // The higher, the more
    // visible target

    private int nTargetAreaTotal = (Reschu.tutorial() || Reschu.extraTutorial()) ? MyGame.nTARGET_AREA_TOTAL_TUTORIAL
	    : MyGame.nTARGET_AREA_TOTAL;
    final private int[] DB_BY_PIXEL = new int[] { 480, 480, 470, 470, 470, 470,
	    460, 460, 450, 450, 450, 440, 440, 430, 430, 430, 420, 410, 410,
	    410, 410, 410, 410, 410, 410, 410, 410, 420, 420, 420, 430, 430,
	    430, 430, 430, 430, 430, 420, 420, 420, 420, 420, 420, 420, 420,
	    420, 430, 430, 440, 440, 450, 450, 460, 460, 460, 460, 470, 470,
	    480, 480, 480, 490, 490, 490, 490, 500, 500, 500, 510, 510, 510,
	    510, 510, 520, 520, 520, 520, 520, 520, 530, 530, 540, 550, 550,
	    560, 560, 570, 570, 580, 580, 590, 590, 590, 600 };

    private int[] DB = new int[MySize.height];

    static public Calendar cal = Calendar.getInstance();

    private VehicleList vehicle_list;
    private PayloadList hintedPayloadList;
    private PayloadList maybePayloadList;
    private PayloadList noHintPayloadList;

    // Consists of three payload tasks on one image
    private PayloadList tutorialPayloadList;

    public Map map;
    public int ex_pos_x, ex_pos_y;
    public static boolean collisionHighlightEnabled;
    public static boolean automationEnabled;
    private Vehicle v;
    private GUI_Listener lsnr;
    private Timer tmr_clock;
    private int elapsedTime;
    private Vehicle currentPayloadVehicle;
    private boolean vehicleColorFlashFlag = true;
    private DBWriter dbWriter;
    private StructTargetNamePool[] targetNamePool;
    private boolean[] targetVisibilityPool;
    private int targetVisibilityPool_index;
    private double vvCollisionDamage = 0;
    private double vhCollisionDamage = 0;
    private int correctHit = 0;
    private int inCorrectHit = 0;

    // private boolean firstHalf = true;

    public Random rnd = new Random();

    private final int workload;
    private final int automation;
    public String mapLogDescription;

    public int getWorkload() {
	return workload;
    }

    public int getAutomation() {
	return automation;
    }

    public synchronized int getElapsedTime() {
	return elapsedTime;
    }

    public Game(GUI_Listener l, int scenario, int workload, int automation) {
	if (Reschu.train())
	    Game.TIME_TOTAL_GAME *= 2;

	if (Reschu._database) {
	    new Thread(new Runnable() {
		public void run() {
		    dbWriter = new DBWriter();
		    dbWriter.CreateTable(Reschu._username);
		}
	    }).start();
	}

	this.workload = workload;
	this.automation = automation;

	lsnr = l;
	FrameStart frmStart = new FrameStart(lsnr);
	frmStart.setSize(400, 300);
	frmStart.setLocation(300, 300);
	frmStart.setAlwaysOnTop(true);
	frmStart.setVisible(true);

	// Juntao: Adjust random seed based on workload situation
	int rndSeed = getSeedNum(scenario);
	if (!Reschu.train() && !Reschu.tutorial() && !Reschu.extraTutorial()) {
	    if (this.workload == MyGame.WORKLOAD_HIGH) {
		rndSeed += 1;
	    } else {
		rndSeed += 2;
	    }
	}
	rnd.setSeed(rndSeed);

	// setProbability_Target_Visibility(scenario);
	PROBABILITY_TARGET_VISIBILITY = 1;

	PanelMsgBoard.Msg("Game Started", MessageType.SystemInfo);
	tmr_clock = new Timer(MySpeed.SPEED_TIMER, this);
	for (int i = 0; i < DB.length; i++)
	    DB[i] = DB_BY_PIXEL[Math.round(i / 5)] / MySize.SIZE_CELL;

	vehicle_list = new VehicleList(this);
	// @change-removed passing random object to PayloatList() 2008-04-01

	boolean order = true;
	if (Reschu.tutorial() || Reschu.extraTutorial() || Reschu.train()) {
	    order = true;
	} else if (this.workload == MyGame.WORKLOAD_HIGH) {
	    order = true;
	} else {
	    order = false;
	}

	hintedPayloadList = new PayloadList(order);
	maybePayloadList = new PayloadList(order);
	noHintPayloadList = new PayloadList(order);
	tutorialPayloadList = new PayloadList(order);

	map = new Map(MySize.width, MySize.height, this, lsnr);
	elapsedTime = 0;

	targetNamePool = new StructTargetNamePool[nTargetAreaTotal];
	for (int i = 0; i < nTargetAreaTotal; i++)
	    targetNamePool[i] = new StructTargetNamePool("" + (char) (65 + i));

	targetVisibilityPool_index = 0;
	targetVisibilityPool = new boolean[nTargetAreaTotal];
	setTargetVisibility();

	setMap();

	// normalize randomizer
	if (Reschu.tutorial() || Reschu.extraTutorial()) {
	    for (int i = 0; i < 21; i++)
		rnd.nextInt(1);
	    map.setHazardArea(rnd);
	    map.setTargetArea_TEMPORARY_FOR_TUTORIAL_BY_CARL(rnd);
	} else {
	    map.setHazardArea(rnd);
	    try {
		map.setTargetArea(rnd);
	    } catch (UserDefinedException e) {
		e.printStackTrace();
	    }
	}

	setVehicle(scenario, workload);
	setPayload();
    }

    public void setListener(GUI_Listener l) {
	lsnr = l;
    }

    public DBWriter getDBWriter() {
	return dbWriter;
    }

    private int getSeedNum(int scenario) {
	if (Reschu.tutorial() || Reschu.train() || Reschu.extraTutorial()) {
	    switch (scenario) {
	    case 1:
	    case 2:
	    case 3:
	    case 4:
		return 50;
	    default:
		return 0;
	    }
	} else {
	    switch (scenario) {
	    /*
	     * @changed 2008-06-29 Carl
	     * 
	     * case 1: return 10; case 2: return 20; case 3: return 30; case 4:
	     * return 40; default: return 0;
	     */
	    case 1:
		return 10;
	    case 2:
		return 20;
	    case 3:
		return 30;
	    case 4:
		return 40;
	    default:
		return 0;
	    }
	}
    }

    /*
     * private void setProbability_Target_Visibility(int scenario) { switch
     * (scenario) { case 1: PROBABILITY_TARGET_VISIBILITY = 1; break; case 2:
     * PROBABILITY_TARGET_VISIBILITY = 1; break; case 3:
     * PROBABILITY_TARGET_VISIBILITY = 0.5; break; case 4:
     * PROBABILITY_TARGET_VISIBILITY = 0.7; break; default:
     * PROBABILITY_TARGET_VISIBILITY = 1; break; } }
     */
    public void setVehicle(int scenario, int workload) {
	int speed = 0;
	if (workload == MyGameMode.HIGH_WORKLOAD) {
	    // High - > Low
	    speed = ScenarioConfig.GetInstance().get_fastVehicleSpeed();
	} else {
	    // Low - > High
	    speed = ScenarioConfig.GetInstance().get_slowVehicleSpeed();
	}

	try {
	    switch (scenario) {
	    case 1:
		vehicle_list.addVehicle(1, Vehicle.TYPE_UAV, "Fire Scout A",
			Vehicle.PAYLOAD_ISR, speed / MySpeed.SPEED_CONTROL,
			rnd, map, lsnr, this);

		if (Reschu.extraTutorial()) {
		    vehicle_list.addVehicle(2, Vehicle.TYPE_UAV,
			    "Fire Scout B", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		} else if (Reschu.expermient() || Reschu.train()) {
		    vehicle_list.addVehicle(2, Vehicle.TYPE_UAV,
			    "Fire Scout B", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		    vehicle_list.addVehicle(3, Vehicle.TYPE_UAV,
			    "Fire Scout C", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		    vehicle_list.addVehicle(4, Vehicle.TYPE_UAV,
			    "Fire Scout D", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		    vehicle_list.addVehicle(5, Vehicle.TYPE_UAV,
			    "Fire Scout E", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		}

		collisionHighlightEnabled = false;
		automationEnabled = false;
		break;
	    case 2:

		vehicle_list.addVehicle(1, Vehicle.TYPE_UAV, "Fire Scout A",
			Vehicle.PAYLOAD_ISR, speed / MySpeed.SPEED_CONTROL,
			rnd, map, lsnr, this);
		if (Reschu.extraTutorial()) {
		    vehicle_list.addVehicle(2, Vehicle.TYPE_UAV,
			    "Fire Scout B", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		} else if (Reschu.expermient() || Reschu.train()) {
		    vehicle_list.addVehicle(2, Vehicle.TYPE_UAV,
			    "Fire Scout B", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		    vehicle_list.addVehicle(3, Vehicle.TYPE_UAV,
			    "Fire Scout C", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		    vehicle_list.addVehicle(4, Vehicle.TYPE_UAV,
			    "Fire Scout D", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		    vehicle_list.addVehicle(5, Vehicle.TYPE_UAV,
			    "Fire Scout E", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		}

		collisionHighlightEnabled = false;
		automationEnabled = true;
		break;
	    case 3:
		vehicle_list.addVehicle(1, Vehicle.TYPE_UAV, "Fire Scout A",
			Vehicle.PAYLOAD_ISR, speed / MySpeed.SPEED_CONTROL,
			rnd, map, lsnr, this);
		if (Reschu.extraTutorial()) {
		    vehicle_list.addVehicle(2, Vehicle.TYPE_UAV,
			    "Fire Scout B", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		} else if (Reschu.expermient() || Reschu.train()) {
		    vehicle_list.addVehicle(2, Vehicle.TYPE_UAV,
			    "Fire Scout B", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		    vehicle_list.addVehicle(3, Vehicle.TYPE_UAV,
			    "Fire Scout C", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		    vehicle_list.addVehicle(4, Vehicle.TYPE_UAV,
			    "Fire Scout D", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		    vehicle_list.addVehicle(5, Vehicle.TYPE_UAV,
			    "Fire Scout E", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		}
		collisionHighlightEnabled = true;
		automationEnabled = false;
		break;
	    case 4:
		vehicle_list.addVehicle(1, Vehicle.TYPE_UAV, "Fire Scout A",
			Vehicle.PAYLOAD_ISR, speed / MySpeed.SPEED_CONTROL,
			rnd, map, lsnr, this);
		if (Reschu.extraTutorial()) {
		    vehicle_list.addVehicle(2, Vehicle.TYPE_UAV,
			    "Fire Scout B", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		} else if (Reschu.expermient() || Reschu.train()) {
		    vehicle_list.addVehicle(2, Vehicle.TYPE_UAV,
			    "Fire Scout B", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		    vehicle_list.addVehicle(3, Vehicle.TYPE_UAV,
			    "Fire Scout C", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		    vehicle_list.addVehicle(4, Vehicle.TYPE_UAV,
			    "Fire Scout D", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		    vehicle_list.addVehicle(5, Vehicle.TYPE_UAV,
			    "Fire Scout E", Vehicle.PAYLOAD_ISR, speed
				    / MySpeed.SPEED_CONTROL, rnd, map, lsnr,
			    this);
		}
		collisionHighlightEnabled = true;
		automationEnabled = true;
		break;
	    default:
		vehicle_list.addVehicle(1, Vehicle.TYPE_UAV, Vehicle.TYPE_UAV
			+ "_1", "ISR", 500, rnd, map, lsnr, this);
		collisionHighlightEnabled = false;
		automationEnabled = false;
		break;
	    }

	} catch (UserDefinedException e) {
	    e.printStackTrace();
	}
    }

    public void initializePayload(PayloadList list, String URL) {
	int lineNum = -1, idx;
	String vec_type, mis_type, stmt, isPreS, autoLevel, result;
	int[] loc;

	
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new InputStreamReader(new FileInputStream(URL), "UTF8"));
	} catch (UnsupportedEncodingException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	} catch (FileNotFoundException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
	String aLine = "";

	try {
	    while ((aLine = br.readLine()) != null) {
	        lineNum++;
	        String[] a = aLine.split("#");
	        if (a.length == 9) {
	    	if (!FileReader.isNumber(a[0].trim())
	    		|| !FileReader.isNumber(a[3].trim())
	    		|| !FileReader.isNumber(a[4].trim())
	    		|| !FileReader.isNumber(a[6].trim())
	    		|| !FileReader.isNumber(a[7].trim())
	    		|| !FileReader.isNumber(a[8].trim())) {
	    	    System.err
	    		    .println("ERROR: Failed to load payload at line ("
	    			    + lineNum + "), check the numbers.");
	    	    continue;
	    	}
	    	if (!Vehicle.isVehicleType(a[1].trim())) {
	    	    System.err
	    		    .println("ERROR: Failed to load payload at line ("
	    			    + lineNum + "), check the vehicle type ("
	    			    + a[1].trim() + ").");
	    	    continue;
	    	}
	    	if (!Target.isTargetType(a[2].trim())) {
	    	    System.err
	    		    .println("ERROR: Failed to load payload at line ("
	    			    + lineNum + "), check the mission type ("
	    			    + a[2].trim() + ").");
	    	    continue;
	    	}

	    	idx = Integer.parseInt(a[0].trim());
	    	vec_type = a[1].trim();
	    	mis_type = a[2].trim();
	    	loc = new int[] { Integer.parseInt(a[3].trim()),
	    		Integer.parseInt(a[4].trim()) };
	    	stmt = a[5].trim();
	    	isPreS = a[6].trim();
	    	autoLevel = a[7].trim();
	    	result = a[8].trim();
	    	list.addPayload(idx, vec_type, mis_type, loc, stmt,
	    		Integer.parseInt(isPreS), Integer.parseInt(autoLevel),
	    		Integer.parseInt(result));
	        } else {
	    	System.err.println("ERROR: Failed to load payload at line ("
	    		+ lineNum + "), check the delimeter.");
	        }
	    }
	} catch (NumberFormatException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	try {
	    br.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void setPayload() {

	if (Reschu.tutorial() || Reschu.extraTutorial() || Reschu.train()) {
	    this.initializePayload(this.tutorialPayloadList,
		    MyURL.LOCAL_TUTORIAL_TASK);
	} else {
	    this.initializePayload(this.hintedPayloadList,
		    MyURL.LOCAL_HINTED_TASK);
	    if (this.automation == MyGame.AUTO_LOW) {
		this.initializePayload(this.maybePayloadList,
			MyURL.LOCAL_MAYBE_TASK_BAD);
		System.out.println(MyURL.URL_MAYBE_TASK_BAD);
	    } else {
		this.initializePayload(this.maybePayloadList,
			MyURL.LOCAL_MAYBE_TASK_GOOD);
	    }
	    this.initializePayload(this.noHintPayloadList,
		    MyURL.LOCAL_NO_HINT_TASK);
	}

    }

    private void setMap() {
	for (int i = 0; i < MySize.height; i++) {
	    for (int j = 0; j < DB[i]; j++)
		map.setCellType(j, i, MyGame.LAND);
	    map.setCellType(DB[i], i, MyGame.SEASHORE);
	    for (int j = DB[i] + 1; j < MySize.width; j++)
		map.setCellType(j, i, MyGame.SEA);
	}
    }

    public void run() {
	AutoTargetAssignAll();
	tmr_clock.start();

	if (!Reschu._database)
	    return;

	if (Reschu.expermient()) {
	    lsnr.EVT_System_GameStart();

	    // below DBwrites are different from writing to the user's table.
	    // we record the login info to the USER table,
	    // which contains infos of all the users
	    getDBWriter().UserTable_SetGameStart(Reschu._username);
	    getDBWriter().UserTable_SetTime(Reschu._username);
	}
    }

    public boolean isRunning() {
	return tmr_clock.isRunning();
    }

    public void stop() {
	tmr_clock.stop();
    }

    public VehicleList getVehicleList() {
	return vehicle_list;
    }

    public PayloadList getTutorialPayloadList() {
	return this.tutorialPayloadList;
    }

    public PayloadList getHintedPayloadList() {
	return hintedPayloadList;
    }

    public PayloadList getMaybePayloadList() {
	return maybePayloadList;
    }

    public PayloadList getNoHintPayloadList() {
	return noHintPayloadList;
    }

    public void vehicle_location_change() {
	lsnr.vehicle_location_changed();
    }

    public Vehicle Vechicle_Location_Check(int x, int y) {
	for (int i = 0; i < vehicle_list.size(); i++) {
	    int v_x = vehicle_list.getVehicle(i).getX();
	    int v_y = vehicle_list.getVehicle(i).getY();
	    int w = Math
		    .round(MySize.SIZE_VEHICLE_WIDTH_PXL / MySize.SIZE_CELL);
	    int h = Math.round(MySize.SIZE_VEHICLE_HEIGHT_PXL
		    / MySize.SIZE_CELL);
	    for (int j = -w; j < w; j++)
		for (int k = -h; k < h; k++)
		    if (v_x == x + j && v_y == y + k)
			return vehicle_list.getVehicle(i);
	}
	return null;
    }

    public StructSelectedPoint Vehicle_Goal_Check(int x, int y) {
	Vehicle v;
	int w = Math.round(MySize.SIZE_WAYPOINT_PXL / MySize.SIZE_CELL);
	for (int i = 0; i < vehicle_list.size(); i++) {
	    v = vehicle_list.getVehicle(i);
	    if (v.hasGoal()) {
		int w_x = (int) v.getPath().get(v.getPathSize() - 1)[0];
		int w_y = (int) v.getPath().get(v.getPathSize() - 1)[1];
		for (int m = -w; m < w; m++)
		    for (int n = -w; n < w; n++)
			if (w_x == x + m && w_y == y + n)
			    return new StructSelectedPoint(v, w_x, w_y, 0); // 0
		// =
		// no
		// meaning
	    }
	}
	return null;
    }

    public StructSelectedPoint Vehicle_Waypoint_Check(int x, int y) {
	Vehicle v;
	int w = Math.round(MySize.SIZE_WAYPOINT_PXL / MySize.SIZE_CELL);
	for (int i = 0; i < vehicle_list.size(); i++) {
	    v = vehicle_list.getVehicle(i);
	    if (v.getPath().size() > 1) {
		for (int j = 0; j < v.getPath().size() - 1; j++) {
		    int w_x = (int) v.getPath().get(j)[0];
		    int w_y = (int) v.getPath().get(j)[1];
		    for (int m = -w; m < w; m++)
			for (int n = -w; n < w; n++)
			    if (w_x == x + m && w_y == y + n)
				return new StructSelectedPoint(v, w_x, w_y, j);
		}
	    }
	}
	return null;
    }

    public synchronized void payloadCorrect() {
	correctHit++;
    }

    public synchronized void payloadIncorrect() {
	inCorrectHit++;
    }

    public synchronized void vvDamage(double i) {
	vvCollisionDamage += i;
    }

    public int getCorrectHit() {
	return correctHit;
    }

    public int getInCorrectHit() {
	return inCorrectHit;
    }

    public double getVVDamage() {
	return this.vvCollisionDamage;
    }

    public double getVHDamage() {
	return this.vhCollisionDamage;
    }

    public Vehicle getCurrentPayloadVehicle() {
	return currentPayloadVehicle;
    }

    public void setCurrentPayloadVehicle(Vehicle v) {
	currentPayloadVehicle = v;
    }

    public void clearCurrentPayloadVehicle() {
	currentPayloadVehicle = null;
    }

    private void AutoTargetAssignAll() {
	Vehicle v;
	for (int i = 0; i < vehicle_list.size(); i++) {
	    v = vehicle_list.getVehicle(i);
	    AutoTargetAssign(v);
	}
    }

    public void AutoTargetAssign(Vehicle v) {
	if (v.getPath().size() == 0 && map.getAvailableTarget() > 0) {
	    Target target = null;

	    if (v.getType() == Vehicle.TYPE_UUV) {
		for (int i = 0; i < map.getListUnassignedTarget().size(); i++) {
		    if (map.getListUnassignedTarget().get(i).getType() == v
			    .getType()
			    && map.getListUnassignedTarget().get(i).isVisible()) {
			target = map.getListUnassignedTarget().get(i);
			v.addGoal(target.getPos()[0], target.getPos()[1]);
			if (elapsedTime != 0)
			    lsnr.EVT_GP_SetGP_by_System(v.getIndex(),
				    target.getName());
			break;
		    }
		}
	    } else if (v.getPayload() == Vehicle.PAYLOAD_COM) {
		for (int i = 0; i < map.getListUnassignedTarget().size(); i++) {
		    if (!map.getListUnassignedTarget().get(i).isVisible()) {
			target = map.getListUnassignedTarget().get(i);
			v.addGoal(target.getPos()[0], target.getPos()[1]);
			if (elapsedTime != 0)
			    lsnr.EVT_GP_SetGP_by_System(v.getIndex(),
				    target.getName());
			break;
		    }
		}
	    } else {
		for (int i = 0; i < map.getListUnassignedTarget().size(); i++) {
		    if (map.getListUnassignedTarget().get(i).isVisible()) {
			target = map.getListUnassignedTarget().get(i);
			v.addGoal(target.getPos()[0], target.getPos()[1]);
			if (elapsedTime != 0)
			    lsnr.EVT_GP_SetGP_by_System(v.getIndex(),
				    target.getName());
			break;
		    }
		}
	    }
	}
    }

    /*
     * public void switchGameProcedure() { try { Image img = null; if
     * (this.procedure == MyGameMode.PROCEDURE_LH) { img =
     * Toolkit.getDefaultToolkit().getImage( new URL(MyURL.URL_PREFIX +
     * "map.jpg")); } else if (this.procedure == MyGameMode.PROCEDURE_HL) { img
     * = Toolkit.getDefaultToolkit().getImage( new URL(MyURL.URL_PREFIX +
     * "darkmap.jpg")); } PanelMap.setImg(img);
     * 
     * for (int i = 0; i < vehicle_list.size(); i++) { // Slow down the vehicle
     * to 4 times when it is night if (this.procedure ==
     * MyGameMode.PROCEDURE_LH) { vehicle_list.getVehicle(i).setVelocity(
     * vehicle_list.getVehicle(i).getVelocity() / 2); } else if (this.procedure
     * == MyGameMode.PROCEDURE_HL) { vehicle_list.getVehicle(i).setVelocity(
     * vehicle_list.getVehicle(i).getVelocity() * 2); } } this.firstHalf =
     * false;
     * 
     * if (this.procedure == MyGameMode.PROCEDURE_HL) {
     * lsnr.Event_Procedure_Switch("High Workload", "Low Workload"); } else {
     * lsnr.Event_Procedure_Switch("Low Workload", "High Workload"); } } catch
     * (MalformedURLException e1) { // TODO Auto-generated catch block
     * e1.printStackTrace(); } }
     */
    public void finishGame() {
	Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit()
		.getScreenSize());
	BufferedImage capture;
	try {
	    capture = new Robot().createScreenCapture(screenRect);
	    ImageIO.write(capture, "bmp", new File(DataRecorder.Timestamp
		    + "_screen.bmp"));
	} catch (Exception ex) {
	    JOptionPane.showConfirmDialog(null, "Screen shot failed");
	} finally {
	    stop(); // stops a timer

	    if (Reschu._database) {
		getDBWriter().ScoreTable_SetScore(Reschu._username,
			this.getCorrectHit());
		getDBWriter().UserTable_SetGameFinish(Reschu._username);
	    }

	    lsnr.Game_End();

	    // Juntao: Disable the main window before thank you window pops up.
	    lsnr.HideSystemWindow();

	    // TEMPORARY SOLUTION
	    FrameEnd frmEnd = new FrameEnd(lsnr);
	    frmEnd.setSize(400, 300);
	    frmEnd.setLocation(300, 300);
	    frmEnd.setVisible(true);
	}
    }

    public void actionPerformed(ActionEvent e) {
	elapsedTime += MySpeed.SPEED_TIMER;

	if (elapsedTime >= Game.TIME_TOTAL_GAME) {
	    finishGame();
	}

	// Juntao: Do not swithc work load.
	// PROCEDURE SWITCH HERE
	/*
	 * if (elapsedTime >= Game.HALF_TOTAL_GAME && this.firstHalf) {
	 * switchGameProcedure(); }
	 */

	vehicleColorFlashFlag = !vehicleColorFlashFlag;

	for (int i = 0; i < vehicle_list.size(); i++) {
	    v = vehicle_list.getVehicle(i);
	    if (v.hasGoal() && (elapsedTime % v.getVelocity() == 0)) {
		v.move();
	    }

	    for (int j = i + 1; j < vehicle_list.size(); j++) {
		Vehicle vOppo = vehicle_list.getVehicle(j);
		// Juntao: count collision iff at least one vehicle is moving
		if (v.getStatus() == MyGame.STATUS_VEHICLE_MOVING
			|| vOppo.getStatus() == MyGame.STATUS_VEHICLE_MOVING) {
		    if (v.inCollision(vOppo)) {
			vvDamage(0.1);
			break;
		    }
		}

	    }
	}
	vehicle_location_change();

	// Juntao: when vehicle enters faked collision zone, zone disappears.
	lsnr.UpdateFakeCollisionZoneListEnter();

	// Update pnlControl's "ENGAGE" button
	if (elapsedTime % MySpeed.SPEED_CLOCK == 0)
	    lsnr.Clock_Tick(elapsedTime);

	// Pending Vehicle's Flashing Color
	if (vehicleColorFlashFlag)
	    MyColor.COLOR_VEHICLE_PENDING = new Color(128, 224, 255, 255);
	else
	    MyColor.COLOR_VEHICLE_PENDING = new Color(228, 124, 155, 255);

	// Update Hazard Area
	int haUpdateSpeed = (Reschu.tutorial() || Reschu.extraTutorial()) ? MySpeed.SPEED_CLOCK_HAZARD_AREA_UPDATE_TUTORIAL
		: MySpeed.SPEED_CLOCK_TARGET_AREA_UPDATE;
	if (elapsedTime % haUpdateSpeed == 0) {
	    map.delHazardArea(rnd, 1);
	    map.setHazardArea(rnd);
	}

	// Update Targets
	try {
	    if (elapsedTime % MySpeed.SPEED_CLOCK_TARGET_AREA_UPDATE == 0) {
		map.garbageTargetCollect();
		map.setTargetArea(rnd);
	    }
	} catch (UserDefinedException ex) {
	    ex.printStackTrace();
	}

	// Auto Target Assign
	// Problem - Should avoid when a vehicle's status is set to PENDING
	// if( elapsedTime % MySpeed.SPEED_CLOCK_AUTO_TARGET_ASSIGN_UPDATE == 0)
	// { AutoTargetAssignAll(); }

	// Check Vehicle - Hazard Area
	if (elapsedTime % MySpeed.SPEED_CLOCK_DAMAGE_CHECK == 0) {
	    for (int i = 0; i < vehicle_list.size(); i++) {
		this.vhCollisionDamage += vehicle_list.getVehicle(i)
			.chkHazardArea();
		// System.out.println(this.vhCollisionDamage);
	    }
	}

	if (elapsedTime % MySpeed.SPEED_LOG == 0) {
	    timelyLog();
	}

	// map.UpdateCriticalZone();
    }

    public void setTargetUsed(String name, boolean isUsed) {
	for (int i = 0; i < nTargetAreaTotal; i++)
	    if (targetNamePool[i].getName() == name)
		targetNamePool[i].setUsed(isUsed);
    }

    public String getEmptyTargetName() {
	for (int i = 0; i < nTargetAreaTotal; i++)
	    if (!targetNamePool[i].isUsed()) {
		targetNamePool[i].setUsed(true);
		return targetNamePool[i].getName();
	    }
	return "X"; // MAKE SURE THIS NEVER HAPPENS!! IT SHOULDN'T BE HAPPEN!
    }

    private void setTargetVisibility() {
	int nVisibleTarget = (int) Math.round(nTargetAreaTotal
		* PROBABILITY_TARGET_VISIBILITY);
	int nInvisibleTarget = nTargetAreaTotal - nVisibleTarget;
	int factor = 2;
	for (int i = 0; i < nTargetAreaTotal; i++) {
	    if (nInvisibleTarget > 0 && i % factor == 0) {
		targetVisibilityPool[i] = false;
		nInvisibleTarget--;
	    } else
		targetVisibilityPool[i] = true;
	}
    }

    public boolean getTargetVisibility() {
	if (targetVisibilityPool_index + 1 == nTargetAreaTotal)
	    targetVisibilityPool_index = 0;
	else
	    targetVisibilityPool_index++;
	return targetVisibilityPool[targetVisibilityPool_index];
    }

    public static double getDistance(double x1, double y1, double x2, double y2) {
	return Math.sqrt(Math.pow((double) (x2 - x1), 2.0)
		+ Math.pow((double) (y2 - y1), 2.0));
    }

    public static double getDistance(int x1, int y1, int x2, int y2) {
	return Math.sqrt(Math.pow((double) (x2 - x1), 2.0)
		+ Math.pow((double) (y2 - y1), 2.0));
    }

    public void GenerateAvoidingPath(Integer[] vIds) {
	ArrayList<Vehicle> vl = new ArrayList<Vehicle>();
	for (Integer vId : vIds) {
	    vl.add(vehicle_list.getVehicleByIndex(vId));
	}

	try {
	    UpdateVehicleAvoidPath(vl);
	    lsnr.UpdateFakeCollisionZoneListAuto(vIds);
	} catch (TimeoutException e) {
	    lsnr.Automation_Application_Failed(vIds);
	}
    }

    private void UpdateVehicleAvoidPath(ArrayList<Vehicle> vl)
	    throws TimeoutException {
	ArrayList<ArrayList<double[]>> avoidingTracks = new ArrayList<ArrayList<double[]>>();
	int iterationTimes = 0;
	for (int i = 0; i < vl.size(); i++) {
	    avoidingTracks.add(new ArrayList<double[]>());
	}

	// id in simuMovingStatus, not vehicle id.
	ArrayList<Integer> simuFinishedIds = new ArrayList<Integer>();

	ArrayList<double[]> simuMovingStatus = new ArrayList<double[]>();
	for (int i = 0; i < vl.size(); i++) {
	    simuMovingStatus.add(vl.get(i).movingStatus.clone());
	    if (vl.get(i).getStatus() != MyGame.STATUS_VEHICLE_MOVING) {
		simuFinishedIds.add(i);
	    }
	}

	while (simuFinishedIds.size() < simuMovingStatus.size()) {
	    for (int i = 0; i < simuMovingStatus.size(); i++) {
		// Arrived vehicle doesnt require updating.
		if (simuFinishedIds.contains(i)) {
		    continue;
		}

		double py = simuMovingStatus.get(i)[1];
		double px = simuMovingStatus.get(i)[0];

		// Simulate to next waypoint, no matter what it is.
		double ty = vl.get(i).getPathAt(0)[1];
		double tx = vl.get(i).getPathAt(0)[0];

		double angle = Math.atan2(ty - py, tx - px);
		double dx = Math.cos(angle);
		double dy = Math.sin(angle);

		for (int j = 0; j < simuMovingStatus.size(); j++) {
		    double distance = Geo.Distance(simuMovingStatus.get(i),
			    simuMovingStatus.get(j));
		    if (i == j || distance > vl.get(i).RSen) {
			continue;
		    }

		    double x = simuMovingStatus.get(j)[0];
		    double y = simuMovingStatus.get(j)[1];
		    double d = Math.pow(Math.hypot(px - x, py - y), 2);

		    double p = 0;
		    if (lsnr.IsFakedCollisionZonePair(vl.get(i).getIndex(), vl
			    .get(j).getIndex())) {
			p = Math.pow(vl.get(i).RDes + vl.get(j).RDes
				+ MyGame.RDES_CALIBRATION, 2);
		    } else {
			p = Math.pow(vl.get(i).RDes + vl.get(j).RDes, 2);
		    }

		    angle = Math.atan2(py - y, px - x);
		    dx += Math.cos(angle) / d * p;
		    dy += Math.sin(angle) / d * p;
		}

		angle = Math.atan2(dy, dx);
		double magnitude = Math.hypot(dx, dy);

		double speed = Math.min(1, 0.2 + magnitude * 0.8);
		double deltaX = Math.cos(angle) * speed * 1;
		double deltaY = Math.sin(angle) * speed * 1;

		simuMovingStatus.get(i)[0] += deltaX;
		simuMovingStatus.get(i)[1] += deltaY;
		simuMovingStatus.get(i)[2] = angle;

		// Only copy x, y and heading. wp flag to be configured in the
		// following code, when next way point is arrived.
		double[] point = new double[] { simuMovingStatus.get(i)[0],
			simuMovingStatus.get(i)[1], simuMovingStatus.get(i)[2],
			0 };
		avoidingTracks.get(i).add(point);

		double[] currentPos = avoidingTracks.get(i).get(
			avoidingTracks.get(i).size() - 1);
		double[] nextWp = new double[] { tx, ty };

		if (Geo.Distance(currentPos, nextWp) < 1) {
		    // Current vehicle arrives to way point.
		    currentPos[3] = 1;

		    // Start from second one. since first wp has been arrived.
		    for (int k = 1; k < vl.get(i).getPathSize(); k++) {
			double[] wp = new double[] {
				(double) vl.get(i).getPathAt(k)[0],
				(double) vl.get(i).getPathAt(k)[1] };
			avoidingTracks.get(i).addAll(Geo.Line(currentPos, wp));
			currentPos = avoidingTracks.get(i).get(
				avoidingTracks.get(i).size() - 1);
			currentPos[3] = 1;
		    }

		    // Arrived to final destination, remove the flag in
		    // avoidingTracks and simuMovingStatus so that it won't be
		    // updated again.
		    // Implemented by adding a finished flag. While loop
		    // terminates when all simulations are finished
		    simuFinishedIds.add(i);
		}
	    }
	    iterationTimes++;
	    if (iterationTimes > 2000) {
		throw new TimeoutException();
	    }
	}

	// update vehicle track. Don't update any unless both path ends in a
	// fixed time.
	for (int i = 0; i < avoidingTracks.size(); i++) {
	    vl.get(i).setTrack(new ArrayList<double[]>(avoidingTracks.get(i)));
	}

    }

    private void timelyLog() {
	DataRecorder.Write("System Log\n" + vehicleInfoToString()
		+ this.mapLogDescription);
    }

    private String vehicleInfoToString() {
	String vehicleInfo = "Vehicles:\n";
	for (int i = 0; i < this.getVehicleList().size(); i++) {
	    vehicleInfo += (this.getVehicleList().getVehicle(i).toString() + "\n");
	}
	return vehicleInfo;
    }
}