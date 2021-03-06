package reschu.constants;

public class MyDB {
    final public static int INVOKER_SYSTEM = 0;
    final public static int INVOKER_USER = 1;

    final public static int WP_ADD_START = 11;
    final public static int WP_ADD_END = 12;
    final public static int WP_MOVE_START = 13;
    final public static int WP_MOVE_END = 14;
    final public static int WP_DELETE_START = 15;
    final public static int WP_DELETE_END = 16;
    final public static int WP_ADD_CANCEL = 17;

    final public static int GP_SET_BY_SYSTEM = 21;
    final public static int GP_SET_START = 22;
    final public static int GP_SET_END_ASSIGNED = 23;
    final public static int GP_SET_END_UNASSIGNED = 24;
    final public static int GP_CHANGE_START = 25;
    final public static int GP_CHANGE_END_ASSIGNED = 26;
    final public static int GP_CHANGE_END_UNASSIGNED = 27;
    final public static int GP_SET_CANCEL = 28;

    final public static int TARGET_GENERATED = 31;
    final public static int TARGET_BECAME_VISIBLE = 32;
    final public static int TARGET_DISAPPEARED = 33;

    final public static int PAYLOAD_ENGAGED_AND_FINISHED = 41;
    final public static int PAYLOAD_ENGAGED = 42;
    final public static int PAYLOAD_FINISHED_CORRECT = 43;
    final public static int PAYLOAD_FINISHED_INCORRECT = 44;
    final public static int PAYLOAD_CHECK = 45;
    final public static int PAYLOAD_HIT = 46;
    final public static int PAYLOAD_SAFE = 47;

    final public static int VEHICLE_DAMAGED = 51;
    final public static int VEHICLE_SPEED_DECREASED = 52;
    final public static int VEHICLE_ARRIVES_TO_TARGET = 53;
    final public static int VEHICLE_INTERSECT_HAZARDAREA = 54;
    final public static int VEHICLE_ESCAPE_HAZARDAREA = 55;

    final public static int HAZARDAREA_GENERATED = 61;
    final public static int HAZARDAREA_DISAPPEARED = 62;

    final public static int SYSTEM_GAME_START = 91;
    final public static int SYSTEM_GAME_END = 92;

    final public static int YVES_VEHICLE_SELECT_TAB = 101;
    final public static int YVES_VEHICLE_DESELECT_TAB = 102;
    final public static int YVES_VEHICLE_SELECT_MAP_LBTN = 103;
    final public static int YVES_VEHICLE_SELECT_MAP_RBTN = 104;

    final public static int AUTOMATION_APPLICATION_FROM_MAP = 200;
    final public static int AUTOMATION_APPLICATION_FROM_CONTROL_PANEL = 201;
    final public static int AUTOMATION_APPLICATION_FAILED = 202;
    final public static int AUTOMATION_APPLICATION_CANCELLED = 203;

    final public static int PROCEDURE_SWITCHED = 210;

    final public static int COLLISION_ZONE_INCREASED = 220;
    final public static int COLLISION_ZONE_DECREASED = 221;

    final public static int STATE_NOT_LOGGED_IN_YET = 0;
    final public static int STATE_WEB_LOG_IN = 1;
    final public static int STATE_GAME_START = 2;
    final public static int STATE_GAME_FINISH = 3;
}
