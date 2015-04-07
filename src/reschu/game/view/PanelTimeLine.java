package reschu.game.view;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.border.*;

import reschu.constants.*;
import reschu.game.controller.GUI_Listener;
import reschu.game.model.Game;
import reschu.game.model.Vehicle;
import reschu.game.model.VehicleList;
import info.clearthought.layout.TableLayout;

public class PanelTimeLine extends JPanel implements MouseListener {
    private static final long serialVersionUID = 7243933752530455674L;

    private final int TitleHeight = 10;
    private final int TimeBarHeight = 24;
    private final int Gap = 3;
    private final int ButtomGap = 6;

    private VehicleIcon[] pnlVehicleIcon;
    private VehicleTime[] pnlVehicleTime;
    private TimeText pnlTimeText;
    // private RemainingTime pnlRemainingTime;
    private TitledBorder bdrTitle;
    // private int number_of_row, row_height;
    private int current_time;
    private VehicleList vehicleList;
    private double[] sizeH;
    private Integer[] addSequence;
    private GUI_Listener lsnr;

    public PanelTimeLine(GUI_Listener gl, Game g, VehicleList vl) {
	this.sizeH = new double[4 + vl.size() * 2];
	for (int i = 0; i < sizeH.length; i++) {
	    if (i == 0) {
		sizeH[i++] = TitleHeight;
		sizeH[i] = Gap;
		continue;
	    }

	    sizeH[i++] = TimeBarHeight;
	    sizeH[i] = Gap;

	}
	sizeH[sizeH.length - 1] = ButtomGap;

	// double size[][] = { { 30, 5, TableLayout.FILL, 5 }, { 10, 3, 24, 3,
	// 24, 3, 24, 3, 24, 3, 24, 3, 24, 6} };
	double size[][] = { { 30, 5, TableLayout.FILL, 5 }, sizeH };
	bdrTitle = BorderFactory.createTitledBorder("Time Line");
	setBorder(bdrTitle);
	setLayout(new TableLayout(size));

	pnlVehicleIcon = new VehicleIcon[vl.size()];
	pnlVehicleTime = new VehicleTime[vl.size()];
	pnlTimeText = new TimeText();
	// pnlRemainingTime = new RemainingTime(g);

	// number_of_row = vl.size() + 2;
	// row_height = this.getHeight() / number_of_row;

	add(pnlTimeText, "2,0");

	for (int i = 0; i < vl.size(); i++) {
	    pnlVehicleIcon[i] = new VehicleIcon(vl.getVehicle(i));
	    pnlVehicleTime[i] = new VehicleTime(vl.getVehicle(i));
	    add(pnlVehicleIcon[i], "0," + 2 * (i + 1));
	    add(pnlVehicleTime[i], "2," + 2 * (i + 1));
	}

	vehicleList = vl;
	lsnr = gl;
	this.addMouseListener(this);
    }

    private int getSelectedVehicle(int yPos) {
	double total = 0;
	for (double i : this.sizeH) {
	    total += i;
	}
	double buttom = total - ButtomGap;
	double top = TitleHeight + Gap;

	if (yPos <= top || yPos >= buttom) {
	    return -1;
	} else {
	    return (int) ((yPos - top) / (TimeBarHeight + Gap));
	}
    }

    /*
     * public synchronized int[] getAddSequence(double[] src) { int resultIndex
     * = 0; int[] result = new int[src.length]; double[] originalArray =
     * src.clone(); Arrays.sort(src); HashSet<Double> s = new HashSet<Double>();
     * for (int i = 0; i < src.length; i++) { s.add(src[i]); }
     * 
     * ArrayList<Double> list = new ArrayList<Double>(); for (Double intValue :
     * s) { list.add(intValue); }
     * 
     * Collections.sort(list);
     * 
     * for (int i = 0; i < list.size(); i++) { for (int j = 0; j <
     * originalArray.length; j++) { if (originalArray[j] == list.get(i)) {
     * result[resultIndex++] = j; } } }
     * 
     * return result; }
     */

    public synchronized Integer[] getAddSequence(
	    HashMap<Integer, Integer> timeTable) {
	ValueComparator bvc = new ValueComparator(timeTable);
	TreeMap<Integer, Integer> sorted_map = new TreeMap<Integer, Integer>(
		bvc);
	sorted_map.putAll(timeTable);

	LinkedList<Integer> seqList = new LinkedList<Integer>();
	for (Integer i : sorted_map.keySet()) {
	    Vehicle v = vehicleList.getVehicle(i);
	    if (v.getStatus() == MyGame.STATUS_VEHICLE_PENDING) {
		seqList.add(i);
	    }
	}

	for (Integer i : sorted_map.keySet()) {
	    Vehicle v = vehicleList.getVehicle(i);
	    if (v.getStatus() == MyGame.STATUS_VEHICLE_MOVING) {
		seqList.add(i);
	    }
	}

	for (Integer i : sorted_map.keySet()) {
	    Vehicle v = vehicleList.getVehicle(i);
	    if (v.getStatus() == MyGame.STATUS_VEHICLE_STASIS
		    || v.getStatus() == MyGame.STATUS_VEHICLE_PAYLOAD) {
		seqList.add(i);
	    }
	}

	return seqList.toArray(new Integer[seqList.size()]);
    }

    class ValueComparator implements Comparator<Integer> {

	Map<Integer, Integer> base;

	public ValueComparator(HashMap<Integer, Integer> timeTable) {
	    this.base = timeTable;
	}

	public int compare(Integer o1, Integer o2) {
	    if (base.get(o1) >= base.get(o2)) {
		return 1;
	    } else {
		return -1;
	    }
	}
    }

    public synchronized void refresh(int milliseconds) {

	repaint();
	current_time = milliseconds;
	pnlTimeText.refresh(current_time);
	// pnlRemainingTime.refresh(current_time);

	// int[] seq = new int[pnlVehicleTime.length];
	// double[] seq = new double[vehicleList.size()];
	HashMap<Integer, Integer> seq = new HashMap<Integer, Integer>();

	// Getting first activity timestamp for every vehicle
	for (int i = 0; i < vehicleList.size(); i++) {
	    Vehicle v = vehicleList.getVehicle(i);
	    if (v.track.size() == 0) {
		seq.put(i, 0);
	    } else {
		for (int j = 0; j < v.track.size(); j++) {
		    // 3 is the index of way point flag
		    if (v.track.get(j)[3] == 1) {
			int timeToArrive = (int) (j * (v.getVelocity() / (double) 1000));
			seq.put(i, timeToArrive);
			break;
		    }
		}
	    }
	}

	addSequence = getAddSequence(seq);
	// DataRecorder.TimeLineRecord(addSequence);

	for (int i = 0; i < pnlVehicleTime.length; i++) {
	    // System.out.println("Remove");
	    remove(pnlVehicleIcon[i]);
	    remove(pnlVehicleTime[i]);
	    validate();
	}

	for (int i = 0; i < addSequence.length; i++) {
	    // System.out.println("Update");
	    pnlVehicleIcon[addSequence[i]].chkEngageEnabled();
	    pnlVehicleTime[addSequence[i]].refresh(current_time);
	    add(pnlVehicleIcon[addSequence[i]], "0," + 2 * (i + 1));
	    add(pnlVehicleTime[addSequence[i]], "2," + 2 * (i + 1));
	    validate();
	}

    }

    public void mouseClicked(MouseEvent e) {
	int yPos = e.getY();

	int selectedVehicleBarLevel = getSelectedVehicle(yPos);
	if (selectedVehicleBarLevel > 0) {
	    lsnr.Vehicle_Selected_From_pnlTimeLine(this.addSequence[selectedVehicleBarLevel - 1]);
	}

    }

    public void mouseEntered(MouseEvent e) {
	Cursor normalCursor = new Cursor(Cursor.HAND_CURSOR);
	setCursor(normalCursor);
    }

    public void mouseExited(MouseEvent e) {
	Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	setCursor(normalCursor);
    }

    public void mousePressed(MouseEvent e) {
	// TODO Auto-generated method stub

    }

    public void mouseReleased(MouseEvent e) {
	// TODO Auto-generated method stub

    }
}

class TimeText extends JPanel {
    private static final long serialVersionUID = 6191134278259404128L;
    private int p1, p2, p3, p4, p5;

    public TimeText() {
    }

    public void paint(Graphics g) {
	/*
	 * for( int i=1; i<=30; i++ ) if( (current_time+i) % 30 == 0 ) { t1 =
	 * current_time+i; t2 = t1 + 30; t3 = t1 + 60; t4 = t1 + 90; t5 = t1 +
	 * 120; break; }
	 */
	p1 = getWidth() / 120;
	p2 = 30 * getWidth() / 120;
	p3 = 60 * getWidth() / 120;
	p4 = 90 * getWidth() / 120;
	p5 = 119 * getWidth() / 120;

	g.setColor(new Color(238, 238, 238));
	g.fillRect(0, 0, getWidth(), getHeight());
	g.setColor(Color.BLACK);
	g.setFont(MyFont.fontSmallBold);
	/*
	 * g.drawString(setTimeFormat(t1), p1-10, 10);
	 * g.drawString(setTimeFormat(t2), p2-10, 10);
	 * g.drawString(setTimeFormat(t3), p3-10, 10);
	 * g.drawString(setTimeFormat(t4), p4-10, 10);
	 * g.drawString(setTimeFormat(t5), p5-10, 10);
	 */
	g.drawString("T", p1, 10);
	g.drawString("T+30", p2 - 10, 10);
	g.drawString("T+60", p3 - 10, 10);
	g.drawString("T+90", p4 - 10, 10);
	g.drawString("T+120", p5 - 30, 10);
    }

    public void refresh(int milliseconds) {
	/*
	 * current_time = milliseconds / 1000;
	 * 
	 * for( int i=0; i<=30; i++ ) if( (current_time+i) % 30 == 0 ) { p1 = i
	 * *getWidth() / 120; p2 = (i+30) *getWidth() / 120; p3 = (i+60)
	 * *getWidth() / 120; p4 = (i+90) *getWidth() / 120; p5 =
	 * (i+119)*getWidth() / 120; break; }
	 */
	repaint();
    }

    public String setTimeFormat(int time) {
	String time_min = "" + time / 60;
	String time_sec = "" + time % 60;

	if (time_min.length() == 1)
	    time_min = "0" + time_min;
	if (time_sec.length() == 1)
	    time_sec = "0" + time_sec;

	return time_min + ":" + time_sec;
    }
}

class VehicleIcon extends JPanel {
    private static final long serialVersionUID = -5961320475456746793L;
    private Vehicle v;
    private Color vColor;
    private boolean colorFlag;

    public VehicleIcon(Vehicle v) {
	this.v = v;
	vColor = MyColor.COLOR_VEHICLE;
    }

    public void paint(Graphics g) {
	if (v.getType().equals(Vehicle.TYPE_UAV)) {
	    g.setColor(Color.BLACK);
	    g.drawArc(5, 0, MySize.SIZE_VEHICLE_WIDTH_TMS_PXL,
		    MySize.SIZE_VEHICLE_HEIGHT_TMS_PXL, 0, 180);
	    g.drawLine(5, MySize.SIZE_VEHICLE_HEIGHT_TMS_PXL / 2,
		    MySize.SIZE_VEHICLE_WIDTH_TMS_PXL + 5,
		    MySize.SIZE_VEHICLE_HEIGHT_TMS_PXL / 2);
	    g.setColor(vColor);
	    g.fillArc(5, 0, MySize.SIZE_VEHICLE_WIDTH_TMS_PXL,
		    MySize.SIZE_VEHICLE_HEIGHT_TMS_PXL, 0, 180);
	    paintString((Graphics2D) g,
		    MySize.SIZE_VEHICLE_WIDTH_TMS_PXL / 2 + 3,
		    MySize.SIZE_VEHICLE_HEIGHT_TMS_PXL / 2 - 5, Color.black,
		    MyFont.fontSmallBold, Integer.toString(v.getIndex()));
	} else if (v.getType().equals(Vehicle.TYPE_UUV)) {
	    g.setColor(Color.BLACK);
	    g.drawArc(5, -MySize.SIZE_VEHICLE_HEIGHT_TMS_PXL / 2,
		    MySize.SIZE_VEHICLE_WIDTH_TMS_PXL,
		    MySize.SIZE_VEHICLE_HEIGHT_TMS_PXL, 180, 180);
	    g.drawLine(5, 0, MySize.SIZE_VEHICLE_WIDTH_TMS_PXL + 5, 0);
	    g.setColor(vColor);
	    g.fillArc(5, -MySize.SIZE_VEHICLE_HEIGHT_TMS_PXL / 2,
		    MySize.SIZE_VEHICLE_WIDTH_TMS_PXL,
		    MySize.SIZE_VEHICLE_HEIGHT_TMS_PXL, 180, 180);
	    paintString((Graphics2D) g,
		    MySize.SIZE_VEHICLE_WIDTH_TMS_PXL / 2 + 3, 14, Color.black,
		    MyFont.fontSmallBold, Integer.toString(v.getIndex()));
	}
    }

    public void paintString(Graphics2D g, int x, int y, Color color, Font font,
	    String str) {
	g.setColor(color);
	g.setFont(font);
	g.drawString(str, x, y);
    }

    public void chkEngageEnabled() {
	if (v.getStatus() == MyGame.STATUS_VEHICLE_PENDING) {
	    colorFlag = !colorFlag;
	    if (colorFlag)
		vColor = MyColor.COLOR_VEHICLE;
	    else
		vColor = MyColor.COLOR_VEHICLE_PENDING;
	} else {
	    vColor = MyColor.COLOR_VEHICLE;
	}
    }
}

class VehicleTime extends JPanel {
    private static final long serialVersionUID = 4435452374430336399L;
    private Vehicle v;
    private int p1, p2, p3, p4, p5;
    protected LinkedList<int[]> pathList = new LinkedList<int[]>(); // TUPLE<DISTANCE/VELOCITY,

    // PATH_TYPE>
    // PATH_TYPE={WP:0,
    // GP:1,
    // GP(COMM):2}

    public VehicleTime(Vehicle v) {
	this.v = v;
    }

    public Vehicle GetVehicle() {
	return v;
    }

    public void paint(Graphics g) {
	g.clearRect(0, 0, getWidth(), getHeight());
	// g.setColor(MyColor.COLOR_VEHICLE_TIMELINE);
	g.setColor(MyColor.COLOR_VEHICLE);
	g.fillRect(0, 0, getWidth(), getHeight());

	// Vertical Line
	p1 = getWidth() / 120;
	p2 = 30 * getWidth() / 120;
	p3 = 60 * getWidth() / 120;
	p4 = 90 * getWidth() / 120;
	p5 = 119 * getWidth() / 120;

	g.setColor(new Color(100, 100, 100));
	g.drawLine(0, 0, getWidth() - 1, 0);
	g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
	g.drawLine(p1, 0, p1, getHeight());
	g.drawLine(p2, 0, p2, getHeight());
	g.drawLine(p3, 0, p3, getHeight());
	g.drawLine(p4, 0, p4, getHeight());
	g.drawLine(p5, 0, p5, getHeight());

	for (int i = 0; i < pathList.size(); i++)
	    if (pathList.get(i)[0] < 120) {
		if (pathList.get(i)[1] == 0)
		    g.setColor(new Color(0, 0, 255, 200));
		else if (pathList.get(i)[1] == 1)
		    g.setColor(MyColor.COLOR_TARGET_OCCUPIED);
		else
		    g.setColor(MyColor.COLOR_TARGET_COMM_OCCUPIED);
		if (v.getTarget() != null && pathList.get(i)[1] != 0) {
		    g.fillRect(pathList.get(i)[0] * getWidth() / 120, 0, 11,
			    getHeight());
		    g.setColor(Color.white);
		    g.setFont(MyFont.fontSmallBold);
		    g.drawString(v.getTarget().getName(), pathList.get(i)[0]
			    * getWidth() / 120 + 2, getHeight() / 2 + 4);
		} else
		    g.fillRect(pathList.get(i)[0] * getWidth() / 120, 0, 3,
			    getHeight());
	    }
    }

    public synchronized void setPathList() {
	pathList.clear();

	ArrayList<double[]> track = v.track;
	for (int i = 0; i < track.size(); i++) {
	    // Is Way Point
	    if (track.get(i)[3] == 1) {
		pathList.add(new int[] {
			(int) (i * (v.getVelocity() / 1000.0)), 0 });
	    }
	}

	if (pathList.size() > 0) {
	    pathList.getLast()[1] = 1;
	}

    }

    public void refresh(int milliseconds) {
	setPathList();
	repaint();
    }
}
