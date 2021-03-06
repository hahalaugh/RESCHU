package reschu.game.model;

import java.util.*;

import reschu.constants.*;
import reschu.game.controller.GUI_Listener;
import reschu.game.controller.Reschu;

public class VehicleList {
    private Game g;
    private LinkedList<Vehicle> v_list = new LinkedList<Vehicle>();

    public VehicleList(Game g) {
	this.g = g;
    }

    /**
     * Checks whether a vehicle list has a vehicle with a given name
     */
    public boolean hasVehicle(String v_name) {
	for (int i = 0; i < v_list.size(); i++)
	    if (v_list.get(i).getName() == v_name)
		return true;
	return false;
    }

    /**
     * Add a vehicle
     * 
     * @param idx
     *            index number
     * @param v_type
     *            vehicle type
     * @param v_name
     *            vehicle name
     * @param v_payload
     *            vehicle's payload
     * @param milliseconds
     *            vehicle's velocity
     * @param rnd
     *            random seed to give the initial position of a vehicle
     * @param m
     *            map
     * @param l
     *            listener
     * @param g
     *            game
     * @throws UserDefinedException
     */
    public void addVehicle(int idx, String v_type, String v_name,
	    String v_payload, int milliseconds, Random rnd, Map m,
	    GUI_Listener l, Game g) throws UserDefinedException {
	if (this.hasVehicle(v_name))
	    throw new UserDefinedException(v_name + " already exists.");

	int x = 0;
	int y = 0;

	if (Reschu.extraTutorial()) {
	    if (idx == 1) {
		x = 258;
		y = 172;
	    }

	    if (idx == 2) {
		x = 200;
		y = 50;
	    }
	} else {
	    x = rnd.nextInt(MySize.width);
	    y = rnd.nextInt(MySize.height);
	}

	//Adjust the initial position of vehicle 1 in tutorial mode. The reaction time was too little for
	//participants to perform required operations.
	if(Reschu.tutorial() && idx == 1)
	{
	    y += 40;
	    x -= 30;
	}
	
	if (v_type == Vehicle.TYPE_UUV) {
	    UUV v_uuv = new UUV(m, g);
	    while (m.getCellType(x, y) == MyGame.LAND) {
		x = rnd.nextInt(MySize.width);
		y = rnd.nextInt(MySize.height);
	    }
	    v_uuv.setIndex(idx);
	    v_uuv.setName(v_name);
	    v_uuv.setType(v_type);
	    v_uuv.setPayload(v_payload);
	    v_uuv.setPos(x, y);
	    v_uuv.setVelocity(milliseconds);
	    v_uuv.setGuiListener(l);
	    v_list.addLast(v_uuv);
	} else if (v_type == Vehicle.TYPE_UAV) {
	    UAV v_uav = new UAV(m, g);
	    v_uav.setIndex(idx);
	    v_uav.setName(v_name);
	    v_uav.setType(v_type);
	    v_uav.setPayload(v_payload);
	    v_uav.setPos(x, y);
	    v_uav.setVelocity(milliseconds);
	    v_uav.setGuiListener(l);
	    v_list.addLast(v_uav);
	}
    }

    /**
     * Returns the size of the vehicle list
     */
    public int size() {
	return v_list.size();
    }

    public Vehicle getVehicle(int i) {
	return v_list.get(i);
    }

    public Vehicle getVehicleByIndex(int index) {
	for (Vehicle v : v_list) {
	    if (v.getIndex() == index) {
		return v;
	    }
	}
	return null;
    }

    /**
     * Get a vehicle with a given name
     * 
     * @param v_name
     *            Vehicle name
     * @throws UserDefinedException
     */
    public Vehicle getVehicle(String v_name) throws UserDefinedException {
	if (!hasVehicle(v_name))
	    throw new UserDefinedException("No such vehicle(" + v_name
		    + ") in Vehicle List.");

	for (int i = 0; i < v_list.size(); i++)
	    if (v_list.get(i).getName() == v_name)
		return v_list.get(i);

	return new Vehicle(new Map(), g); // Never reaches.
    }

    /**
     * Get a vehicle at a given position.
     * 
     * @param x
     *            x-coordinate
     * @param y
     *            y-coordinate
     */
    public Vehicle getVehicle(int x, int y) {
	for (int i = 0; i < v_list.size(); i++) {
	    if (v_list.get(i).getX() == x && v_list.get(i).getY() == y)
		return v_list.get(i);
	}
	return new Vehicle(new Map(), g); // Never reaches.
    }

    /**
     * Returns total damage of all vehicles
     */
    public int getTotalDamage() {
	int total_damage = 0;
	for (int i = 0; i < size(); i++) {
	    total_damage += getVehicle(i).getDamage();
	}
	return total_damage;
    }
}