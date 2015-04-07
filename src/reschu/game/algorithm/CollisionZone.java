package reschu.game.algorithm;

import java.awt.geom.Rectangle2D;

public class CollisionZone {
    public CollisionZone(Rectangle2D collisionArea, int[] involvedVehicles) {
	super();
	this.collisionArea = collisionArea;
	this.involvedVehicles = involvedVehicles;
    }

    // Bounding box of collision area;
    // public Integer[][] collisionArea;
    public Rectangle2D collisionArea;

    // involved vehicle id;
    public int[] involvedVehicles;

    @Override
    public String toString() {
	Rectangle2D r = this.collisionArea.getBounds2D();
	return String.format(
		"Area: [%d, %d, %d, %d] vehicles: %s\n",
		(int) r.getX(), (int) r.getY(),
		(int) r.getX() + (int) r.getWidth(),
		(int) r.getY() + (int) r.getHeight(),
		involvedVehiclesToString());
    }

    private String involvedVehiclesToString() {
	String result = "[";
	for (int i : involvedVehicles) {
	    result += (i + " ");
	}

	result += "]";

	return result;
    }
}
