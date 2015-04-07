package reschu.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import reschu.constants.MyGame;
import reschu.game.model.Vehicle;
import reschu.game.model.VehicleList;

public class DataRecorder {
    private final static String Path = (new Date()).getTime() + "_data.txt";

    public enum SystemAction {
	Highlight, PopUpQuestion, GainScore, LoseScore
    }

    public enum UserAction {
	Start, End, SelectVehicle, AddWaypoint, DeleteWaypoint, ModifyWaypoint, ChangeDestination, EngageTarget, SubmitQuestion,
    }

    public enum VehicleAction {
	Conflict, HitHazrd, ArriveTarget
    }

    public enum VehicleSelectionSource {
	Map, Timeline, StatusPanel
    }

    private enum ScoreOpAdd {
	QuestionCorrect, Hit, CR,
    }

    private enum ScoreOpLose {
	Conflict, HitHazard, QuestionWrong, Miss, FA
    }

    private static String currentTime() {
	Date date = new Date();
	return (new Timestamp(date.getTime())).toString();
    }

    public static void Start() {
	String s = "Experiment Start";
	Write(s);
    }

    public static void Stop() {
	String s = "Experiment Stop";
	Write(s);
    }

    public static void Select(int id, VehicleSelectionSource from) {
	String s = String
		.format("Vehicle %d selected from %s", id, from.name());
	Write(s);
    }

    public static void AddWaypoint(int id, int[] wp) {
	String s = String.format("Vehicle %d adds waypoint at %d,%d", id,
		wp[0], wp[1]);
	Write(s);
    }

    public static void ModifyWaypoint(int id, int[] preWp, int[] postWp) {
	String s = String.format(
		"Vehicle %d changes waypoint from %d,%d --> %d, %d", id,
		preWp[0], preWp[1], postWp[0], postWp[1]);
	Write(s);
    }

    public static void DeleteWaypoint(int id, int[] wp) {
	String s = String.format("Vehicle %d deletes waypoint at %d,%d", id,
		wp[0], wp[1]);
	Write(s);
    }

    public static void AddGoal(int id, int[] goal) {
	String s = String.format("Vehicle %d adds goal at %d,%d", id, goal[0],
		goal[1]);
	Write(s);
    }

    public static void ChangeGoal(int id, int[] pre, int[] post) {
	String s = String.format(
		"Vehicle %d changes goal from %d,%d --> %d, %d", id, pre[0],
		pre[1], post[0], post[1]);
	Write(s);
    }

    public static void Arrives(int id, int[] co) {
	String s = String.format("Vehicle %d arrives at %d,%d and stop", id,
		co[0], co[1]);
	Write(s);
    }

    public static void ArrivesTarget(int id, int targetId, int[] target) {
	String s = String.format("Vehicle %d arrives target %d at %d,%d", id,
		targetId, target[0], target[1]);
	Write(s);
    }

    public static void EngageTarget(int id, String targetName, int[] target,
	    VehicleSelectionSource from) {
	String s = String.format(
		"Vehicle %d engages target %d at %d,%d from %s", id,
		targetName, target[0], target[1], from);
	Write(s);
    }

    public static void Conflict(int id, int[] pos, ArrayList<int[]> vList) {
	String conflictList = "";
	for (int[] i : vList) {
	    conflictList += String.format("[id:%d, %d,%d]", i[0], i[1], i[2]);
	}

	String s = String.format("Vehicle %d at %d,%d conflicts with %s", id,
		pos[0], pos[1], conflictList);
	Write(s);
    }

    public static void HitHazard(int id, int[] pos, int[] hazardPos) {
	String s = String.format("Vehicle %d at %d,%d is in hazard %d,%d", id,
		pos[0], pos[1], hazardPos[0], hazardPos[1]);
	Write(s);
    }

    public static void HighLightCollision(ArrayList<Integer> vList, int[] lu,
	    int[] rd) {
	String highlightList = "";
	for (int i : vList) {
	    highlightList += String.format("[%d]", i);
	}

	String s = String
		.format("Confliction highlight at %d,%d -> %d,%d shown for vehicles %s",
			lu[0], lu[1], rd[0], rd[1], highlightList);
	Write(s);
    }

    public static void PopUpQuestion(int id) {
	String s = String.format("Question %d pops up.", id);
	Write(s);
    }

    public static void SubmitQuestion(int qId, int aId) {

	String s = String.format("Answer %d for question %d submitted.", aId,
		qId);
	Write(s);
    }

    public static void TimeLineRecord(int[] timeline) {
	String s = "Timeline:";
	for (int tl : timeline) {
	    s += tl;
	    s += ",";
	}
	Write(s);
    }

    public static void AddScore(int added, ScoreOpAdd op) {
	String s = String.format("Score added to %d because of %s", added,
		op.name());
	Write(s);
    }

    public static void LoseScore(int lost, ScoreOpLose op) {
	String s = String.format("Score lost to %d because of %s", lost,
		op.name());
	Write(s);
    }

    public static void VehicleStatusRecord(VehicleList vList) {
	String s = "Vehicle Status:";
	for (int i = 0; i < vList.size(); i++) {
	    Vehicle v = vList.getVehicle(i);
	    String status = "";
	    switch (v.getStatus()) {
	    case MyGame.STATUS_VEHICLE_STASIS:
		status += "Idle";
		break;

	    case MyGame.STATUS_VEHICLE_MOVING:
		status += "Moving";
		break;

	    case MyGame.STATUS_VEHICLE_PENDING:
		status += "Pending";
		break;

	    case MyGame.STATUS_VEHICLE_PAYLOAD:
		status += "Payload";
		break;

	    default:
		status += "Unknown";
		break;
	    }
	    s += String.format(" Vehicle: %d (%d, %d) %s", v.getIndex(),
		    v.getX(), v.getY(), status);
	}
	Write(s);
    }

    public static synchronized void Write(String s) {
	PrintWriter out;
	try {
	    out = new PrintWriter(
		    new BufferedWriter(new FileWriter(Path, true)));
	    out.println(currentTime() + "    " + s);
	    out.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
}
