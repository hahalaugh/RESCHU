package reschu.game.view;

import info.clearthought.layout.TableLayout;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

import reschu.constants.*;
import reschu.game.algorithm.CollisionZone;
import reschu.game.controller.GUI_Listener;
import reschu.game.model.Game;
import reschu.game.model.Payload;
import reschu.game.model.Vehicle;

public class PanelControl extends JPanel implements ChangeListener,
	ActionListener {
    private static final long serialVersionUID = 6768850931538883107L;

    private JTabbedPane tabbedPane;
    private VehiclePanel[] pnlVehicle;
    private VehicleGeneralPanel pnlGeneral;
    private GUI_Listener lsnr;
    private JScrollPane scrollPane;
    private Game game;
    private Timer tmr;
    private boolean[] colorFlag;
    private boolean eventFromMap;

    public synchronized void StopTimer() {
	tmr.stop();
    }

    public PanelControl(GUI_Listener l, Game g, String strTitle) {
	Vehicle v;

	game = g;
	lsnr = l;
	colorFlag = new boolean[game.getVehicleList().size()];
	tmr = new Timer(MySpeed.SPEED_CLOCK, this);
	tmr.start();
	eventFromMap = false;
	tabbedPane = new JTabbedPane();
	tabbedPane.addChangeListener(this);
	pnlVehicle = new VehiclePanel[game.getVehicleList().size()];
	pnlGeneral = new VehicleGeneralPanel(lsnr, game);
	pnlGeneral.setPreferredSize(new Dimension(getWidth(), 200));
	scrollPane = new JScrollPane(pnlGeneral);
	// scrollPane.remove(scrollPane.getHorizontalScrollBar());

	tabbedPane.addTab("ALL", scrollPane);
	tabbedPane.setMnemonicAt(0, KeyEvent.VK_A);

	for (int i = 0; i < game.getVehicleList().size(); i++) {
	    v = game.getVehicleList().getVehicle(i);
	    pnlVehicle[i] = new VehiclePanel(lsnr, v);
	    tabbedPane.addTab(v.getIndex() + "(" + v.getType() + ")",
		    pnlVehicle[i]);
	    tabbedPane.setMnemonicAt(i + 1, getVK(v));
	}

	setLayout(new GridLayout(1, 1));
	add(tabbedPane);
    }

    public void UpdateVehicleCollisionInfo(ArrayList<CollisionZone> cz) {
	this.pnlGeneral.setCollisionInfo(cz);
    }

    public void setEnabled(boolean enabled) {
	tabbedPane.setEnabled(enabled);
    }

    private int getVK(Vehicle v) {
	switch (v.getIndex()) {
	case 0:
	    return KeyEvent.VK_0;
	case 1:
	    return KeyEvent.VK_1;
	case 2:
	    return KeyEvent.VK_2;
	case 3:
	    return KeyEvent.VK_3;
	case 4:
	    return KeyEvent.VK_4;
	case 5:
	    return KeyEvent.VK_5;
	case 6:
	    return KeyEvent.VK_6;
	case 7:
	    return KeyEvent.VK_7;
	case 8:
	    return KeyEvent.VK_8;
	case 9:
	    return KeyEvent.VK_9;
	}
	return 0;
    }

    public void stateChanged(ChangeEvent e) {
	int i = tabbedPane.getSelectedIndex();
	if (i == 0) {
	    lsnr.Vehicle_Unselected_From_pnlControl();
	    if (!eventFromMap && game.isRunning()) {
		lsnr.EVT_VSelect_Tab_All();
	    }
	} else {
	    lsnr.Vehicle_Selected_From_pnlControl(i - 1);
	    if (!eventFromMap) {
		lsnr.EVT_VSelect_Tab(i);
	    }
	}
	eventFromMap = false;
    }

    public void Show_Vehicle_Status(int idx) {
	eventFromMap = true;
	tabbedPane.setSelectedIndex(idx);
    }

    public void Show_All_Vehicle_Status() {
	lsnr.Vehicle_Unselected_From_pnlMap();
    }

    public void chkEngageEnabled() {
	pnlGeneral.chkEngageEnabled();
	for (int i = 0; i < pnlVehicle.length; i++)
	    pnlVehicle[i].chkEngageEnabled();
    }

    /**
     * Updates the damage info of this vehicle
     */
    public void Update_Vehicle_Damage(Vehicle v) {
	pnlGeneral.Update_Damage(v);
	pnlVehicle[v.getIndex() - 1].Update_Damage();
    }

    /**
     * Update the mission info of this vehicle to the mission of this payload
     */
    public void Update_Vehicle_Payload(Vehicle v, Payload p) {
	pnlVehicle[v.getIndex() - 1].Update_Payload(p);
    }

    /**
     * Clears the mission info of this vehicle
     */
    public void Update_Vehicle_Payload_Clear(Vehicle v) {
	pnlVehicle[v.getIndex() - 1].Payload_Clear();
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == tmr) {
	    for (int i = 0; i < game.getVehicleList().size(); i++) {
		if (game.getVehicleList().getVehicle(i).getStatus() == MyGame.STATUS_VEHICLE_PENDING) {
		    colorFlag[i] = !colorFlag[i];
		} else {
		    colorFlag[i] = false;
		}
		// below: i+1 because 0-th tab is for "all"
		if (colorFlag[i])
		    tabbedPane.setForegroundAt(i + 1,
			    MyColor.COLOR_HIGHLIGHT_TAB);
		else
		    tabbedPane.setForegroundAt(i + 1, Color.BLACK);
	    }
	}
    }
}

class VehicleGeneralPanel extends JPanel {
    private static final long serialVersionUID = -8910858184513488565L;
    private VehicleCompactInfo[] infoList;
    private Game game;

    public void setCollisionInfo(ArrayList<CollisionZone> collisionInfo) {
	for (int i = 0; i < infoList.length; i++) {
	    HashSet<Integer> s = new HashSet<Integer>();
	    Vehicle v = infoList[i].getV();

	    for (CollisionZone cz : collisionInfo) {
		// aslist and contains doesnt work for primitive types.Copy that
		// to Integer[] style.
		Integer[] tmpArray = new Integer[cz.involvedVehicles.length];
		for (int k = 0; k < cz.involvedVehicles.length; k++) {
		    tmpArray[k] = cz.involvedVehicles[k];
		}
		if (Arrays.asList(tmpArray).contains((Integer) v.getIndex())) {
		    for (int id : cz.involvedVehicles) {
			s.add(id);
		    }
		}
	    }

	    infoList[i].setInCollisionVIds(s.toArray(new Integer[s.size()]),
		    Game.automationEnabled, Game.collisionHighlightEnabled);
	}
    }

    public VehicleCompactInfo[] getInfoList() {
	return infoList;
    }

    public void setInfoList(VehicleCompactInfo[] infoList) {
	this.infoList = infoList;
    }

    public VehicleGeneralPanel(GUI_Listener l, Game g) {
	double size[][] = { { TableLayout.FILL }, { 35, 35, 35, 35, 35 } };
	setLayout(new TableLayout(size));
	game = g;
	infoList = new VehicleCompactInfo[game.getVehicleList().size()];
	for (int i = 0; i < g.getVehicleList().size(); i++) {
	    infoList[i] = new VehicleCompactInfo(l, g.getVehicleList()
		    .getVehicle(i));
	    add(infoList[i], "0," + i);
	}
    }

    public void chkEngageEnabled() {
	for (int i = 0; i < infoList.length; i++)
	    infoList[i].chkEngageEnabled();
    }

    public void Update_Damage(Vehicle v) {
	infoList[v.getIndex() - 1].Update_Damage();
    }
}

class VehicleCompactInfo extends JPanel implements ActionListener {
    private static final long serialVersionUID = 3608652661736826513L;
    private final String APPROACHING_COLLISION = "Approaching Collision";
    double size4Grids[][] = { { 0.08, 0.54, 0.18, 0.2 }, { TableLayout.FILL } };
    double size3Grids[][] = { { 0.08, 0.72, 0.2 }, { TableLayout.FILL } };
    // private FlowLayout fl = new FlowLayout();
    private TitledBorder bdrTitle;
    private JPanel pnlVehicle, pnlInfo;
    private JLabel lblHealth, lblStatus;
    private JButton btnEngage;
    private JButton btnApplyAuto;
    private GUI_Listener lsnr;
    private Vehicle v;
    private VehicleIcon2 iconV;
    private Color COLOR_BACK = new Color(178, 178, 178, 255);
    private int intDamage;
    private Integer[] inCollisionVIds;

    private String GetCollisionZoneString() {
	String result = "[";
	for (Integer i : inCollisionVIds) {
	    result += (i + ",");
	}

	if (result.length() > 1) {
	    result = result.substring(0, result.length() - 1);
	}

	result += "]";
	return result;
    }

    private MouseAdapter mAdapter = new MouseAdapter() {
	@Override
	public void mouseEntered(MouseEvent e) {
	    Cursor normalCursor = new Cursor(Cursor.HAND_CURSOR);
	    setCursor(normalCursor);
	}

	@Override
	public void mouseExited(MouseEvent e) {
	    Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	    setCursor(normalCursor);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	    lsnr.Vehicle_Selected_From_pnlControl(v.getIndex() - 1);
	}

    };

    public void setInCollisionVIds(Integer[] inCollisionVIds,
	    boolean isAutomationEnabled, boolean isHighlightEnabled) {
	this.inCollisionVIds = inCollisionVIds;

	if (Arrays.asList(this.inCollisionVIds).contains(this.v.getIndex())) {

	    if (isAutomationEnabled) {
		btnApplyAuto.setEnabled(true);
	    }
	    if (isHighlightEnabled) {
		lblStatus.setText(APPROACHING_COLLISION + " "
			+ GetCollisionZoneString());
	    }
	} else {

	    if (isAutomationEnabled) {
		btnApplyAuto.setEnabled(false);
	    }
	    if (isHighlightEnabled) {
		lblStatus.setText("");
	    }
	}
    }

    public Vehicle getV() {
	return v;
    }

    public VehicleCompactInfo(GUI_Listener l, Vehicle v) {
	this.v = v;
	lsnr = l;

	bdrTitle = BorderFactory.createTitledBorder("");
	setBorder(bdrTitle);
	intDamage = (int) Math.round(v.getDamage());

	// FIRST
	pnlVehicle = new JPanel();
	iconV = new VehicleIcon2(v);
	pnlVehicle.setBorder(bdrTitle);
	pnlVehicle.setLayout(new GridLayout(0, 1));
	pnlVehicle.add(iconV);
	pnlVehicle.addMouseListener(mAdapter);

	// SECOND
	lblHealth = new JLabel("Damage : " + intDamage); // COLOR =
							 // lblHealth.setForeground
	lblStatus = new JLabel("");
	pnlInfo = new JPanel();
	// pnlInfo.setLayout(new GridLayout(0, 0));
	pnlInfo.setBorder(bdrTitle);
	pnlInfo.setBackground(COLOR_BACK);
	// pnlInfo.add(lblHealth);
	pnlInfo.add(lblStatus);
	pnlInfo.addMouseListener(mAdapter);

	// THIRD
	btnApplyAuto = new JButton("AUTO");
	btnApplyAuto.addActionListener(this);
	btnApplyAuto.setEnabled(false);

	// FOURTH
	btnEngage = new JButton("ENGAGE");
	btnEngage.addActionListener(this);
	btnEngage.setEnabled(false);

	// SETTING LAYOUT
	if (Game.automationEnabled) {
	    setLayout(new TableLayout(this.size4Grids));
	    // insert_grid(gbc, pnlVehicle, 0, 0, 1, 1, 0.1, 1.0, 0);
	    add(pnlVehicle, "0,0");
	    // insert_grid(gbc, pnlInfo, 1, 0, 1, 1, 0.85, 1.0, 0);
	    add(pnlInfo, "1,0");
	    // insert_grid(gbc, btnApplyAuto, 2, 0, 1, 1, 0.025, 1.0, 0);
	    add(btnApplyAuto, "2,0");
	    // insert_grid(gbc, btnEngage, 3, 0, 1, 1, 0.025, 1.0, 0);
	    add(btnEngage, "3,0");
	} else {
	    setLayout(new TableLayout(this.size3Grids));
	    // insert_grid(gbc, pnlVehicle, 0, 0, 1, 1, 0.1, 1.0, 0);
	    add(pnlVehicle, "0,0");
	    // insert_grid(gbc, pnlInfo, 1, 0, 1, 1, 0.85, 1.0, 0);
	    add(pnlInfo, "1,0");
	    // insert_grid(gbc, btnEngage, 3, 0, 1, 1, 0.025, 1.0, 0);
	    add(btnEngage, "2,0");
	}

    }

    public void chkEngageEnabled() {
	repaint();

	iconV.chkEngageEnabled();

	if (v.getStatus() == MyGame.STATUS_VEHICLE_PENDING)
	    btnEngage.setEnabled(true);
	else
	    btnEngage.setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == btnEngage) {
	    if (v.getPayload() == Vehicle.PAYLOAD_COM)
		v.COM_Payload();
	    else
		lsnr.Vehicle_Engage_From_pnlControl(v);
	}

	if (e.getSource() == btnApplyAuto) {
	    lsnr.Automation_Applied_From_pnlControl(inCollisionVIds.clone());
	    /*
	     * String info = "Apply automation for vehicle " +
	     * Arrays.toString(inCollisionVIds) + "?";
	     * System.out.println("Update Path from panel control!"); if
	     * (JOptionPane.showConfirmDialog(null, info, "Automation",
	     * JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
	     * 
	     * } else {
	     * lsnr.Automation_Application_Canceled(inCollisionVIds.clone()); }
	     */
	}
    }

    public void Update_Damage() {
	if (intDamage != Math.round(v.getDamage()))
	    lblHealth.setForeground(Color.red);
	else
	    lblHealth.setForeground(Color.black);
	intDamage = (int) Math.round(v.getDamage());
	lblHealth.setText("Damage : " + Math.round(intDamage));
    }

    /*
     * private void insert_grid(GridBagConstraints gbc, Component cmpt, int x,
     * int y, int width, int height, double percent_x, double percent_y, int
     * ins) { gbc.fill = GridBagConstraints.BOTH; gbc.gridx = x; gbc.gridy = y;
     * gbc.gridwidth = width; gbc.gridheight = height; gbc.weightx = percent_x;
     * gbc.weighty = percent_y; gbc.insets = new Insets(ins, ins, ins, ins);
     * layout.setConstraints(cmpt, gbc); }
     */
    public JLabel getLblStatus() {
	return lblStatus;
    }

    public JButton getBtnApplyAuto() {
	return btnApplyAuto;
    }

}

class VehiclePanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 4435452374430336399L;
    private GridBagLayout grid_bag_layout = new GridBagLayout();
    private Vehicle selectedVehicle;
    private JLabel lblVehicle;
    private ImageIcon imgIcon;
    private JPanel pnlVehicle, pnlStatus;// , pnlControl;
    private JLabel lblDamage, lblMission;
    private JTextArea txtMission = new JTextArea(5, 5);
    private JScrollPane scrollPane;
    private JProgressBar progressBar;
    // private JButton btnGoal, btnAddWP, btnDelWP;
    private JButton btnEngage;
    private TitledBorder bdrTitle;
    private GUI_Listener lsnr;
    private String name;

    VehiclePanel(GUI_Listener e, Vehicle v) {
	double size[][] = { { 150, TableLayout.FILL }, { TableLayout.FILL } };

	GridBagConstraints gbc = new GridBagConstraints();
	lsnr = e;
	selectedVehicle = v;
	name = v.getName();
	// setLayout(grid_bag_layout);
	setLayout(new TableLayout(size));
	setPreferredSize(new Dimension(180, 230));

	//
	// TOP-LEFT
	pnlVehicle = new JPanel();
	bdrTitle = BorderFactory.createTitledBorder("Vehicle Info");
	pnlVehicle.setBorder(bdrTitle);
	imgIcon = new ImageIcon("Temporarily Unavailable");
	try {
	    imgIcon = new ImageIcon(new URL(MyURL.URL_VEHICLE + v.getType()
		    + "_" + v.getPayload() + ".jpg"));
	} catch (MalformedURLException urle) {
	    urle.printStackTrace();
	}
	// lblVehicle = new JLabel(v.getName(), imgIcon, JLabel.CENTER);
	lblVehicle = new JLabel("", imgIcon, JLabel.CENTER);
	lblVehicle.setVerticalTextPosition(JLabel.BOTTOM);
	lblVehicle.setHorizontalTextPosition(JLabel.CENTER);
	lblVehicle.setFont(MyFont.fontBold);
	pnlVehicle.add(lblVehicle);

	//
	// TOP-RIGHT
	bdrTitle = BorderFactory
		.createTitledBorder("Vehicle Health And Status");
	pnlStatus = new JPanel();
	pnlStatus.setLayout(grid_bag_layout); // pnlStatus.setLayout(new
					      // GridLayout(5,1));
	pnlStatus.setBorder(bdrTitle);
	lblDamage = new JLabel("Damage Level");
	lblMission = new JLabel("Current Mission");
	txtMission.setEditable(false);
	txtMission.setLineWrap(true);
	scrollPane = new JScrollPane(txtMission);
	scrollPane.setAutoscrolls(true);
	scrollPane.remove(scrollPane.getHorizontalScrollBar());
	btnEngage = new JButton("Engage");
	btnEngage.addActionListener(this);
	progressBar = new JProgressBar(0, 100);
	progressBar.setValue(0);
	progressBar.setStringPainted(true);
	insert_grid(gbc, lblDamage, 0, 0, 1, 1, 1, 0.1, 0);
	pnlStatus.add(lblDamage);
	insert_grid(gbc, progressBar, 0, 1, 1, 1, 1, 0.1, 0);
	pnlStatus.add(progressBar);
	insert_grid(gbc, lblMission, 0, 2, 1, 1, 1, 0.1, 0);
	pnlStatus.add(lblMission);
	insert_grid(gbc, scrollPane, 0, 3, 1, 1, 1, 0.5, 0);
	pnlStatus.add(scrollPane);
	insert_grid(gbc, btnEngage, 0, 4, 1, 1, 1, 0.1, 0);
	pnlStatus.add(btnEngage);

	add(pnlVehicle, "0,0");
	add(pnlStatus, "1,0");
	// insert_grid(gbc, pnlVehicle, 0, 0, 1, 1, 0, 0, 0); add(pnlVehicle);
	// insert_grid(gbc, pnlStatus, 1, 0, 1, 1, 0.9, 0, 0); add(pnlStatus);
    }

    public String getName() {
	return name;
    }

    public void chkEngageEnabled() {
	if (selectedVehicle.getStatus() == MyGame.STATUS_VEHICLE_PENDING)
	    btnEngage.setEnabled(true);
	else
	    btnEngage.setEnabled(false);
    }

    public void Update_Damage() {
	progressBar.setValue((int) Math.round(selectedVehicle.getDamage()));
    }

    public void Update_Payload(Payload p) {
	txtMission.setText(p.getStatement());
    }

    public void Payload_Clear() {
	txtMission.setText("");
    }

    private void insert_grid(GridBagConstraints gbc, Component cmpt, int x,
	    int y, int width, int height, double percent_x, double percent_y,
	    int ins) {
	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridx = x;
	gbc.gridy = y;
	gbc.gridwidth = width;
	gbc.gridheight = height;
	gbc.weightx = percent_x;
	gbc.weighty = percent_y;
	gbc.insets = new Insets(ins, ins, ins, ins);
	grid_bag_layout.setConstraints(cmpt, gbc);
    }

    public void actionPerformed(ActionEvent e) {
	// if( e.getSource() == btnGoal ){
	// lsnr.Vehicle_Goal_From_pnlControl(selectedVehicle);}
	// if( e.getSource() == btnAddWP){ if( selectedVehicle.hasGoal() )
	// lsnr.Vehicle_WP_Add_From_pnlControl(selectedVehicle);}
	// if( e.getSource() == btnDelWP){ if( selectedVehicle.hasWaypoint() )
	// lsnr.Vehicle_WP_Del_From_pnlControl(selectedVehicle);}
	if (e.getSource() == btnEngage) {
	    if (selectedVehicle.getPayload() == Vehicle.PAYLOAD_COM)
		selectedVehicle.COM_Payload();
	    else
		lsnr.Vehicle_Engage_From_pnlControl(selectedVehicle);
	}
    }
}

// MAKE THIS SEPERATE
class VehicleIcon2 extends JPanel {
    private static final long serialVersionUID = -5961320475456746793L;
    private Vehicle v;
    private Color vColor;
    private boolean colorFlag;

    public VehicleIcon2(Vehicle v) {
	this.v = v;
	vColor = MyColor.COLOR_VEHICLE;
    }

    public void paint(Graphics g) {
	Graphics2D g2 = (Graphics2D) g.create();
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);
	if (v.getType() == "UAV") {
	    g.setColor(Color.BLACK);
	    g.drawArc(1, 3, MySize.SIZE_VEHICLE_WIDTH_PXL,
		    MySize.SIZE_VEHICLE_HEIGHT_PXL, 0, 180);
	    g.drawLine(1, MySize.SIZE_VEHICLE_HEIGHT_PXL / 2 + 3,
		    MySize.SIZE_VEHICLE_WIDTH_PXL + 1,
		    MySize.SIZE_VEHICLE_HEIGHT_PXL / 2 + 3);
	    g.setColor(vColor);
	    g.fillArc(1, 3, MySize.SIZE_VEHICLE_WIDTH_PXL,
		    MySize.SIZE_VEHICLE_HEIGHT_PXL, 0, 180);
	    paintString((Graphics2D) g, MySize.SIZE_VEHICLE_WIDTH_PXL / 2 - 1,
		    MySize.SIZE_VEHICLE_HEIGHT_PXL / 2 - 2, Color.black,
		    MyFont.fontSmallBold, Integer.toString(v.getIndex()));
	} else if (v.getType() == "UUV") {
	    g.setColor(Color.BLACK);
	    g.drawArc(1, -MySize.SIZE_VEHICLE_HEIGHT_PXL / 2 + 3,
		    MySize.SIZE_VEHICLE_WIDTH_PXL,
		    MySize.SIZE_VEHICLE_HEIGHT_PXL, 180, 180);
	    g.drawLine(1, 3, MySize.SIZE_VEHICLE_WIDTH_PXL + 1, 3);
	    g.setColor(vColor);
	    g.fillArc(1, -MySize.SIZE_VEHICLE_HEIGHT_PXL / 2 + 3,
		    MySize.SIZE_VEHICLE_WIDTH_PXL,
		    MySize.SIZE_VEHICLE_HEIGHT_PXL, 180, 180);
	    paintString((Graphics2D) g, MySize.SIZE_VEHICLE_WIDTH_PXL / 2, 18,
		    Color.black, MyFont.fontSmallBold,
		    Integer.toString(v.getIndex()));
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
	repaint();
    }
}
