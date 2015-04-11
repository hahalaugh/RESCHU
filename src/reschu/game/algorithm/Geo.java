package reschu.game.algorithm;

import java.util.ArrayList;

public class Geo {
    // Turn radius of vehicle. It tunes the DAEps and optimal turn radius when
    // executing maneuver.
    public static double RR = 10;

    // Proportional control ratio constant
    private static double K = 0.01;

    // The increments of each step. It is applied in circular movement and
    // linear movement
    // In circular movement, step = 1e-2 means the included angle between
    // pre-move radius and post-move radius is 1e-2
    public static final double Step = Math.PI / 180;

    // Angular Epsilon. It is applied when comparing movement heading.
    private static final double AEps = 2 * Step;

    // Distance Angular Epsilon. It is applied when comparing the position of
    // point on the circle.
    // private static final double DAEps = 10 * Step;

    // Distance Epsilon. It is applied when comparing the position of point on a
    // line.
    private static final double DEps = 2 * Step;

    // Tuning parameter for CA Radius
    private static final double Lambda = 0.5;

    public enum Side {
	Left, Right, Linear, InverseLinear
    }

    public static boolean DEqual(double a, double b) {
	return Math.abs(a - b) <= DEps;
    }

    public static double DistancePL(double[] vector, double[] p) {
	double x0 = p[0];
	double y0 = p[1];

	double x1 = vector[0];
	double y1 = vector[1];

	double x2 = vector[0] + 10 * Math.cos(vector[2]);
	double y2 = vector[1] + 10 * Math.sin(vector[2]);

	return (Math.abs((y2 - y1) * x0 + (x1 - x2) * y0
		+ ((x2 * y1) - (x1 * y2))))
		/ (Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x1 - x2, 2)));
    }

    public static double Distance(double[] src, double[] dest) {
	double x1 = src[0];
	double y1 = src[1];
	double x2 = dest[0];
	double y2 = dest[1];

	return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static double Radian(double[] src, double[] dest) {
	double[] vect = new double[] { dest[0] - src[0], dest[1] - src[1] };
	double vSin = vect[1]
		/ Math.sqrt(vect[0] * vect[0] + vect[1] * vect[1]);
	double vCos = vect[0]
		/ Math.sqrt(vect[0] * vect[0] + vect[1] * vect[1]);
	double theta = Math.acos(vCos);

	return (vSin > 0) ? theta : 2 * Math.PI - theta;
    }

    public static ArrayList<double[]> Line(double[] src, double[] dest) {
	ArrayList<double[]> result = new ArrayList<double[]>();
	double heading = Radian(src, dest);
	for (double i = 0; i < Distance(src, dest); i += 1) {
	    result.add(new double[] { src[0] + i * Math.cos(heading),
		    src[1] + i * Math.sin(heading), heading, 0 });
	}

	return ReduceDuplication(result);
    }

    public static double[] DubinsMove(double[] src, double[] dest, double R) {
	if (Geo.IsHeading(src, dest)) {
	    return Forward(src);
	} else {
	    Side s = Geo.DestLocating(src, dest);
	    Side oppo = (s == Side.Right ? Side.Left : Side.Right);
	    double distance = Geo.Distance(Geo.Center(src, R, s), dest);

	    if (Geo.DEqual(distance, R)) {
		return Turn(src, R, s);
	    } else if (distance > R) {
		return Turn(src, R, s);
	    } else {
		return Turn(src, R, oppo);
	    }
	}
    }

    public static double DubinsR(double[] src, double[] dest) {
	double theta = K * Geo.Theta(src[2], Geo.Radian(src, dest));
	return Math.log((1 / theta)) * Math.log((1 / theta));
    }

    private static ArrayList<double[]> D(double[] src, double[] dest, double R) {
	if (Geo.IsHeading(src, dest)) {
	    return Line(src, dest);
	} else {
	    ArrayList<double[]> list = new ArrayList<double[]>();

	    Side s = Geo.DestLocating(src, dest);
	    Side oppo = (s == Side.Right ? Side.Left : Side.Right);
	    double distance = Geo.Distance(Geo.Center(src, R, s), dest);

	    src = Geo.DEqual(distance, R) ? Turn(src, R, s)
		    : Turn(src, R, oppo);

	    list.add(src);
	    list.addAll(D(src, dest, R));

	    return list;
	}
    }

    public static ArrayList<double[]> Dubins(double[] src, double[] dest) {
	return D(src, dest, DubinsR(src, dest));
    }

    public static double[] Forward(double[] src) {
	return new double[] { src[0] + Step * Math.cos(src[2]),
		src[1] + Step * Math.sin(src[2]), src[2], 0 };
    }

    public static boolean IsHeading(double[] src, double[] dest) {
	// return Math.abs(Radian(src, dest) - src[2]) < AEps;
	return Geo.Theta(src[2], Radian(src, dest)) < AEps;
    }

    public static Side DestLocating(double[] src, double[] dest) {
	double theta = src[2];
	double[] pre = { src[0] + 10 * Math.cos(theta),
		src[1] + 10 * Math.sin(theta) };
	double delta = (src[0] - dest[0]) * (pre[1] - dest[1])
		- (src[1] - dest[1]) * (pre[0] - dest[0]);

	if (Math.abs(delta) < AEps) {
	    if (Math.abs(Distance(src, pre) + Distance(pre, dest)
		    - Distance(src, dest)) < AEps) {
		return Side.Linear;
	    } else {
		return Side.InverseLinear;
	    }
	} else {
	    if (delta > 0) {
		return Side.Left;
	    } else {
		return Side.Right;
	    }
	}
    }

    public static double[] Turn(double[] src, double radius, Side side) {
	double turnFlag = (side == Side.Right ? -1 : 1);
	double[] center = Center(src, radius, side);
	double delta = Step * turnFlag;
	double angular = delta + Radian(center, src);
	double heading = src[2] + delta;

	heading = ((heading < 0) ? (heading += 2 * Math.PI)
		: (heading % (2 * Math.PI)));
	double[] result = new double[] {
		center[0] + radius * Math.cos(angular),
		center[1] + radius * Math.sin(angular), heading, 0 };

	return result;
    }

    public static double[] Center(double[] src, double radius, Side side) {
	double turnFlag = (side == Side.Right ? -1 : 1);
	double oX = (src[0] - turnFlag * radius * Math.sin(src[2]));
	double oY = (src[1] + turnFlag * radius * Math.cos(src[2]));

	return new double[] { oX, oY };
    }

    public static double Los(double[] src, double[] dest) {
	double LOS = Math.abs(Radian(src, dest) - src[2]);
	return LOS < Math.PI ? LOS : (2 * Math.PI - LOS);
    }

    public static double Zem(double[] src, double[] dest) {
	double tGo = TGo(src, dest);
	double[] newSrc = new double[] { src[0] + tGo * Math.cos(src[2]),
		src[1] + tGo * Math.sin(src[2]) };
	double[] newDest = new double[] { dest[0] + tGo * Math.cos(dest[2]),
		dest[1] + tGo * Math.sin(dest[2]) };

	return Distance(newSrc, newDest);
    }

    public static double TGo(double[] src, double[] dest) {
	double velocity = 1;
	double[] vSrcDir = new double[] { Math.cos(src[2]),
		Math.cos(src[2] - (Math.PI / 2)) };
	double[] vDestDir = new double[] { Math.cos(dest[2]),
		Math.cos(dest[2] - (Math.PI / 2)) };

	double dirCosDiffMagnitude = Distance(vSrcDir, vDestDir);
	double distance = Distance(src, dest);

	double[] pVec = new double[] { src[0] - dest[0], src[1] - dest[1] };
	double[] vVec = new double[] { vSrcDir[0] - vDestDir[0],
		vSrcDir[1] - vDestDir[1] };

	double[] o = new double[] { 0, 0 };
	double pVecTheta = Radian(o, pVec);
	double vVecTheta = Radian(o, vVec);
	double includedAngle = Math.abs(pVecTheta - vVecTheta);

	includedAngle = (includedAngle > Math.PI) ? (2 * Math.PI - includedAngle)
		: includedAngle;

	return -(distance * dirCosDiffMagnitude * Math.cos(includedAngle))
		/ (velocity * dirCosDiffMagnitude * dirCosDiffMagnitude);
    }

    public static double CalibrateR(double ZEM, double RDes) {
	return RR * Math.exp(Lambda * ZEM / RDes);
    }

    public static double Theta(double src, double dest) {
	double theta = Math.abs(src - dest);
	return theta <= Math.PI ? theta : (2 * Math.PI - theta);
    }

    /*
     * private static synchronized ArrayList<double[]> ReduceDuplicationHead(
     * ArrayList<double[]> track, double[] head) { track =
     * ReduceDuplication(track); if (track.size() > 0) { if ((int)
     * Math.floor(track.get(0)[0]) == (int) Math.floor(head[0]) && (int)
     * Math.floor(track.get(0)[1]) == (int) Math .floor(head[1])) {
     * track.remove(0); } }
     * 
     * return track; }
     */
    public static synchronized ArrayList<double[]> ReduceDuplication(
	    ArrayList<double[]> track) {
	ArrayList<double[]> result = new ArrayList<double[]>();

	if (track.size() == 0) {
	    return track;
	} else {
	    // result.add(track.get(0));
	    double[] cur = track.get(0).clone();

	    if (track.size() == 1) {
		result.add(cur);
	    } else {
		for (int i = 1; i < track.size(); i++) {
		    double[] tmp = track.get(i);

		    if (i == track.size() - 1) {
			result.add(track.get(i));
		    } else if (((int) Math.floor(tmp[0]) != (int) Math
			    .floor(cur[0]))
			    || ((int) Math.floor(cur[1]) != (int) Math
				    .floor(tmp[1]))) {
			result.add(track.get(i - 1));
			cur = track.get(i).clone();
		    }
		}

	    }
	    return result;
	}
    }

    public static double CollisionT(double[] v1, double[] v2, double distance,
	    double s1, double s2) {
	if (Geo.Zem(v1, v2) <= 0) {
	    return -1;
	} else {
	    double a = v1[0] - v2[0];
	    double b = s1 * Math.cos(v1[2]) - s2 * Math.cos(v2[2]);

	    double c = v1[1] - v2[1];
	    double d = s1 * Math.sin(v1[2]) - s2 * Math.sin(v2[2]);

	    double A = b * b + d * d;
	    double B = 2 * (a * b + c * d);
	    double C = (a * a + c * c - distance * distance);

	    return (-B - Math.sqrt(B * B - 4 * A * C)) / (2 * A);
	}
    }

    public static double[] RIPNAMove(double[] v1, double[] v2, double RDes) {
	double[] tmpSelf = v1;
	double[] tmpOppo = v2;
	double[] result = null;

	double selfLOSTheta = Geo.Los(tmpSelf, tmpOppo);
	double oppoLOSTheta = Geo.Los(tmpOppo, tmpSelf);
	double selfHeading = tmpSelf[2];
	double selfLOS = Geo.Radian(tmpSelf, tmpOppo);
	double targetZEM = Geo.Zem(tmpSelf, tmpOppo);

	if (selfLOSTheta < oppoLOSTheta) {
	    if (selfHeading < selfLOS) {
		result = Geo.Turn(tmpSelf, Geo.CalibrateR(targetZEM, RDes),
			Side.Left);
	    } else {
		result = Geo.Turn(tmpSelf, Geo.CalibrateR(targetZEM, RDes),
			Side.Right);
	    }
	} else {
	    if (selfHeading < selfLOS) {
		result = Geo.Turn(tmpSelf, Geo.CalibrateR(targetZEM, RDes),
			Side.Right);
	    } else {
		result = Geo.Turn(tmpSelf, Geo.CalibrateR(targetZEM, RDes),
			Side.Left);
	    }
	}
	return result;
    }
}