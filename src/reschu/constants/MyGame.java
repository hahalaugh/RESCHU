package reschu.constants;

import reschu.app.ScenarioConfig;

public class MyGame {
    final static public String VERSION_INFO = "RESCHU VER 1.0.0";

    final static public int LAND = 0;
    final static public int SEASHORE = 1;
    final static public int SEA = 2;

    final static public int STATUS_VEHICLE_STASIS = 0;
    final static public int STATUS_VEHICLE_MOVING = 1;
    final static public int STATUS_VEHICLE_PENDING = 2;
    final static public int STATUS_VEHICLE_PAYLOAD = 3;

    // final static public int nHAZARD_AREA = 14;
    final static public int nHAZARD_AREA = ScenarioConfig.GetInstance()
	    .get_hazardNumber();
    final static public int nHAZARD_AREA_TUTORIAL = 3;

    final static public int nTARGET_AREA_LAND = 4;
    final static public int nTARGET_AREA_LAND_TUTORIAL = 7;
    final static public int nTARGET_AREA_SHORE = 3;
    final static public int nTARGET_AREA_COMM = 0;
    final static public int nTARGET_AREA_TOTAL = nTARGET_AREA_LAND
	    + nTARGET_AREA_SHORE + nTARGET_AREA_COMM;
    final static public int nTARGET_AREA_TOTAL_TUTORIAL = nTARGET_AREA_LAND_TUTORIAL
	    + nTARGET_AREA_SHORE + nTARGET_AREA_COMM;

    final static public int TASK_HINT = 0;
    final static public int TASK_MAYBE = 1;
    final static public int TASK_NO_HINT = 2;

    final static public int EXPECTING_SAFE = 0;
    final static public int EXPECTING_HIT = 1;
    final static public int EXPECTING_RESIGN = 2;

    final static public int SAFE_OPTION = 0;
    final static public int HIT_OPTION = 1;

    final static public int WORKLOAD_LOW = 0;
    final static public int WORKLOAD_HIGH = 1;

    final static public int AUTO_LOW = 0;
    final static public int AUTO_HIGH = 1;

    final static public double AUTO_RELIABILITY = 0.8;
    final static public double RDES_CALIBRATION = 5;
}
