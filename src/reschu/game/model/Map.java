package reschu.game.model;

import java.util.*;

import reschu.constants.*;
import reschu.game.controller.GUI_Listener;
import reschu.game.controller.Reschu;

public class Map {
    private Game g;
    private GUI_Listener lsnr;
    private int[][] map_array;
    private LinkedList<Target> listAssignedTarget = new LinkedList<Target>();
    private LinkedList<Target> listUnassignedTarget = new LinkedList<Target>();
    private LinkedList<int[]> listHazard = new LinkedList<int[]>();

    Map() {
    }

    Map(int width, int height, Game g, GUI_Listener l) {
	map_array = new int[width][height];
	this.g = g;
	lsnr = l;
    }

    public LinkedList<Target> getListAssignedTarget() {
	return listAssignedTarget;
    }

    public LinkedList<Target> getListUnassignedTarget() {
	return listUnassignedTarget;
    }

    public LinkedList<int[]> getListHazard() {
	return listHazard;
    }

    public int getTargetSize(String target_type) {
	int cnt = 0;
	for (int i = 0; i < listAssignedTarget.size(); i++)
	    if (listAssignedTarget.get(i).getMission() == target_type)
		cnt++;
	for (int i = 0; i < listUnassignedTarget.size(); i++)
	    if (listUnassignedTarget.get(i).getMission() == target_type)
		cnt++;
	return cnt;
    }

    public synchronized void setCellType(int x, int y, int type) {
	map_array[x][y] = type;
    }

    public synchronized int getCellType(int x, int y) {
	return map_array[x][y];
    }

    public synchronized void addTarget(Target t) {
	if (g.isRunning())
	    lsnr.EVT_Target_Generated(t.getName(), t.getPos(), t.isVisible());
	listUnassignedTarget.addLast(t);
    }

    public synchronized void addHazard(int x, int y, int size) {
	if (g.isRunning())
	    lsnr.EVT_HazardArea_Generated(new int[] { x, y });
	listHazard.addLast(new int[] { x, y, size });
    }

    public synchronized void assignTarget(int[] coordinate) {
	for (int i = 0; i < listUnassignedTarget.size(); i++) {
	    if (boundaryCheck(coordinate[0], coordinate[1],
		    listUnassignedTarget.get(i).getPos())) {
		listAssignedTarget.add(listUnassignedTarget.get(i));
		listUnassignedTarget.remove(i);
	    }
	}
    }

    public synchronized void unassignTarget(int[] coordinate) {
	for (int i = 0; i < listAssignedTarget.size(); i++) {
	    if (boundaryCheck(coordinate[0], coordinate[1], listAssignedTarget
		    .get(i).getPos())) {
		listUnassignedTarget.add(listAssignedTarget.get(i));
		listAssignedTarget.remove(i);
	    }
	}
    }

    public synchronized void delHazard(int idx) {
	lsnr.EVT_HazardArea_Disappeared(new int[] { listHazard.get(idx)[0],
		listHazard.get(idx)[1] });
	listHazard.remove(idx);
    }

    public synchronized boolean isTarget(int x, int y) {
	if (isAssignedTarget(x, y) || isUnassignedTarget(x, y))
	    return true;
	return false;
    }

    public synchronized boolean isAssignedTarget(int x, int y) {
	for (int i = 0; i < listAssignedTarget.size(); i++) {
	    if (listAssignedTarget.get(i).getPos()[0] == x
		    && listAssignedTarget.get(i).getPos()[1] == y)
		return true;
	}
	return false;
    }

    public synchronized boolean isUnassignedTarget(int x, int y) {
	for (int i = 0; i < listUnassignedTarget.size(); i++) {
	    if (listUnassignedTarget.get(i).getPos()[0] == x
		    && listUnassignedTarget.get(i).getPos()[1] == y)
		return true;
	}
	return false;
    }

    public synchronized boolean isHazard(int x, int y) {
	for (int i = 0; i < listHazard.size(); i++) {
	    if (listHazard.get(i)[0] == x && listHazard.get(i)[1] == y)
		return true;
	}
	return false;
    }

    private boolean boundaryCheck(int x, int y, int[] target_pos) {
	int w = Math.round(MySize.SIZE_TARGET_PXL / MySize.SIZE_CELL / 2);

	for (int i = -w; i < w; i++)
	    for (int j = -w; j < w; j++)
		if (x + i == target_pos[0] && y + j == target_pos[1])
		    return true;
	return false;
    }

    public void setHazardArea(Random rnd) {
	int nHazard = (Reschu.tutorial() || Reschu.extraTutorial()) ? MyGame.nHAZARD_AREA_TUTORIAL
		: MyGame.nHAZARD_AREA;
	int nHazardNeed = nHazard - getListHazard().size();
	int x, y;
	int count = 0; 
	
	for (int i = 0; i < nHazardNeed; i++) {
	    while (!chkOkayToAdd((x = rnd.nextInt(MySize.width)),
		    (y = rnd.nextInt(MySize.height)))) {
		if(count++ > 10000)
		{
		    break;
		}
	    }
	    addHazard(x, y, rnd.nextInt(5));
	}
    }

    private boolean chkOkayToAdd(int x, int y) {
	final int LIMIT_DISTANCE = 40;
	final boolean DEBUG = false;

	for (int i = 0; i < listAssignedTarget.size(); i++) {
	    Target t = listAssignedTarget.get(i);
	    if (Game.getDistance(t.getPos()[0], t.getPos()[1], x, y) < LIMIT_DISTANCE) {
		int x1 = t.getPos()[0], y1 = t.getPos()[1], x2 = x, y2 = y;
		double d = Game.getDistance(t.getPos()[0], t.getPos()[1], x, y);
		if (DEBUG)
		    System.err.println("LAT (" + x1 + "," + y1
			    + ") with randomly generated (" + x2 + "," + y2
			    + "), d=(" + d + ")");
		return false;
	    }
	}
	for (int i = 0; i < listUnassignedTarget.size(); i++) {
	    Target t = listUnassignedTarget.get(i);
	    if (Game.getDistance(t.getPos()[0], t.getPos()[1], x, y) < LIMIT_DISTANCE) {
		int x1 = t.getPos()[0], y1 = t.getPos()[1], x2 = x, y2 = y;
		double d = Game.getDistance(t.getPos()[0], t.getPos()[1], x, y);
		if (DEBUG)
		    System.err.println("LUT(" + x1 + "," + y1
			    + ") with randomly generated (" + x2 + "," + y2
			    + "), d=(" + d + ")");
		return false;
	    }
	}
	for (int i = 0; i < listHazard.size(); i++) {
	    if (Game.getDistance(x, y, listHazard.get(i)[0],
		    listHazard.get(i)[1]) < LIMIT_DISTANCE) {
		int x1 = listHazard.get(i)[0], y1 = listHazard.get(i)[1], x2 = x, y2 = y;
		double d = Game.getDistance(x, y, listHazard.get(i)[0],
			listHazard.get(i)[1]);
		if (DEBUG)
		    System.err.println("LHA(" + x1 + "," + y1
			    + ") with randomly generated (" + x2 + "," + y2
			    + "), d=(" + d + ")");
		return false;
	    }
	}
	for (int i = 0; i < g.getVehicleList().size(); i++) {
	    Vehicle v = g.getVehicleList().getVehicle(i);
	    if (Game.getDistance(v.getX(), v.getY(), x, y) < LIMIT_DISTANCE) {
		int x1 = v.getX(), y1 = v.getY(), x2 = x, y2 = y;
		double d = Game.getDistance(v.getX(), v.getY(), x, y);
		if (DEBUG)
		    System.err.println("LVH(" + x1 + "," + y1
			    + ") with randomly generated (" + x2 + "," + y2
			    + "), d=(" + d + ")");
		return false;
	    }
	}
	return true;
    }

    public void delHazardArea(Random rnd, int n) {
	for (int i = 0; i < n; i++)
	    delHazard(rnd.nextInt(getListHazard().size()));
    }

    public void setTargetArea(Random rnd) throws UserDefinedException {
	int nTargetAreaLand = (Reschu.tutorial() || Reschu.extraTutorial()) ? MyGame.nTARGET_AREA_LAND_TUTORIAL
		: MyGame.nTARGET_AREA_LAND;
	int nLandTargetNeed = nTargetAreaLand - getTargetSize("LAND");
	int nShoreTargetNeed = MyGame.nTARGET_AREA_SHORE
		- getTargetSize("SHORE");
	int nCommTargetNeed = MyGame.nTARGET_AREA_COMM - getTargetSize("COMM");

	int cnt = 0;
	final int limit = 100000;

	for (int i = 0; i < nLandTargetNeed; i++) {
	    int x, y;
	    do {
		x = rnd.nextInt(MySize.width);
		y = rnd.nextInt(MySize.height);
		if (++cnt >= limit) {
		    throw new UserDefinedException(
			    "setTargetArea(land) try limit exceed");
		}
	    } while (!(getCellType(x, y) != MyGame.SEA && chkOkayToAdd(x, y)));
	    Target t = new Target(g.getEmptyTargetName(),
		    chkTargetOffset(x, y), "LAND", "UAV",
		    g.getTargetVisibility());
	    addTarget(t);
	}
	for (int i = 0; i < nShoreTargetNeed; i++) {
	    int x, y;
	    do {
		x = rnd.nextInt(MySize.width);
		y = rnd.nextInt(MySize.height);
		if (++cnt >= limit) {
		    throw new UserDefinedException(
			    "setTargetArea(shore) try limit exceed");
		}
	    } while (!(getCellType(x, y) == MyGame.SEASHORE && chkOkayToAdd(x,
		    y)));
	    Target t = new Target(g.getEmptyTargetName(),
		    chkTargetOffset(x, y), "SHORE", "UAV",
		    g.getTargetVisibility());
	    addTarget(t);
	}
	for (int i = 0; i < nCommTargetNeed; i++) {
	    int x, y;
	    do {
		x = rnd.nextInt(MySize.width);
		y = rnd.nextInt(MySize.height);
		if (++cnt >= limit) {
		    throw new UserDefinedException(
			    "setTargetArea(comm) try limit exceed");
		}
	    } while (!chkOkayToAdd(x, y));
	    Target t = new Target(g.getEmptyTargetName(),
		    chkTargetOffset(x, y), "COMM", "UAV",
		    g.getTargetVisibility());
	    addTarget(t);
	}
    }

    private int[] chkTargetOffset(int x, int y) {
	int offset = 10;
	if (x < offset)
	    x += offset;
	if (y < offset)
	    y += offset;
	if (x > MySize.width - offset)
	    x -= offset;
	if (y > MySize.height - offset)
	    y -= offset;
	return new int[] { x, y };
    }

    public int getAvailableTarget() {
	int cnt = 0;
	for (int i = 0; i < getListUnassignedTarget().size(); i++) {
	    if (getListUnassignedTarget().get(i).isVisible())
		cnt++;
	}
	return cnt;
    }

    public void garbageTargetCollect() {
	for (int i = 0; i < getListAssignedTarget().size(); i++)
	    if (getListAssignedTarget().get(i).isDone()) {
		Target t = getListAssignedTarget().get(i);
		lsnr.EVT_Target_Disappeared(t.getName(), t.getPos());
		g.setTargetUsed(t.getName(), false);
		getListAssignedTarget().remove(i);
	    }
    }

    // @TEMPORARY SOLUTION JUST FOR TUTORIAL BY CARL
    //
    // MANUAL SETTARGETAREA CODE I WAS ADDING
    // public void manual_setTargetArea(int x, int y, String Mission_Type,
    // String Vehicle_Type) {
    // Target t = new Target(g.getEmptyTargetName(), chkTargetOffset(x, y),
    // Mission_Type, Vehicle_Type, g.getTargetVisibility());
    // addTarget(t);
    // }

    // THIS IS TEMPORARY
    private boolean chkOkayToAdd_TEMPORARY_FOR_TUTORIAL_BY_CARL(int x, int y) {
	for (int j = 0; j < listAssignedTarget.size(); j++) {
	    Target t = listAssignedTarget.get(j);
	    if (Game.getDistance(t.getPos()[0], t.getPos()[1], x, y) < MySize.SIZE_HAZARD_3_PXL)
		// System.err.println("Target[" + t.getName() +"] d=" +
		// Game.getDistance(t.getPos()[0], t.getPos()[1], x, y ));
		return false;
	}
	for (int i = 0; i < listUnassignedTarget.size(); i++) {
	    Target t = listUnassignedTarget.get(i);
	    if (Game.getDistance(t.getPos()[0], t.getPos()[1], x, y) < MySize.SIZE_HAZARD_3_PXL)
		// System.err.println("Target[" + t.getName() +"] d=" +
		// Game.getDistance(t.getPos()[0], t.getPos()[1], x, y ));
		return false;
	}
	for (int i = 0; i < listHazard.size(); i++) {
	    if (Game.getDistance(x, y, listHazard.get(i)[0],
		    listHazard.get(i)[1]) < MySize.SIZE_HAZARD_3_PXL)
		return false;
	}
	for (int i = 0; i < g.getVehicleList().size(); i++) {
	    Vehicle v = g.getVehicleList().getVehicle(i);
	    if (Game.getDistance(v.getX(), v.getY(), x, y) < MySize.SIZE_HAZARD_3_PXL)
		return false;
	}
	return true;
    }

    // THIS IS TEMPORARY
    public void setTargetArea_TEMPORARY_FOR_TUTORIAL_BY_CARL(Random rnd) {
	int nTargetAreaLand = (Reschu.tutorial() || Reschu.extraTutorial()) ? MyGame.nTARGET_AREA_LAND_TUTORIAL
		: MyGame.nTARGET_AREA_LAND;
	int nLandTargetNeed = nTargetAreaLand - getTargetSize("LAND");
	int nShoreTargetNeed = MyGame.nTARGET_AREA_SHORE
		- getTargetSize("SHORE");
	int nCommTargetNeed = MyGame.nTARGET_AREA_COMM - getTargetSize("COMM");

	// System.out.println("Shore "+nShoreTargetNeed);

	for (int i = 0; i < nLandTargetNeed; i++) {
	    int x, y;
	    do {
		// System.out.println("LAND");
		x = rnd.nextInt(MySize.width);
		y = rnd.nextInt(MySize.height);
	    } while (getCellType(x, y) == MyGame.SEA
		    || !chkOkayToAdd_TEMPORARY_FOR_TUTORIAL_BY_CARL(x, y));
	    Target t = new Target(g.getEmptyTargetName(),
		    chkTargetOffset(x, y), "LAND", "UAV",
		    g.getTargetVisibility());
	    addTarget(t);
	}
	for (int i = 0; i < nShoreTargetNeed; i++) {
	    int x, y;
	    do {
		// System.out.println("SHORE");
		x = rnd.nextInt(MySize.width);
		y = rnd.nextInt(MySize.height);
		// if (getCellType(y,x)!=MyGame.SEASHORE)
		// System.out.println("LAND TYPE PROB");
		// if (!chkOkayToAdd(x,y)) System.out.println("GROUND TAKEN");
	    } while (getCellType(x, y) != MyGame.SEASHORE
		    || !chkOkayToAdd_TEMPORARY_FOR_TUTORIAL_BY_CARL(x, y));
	    Target t = new Target(g.getEmptyTargetName(),
		    chkTargetOffset(x, y), "SHORE", "UAV",
		    g.getTargetVisibility());
	    addTarget(t);
	}
	for (int i = 0; i < nCommTargetNeed; i++) {
	    int x, y;
	    do {
		x = rnd.nextInt(MySize.width);
		y = rnd.nextInt(MySize.height);
	    } while (!chkOkayToAdd_TEMPORARY_FOR_TUTORIAL_BY_CARL(x, y));
	    Target t = new Target(g.getEmptyTargetName(),
		    chkTargetOffset(x, y), "COMM", "UAV",
		    g.getTargetVisibility());
	    addTarget(t);
	}
    }

    @Override
    public String toString() {
	String result = "Assigned targets:\n";
	for (Target t : this.getListAssignedTarget()) {
	    if (t.isVisible()) {
		result += t.toString();
	    }
	}

	result += "Unassigned targets:\n";
	for (Target t : this.getListUnassignedTarget()) {
	    if (t.isVisible()) {
		result += t.toString();
	    }
	}

	result += hazardsToString();

	return result;
    }

    private String hazardsToString() {
	String result = "Hazards:\n";
	for (int[] i : getListHazard()) {
	    result += String.format("[%d, %d] ", i[0], i[1]);
	}
	result += "\n";
	return result;
    }
}
