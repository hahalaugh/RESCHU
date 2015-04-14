package reschu.game.algorithm;

import java.awt.Polygon;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import reschu.constants.MyGame;
import reschu.constants.MySize;
import reschu.game.model.Vehicle;
import reschu.game.model.VehicleList;

public class CollisionMap {
    public final static int HAZARD_CONTOUR = 1024;
    public final static byte TRACK_COLLISION = 1;
    public final static byte HAZARD_COLLISION = 2;

    static private Integer[][][] GetPathMap(int height, int width,
	    VehicleList vl, Integer[] freeVIds) {
	Integer[][][] mp = new Integer[width][height][];
	for (int i = 0; i < freeVIds.length; i++) {
	    Vehicle v = vl.getVehicleByIndex(freeVIds[i]);
	    ArrayList<double[]> track = v.getTrack();
	    for (int j = 0; j < track.size(); j++) {
		double[] pixel = track.get(j);
		// Set cell with vehicle id

		mp[(int) pixel[0]][(int) pixel[1]] = RemoveDuplicates(ExpendArray(
			mp[(int) pixel[0]][(int) pixel[1]], v.getIndex()));
	    }
	}
	return mp;
    }

    private static Integer[] RemoveDuplicates(Integer[] arr) {
	return new HashSet<Integer>(Arrays.asList(arr)).toArray(new Integer[0]);
    }

    static private Integer[] ExpendArray(Integer[] ori, int item) {
	if (ori == null) {
	    return new Integer[] { item };
	} else {
	    Integer[] expended = new Integer[ori.length + 1];
	    System.arraycopy(ori, 0, expended, 0, ori.length);
	    expended[expended.length - 1] = item;
	    return expended;
	}
    }

    static public byte[][] GetVHCollision(int height, int width,
	    LinkedList<int[]> hazards, VehicleList vl) {
	double[][] map = new double[width][height];
	byte[][] collisionMap = new byte[width][height];

	// Adding hazard contours to track map
	for (int i = 0; i < hazards.size(); i++) {
	    double x0 = hazards.get(i)[0];
	    double y0 = hazards.get(i)[1];

	    int radius = 15;

	    for (int angle = 0; angle <= 360; angle++) {
		int x = (int) (x0 + radius * Math.cos(angle * Math.PI / 180));
		int y = (int) (y0 + radius * Math.sin(angle * Math.PI / 180));

		if (x < width && x > 0 && y < height && y > 0) {
		    map[x][y] = HAZARD_CONTOUR;
		}
	    }
	}

	for (int i = 0; i < vl.size(); i++) {
	    Vehicle v = vl.getVehicle(i);
	    ArrayList<double[]> track = v.getTrack();
	    for (int j = 0; j < track.size(); j++) {
		double[] pixel = track.get(j);

		double x = pixel[0];
		double y = pixel[1];

		if (x > 0 && x < width && y > 0 && y < height) {
		    for (int hor = -1; hor <= 1; hor++) {
			for (int ver = -1; ver <= 1; ver++) {
			    // Is hazard and also within the range of sensor
			    if (map[(int) (x + hor)][(int) (y + ver)] == HAZARD_CONTOUR
				    && Geo.Distance(v.movingStatus,
					    new double[] { x + hor, y + ver }) < v.RSen) {
				collisionMap[(int) (x + hor)][(int) (y + ver)] = CollisionMap.HAZARD_COLLISION;
			    }
			}
		    }
		}
	    }
	}

	return collisionMap;
    }

    static public ArrayList<CollisionZone> GetVVCollision(int height,
	    int width, VehicleList vl) {
	// SQUARE TO REPRESENT THE COLLISION AREA
	// ArrayList<int[]> CollisionPrediction = new ArrayList<int[]>();
	ArrayList<CollisionZone> CollisionPrediction = new ArrayList<CollisionZone>();
	for (int i = 0; i < vl.size(); i++) {
	    Vehicle vSource = vl.getVehicle(i);
	    if (vSource.getStatus() != MyGame.STATUS_VEHICLE_MOVING) {
		for (int j = 0; j < vl.size(); j++) {
		    Vehicle vOppo = vl.getVehicle(j);
		    if (i == j
			    || Geo.Distance(vSource.movingStatus,
				    vOppo.movingStatus) > vSource.RSen
			    || vOppo.getStatus() != MyGame.STATUS_VEHICLE_MOVING) {
			continue;
		    }

		    ArrayList<double[]> oppoTrack = vOppo.getTrack();
		    double[] sourcePos = vSource.movingStatus;
		    double[] distances = new double[oppoTrack.size()];
		    ArrayList<int[]> CollisionRange = new ArrayList<int[]>();
		    ArrayList<double[]> CollisionArea = new ArrayList<double[]>();

		    for (int k = 0; k < oppoTrack.size(); k++) {
			distances[k] = Geo.Distance(oppoTrack.get((k)),
				sourcePos);
		    }

		    CollisionRange = GetCollisionRange(distances, vSource.RDes
			    + vOppo.RDes);

		    // CollisionArea.add(sourcePos);
		    for (int k = 0; k < CollisionRange.size(); k++) {
			for (int cursor = CollisionRange.get(k)[0]; cursor < CollisionRange
				.get(k)[1]; cursor++) {
			    CollisionArea.add(oppoTrack.get(cursor));
			}
		    }

		    if (!CollisionArea.isEmpty()) {
			// adding involved vehicle and bounding box of
			// collision
			// area
			CollisionPrediction.add(new CollisionZone(
				PreciseCollisionArea(CollisionArea),
				new Integer[] { vSource.getIndex(),
					vOppo.getIndex() }, false));
		    }
		}
	    } else {
		for (int j = i + 1; j < vl.size(); j++) {
		    Vehicle vOppo = vl.getVehicle(j);

		    if (Geo.Distance(vSource.movingStatus, vOppo.movingStatus) > vSource.RSen
			    || vOppo.getStatus() != MyGame.STATUS_VEHICLE_MOVING) {
			continue;
		    }

		    ArrayList<double[]> trackSource = vSource.getTrack();
		    ArrayList<double[]> trackTarget = vOppo.getTrack();
		    ArrayList<int[]> CollisionRange = new ArrayList<int[]>();
		    ArrayList<double[]> CollisionArea = new ArrayList<double[]>();

		    // Time to arrive
		    int step = trackSource.size() < trackTarget.size() ? trackSource
			    .size() : trackTarget.size();
		    double[] distances = new double[step];

		    for (int k = 0; k < step; k++) {
			distances[k] = Geo.Distance(trackSource.get((k)),
				trackTarget.get(k));
		    }

		    CollisionRange = GetCollisionRange(distances, vSource.RDes
			    + vOppo.RDes);

		    for (int[] range : CollisionRange) {
			for (int q = range[0]; q < range[1]; q++) {
			    if (q < trackSource.size()
				    && q < trackTarget.size()) {
				CollisionArea.add(trackSource.get(q));
				CollisionArea.add(trackTarget.get(q));
			    }
			}

			if (!CollisionArea.isEmpty()) {
			    // adding involved vehicle and bounding box of
			    // collision
			    // area
			    // CollisionPrediction.add(new
			    // CollisionZone(GetBoundingBox(CollisionArea), new
			    // int[] { vSource.getIndex(), vOppo.getIndex() }));
			    CollisionPrediction.add(new CollisionZone(
				    PreciseCollisionArea(CollisionArea),
				    new Integer[] { vSource.getIndex(),
					    vOppo.getIndex() }, false));
			}
		    }
		}
	    }
	}

	return CollisionPrediction;
    }

    static private ArrayList<int[]> GetCollisionRange(double[] distances,
	    double collisionRadius) {
	ArrayList<int[]> collisionRange = new ArrayList<int[]>();
	for (int i = 0; i < distances.length; i++) {
	    if (distances[i] <= collisionRadius) {
		int left = i;
		int right = i;
		while (i < distances.length - 1
			&& distances[i + 1] <= collisionRadius) {
		    right = ++i;
		}
		int[] range = { left, right };
		collisionRange.add(range);
	    }
	}
	return collisionRange;
    }

    static public boolean pointInPolygon(Integer[] polyX, Integer[] polyY,
	    Integer x, Integer y) {
	if (polyX.length != polyY.length) {
	    return false;
	} else if (polyX.length < 3) {
	    // Need at least three points to consist a polygon.
	    return false;
	} else {
	    int i, j = polyX.length - 1;
	    boolean oddNodes = false;

	    for (i = 0; i < polyX.length; i++) {
		if ((polyY[i] < y && polyY[j] >= y || polyY[j] < y
			&& polyY[i] >= y)
			&& (polyX[i] <= x || polyX[j] <= x)) {
		    oddNodes ^= (polyX[i] + (y - polyY[i])
			    / (polyY[j] - polyY[i]) * (polyX[j] - polyX[i]) < x);
		}
		j = i;
	    }

	    return oddNodes;
	}
    }

    static public Rectangle2D PreciseCollisionArea(ArrayList<double[]> points) {
	Polygon r = new Polygon();
	for (double[] point : points) {
	    int x = (int) point[0];
	    int y = (int) point[1];
	    r.addPoint(x, y);
	}
	return r.getBounds2D();
    }

    // Fake a collision zone. Find the most suspicious cross and add a highlight
    // area on it.
    public static CollisionZone FakeCollisionZone(int height, int width,
	    VehicleList vl, ArrayList<CollisionZone> cz,
	    ArrayList<CollisionZone> fakeCz) {
	// find free vehicles
	ArrayList<CollisionZone> tempCZ = new ArrayList<CollisionZone>(cz);
	tempCZ.addAll(fakeCz);

	Integer[] freeVIds = GetCollisionFreeVehicles(tempCZ);

	if (freeVIds.length <= 1) {
	    return null;
	}

	Integer[][][] pathMap = GetPathMap(height, width, vl, freeVIds);

	ArrayList<CollisionIntersect> cis = new ArrayList<CollisionIntersect>();
	for (int i = 0; i < pathMap.length; i++) {
	    for (int j = 0; j < pathMap[0].length; j++) {
		if (pathMap[i][j] != null && pathMap[i][j].length >= 2) {
		    cis.add(new CollisionIntersect(i, j, pathMap[i][j], vl));
		}
	    }
	}

	// according to path map, get if there is available collision zone.
	Collections.sort(cis, new CollisionIntersectComparator());
	CollisionZone zone = null;
	if (cis.size() > 0) {
	    CollisionIntersect fakeCI = cis.get(cis.size() - 1);
	    if (fakeCI != null) {
		Polygon r = new Polygon();
		Random rnd = new Random(5);
		r.addPoint((int) fakeCI.pos[0] + MySize.SIZE_FAKE_COLLISION
			+ rnd.nextInt(10), (int) fakeCI.pos[1]
			+ MySize.SIZE_FAKE_COLLISION + rnd.nextInt(10));
		r.addPoint((int) fakeCI.pos[0] + MySize.SIZE_FAKE_COLLISION
			+ rnd.nextInt(10), (int) fakeCI.pos[1]
			- MySize.SIZE_FAKE_COLLISION - rnd.nextInt(10));
		r.addPoint((int) fakeCI.pos[0] - MySize.SIZE_FAKE_COLLISION
			- rnd.nextInt(10), (int) fakeCI.pos[1]
			+ MySize.SIZE_FAKE_COLLISION + rnd.nextInt(10));
		r.addPoint((int) fakeCI.pos[0] - MySize.SIZE_FAKE_COLLISION
			- rnd.nextInt(10), (int) fakeCI.pos[1]
			- MySize.SIZE_FAKE_COLLISION - rnd.nextInt(10));

		zone = new CollisionZone(r.getBounds2D(),
			fakeCI.involvedVehicleId, true);
	    }
	}

	return zone;
    }

    private static Integer[] GetCollisionFreeVehicles(
	    ArrayList<CollisionZone> curCollisionZones) {
	HashSet<Integer> vehiclesInCollision = new HashSet<Integer>();
	ArrayList<Integer> cFreeVehicles = new ArrayList<Integer>();
	for (CollisionZone cz : curCollisionZones) {
	    for (Integer i : cz.involvedVehicles) {
		// add ID
		vehiclesInCollision.add(i);
	    }
	}

	for (int i = 1; i <= 5; i++) {
	    if (!vehiclesInCollision.contains(i)) {
		cFreeVehicles.add(i);
	    }
	}

	return cFreeVehicles.toArray(new Integer[cFreeVehicles.size()]);
    }
}

class CollisionIntersect {
    public CollisionIntersect(int x, int y, Integer[] involvedVehicleId,
	    VehicleList vl) {
	super();
	this.involvedVehicleId = involvedVehicleId;
	pos = new double[] { x, y };
	this.vl = vl;
    }

    Integer[] involvedVehicleId;
    double[] pos;
    private VehicleList vl;

    public double Distance() {
	double total = 0;
	for (int i = 0; i < involvedVehicleId.length; i++) {
	    total += Geo.Distance(pos,
		    vl.getVehicleByIndex(involvedVehicleId[i]).movingStatus);
	}
	return total / (involvedVehicleId.length);
    }
}

class CollisionIntersectComparator implements Comparator<CollisionIntersect> {
    public int compare(CollisionIntersect o1, CollisionIntersect o2) {
	return Double.compare(o1.Distance(), o2.Distance());
    }
}
