package reschu.game.model;

import java.util.ArrayList;
import java.util.Random;

import reschu.game.controller.Reschu;

public class PayloadList {
    private ArrayList<Payload> payload_list = new ArrayList<Payload>();
    private Random rnd;
    private int index = 0;
    private boolean isInOrder;

    public PayloadList(boolean isInOrder) {
	rnd = new Random(System.currentTimeMillis());
	this.isInOrder = isInOrder;
    }

    public int size() {
	return payload_list.size();
    }

    public void addPayload(int idx, String vType, String tType, int[] loc,
	    String stmt, int isPreS, int al, int isTE) {
	payload_list.add(new Payload(idx, loc, vType, tType, stmt, isPreS, al,
		isTE));
    }

    public Payload getPayload(String vType, String tType) {
	Payload p = null;

	if (Reschu.train() || Reschu.tutorial()) {
	    for (int i = 0; i < payload_list.size(); i++) {
		p = payload_list.get(i);
		if (p.getVehicleType().equals(vType)
			&& p.getTargetType().equals(tType))
		    break;
	    }
	}

	else {
	    do {
		// Juntao: Do not randomly get payload. Sequential instead
		if (payload_list.size() != 0) {
		    if (isInOrder) {
			// Order
			p = payload_list.get(index++ % payload_list.size());
		    } else {
			// Reverse order
			p = payload_list.get((payload_list.size() - 1)
				- (index++ % payload_list.size()));
		    }

		} else {
		    p = payload_list.get(rnd.nextInt(payload_list.size() - 1));
		}

		// Juntao: Do not return null payload. Ignore the cnt logic
		// here.
		// if (++cnt >= payload_list.size())
		// return null;
	    } while (!p.getVehicleType().equals(vType)
		    || !p.getTargetType().equals(tType) || p.isDone());

	    // Juntao: Do not set any payload to done. Keep looping.
	    // p.setDone(true);
	}
	return p;
    }
}
