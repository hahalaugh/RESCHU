package reschu.game.model;

public class Target {
    final static public String MISSION_SHORE = "SHORE";
    final static public String MISSION_LAND = "LAND";

    private int[] pos;
    private String mission; // SHORE, LAND
    private String type;
    private boolean done;
    private String name;
    private boolean visible;

    Target() {
    }

    Target(String s, int[] p, String m, String t, boolean v) {
	name = s;
	pos = p;
	mission = m;
	type = t;
	done = false;
	visible = v;
    }

    public void setPos(int[] p) {
	pos = p;
    }

    public void setMission(String m) {
	mission = m;
    }

    public void setType(String t) {
	type = t;
    }

    public void setName(String i) {
	name = i;
    }

    public int[] getPos() {
	return pos;
    }

    public String getMission() {
	return mission;
    }

    public String getType() {
	return type;
    }

    public String getName() {
	return name;
    }

    public void setDone() {
	done = true;
    }

    public boolean isDone() {
	return done;
    }

    public void setVisible(boolean v) {
	visible = v;
    }

    public boolean isVisible() {
	return visible;
    }

    static public boolean isTargetType(String s) {
	if (s.equals(MISSION_LAND) || s.equals(MISSION_SHORE))
	    return true;
	return false;
    }

    @Override
    public String toString() {
	return String.format("name: %s, pos: [%d, %d], type: %s\n", getName(),
		getPos()[0], getPos()[1], getType());
    }

}
