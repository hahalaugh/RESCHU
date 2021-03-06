package reschu.game.controller;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import reschu.app.AppMain;
import reschu.constants.*;
import reschu.data.DataRecorder;
import reschu.game.algorithm.CollisionZone;
import reschu.game.model.Game;
import reschu.game.model.Payload;
import reschu.game.model.UserDefinedException;
import reschu.game.model.Vehicle;
import reschu.game.model.VehicleList;
import reschu.game.utils.SituationAwareness;
import reschu.game.view.MyCanvas;
import reschu.game.view.PanelControl;
import reschu.game.view.PanelMap;
import reschu.game.view.PanelMsgBoard;
import reschu.game.view.PanelPayload;
import reschu.game.view.PanelPayloadControls;
import reschu.game.view.PanelTimeLine;
import reschu.game.view.RemainingTime;
import reschu.game.view.PanelMsgBoard.MessageType;
import reschu.tutorial.Tutorial;
import info.clearthought.layout.TableLayout;

public class Reschu extends JFrame implements GUI_Listener {
    private static final long serialVersionUID = -6078272171985479839L;

    public static String _username;
    public static int _workload;
    public static int _scenario; // scenario number ranges between 1 and 4
    public static int _gamemode; // the game has several modes. see
    // reschu.constant.MyGameMode
    public static boolean _database; // if set to false, we don't write to
    // database
    public static int _automation;

    public JPanel pnlMapContainer;
    public static JPanel pnlPayloadContainer;
    public PanelControl pnlControl;
    public MyCanvas payload_canvas;
    public PanelPayload pnlPayload;
    public PanelMap pnlMap;
    public PanelPayloadControls pnlPayloadControls;
    public PanelMsgBoard pnlMsgBoard;
    public PanelTimeLine pnlTimeLine;
    public JScrollPane pnlScrollTimeLine;
    public RemainingTime pnlRemainingTime;

    public Game game;
    private double origin_time;
    private TitledBorder bdrTitle;

    private Tutorial tutorial;

    /** Interactive Tutorial Mode? */
    public static boolean tutorial() {
	return _gamemode == MyGameMode.TUTORIAL_MODE;
    }

    // Juntao: 2-phase tutorial to make further explaination.
    public static boolean extraTutorial() {
	return _gamemode == MyGameMode.EXTRA_TUTORIAL_MODE;
    }

    /** Training Mode? */
    public static boolean train() {
	return _gamemode == MyGameMode.TRAIN_MODE;
    }

    /** Replay Mode? */
    public static boolean replay() {
	return _gamemode == MyGameMode.REPLAY_MODE;
    }

    /** Experiment Mode? */
    public static boolean expermient() {
	return (_gamemode == MyGameMode.ADMINISTRATOR_MODE || _gamemode == MyGameMode.USER_MODE);
    }

    /**
     * Normal constructor for RESCHU.
     * 
     * @param gamemode
     * @param scenario
     * @param username
     * @param main
     * @param database
     */
    public Reschu(int gamemode, int scenario, String username, AppMain main,
	    boolean database, int workload, int automation) {
	super(AppMain.VERSION);
	_gamemode = gamemode;
	_scenario = scenario;
	_username = username;
	_database = database;
	_workload = workload;
	_automation = automation;

	setDefaultCloseOperation(EXIT_ON_CLOSE);

	//Juntao 8/2/2015
	//_gamemode = MyGameMode.EXTRA_TUTORIAL_MODE;
		
	if (tutorial() || extraTutorial())
	    tutorial = new Tutorial(this, main);
	if (train()) {
	    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	    addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    if (game.isRunning()) {
			setVisible(false);
			JOptionPane
				.showMessageDialog(
					null,
					"Congratulations! You are now ready to proceed to the main experiment (10 mins in length).",
					"Message", 1);
		    }
		}
	    });
	}

	initComponents();

    }

    private void initComponents() {
	double sizeMain[][] = {
		{ TableLayout.FILL, 480, 5, 820, TableLayout.FILL },
		{ 370, 150, 220, 170, 30 } };
	double sizePayload[][] = { { TableLayout.FILL, 0.175 },
		{ TableLayout.FILL } };
	double sizeMap[][] = { { TableLayout.FILL }, { TableLayout.FILL } };

	Container content_pane = getContentPane();
	content_pane.setLayout(new TableLayout(sizeMain));

	game = new Game(this, _scenario, _workload, _automation);
	origin_time = System.currentTimeMillis();

	payload_canvas = new MyCanvas();
	pnlPayload = new PanelPayload(this, "PAYLOAD_PANEL", payload_canvas,
		game);
	payload_canvas.addListener(pnlPayload);
	payload_canvas.addGLEventListener(pnlPayload);

	// Create Each Panel Objects
	pnlMap = new PanelMap(this, game, "MAP_PANEL");
	pnlControl = new PanelControl(this, game, "CONTROL_PANEL");
	pnlPayloadControls = new PanelPayloadControls(this, "PAYLOAD_CONTROLS",
		origin_time);
	pnlMsgBoard = new PanelMsgBoard();
	pnlTimeLine = new PanelTimeLine(this, game, game.getVehicleList());
	pnlScrollTimeLine = new JScrollPane(pnlTimeLine);
	pnlRemainingTime = new RemainingTime(game);

	// Panel Payload Container Setup
	pnlPayloadContainer = new JPanel();
	bdrTitle = BorderFactory.createTitledBorder("Payload");
	pnlPayloadContainer.setBorder(bdrTitle);
	pnlPayloadContainer.setLayout(new TableLayout(sizePayload));
	pnlPayloadContainer.add(payload_canvas, "0,0");
	pnlPayloadContainer.add(pnlPayloadControls, "1,0");

	// Panel Map Container Setup
	pnlMapContainer = new JPanel();
	bdrTitle = BorderFactory.createTitledBorder("Map");
	pnlMapContainer.setBorder(bdrTitle);
	pnlMapContainer.setLayout(new TableLayout(sizeMap));
	pnlMapContainer.add(pnlMap, "0,0");
	pnlPayloadControls.setEnabled(false);
	pnlPayloadControls.setListener(this);

	add(pnlPayloadContainer, "1, 0");
	add(pnlMsgBoard, "1, 1");
	add(pnlControl, "1, 2");
	add(pnlScrollTimeLine, "1, 3");
	add(pnlRemainingTime, "1, 4");
	add(pnlMapContainer, "3, 0, 3, 4");

	// Create PanelMap's double-buffer
	// The new JFrame does not make a back buffer (swing automatically
	// creates it)
	// This has to be commented out if we do not extend JApplet.
	// pnlMap.init_buffer();

	// Disable the map panel until the game starts
	pnlMap.setEnabled(false);
    }

    /**
     * For situation awareness research on RESCHU. Checks whether there is any
     * hazard area in between a vehicle's path.
     * 
     * @param vIdx
     */
    private void checkIntersect(int vIdx) {
	int haIdx = SituationAwareness.checkIntersect(game.getVehicleList()
		.getVehicle(vIdx - 1), game.map.getListHazard(),
		MySize.SIZE_HAZARD_3_PXL);
	if (haIdx > 0) {
	    if (!game.getVehicleList().getVehicle(vIdx - 1).getIntersect()) {
		game.getVehicleList().getVehicle(vIdx - 1).setIntersect(true);
		// EVT_Vehicle_IntersectHazardArea(vIdx,
		// game.map.getListHazard().get(haIdx));
	    }
	} else {
	    if (game.getVehicleList().getVehicle(vIdx - 1).getIntersect()) {
		game.getVehicleList().getVehicle(vIdx - 1).setIntersect(false);
		// EVT_Vehicle_EscapeHazardArea(vIdx);
	    }
	}
    }

    // Gui_Listener Interface
    public void vehicle_location_changed() {
	// TODO: Do I really want to repaint the whole map here?
	pnlMap.repaint();
    }

    public void Clock_Tick(int milliseconds) {
	if (tutorial() || extraTutorial())
	    if (milliseconds % 1000 == 0)
		tutorial.tick();
	pnlTimeLine.refresh(milliseconds);
	pnlRemainingTime.refresh(milliseconds);
	pnlControl.chkEngageEnabled();

	// This decreases the remaining duration of TextOnTop (the warning msg)
	pnlMap.decreaseTextOnTopTime();
    }

    public void Game_Start() {
	// screen resolution check for Java WebStart.
	// if( getHeight() < MySize.MAP_HEIGHT_PXL ) {
	// setVisible(false);
	// JOptionPane.showMessageDialog(null,
	// "You need a minimum screen resolution of 1280 x 1024. Please try again.",
	// "Message", 1);
	// System.exit(0);
	// }

	if (train()) {
	    // enables the "close" function when Training mode
	    setDefaultCloseOperation(EXIT_ON_CLOSE);

	    // Welcome message. This should encourage users to play this
	    // training mode.
	    JOptionPane
		    .showMessageDialog(
			    null,
			    "Welcome to the last step before you proceed to the main experiment. "
				    + "You will be able to train on a full-sized team.",
			    "Message", 1);
	    JOptionPane
		    .showMessageDialog(
			    null,
			    "Unlike the actual experiment, "
				    + "the camera window in the training round will show the same image "
				    + "as you engage different targets in order to give you a chance to practice.",
			    "Message", 1);
	    JOptionPane
		    .showMessageDialog(
			    null,
			    "You will have 10 minutes training time. "
				    + "when you are done to proceed to the main experiment. ",
			    "Message", 1);

	}

	new Thread(game).start();
	if (expermient()) {
	    DataRecorder.Start();
	}
	// enable panels which are initially disabled
	pnlMap.setEnabled(true);
    }

    public void Game_End() {
	// PanelMsgBoard.Msg("YOUR TOTAL SCORE: " + game.getCorrectHit(),
	// MessageType.SystemInfo);
	EVT_System_GameEnd(this.game.getWorkload(), game.getAutomation(),
		game.getCorrectHit(), game.getInCorrectHit(),
		game.getVVDamage(), game.getVHDamage());
	game.stop();
	DataRecorder.Stop();
	Thread.currentThread().interrupt();
    }

    /**
     * Shows a message on the top of the PanelMap
     * 
     * @param msg
     *            message
     * @param duration
     *            duration of displaying the message (in second)
     */
    public void showMessageOnTopOfMap(String msg, int duration) {
	pnlMap.setTextOnTop(msg, duration);
    }

    public void Rotate_Clockwise_Selected() {
	pnlPayload.r_c_2();
    }

    public void Pan_Up_Selected() {
    }// pnlPayload.pan_up(); }

    public void Rotate_Counter_Selected() {
	pnlPayload.r_c_c_2();
    }

    public void Pan_Down_Selected() {
    }// pnlPayload.pan_down(); }

    public void Zoom_In() {
	pnlPayload.zoom_in();
    }

    public void Zoom_Out() {
	pnlPayload.zoom_out();
    }

    public void Vehicle_Selected_From_pnlMap(int idx) {
	// Don't reflect vehicle selection on panel
	// pnlControl.Show_Vehicle_Status(idx);
    }

    public void Vehicle_Unselected_From_pnlMap() {
	pnlMap.setClear();
	pnlMap.setSelectedVehicle(null);
	pnlControl.Show_Vehicle_Status(0);
    }

    public void Vehicle_Engage_From_pnlMap(Vehicle v) {
	pnlControl.Show_Vehicle_Status(v.getIndex());
	Engage(v);

    }

    // Events From pnlPayload
    /**
     * Called from PanelPayload when the user finishes the visual task. When
     * this method is called, the followings are executed. 1. set the target as
     * "DONE" so that later garbage collected 2. set the vehicle's status as
     * "STASIS" so that it stops flashing 3. tell the game to assign a target to
     * this vehicle 4. clear the game's current payload vehicle. 5. set the
     * panels enabled 6. clear the mission info text
     */
    public void Payload_Finished_From_pnlPayload(Vehicle v) {
	v.getTarget().setDone();
	v.setStatus(MyGame.STATUS_VEHICLE_STASIS);
	game.AutoTargetAssign(v);
	game.clearCurrentPayloadVehicle();
	pnlMap.setEnabled(true);
	pnlControl.setEnabled(true);
	pnlTimeLine.setEnabled(true);
	pnlPayloadControls.setEnabled(false);
	pnlControl.Update_Vehicle_Payload_Clear(v);
	pnlControl.Show_All_Vehicle_Status();
    }

    public void Payload_Assigned_From_pnlPayload(Vehicle v, Payload p) {
	PanelMsgBoard.Msg(
		"[MISSION(" + v.getIndex() + ")] " + p.getStatement(),
		MessageType.PayloadDescription);
	pnlControl.Update_Vehicle_Payload(v, p);
    }

    public void Payload_Graphics_Update() {
	// TODO: NEED TO FIND OUT A MORE EFFICIENT WAY TO REPAINT THE PAYLOAD
	// WINDOW!!!
	payload_canvas.repaint();
    }

    /**
     * (For T3) Called to enable/disable the submit button in the payloadControl
     * panel
     */
    public void Payload_Submit(boolean submit) {
	pnlPayloadControls.enableSubmit(submit);
    }

    // Events From Vehicle
    // public void Vehicle_Reached_Target_From_Vehicle(Vehicle v) {}
    public void Vehicle_Damaged_By_Hazard_Area_From_Vehicle(Vehicle v) {
	pnlControl.Update_Vehicle_Damage(v);
    }

    public void Target_Become_Visible_From_Vehicle(Vehicle v) {
	game.AutoTargetAssign(v);
    }

    public void Hide_Popup(Vehicle v) {
	pnlMap.HidePopup(v);
    }

    // Events From pnlControl
    public void Vehicle_Selected_From_pnlControl(int idx) {
	pnlMap.setSelectedVehicle(game.getVehicleList().getVehicle(idx));
    }

    public void Vehicle_Unselected_From_pnlControl() {
	pnlMap.setClear();
	pnlMap.setSelectedVehicle(null);
    }

    public void Vehicle_Goal_From_pnlControl(Vehicle v) {
	pnlMap.setClear();
	pnlMap.setGoal(v);
    }

    public void Vehicle_WP_Add_From_pnlControl(Vehicle v) {
	pnlMap.setClear();
	pnlMap.addWP(v);
    }

    public void Vehicle_WP_Del_From_pnlControl(Vehicle v) {
	pnlMap.setClear();
	pnlMap.delWP(v);
    }

    public void Vehicle_Engage_From_pnlControl(Vehicle v) {
	pnlControl.Show_Vehicle_Status(v.getIndex());
	Engage(v);
    }

    private void Engage(Vehicle v) {
	pnlMap.setEnabled(false);
	pnlControl.setEnabled(false);
	pnlPayloadControls.setEnabled(true);
	pnlTimeLine.setEnabled(false);
	v.setStatus(MyGame.STATUS_VEHICLE_PAYLOAD);
	game.setCurrentPayloadVehicle(v);
	pnlPayload.set_payload(v);

	Color c = Color.RED;
	switch (pnlPayload.curPayload.getTaskType()) {
	case MyGame.TASK_NO_HINT:
	    // hitEnabled = false;
	    c = Color.GREEN;
	    break;
	case MyGame.TASK_MAYBE:
	    c = Color.ORANGE;
	    break;
	case MyGame.TASK_HINT:
	    c = Color.RED;
	    break;
	}

	bdrTitle = BorderFactory.createTitledBorder(
		BorderFactory.createLineBorder(c, 5), "Payload");
	pnlPayloadContainer.setBorder(bdrTitle);

	String tDescription = String.format("Task type: %d, task image: %s",
		pnlPayload.curPayload.getTaskType(),
		pnlPayload.curPayload.getFilename());
	EVT_Payload_Engaged(v.getIndex(), v.getTarget().getName(), tDescription);
    }

    // DB
    private void Write(int invoker, int type, int vIdx, String log, int X, int Y) {
	// DB is not used in this demo version. So deleted.
	if (tutorial() || extraTutorial()) {
	    String target = (log.indexOf("[") >= 0) ? log.substring(
		    log.indexOf("[") + 1, log.indexOf("[") + 2) : "";
	    tutorial.event(type, vIdx, target);
	}

	if (Reschu.expermient()) {
	    DataRecorder.Write("Vehicle: " + vIdx + " " + log + " " + X + ","
		    + Y);
	}
    }

    public void EVT_WP_AddWP_Start(int vIdx) {
	Write(MyDB.INVOKER_USER, MyDB.WP_ADD_START, vIdx, "WP add start", -1,
		-1);
    }

    public void EVT_WP_AddWP_End(int vIdx, int mouseCoordX, int mouseCoordY) {
	checkIntersect(vIdx);
	Write(MyDB.INVOKER_USER, MyDB.WP_ADD_END, vIdx, "WP add end",
		mouseCoordX, mouseCoordY);
    }

    public void EVT_WP_AddWP_Cancel(int vIdx) {
	Write(MyDB.INVOKER_USER, MyDB.WP_ADD_CANCEL, vIdx, "WP add canceled",
		-1, -1);
    }

    public void EVT_WP_MoveWP_Start(int vIdx, int mouseCoordX, int mouseCoordY) {
	Write(MyDB.INVOKER_USER, MyDB.WP_MOVE_START, vIdx, "WP move start",
		mouseCoordX, mouseCoordY);
    }

    public void EVT_WP_MoveWP_End(int vIdx, int mouseCoordX, int mouseCoordY) {
	checkIntersect(vIdx);
	Write(MyDB.INVOKER_USER, MyDB.WP_MOVE_END, vIdx, "WP move end",
		mouseCoordX, mouseCoordY);
    }

    public void EVT_WP_DeleteWP_Start(int vIdx) {
	Write(MyDB.INVOKER_USER, MyDB.WP_DELETE_START, vIdx, "WP delete start",
		-1, -1);
    }

    public void EVT_WP_DeleteWP_End(int vIdx, int mouseCoordX, int mouseCoordY) {
	checkIntersect(vIdx);
	Write(MyDB.INVOKER_USER, MyDB.WP_DELETE_END, vIdx, "WP delete end",
		mouseCoordX, mouseCoordY);
    }

    public void EVT_GP_SetGP_by_System(int vIdx, String targetName) {
	checkIntersect(vIdx);
	Write(MyDB.INVOKER_SYSTEM, MyDB.GP_SET_BY_SYSTEM, vIdx,
		"Goal set by system, assigned to target[" + targetName + "]",
		-1, -1);
    }

    public void EVT_GP_SetGP_Start(int vIdx) {
	Write(MyDB.INVOKER_USER, MyDB.GP_SET_START, vIdx, "Goal set start", -1,
		-1);
    }

    public void EVT_GP_SetGP_End_Assigned(int vIdx, int mouseCoordX,
	    int mouseCoordY, String targetName) {
	checkIntersect(vIdx);
	Write(MyDB.INVOKER_USER, MyDB.GP_SET_END_ASSIGNED, vIdx,
		"Goal set end, assigned to target[" + targetName + "]",
		mouseCoordX, mouseCoordY);
    }

    public void EVT_GP_SetGP_End_Unassigned(int vIdx, int mouseCoordX,
	    int mouseCoordY) {
	checkIntersect(vIdx);
	Write(MyDB.INVOKER_USER, MyDB.GP_SET_END_UNASSIGNED, vIdx,
		"Goal set end, no assign", mouseCoordX, mouseCoordY);
    }

    /*
     * public void EVT_GP_SetGP_Cancel(int vIdx) { Write(MyDB.INVOKER_USER,
     * MyDB.GP_SET_CANCEL, vIdx, "Goal set canceled", -1, -1); }
     */
    public void EVT_GP_ChangeGP_Start(int vIdx, int mouseCoordX,
	    int mouseCoordY, String targetName) {
	Write(MyDB.INVOKER_USER, MyDB.GP_CHANGE_START, vIdx,
		"Goal change start from Target[" + targetName + "]",
		mouseCoordX, mouseCoordY);
    }

    public void EVT_GP_ChangeGP_End_Assigned(int vIdx, int mouseCoordX,
	    int mouseCoordY, String targetName) {
	checkIntersect(vIdx);
	Write(MyDB.INVOKER_USER, MyDB.GP_CHANGE_END_ASSIGNED, vIdx,
		"Goal change end, target[" + targetName + "] assigned",
		mouseCoordX, mouseCoordY);
    }

    public void EVT_GP_ChangeGP_End_Unassigned(int vIdx, int mouseCoordX,
	    int mouseCoordY) {
	checkIntersect(vIdx);
	Write(MyDB.INVOKER_USER, MyDB.GP_CHANGE_END_UNASSIGNED, vIdx,
		"Goal change end, target unassigned", mouseCoordX, mouseCoordY);
    }

    public void EVT_Target_Generated(String targetName, int[] targetPos,
	    boolean visibility) {
	Write(MyDB.INVOKER_SYSTEM, MyDB.TARGET_GENERATED, -1, "Target["
		+ targetName + "] generated (visibile=" + visibility + ")",
		targetPos[0], targetPos[1]);
    }

    public void EVT_Target_BecameVisible(String targetName, int[] targetPos) {
	Write(MyDB.INVOKER_SYSTEM, MyDB.TARGET_BECAME_VISIBLE, -1, "Target["
		+ targetName + "] became visible", targetPos[0], targetPos[1]);
    }

    public void EVT_Target_Disappeared(String targetName, int[] targetPos) {
	Write(MyDB.INVOKER_SYSTEM, MyDB.TARGET_DISAPPEARED, -1, "Target["
		+ targetName + "] disappeared", targetPos[0], targetPos[1]);
    }

    public void EVT_Payload_EngagedAndFinished_COMM(int vIdx, String targetName) {
	Write(MyDB.INVOKER_USER, MyDB.PAYLOAD_ENGAGED_AND_FINISHED, vIdx,
		"Payload Engaged and Finished, COMM", -1, -1);
    }

    public void EVT_Payload_Engaged(int vIdx, String targetName,
	    String description) {
	Write(MyDB.INVOKER_USER, MyDB.PAYLOAD_ENGAGED, vIdx,
		"Payload Engaged to Target[" + targetName + "] Description: "
			+ description, -1, -1);
    }

    public void EVT_Payload_Finished_Correct(int vIdx, String targetName) {
	// play(WAVPlayer.CORRECT);
	Write(MyDB.INVOKER_USER, MyDB.PAYLOAD_FINISHED_CORRECT, vIdx,
		"Payload Finished, CORRECT", -1, -1);
    }

    public void EVT_Payload_Finished_Incorrect(int vIdx, String targetName) {
	// play(WAVPlayer.INCORRECT);
	Write(MyDB.INVOKER_USER, MyDB.PAYLOAD_FINISHED_INCORRECT, vIdx,
		"Payload Finished, INCORRECT", -1, -1);
    }

    public void EVT_Vehicle_Damaged(int vIdx, int haX, int haY) {
	Write(MyDB.INVOKER_SYSTEM, MyDB.VEHICLE_DAMAGED, vIdx,
		"Damaged with a HazardArea", haX, haY);
    }

    public void EVT_Vehicle_SpeedDecreased(int vIdx, int curSpeed) {
	// play(WAVPlayer.PENALIZED);
	Write(MyDB.INVOKER_SYSTEM, MyDB.VEHICLE_SPEED_DECREASED, vIdx,
		"Speed Decreased to (" + curSpeed + ")", -1, -1);
    }

    public void EVT_Vehicle_ArrivesToTarget(int vIdx, String targetName, int x,
	    int y) {
	// play(WAVPlayer.VEHICLE_ARRIVE);
	Write(MyDB.INVOKER_SYSTEM, MyDB.VEHICLE_ARRIVES_TO_TARGET, vIdx,
		"Arrives To Target[" + targetName + "]", x, y);
    }

    public void EVT_Vehicle_IntersectHazardArea(int vIdx, int[] threat) {
	Write(MyDB.INVOKER_SYSTEM, MyDB.VEHICLE_INTERSECT_HAZARDAREA, vIdx,
		"Intersect with a HazardArea", threat[0], threat[1]);
    }

    public void EVT_Vehicle_EscapeHazardArea(int vIdx) {
	Write(MyDB.INVOKER_SYSTEM, MyDB.VEHICLE_ESCAPE_HAZARDAREA, vIdx,
		"Escape from a HazardArea", -1, -1);
    }

    public void EVT_HazardArea_Generated(int[] pos) {
	for (int vIdx = 0; vIdx < game.getVehicleList().size(); vIdx++)
	    checkIntersect(vIdx + 1);
	// Write(MyDB.INVOKER_SYSTEM, MyDB.HAZARDAREA_GENERATED, -1,
	// "HazardArea Generated", pos[0], pos[1]);
    }

    public void EVT_HazardArea_Disappeared(int[] pos) {
	for (int vIdx = 0; vIdx < game.getVehicleList().size(); vIdx++)
	    checkIntersect(vIdx + 1);
	// Write(MyDB.INVOKER_SYSTEM, MyDB.HAZARDAREA_DISAPPEARED, -1,
	// "HazardArea Disappeared", pos[0], pos[1]);
    }

    public void EVT_System_GameStart() {
	Write(MyDB.INVOKER_SYSTEM,
		MyDB.SYSTEM_GAME_START,
		-1,
		"Game Start, username=" + _username + ", scenario=" + _scenario,
		-1, -1);
    }

    public void EVT_System_GameEnd(int workload, int automation, int correct,
	    int incorrect, double vvDamage, double vhDamage) {

	workload++;
	automation++;

	String s = "Game End. Scenario = " + this._scenario + " workload = "
		+ workload + " automation = " + automation + " correct = "
		+ correct + " incorrect = " + incorrect + " vvdamage = "
		+ vvDamage + " vhDamage = " + vhDamage;
	Write(MyDB.INVOKER_SYSTEM, MyDB.SYSTEM_GAME_END, -1, s, -1, -1);
    }

    /**
     * Yves
     */
    public void EVT_VSelect_Map_LBtn(int vIdx) {
	Write(MyDB.INVOKER_USER, MyDB.YVES_VEHICLE_SELECT_MAP_LBTN, vIdx,
		"Vehicle select map Lbtn", -1, -1);
    }

    public void EVT_VSelect_Map_RBtn(int vIdx) {
	Write(MyDB.INVOKER_USER, MyDB.YVES_VEHICLE_SELECT_MAP_RBTN, vIdx,
		"Vehicle select map Rbtn", -1, -1);
    }

    public void EVT_VSelect_Tab(int vIdx) {
	Write(MyDB.INVOKER_USER, MyDB.YVES_VEHICLE_SELECT_TAB, vIdx,
		"Vehicle select tab", -1, -1);
    }

    public void EVT_VSelect_Tab_All() {
	Write(MyDB.INVOKER_USER, MyDB.YVES_VEHICLE_DESELECT_TAB, -1,
		"Vehicle deselect tab", -1, -1);
    }

    /*
     * private void play(String arg) { new WAVPlayer(arg).start(); }
     */

    // Juntao: Check payload improve image quality.
    public synchronized void CheckPayload() {
	// TODO Auto-generated method stub
	if (pnlPayload.isEnabled()) {
	    // pnlPayload.glControlEnabled(true);
	    pnlPayload.ReplaceImage();

	    // Giving Payload checking information
	    if (!pnlPayload.imageReplaced) {
		PanelMsgBoard.Msg("Checking payload image!",
			MessageType.SystemInfo);
		Write(MyDB.INVOKER_USER, MyDB.PAYLOAD_CHECK, -1,
			"User check payload image", -1, -1);
	    } else {
		PanelMsgBoard.Msg("Payload already checked!",
			MessageType.SystemInfo);
	    }
	}
    }

    public void SafePayload() {
	// TODO Auto-generated method stub
	if (pnlPayload.isEnabled()) {
	    pnlPayload.checkCorrect(MyGame.SAFE_OPTION);
	    Write(MyDB.INVOKER_USER, MyDB.PAYLOAD_SAFE, -1,
		    "User leaves payload by selecting SAFE", -1, -1);
	}
    }

    public void HitPayload() {
	if (pnlPayload.isEnabled()) {
	    pnlPayload.checkCorrect(MyGame.HIT_OPTION);
	    Write(MyDB.INVOKER_USER, MyDB.PAYLOAD_HIT, -1,
		    "User leaves payload by selecting HIT", -1, -1);
	}
    }

    public void Update_Collision_Info(ArrayList<CollisionZone> cz) {
	// TODO Auto-generated method stub
	this.pnlControl.UpdateVehicleCollisionInfo(cz);
    }

    public void Automation_Applied_From_pnlControl(Integer[] vIds) {
	// TODO Auto-generated method stub
	game.GenerateAvoidingPath(vIds);
	Write(MyDB.INVOKER_USER,
		MyDB.AUTOMATION_APPLICATION_FROM_CONTROL_PANEL, -1,
		"User applies automation from control panel for vehicle "
			+ IntegerArrayToString(vIds), -1, -1);
    }

    public void Automation_Application_Failed(Integer[] vIds) {
	// Display warning information about automation failure.
	String vIdString = "";
	for (int i : vIds) {
	    vIdString += (i + ",");
	}
	vIdString = vIdString.substring(0, vIdString.length() - 1);

	String msg = "Automation to avoid collision of " + vIdString
		+ " not available";
	pnlMap.setTextOnTop(msg, 5);

	Write(MyDB.INVOKER_USER, MyDB.AUTOMATION_APPLICATION_FAILED, -1,
		"User applies automation from for vehicle "
			+ IntegerArrayToString(vIds) + " failed", -1, -1);
    }

    public void Automation_Applied_From_pnlMap(Integer[] vIds) {
	// TODO Auto-generated method stub
	game.GenerateAvoidingPath(vIds);
	Write(MyDB.INVOKER_USER, MyDB.AUTOMATION_APPLICATION_FROM_MAP, -1,
		"User applies automation from map panel for vehicle "
			+ IntegerArrayToString(vIds), -1, -1);
    }

    public void Vehicle_Selected_From_pnlTimeLine(int idx) {
	// TODO Auto-generated method stub
	pnlMap.setSelectedVehicle(game.getVehicleList().getVehicle(idx));
	Write(MyDB.INVOKER_USER, MyDB.YVES_VEHICLE_SELECT_TAB, idx,
		"User selects vehicle " + idx + " from timeline panel", -1, -1);
    }

    public void Event_Procedure_Switch(String from, String to) {
	// TODO Auto-generated method stub
	Write(MyDB.INVOKER_SYSTEM, MyDB.PROCEDURE_SWITCHED, -1,
		"Experiment procedure switched from " + from + " to " + to, -1,
		-1);
    }

    private String IntegerArrayToString(Integer[] a) {
	String result = "";
	for (int i : a) {
	    result += (i + ",");
	}

	result.substring(0, result.length() - 1);
	return result;
    }

    public void Automation_Application_Canceled(Integer[] vIds) {
	// TODO Auto-generated method stub
	Write(MyDB.INVOKER_USER, MyDB.AUTOMATION_APPLICATION_CANCELLED, -1,
		"User cancelled automation application for vehicle "
			+ IntegerArrayToString(vIds), -1, -1);
    }

    public void UpdateMapLogData(String mapData) {
	// TODO Auto-generated method stub
	game.mapLogDescription = new String(mapData);
    }

    public void HideSystemWindow() {
	// TODO Auto-generated method stub
	this.setVisible(false);
    }

    // Dynamically add vehicle for traning purpose.
    public void TutorialAddVehicle() {
	// TODO Auto-generated method stub
	Vehicle v = game.getVehicleList().getVehicle(0);
	try {
	    game.getVehicleList().addVehicle(2, Vehicle.TYPE_UAV,
		    "Fire Scout B", Vehicle.PAYLOAD_ISR, v.getVelocity(),
		    game.rnd, game.map, this, game);

	    this.remove(pnlScrollTimeLine);
	    pnlTimeLine = new PanelTimeLine(this, game, game.getVehicleList());
	    pnlScrollTimeLine = new JScrollPane(pnlTimeLine);
	    add(pnlScrollTimeLine, "1, 3");

	    this.remove(pnlControl);
	    pnlControl.StopTimer();
	    pnlControl = new PanelControl(this, game, "CONTROL_PANEL");
	    add(pnlControl, "1, 2");

	    this.revalidate();

	} catch (UserDefinedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public void CollisionZoneDecreased(int currentNumber) {
	// TODO Auto-generated method stub
	String message = String.format("Collision Zone number reduced to %d",
		currentNumber);
	Write(MyDB.INVOKER_SYSTEM, MyDB.COLLISION_ZONE_DECREASED, -1, message,
		-1, -1);
    }

    public void CollisionZoneIncreased(int currentNumber) {
	// TODO Auto-generated method stub
	// TODO Auto-generated method stub
	String message = String.format("Collision Zone number increased to %d",
		currentNumber);
	Write(MyDB.INVOKER_SYSTEM, MyDB.COLLISION_ZONE_INCREASED, -1, message,
		-1, -1);
    }

    public void UpdateFakeCollisionZoneListAuto(Integer[] vIds) {
	// TODO Auto-generated method stub
	Iterator<CollisionZone> itCz = pnlMap.fakeCollisionZones.iterator();
	while (itCz.hasNext()) {
	    CollisionZone cz = itCz.next();
	    boolean contains = true;
	    for (int i = 0; i < cz.involvedVehicles.length; i++) {
		contains &= Arrays.asList(vIds)
			.contains(cz.involvedVehicles[i]);
	    }

	    if (contains) {
		itCz.remove();
	    }
	}
    }

    public void UpdateFakeCollisionZoneListEnter() {
	VehicleList vl = game.getVehicleList();
	Iterator<CollisionZone> itCz = pnlMap.fakeCollisionZones.iterator();
	while (itCz.hasNext()) {
	    CollisionZone cz = itCz.next();
	    for (int i = 0; i < vl.size(); i++) {
		Vehicle v = vl.getVehicle(i);
		Point2D p = new Point2D.Double(v.movingStatus[0],
			v.movingStatus[1]);
		if (cz.collisionArea.contains(p)) {
		    itCz.remove();
		    break;
		}
	    }
	}

    }

    public void UpdateFakeCollisionZoneListTrack(Integer vId) {
	// TODO Auto-generated method stub
	Iterator<CollisionZone> itCz = pnlMap.fakeCollisionZones.iterator();
	while (itCz.hasNext()) {
	    CollisionZone cz = itCz.next();
	    if (Arrays.asList(cz.involvedVehicles).contains(vId)) {
		itCz.remove();
		break;
	    }
	}
    }

    public boolean IsFakedCollisionZonePair(Integer vId1, Integer vId2) {
	// TODO Auto-generated method stub
	for (int i = 0; i < pnlMap.fakeCollisionZones.size(); i++) {
	    CollisionZone cz = pnlMap.fakeCollisionZones.get(i);
	    if (Arrays.asList(cz.involvedVehicles).contains(vId1)
		    && Arrays.asList(cz.involvedVehicles).contains(vId2)) {
		return true;
	    }
	}
	return false;
    }

    public void Update_Hazard_Highlight_Info(String highlightList) {
	// TODO Auto-generated method stub
	DataRecorder.Write("VH Collision:" + highlightList);
    }

}