package reschu.app;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import info.clearthought.layout.TableLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import reschu.constants.*;
import reschu.game.controller.Reschu;

public class AppMain implements ActionListener {
    final private boolean WRITE_TO_DATABASE = false;
    public final static String VERSION = "RESCHU 080215_1";
    private String _username;
    private int _gamemode = MyGameMode.TUTORIAL_MODE;
    private int _scenario;
    private int _workload;
    private int _automation;
    private JFrame _frmLogin;
    private JButton _btnStart;
    private JComboBox<String> _cmbBoxGameMode, _cmbBoxScenario,
	    _cmbBoxWorkload, _cmbBoxAutomation;
    private Reschu reschu;

    /**
     * When tutorial is finished, RESCHU automatically restarts in the training
     * mode.
     */
    public void Restart_Reschu() {

	switch (_gamemode) {
	case MyGameMode.TUTORIAL_MODE:
	    _gamemode = MyGameMode.EXTRA_TUTORIAL_MODE;
	    break;
	case MyGameMode.EXTRA_TUTORIAL_MODE:
	    _gamemode = MyGameMode.TRAIN_MODE;
	    break;
	default:
	    break;
	}

	reschu.Game_End();
	reschu.dispose();
	reschu = null;

	initRESCHU(_username, _scenario, _workload, _automation);
    }

    /**
     * This should NEVER be called if you are NOT in the tutorial mode.
     */
    public void TutorialFinished() {
    }

    private void initRESCHU(String username, int scenario, int workload,
	    int automation) {
	// Setting _scenario again seems counter-intuitive here.
	// Since we are differentiating between administrators and subjects,
	// we need to update the scenario number here again.
	_scenario = scenario;
	_workload = workload;
	_automation = automation;
	// Create an instance of Reschu (JFrame)
	reschu = new Reschu(_gamemode, scenario, _username, this,
		WRITE_TO_DATABASE, workload, automation);
	reschu.pack();
	reschu.setVisible(true);
	reschu.setResizable(false);
	reschu.setExtendedState(JFrame.NORMAL);
    }

    private void setFrmLogin() {
	TitledBorder border;
	ImageIcon imgIcon;

	JLabel lblHAL, lblGameMode, lblScenario, lblProcedure, lblAutomation;

	JPanel pnl = new JPanel();
	JPanel pnlInside = new JPanel();

	String[] scenarios = { "Scenario 1", "Scenario 2", "Scenario 3",
		"Scenario 4" };
	String[] gamemodes = { "Tutorial", "Experiment" };
	String[] workloads = { "1", "2" };
	String[] automations = { "1", "2" };

	border = BorderFactory.createTitledBorder("");

	lblHAL = new JLabel();
	lblGameMode = new JLabel("Mode");
	lblScenario = new JLabel("Scenario");
	lblProcedure = new JLabel("Load");
	lblAutomation = new JLabel("Auto");

	_btnStart = new JButton("START");
	_btnStart.addActionListener(this);
	_cmbBoxGameMode = new JComboBox<String>(gamemodes);
	_cmbBoxGameMode.addActionListener(this);
	_cmbBoxScenario = new JComboBox<String>(scenarios);
	_cmbBoxScenario.addActionListener(this);
	_cmbBoxWorkload = new JComboBox<String>(workloads);
	_cmbBoxWorkload.addActionListener(this);
	_cmbBoxAutomation = new JComboBox<String>(automations);
	_cmbBoxAutomation.addActionListener(this);

	try {
	    imgIcon = new ImageIcon(new URL(MyURL.URL_PREFIX + "HAL.png"));
	    lblHAL = null;
	    lblHAL = new JLabel("", imgIcon, JLabel.CENTER);
	} catch (MalformedURLException urle) {
	    System.out.println(urle.getMessage());
	}

	_frmLogin = new JFrame(VERSION);
	_frmLogin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	_frmLogin.setLayout(new GridLayout(0, 1));
	_frmLogin.setResizable(false);
	_frmLogin.add(pnl);
	_frmLogin.setLocation(300, 300);
	_frmLogin.setAlwaysOnTop(true);
	_frmLogin.setVisible(true);

	double sizeMain[][] = {
		{ TableLayout.FILL, 50, 238, 50, TableLayout.FILL },
		{ 10, 194, 200, TableLayout.FILL } };
	double sizeInside[][] = {
		{ TableLayout.FILL, 60, 10, 140, TableLayout.FILL },
		{ TableLayout.FILL, 25, 3, 25, 3, 25, 3, 25, 3, 25, 10, 25,
			TableLayout.FILL } };

	pnlInside.setLayout(new TableLayout(sizeInside));
	pnlInside.setBorder(border);
	pnlInside.add(lblGameMode, "1,3");
	pnlInside.add(_cmbBoxGameMode, "3,3");
	pnlInside.add(lblScenario, "1,5");
	pnlInside.add(_cmbBoxScenario, "3,5");
	pnlInside.add(lblProcedure, "1,7");
	pnlInside.add(_cmbBoxWorkload, "3,7");
	pnlInside.add(lblAutomation, "1,9");
	pnlInside.add(this._cmbBoxAutomation, "3,9");
	pnlInside.add(_btnStart, "1,11, 3,11");

	pnl.setLayout(new TableLayout(sizeMain));
	pnl.setBorder(border);
	pnl.setBackground(Color.WHITE);
	pnl.add(lblHAL, "1,1, 3,1");
	pnl.add(pnlInside, "2,2");

	this._cmbBoxAutomation.setEnabled(false);
	this._cmbBoxWorkload.setEnabled(false);

	_frmLogin.setSize(450, 500);
    }

    public void actionPerformed(ActionEvent ev) {
	if (ev.getSource() == _cmbBoxGameMode) {
	    _gamemode = _cmbBoxGameMode.getSelectedIndex();
	    switch (_gamemode) {
	    case 0:
		_gamemode = MyGameMode.TUTORIAL_MODE;
		this._cmbBoxAutomation.setEnabled(false);
		this._cmbBoxWorkload.setEnabled(false);
		break;
	    case 1:
		_gamemode = MyGameMode.ADMINISTRATOR_MODE;
		this._cmbBoxAutomation.setEnabled(true);
		this._cmbBoxWorkload.setEnabled(true);
		break;
	    }
	}
	if (ev.getSource() == _btnStart) {
	    if (_gamemode == MyGameMode.TUTORIAL_MODE) {
		_workload = 1;
	    } else {
		_workload = _cmbBoxWorkload.getSelectedIndex();
	    }

	    _automation = _cmbBoxAutomation.getSelectedIndex();
	    _scenario = _cmbBoxScenario.getSelectedIndex() + 1;
	    initRESCHU(_username, _scenario, _workload, _automation);
	    _frmLogin.setVisible(false);
	    _frmLogin.dispose();
	}
    }

    static public void main(String argv[]) {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		File configFile = new File(ScenarioConfig.CONFIG_PATH);
		if (!configFile.exists()) {
		    ScenarioConfig.CreateDefaultConfig();
		}

		AppMain app = new AppMain();
		app.setFrmLogin();
	    }
	});
    }
}
