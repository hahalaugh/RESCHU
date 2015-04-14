package reschu.game.algorithm;

import java.awt.geom.Rectangle2D;

public class CollisionZone {
    
    private static int indexCurosr = 0;
    
    public CollisionZone(Rectangle2D collisionArea, Integer[] involvedVehicles,
	    boolean isFaked) {
	super();
	this.collisionArea = collisionArea;
	this.involvedVehicles = involvedVehicles;
	this.isFaked = isFaked;
    }

    //
    // Bounding box of collision area;
    // public Integer[][] collisionArea;
    public Rectangle2D collisionArea;

    // involved vehicle id;
    public Integer[] involvedVehicles;

    // is faked collision area?
    public boolean isFaked;

    @Override
    public String toString() {
	Rectangle2D r = this.collisionArea.getBounds2D();
	return String.format("Area: [%d, %d, %d, %d] vehicles: %s\n faked: %b",
		(int) r.getX(), (int) r.getY(),
		(int) r.getX() + (int) r.getWidth(),
		(int) r.getY() + (int) r.getHeight(),
		involvedVehiclesToString(), this.isFaked);
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
