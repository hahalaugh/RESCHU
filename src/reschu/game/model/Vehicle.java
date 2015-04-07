package reschu.game.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import reschu.constants.*;
import reschu.game.algorithm.Geo;
import reschu.game.controller.GUI_Listener;

public class Vehicle {
    final static public String TYPE_UAV = "UAV";
    final static public String TYPE_UUV = "UUV";
    final static public String PAYLOAD_ISR = "ISR";
    final static public String PAYLOAD_COM = "COM";

    private String name;
    private String type;
    private String payload;
    // private int pos_x, pos_y;
    private Target target;

    // {x, y, angel, wp}
    public double[] movingStatus = new double[4];
    public ArrayList<double[]> track = new ArrayList<double[]>();
    private LinkedList<int[]> path = new LinkedList<int[]>();
    private Map map;
    private int index;
    private int velocity;
    private GUI_Listener lsnr;
    private int status;
    private double vDamage;
    private Game g;
    // private int velocity_buffer;
    // private int velocity_scale;
    // private int UUV_stuck_count;
    // private boolean UUV_stuck;
    private boolean intersect;
    public final double RDes = 15;
    public final double RSen = 200;

    public boolean inCollision(Vehicle v) {
	return Geo.Distance(movingStatus, v.movingStatus) < (RDes + v.RDes);
    }

    public synchronized ArrayList<double[]> getTrack() {
	return track;
    }

    public synchronized void setTrack(ArrayList<double[]> t) {
	track = new ArrayList<double[]>(t);
    }

    /**
     * Set the position of this vehicle (synchronized)
     */
    public synchronized void setPos(int x, int y) {
	setX(x);
	setY(y);
    }

    /**
     * Get a path of this vehicle (synchronized)
     */
    public synchronized LinkedList<int[]> getPath() {
	return path;
    }

    /**
     * Add a waypoint to the path of this vehicle (synchronized)
     */
    public synchronized void addPath(int idx, int[] e) {
	path.add(idx, e);
	generateTrack();
    }

    /**
     * Add a waypoint to the last path of this vehicle (synchronized)
     */
    public synchronized void addPathLast(int[] e) {
	path.addLast(e);
	generateTrack();
    }

    /**
     * Set a path of this vehicle (synchronized)
     */
    public synchronized void setPath(int idx, int[] e) {
	path.set(idx, e);
	generateTrack();
    };

    /**
     * Get the size of a path of this vehicle (synchronized)
     */
    public synchronized int getPathSize() {
	return path.size();
    }

    /**
     * Get a coordinate of a waypoint of this vehicle (synchronized)
     */
    public synchronized int[] getPathAt(int idx) {
	return path.get(idx);
    }

    /**
     * Remove a waypoint in the path of this vehicle (synchronized)
     */
    public synchronized void removePathAt(int idx) {
	path.remove(idx);
	generateTrack();
    }

    /**
     * Get a coordinate at the first path of this vehicle (synchronized)
     */
    public synchronized int[] getFirstPath() {
	return path.getFirst();
    }

    /**
     * Get a coordinate at the last path of this vehicle (synchronized)
     */
    public synchronized int[] getLastPath() {
	return path.getLast();
    }

    /**
     * Remove the first waypoint of path of this vehicle (synchronized)
     */
    public synchronized void removeFirstPath() {
	path.removeFirst();
	generateTrack();
    }

    /**
     * Returns a map that this vehicle is assigned to
     */
    public synchronized Map getMap() {
	return map;
    }

    public synchronized void setX(double x) {
	movingStatus[0] = x;
    }

    public synchronized int getX() {
	return (int) Math.floor(movingStatus[0]);
    }

    public synchronized void setY(double y) {
	movingStatus[1] = y;
    }

    public synchronized int getY() {
	return (int) Math.floor(movingStatus[1]);
    }

    public void setName(String strName) {
	name = strName;
    }

    public String getName() {
	return name;
    }

    public void setType(String strType) {
	type = strType;
    }

    public String getType() {
	return type;
    }

    public void setIndex(int idx) {
	index = idx;
    }

    public int getIndex() {
	return index;
    }

    public void setPayload(String setPayload) {
	payload = setPayload;
    }

    public String getPayload() {
	return payload;
    }

    public void setVelocity(int milliseconds) {
	velocity = milliseconds;
    }

    public int getVelocity() {
	return velocity;
    }

    public void setTarget(Target t) {
	target = t;
    }

    public Target getTarget() {
	return target;
    }

    public void setStatus(int i) {
	status = i;
    }

    public int getStatus() {
	return status;
    }

    public void setIntersect(boolean b) {
	intersect = b;
    }

    public boolean getIntersect() {
	return intersect;
    }

    static public boolean isVehicleType(String s) {
	if (s.equals(TYPE_UAV) || s.equals(TYPE_UUV))
	    return true;
	else
	    return false;
    }

    public double getDamage() {
	return vDamage;
    }

    public void setGuiListener(GUI_Listener l) {
	lsnr = l;
    }

    public Vehicle(Map m, Game g) {
	setX(0);
	setY(0);
	setTarget(null);
	this.g = g;
	map = m;
	setStatus(MyGame.STATUS_VEHICLE_STASIS);
	vDamage = 0;
	// velocity_scale = MySpeed.SPEED_TIMER;
	// velocity_buffer = 0;
	// UUV_stuck_count = 0;
	// UUV_stuck = false;
	intersect = false;
    }

    private boolean boundaryCheck(int x, int y, int[] target_pos) {
	int w = Math.round(MySize.SIZE_TARGET_PXL / MySize.SIZE_CELL / 2);
	if ((x <= target_pos[0] + w) && (x >= target_pos[0] - w)
		&& (y <= target_pos[1] + w) && (y >= target_pos[1] - w))
	    return true;
	return false;
    }

    /**
     * Returns true if a vehicle's goal point is one of occupied targets.
     */
    public boolean isAssignededTarget(int x, int y) {
	int[] target_pos;

	for (int i = 0; i < getMap().getListAssignedTarget().size(); i++) {
	    target_pos = getMap().getListAssignedTarget().get(i).getPos();

	    if (boundaryCheck(x, y, target_pos)) {
		return true;
	    }
	}
	return false;
    }

    public void addGoal(int x, int y) {
	int[] target_pos;
	Target t;
	boolean assigned = false;

	for (int i = 0; i < getMap().getListUnassignedTarget().size(); i++) {
	    setTarget(null);
	    target_pos = getMap().getListUnassignedTarget().get(i).getPos();
	    t = getMap().getListUnassignedTarget().get(i);

	    if (!t.isVisible()) {
		if (getPayload() == Vehicle.PAYLOAD_COM
			&& boundaryCheck(x, y, target_pos)) {
		    x = target_pos[0];
		    y = target_pos[1];
		    setTarget(getMap().getListUnassignedTarget().get(i));
		    // PanelMsgBoard.Msg("Vehicle ["+index+"] is assigned to a target type ["+target.getMission()+"]");
		    // PanelMsgBoard.Msg("Vehicle [" + index +
		    // "] has been assigned to a target.",
		    // MessageType.SystemInfo);
		    getMap().assignTarget(new int[] { x, y });
		    assigned = true;
		    break;
		} else if (getPayload() != Vehicle.PAYLOAD_COM
			&& boundaryCheck(x, y, target_pos)) {
		    // 2008-04-05
		    // UAV to grey target ¡°You cannot assign a UAV to a grey
		    // target, please reassign¡±
		    lsnr.showMessageOnTopOfMap(
			    "You cannot assign a UAV to a grey target, please reassign "
				    + type + " " + index, 5);
		}
	    } else {
		if (getPayload() != Vehicle.PAYLOAD_COM
			&& boundaryCheck(x, y, target_pos)) {
		    x = target_pos[0];
		    y = target_pos[1];
		    if (type == Vehicle.TYPE_UUV
			    && getMap().getListUnassignedTarget().get(i)
				    .getMission() != "SHORE") {
			// 2008-04-05
			// UUV to land target (grey or red) ¡°You cannot assign
			// a UUV to a land target, please reassign¡±
			lsnr.showMessageOnTopOfMap(
				"You cannot assign a UUV to a land target, please reassign "
					+ type + " " + index, 5);
			break;
		    }
		    setTarget(getMap().getListUnassignedTarget().get(i));
		    // PanelMsgBoard.Msg("Vehicle [" + index +
		    // "] has been assigned to a target.",
		    // MessageType.SystemInfo);
		    getMap().assignTarget(new int[] { x, y });
		    assigned = true;
		    break;
		} else if (getPayload() == Vehicle.PAYLOAD_COM
			&& boundaryCheck(x, y, target_pos)) {
		    // 2008-04-05
		    // HALE to red target ¡°You cannot assign a HALE to a red
		    // target, please reassign¡±
		    lsnr.showMessageOnTopOfMap(
			    "You cannot assign a HALE to a red target, please reassign "
				    + type + " " + index, 5);
		}
	    }
	}
	addPathLast(new int[] { x, y });

	setStatus(MyGame.STATUS_VEHICLE_MOVING);
	if (g.getElapsedTime() != 0) {
	    if (assigned)
		lsnr.EVT_GP_SetGP_End_Assigned(index, x, y, getTarget()
			.getName());
	    else
		lsnr.EVT_GP_SetGP_End_Unassigned(index, x, y);
	}
    }

    public void changeGoal(int[] ex_goal, int x, int y) {
	if (x < 0 || x > MySize.width || y < 0 || y > MySize.height)
	    return;

	int[] ex_target_pos, new_target_pos;
	Target t;
	boolean assigned = false;

	for (int i = 0; i < getMap().getListAssignedTarget().size(); i++) {
	    ex_target_pos = getMap().getListAssignedTarget().get(i).getPos();

	    if (ex_goal[0] == ex_target_pos[0]
		    && ex_goal[1] == ex_target_pos[1]) {
		setTarget(null);
		getMap().unassignTarget(ex_target_pos);
		break;
	    }
	}

	for (int i = 0; i < getMap().getListUnassignedTarget().size(); i++) {
	    new_target_pos = getMap().getListUnassignedTarget().get(i).getPos();
	    t = getMap().getListUnassignedTarget().get(i);

	    if (!t.isVisible()) {
		if (getPayload() == Vehicle.PAYLOAD_COM
			&& boundaryCheck(x, y, new_target_pos)) {
		    x = new_target_pos[0];
		    y = new_target_pos[1];
		    setTarget(getMap().getListUnassignedTarget().get(i));
		    // PanelMsgBoard.Msg("Vehicle [" + index +
		    // "] has been assigned to a target.",
		    // MessageType.SystemInfo);
		    getMap().assignTarget(new int[] { x, y });
		    assigned = true;
		    break;
		} else if (getPayload() != Vehicle.PAYLOAD_COM
			&& boundaryCheck(x, y, new_target_pos)) {
		    // 2008-04-05
		    // UAV to grey target ¡°You cannot assign a UAV to a grey
		    // target, please reassign¡±
		    lsnr.showMessageOnTopOfMap(
			    "You cannot assign a UAV to a grey target, please reassign "
				    + type + " " + index, 5);
		}
	    } else {
		if (getPayload() != Vehicle.PAYLOAD_COM
			&& boundaryCheck(x, y, new_target_pos)) {
		    x = new_target_pos[0];
		    y = new_target_pos[1];
		    if (type == Vehicle.TYPE_UUV
			    && getMap().getListUnassignedTarget().get(i)
				    .getMission() != "SHORE") {
			// 2008-04-05
			// UUV to land target (grey or red) ¡°You cannot assign
			// a UUV to a land target, please reassign¡±
			lsnr.showMessageOnTopOfMap(
				"You cannot assign a UUV to a land target, please reassign "
					+ type + " " + index, 5);
			break;
		    }
		    setTarget(getMap().getListUnassignedTarget().get(i));
		    // PanelMsgBoard.Msg("Vehicle [" + index +
		    // "] has been assigned to a target.",
		    // MessageType.SystemInfo);
		    getMap().assignTarget(new int[] { x, y });
		    assigned = true;
		    break;
		} else if (getPayload() == Vehicle.PAYLOAD_COM
			&& boundaryCheck(x, y, new_target_pos)) {
		    // 2008-04-05
		    // HALE to red target ¡°You cannot assign a HALE to a red
		    // target, please reassign¡±
		    lsnr.showMessageOnTopOfMap(
			    "You cannot assign a HALE to a red target, please reassign "
				    + type + " " + index, 5);
		}
	    }
	}
	setPath(getPathSize() - 1, new int[] { x, y });
	if (g.getElapsedTime() != 0) {
	    if (assigned)
		lsnr.EVT_GP_ChangeGP_End_Assigned(index, x, y, getTarget()
			.getName());
	    else
		lsnr.EVT_GP_ChangeGP_End_Unassigned(index, x, y);
	}
    }

    public synchronized int addWaypoint(int x, int y) {
	double d, idx = 0;
	double distance = 9999999; // infinite

	d = Game.getDistance(getX(), getY(), x, y)
		+ Game.getDistance(getPathAt(0)[0], getPathAt(0)[1], x, y);
	if (d < distance)
	    distance = d;
	idx = 0;

	for (int i = 0; i < getPathSize() - 1; i++) {
	    d = Game.getDistance(getPathAt(i)[0], getPathAt(i)[1], x, y)
		    + Game.getDistance(getPathAt(i + 1)[0],
			    getPathAt(i + 1)[1], x, y);
	    if (d < distance) {
		distance = d;
		idx = i + 1;
	    }
	}

	if (x > 0 && x < MySize.width && y > 0 && y < MySize.height)
	    addPath((int) idx, new int[] { x, y });
	return (int) idx;
    }

    public synchronized int addWaypoint(int x, int y, int idx) {
	addPath(idx, new int[] { x, y });

	return idx;
    }

    public void delWaypoint(int x, int y) {
	for (int i = 0; i < getPathSize() - 1; i++)
	    if (getPathAt(i)[0] == x && getPathAt(i)[1] == y) {
		removePathAt(i);

	    }
    }

    public void delWaypoint(int[] coordinate) {
	for (int i = 0; i < getPathSize() - 1; i++)
	    if (getPathAt(i) == coordinate) {
		removePathAt(i);
	    }
    }

    public void changeWaypoint(int ex_x, int ex_y, int new_x, int new_y) {
	if (new_x < 0 || new_x > MySize.width || new_y < 0
		|| new_y > MySize.height)
	    return;

	for (int i = 0; i < getPathSize() - 1; i++) {
	    if (getPathAt(i)[0] == ex_x && getPathAt(i)[1] == ex_y) {
		getPathAt(i)[0] = new_x;
		getPathAt(i)[1] = new_y;
		this.generateTrack();
	    }
	}
    }

    public boolean hasGoal() {
	if (getPathSize() == 0)
	    return false;
	return true;
    }

    public boolean hasWaypoint() {
	if (getPathSize() < 2)
	    return false;
	return true;
    }

    // Moving Algorithms
    public double getDistance(int pos_x, int pos_y) {
	if (getPathSize() == 0)
	    return 0;
	return Math.sqrt(Math.pow((double) (pos_x - getFirstPath()[0]), 2.0)
		+ Math.pow((double) (pos_y - getFirstPath()[1]), 2.0));
    }

    public synchronized void move() {
	if (hasGoal()) {
	    this.setStatus(MyGame.STATUS_VEHICLE_MOVING);
	    updateMovingStatus();
	    track.remove(0);
	    this.payloadCheck(getX(), getY());
	}
    }

    protected synchronized void updateMovingStatus() {
	if (this.track.size() > 0) {
	    movingStatus = this.track.get(0).clone();
	}

    }

    public void moveRandom(int i) {
	Random rnd = new Random();
	moveTo(rnd.nextInt(8));
    }

    public void moveHillClimbing() {
	double presentDistance, d = 999999999;
	Random rnd = new Random();
	int direction = 8;

	presentDistance = getDistance(getX(), getY());

	for (int i = 0; i < 8; i++) {
	    direction = rnd.nextInt(8);
	    switch (direction) {
	    case 0:
		d = getDistance(getX() - 1, getY() - 1);
		break;
	    case 1:
		d = getDistance(getX() - 1, getY());
		break;
	    case 2:
		d = getDistance(getX() - 1, getY() + 1);
		break;
	    case 3:
		d = getDistance(getX(), getY() - 1);
		break;
	    case 4:
		d = getDistance(getX(), getY() + 1);
		break;
	    case 5:
		d = getDistance(getX() + 1, getY() - 1);
		break;
	    case 6:
		d = getDistance(getX() + 1, getY());
		break;
	    case 7:
		d = getDistance(getX() + 1, getY() + 1);
		break;
	    }
	    if (d < presentDistance && chkValidMove(direction)) {
		break;
	    }
	}

	moveTo(direction);
    }

    public void moveBestFirst() {
	int direction = 0;
	double d = 999999999, bestDistance = 999999999;

	for (int i = 0; i < 8; i++) {
	    switch (i) {
	    case 0:
		d = getDistance(getX() - 1, getY() - 1);
		break;
	    case 1:
		d = getDistance(getX() - 1, getY());
		break;
	    case 2:
		d = getDistance(getX() - 1, getY() + 1);
		break;
	    case 3:
		d = getDistance(getX(), getY() - 1);
		break;
	    case 4:
		d = getDistance(getX(), getY() + 1);
		break;
	    case 5:
		d = getDistance(getX() + 1, getY() - 1);
		break;
	    case 6:
		d = getDistance(getX() + 1, getY());
		break;
	    case 7:
		d = getDistance(getX() + 1, getY() + 1);
		break;
	    }
	    if (d < bestDistance) {
		bestDistance = d;
		direction = i;
	    }
	}
	if (chkValidMove(direction))
	    moveTo(direction);
    }

    public void moveTo(int direction) {
	switch (direction) {
	case 0: // up-left
	    setX(getX() - MySpeed.VELOCITY);
	    setY(getY() - MySpeed.VELOCITY);
	    break;
	case 1: // up
	    setX(getX() - MySpeed.VELOCITY);
	    break;
	case 2: // up-right
	    setX(getX() - MySpeed.VELOCITY);
	    setY(getY() + MySpeed.VELOCITY);
	    break;
	case 3: // left
	    setY(getY() - MySpeed.VELOCITY);
	    break;
	case 4: // right
	    setY(getY() + MySpeed.VELOCITY);
	    break;
	case 5: // down-left
	    setX(getX() + MySpeed.VELOCITY);
	    setY(getY() - MySpeed.VELOCITY);
	    break;
	case 6: // down
	    setX(getX() + MySpeed.VELOCITY);
	    break;
	case 7: // down-right
	    setX(getX() + MySpeed.VELOCITY);
	    setY(getY() + MySpeed.VELOCITY);
	    break;
	default:
	    break;
	}
	payloadCheck(getX(), getY());
    }

    private void payloadCheck(int pos_x, int pos_y) {
	if (getPathSize() != 0 && Math.abs(pos_x - getFirstPath()[0]) <= 1
		&& Math.abs(pos_y - getFirstPath()[1]) <= 1) {
	    if (getPathSize() == 1 && target == null) {
		setStatus(MyGame.STATUS_VEHICLE_STASIS);
	    }

	    else if (getPathSize() == 1 && target != null) {
		// VEHICLE ARRIVED TO ITS GOAL WHERE THE PLACE IS THE ONE OF
		// UNASSIGNED_TARGETS
		if (getPayload() == Vehicle.PAYLOAD_COM) {
		    setStatus(MyGame.STATUS_VEHICLE_PENDING);
		} else {
		    setStatus(MyGame.STATUS_VEHICLE_PENDING);
		    //String msg = "Vehicle [" + index
			   // + "] has reached its target.";
		    // PanelMsgBoard.Msg(msg, MessageType.SystemInfo);
		}
		lsnr.EVT_Vehicle_ArrivesToTarget(index, getTarget().getName(),
			getTarget().getPos()[0], getTarget().getPos()[1]);
	    }

	    lsnr.Hide_Popup(this);
	    removeFirstPath();
	}
    }

    public synchronized boolean chkValidMove(int direction) {
	switch (direction) {
	case 0: // up-left
	    if ((getX() > 0 && getY() > 0)
		    && (chkValidPosition(getX() - MySpeed.VELOCITY, getY()
			    - MySpeed.VELOCITY)))
		return true;
	    return false;
	case 1: // up
	    if ((getX() > 0)
		    && (chkValidPosition(getX() - MySpeed.VELOCITY, getY())))
		return true;
	    return false;
	case 2: // up-right
	    if ((getX() > 0 && getY() < MySize.height - 1)
		    && (chkValidPosition(getX() - MySpeed.VELOCITY, getY()
			    + MySpeed.VELOCITY)))
		return true;
	    return false;
	case 3: // left
	    if ((getY() > 0)
		    && (chkValidPosition(getX(), getY() - MySpeed.VELOCITY)))
		return true;
	    return false;
	case 4: // right
	    if ((getY() < MySize.height - 1)
		    && (chkValidPosition(getX(), getY() + MySpeed.VELOCITY)))
		return true;
	    return false;
	case 5: // down-left
	    if ((getX() < MySize.width - 1 && getY() > 0)
		    && (chkValidPosition(getX() + MySpeed.VELOCITY, getY()
			    - MySpeed.VELOCITY)))
		return true;
	    return false;
	case 6: // down
	    if ((getX() < MySize.width - 1)
		    && (chkValidPosition(getX() + MySpeed.VELOCITY, getY())))
		return true;
	    return false;
	case 7: // down-right
	    if ((getX() < MySize.width - 1 && getY() < MySize.height - 1)
		    && (chkValidPosition(getX() + MySpeed.VELOCITY, getY()
			    + MySpeed.VELOCITY)))
		return true;
	    return false;
	default:
	    return false;
	}
    }

    public synchronized boolean chkValidPosition(int width, int height) {
	if (type == Vehicle.TYPE_UUV) {
	    if (getMap().getCellType(width, height) == MyGame.LAND)
		return false;
	}
	return true;
    }

    public double chkHazardArea() {
	int damage = 0;
	double globalDamage = 0;
	double d;
	int[] hazard_pos;
	for (int i = 0; i < map.getListHazard().size(); i++) {
	    hazard_pos = map.getListHazard().get(i);
	    d = Math.sqrt(Math.pow(
		    (double) (this.movingStatus[0] - hazard_pos[0]), 2.0)
		    + Math.pow((double) (this.movingStatus[1] - hazard_pos[1]),
			    2.0))
		    * MySize.SIZE_CELL;
	    if (d <= MySize.SIZE_HAZARD_1_PXL) {
		damage += 50;
		globalDamage += 0.05;
	    }

	    else if (d < MySize.SIZE_HAZARD_2_PXL
		    && d > MySize.SIZE_HAZARD_1_PXL) {
		damage += 30;
		globalDamage += 0.03;
	    } else {
		damage += 0;
		globalDamage += 0;
	    }

	    if (d < 50d && d > 45d)
		lsnr.EVT_Vehicle_Damaged(getIndex(), hazard_pos[0],
			hazard_pos[1]);

	}
	// We don't decrease the speed of a vehicle anymore
	// setBuffer(damage);
	vDamage += (double) (damage) / 100;
	lsnr.Vehicle_Damaged_By_Hazard_Area_From_Vehicle(this);

	return globalDamage;
    }

    // private void setBuffer(int i) {
    // velocity_buffer += i;
    // if( velocity_buffer >= velocity_scale ) {
    // velocity += velocity_scale;
    // velocity_buffer -= velocity_scale;
    // lsnr.EVT_Vehicle_SpeedDecreased(index, velocity);
    // }
    // }

    private void updateVisibility(int x, int y) {
	int[] target_pos;
	Target t;
	int w = Math.round(MySize.SIZE_TARGET_PXL / MySize.SIZE_CELL / 2);
	int d = Math.round(MySize.SIZE_UAV_COMM_PXL / MySize.SIZE_CELL / 3);

	for (int i = 0; i < getMap().getListAssignedTarget().size(); i++) {
	    setTarget(null);
	    target_pos = getMap().getListAssignedTarget().get(i).getPos();
	    t = getMap().getListAssignedTarget().get(i);
	    if (!t.isVisible()) {
		if ((x - d <= target_pos[0] + w)
			&& (x + d >= target_pos[0] - w)
			&& (y - d <= target_pos[1] + w)
			&& (y + d >= target_pos[1] - w)) {
		    t.setVisible(true);
		    lsnr.EVT_Target_BecameVisible(t.getName(), t.getPos());
		    break;
		}
	    }
	}
    }

    public void COM_Payload() {
	updateVisibility(getX(), getY());
	getMap().unassignTarget(new int[] { getX(), getY() });
	lsnr.Target_Become_Visible_From_Vehicle(this);
	setStatus(MyGame.STATUS_VEHICLE_MOVING);
	lsnr.EVT_Payload_EngagedAndFinished_COMM(index, getTarget().getName());
    }

    private synchronized void generateTrack() {
	track.clear();
	double[] src = movingStatus.clone();

	for (int[] p : path) {
	    double[] dest = new double[] { (double) p[0], (double) p[1] };
	    track.addAll(Geo.Line(src, dest));
	    track.get(track.size() - 1)[3] = 1;
	    src = dest;
	}
    }

    @Override
    public String toString() {
	return String.format(
		"id: %d, coor: [%d, %d], paths: %s, status: %d, target: %s",
		getIndex(), getX(), getY(), pathToString(), getStatus(),
		(getTarget() == null ? -1 : getTarget().getName()));
    }

    private String pathToString() {
	String wps = "";
	for (int[] i : this.getPath()) {
	    wps += "[";
	    wps += i[0] + "," + i[1];
	    wps += "] ";
	}
	return wps;
    }
}
