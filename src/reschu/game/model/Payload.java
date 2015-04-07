package reschu.game.model;

import reschu.constants.MyURL;

public class Payload {
    private int idx;
    private String statement;
    private String filename;
    private String vehicleType;
    private String targetType;
    private int[] location;
    private boolean done;
    private int isPreSelected;
    // private int[] preSelectLocation;
    private int taskType;
    private int expectingAction;

    public int getIsPreSelected() {
	return isPreSelected;
    }

    public void setIsPreSelected(int isPreSelected) {
	this.isPreSelected = isPreSelected;
    }

    public int getTaskType() {
	return taskType;
    }

    public void setTaskType(int taskType) {
	this.taskType = taskType;
    }

    public int getExpectingAction() {
	return expectingAction;
    }

    public void setExpectingAction(int result) {
	this.expectingAction = result;
    }

    public Payload(int i, int[] loc, String vType, String tType, String stmt,
	    int isPreS, int al, int ea) {
	idx = i;
	filename = idx + "_" + vType + "_" + tType + ".jpg";
	vehicleType = vType;
	targetType = tType;
	location = loc;
	statement = stmt;
	done = false;
	isPreSelected = isPreS;
	taskType = al;
	expectingAction = ea;
    }

    public String getStatement() {
	return statement;
    }

    public String getFilename() {
	return MyURL.URL_PAYLOAD + filename;
    }

    public String getClearFilename() {
	return MyURL.URL_PAYLOAD + "clear_" + filename;
    }

    public String getVehicleType() {
	return vehicleType;
    }

    public String getTargetType() {
	return targetType;
    }

    public int[] getLocation() {
	return location;
    }

    public boolean isDone() {
	return done;
    }

    public void setDone(boolean b) {
	done = b;
    }
}
