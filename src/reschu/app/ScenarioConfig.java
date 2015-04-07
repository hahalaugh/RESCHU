package reschu.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.JOptionPane;

public class ScenarioConfig {
    public static final String CONFIG_PATH = "config.resource";

    private int _gameTime;
    private int _slowVehicleSpeed;
    private int _fastVehicleSpeed;
    private int _speedTimer;
    private int _hazardNumber;
    private String _surveyURL;

    public String get_surveyURL() {
	return _surveyURL;
    }

    public void set_surveyURL(String _surveyURL) {
	this._surveyURL = _surveyURL;
    }

    public int get_hazardNumber() {
	return _hazardNumber;
    }

    public void set_hazardNumber(int _hazardNumber) {
	this._hazardNumber = _hazardNumber;
    }

    public int get_gameTime() {
	return _gameTime;
    }

    public int get_slowVehicleSpeed() {
	return _slowVehicleSpeed;
    }

    public int get_fastVehicleSpeed() {
	return _fastVehicleSpeed;
    }

    public int get_speedTimer() {
	return _speedTimer;
    }

    public ScenarioConfig(int _gameTime, int _slowVehicleSpeed,
	    int _fastVehicleSpeed, int _speedTimer, int _hazardNumber,
	    String _surveyURL) {
	super();
	this._gameTime = _gameTime;
	this._slowVehicleSpeed = _slowVehicleSpeed;
	this._fastVehicleSpeed = _fastVehicleSpeed;
	this._speedTimer = _speedTimer;
	this._hazardNumber = _hazardNumber;
	this._surveyURL = _surveyURL;
    }

    public static void CreateDefaultConfig() {
	Properties prop = new Properties();
	OutputStream output = null;

	try {
	    File configFile = new File(CONFIG_PATH);
	    if (!configFile.exists()) {
		if (!configFile.createNewFile()) {
		    throw new Exception("Creating configuration file failed");
		}
	    }

	    output = new FileOutputStream(CONFIG_PATH);

	    // Default value to be determined.
	    prop.setProperty("SPEED_TIMER", "250");
	    prop.setProperty("FAST_VEHICLE_SPEED", "250");
	    prop.setProperty("SLOW_VEHICLE_SPEED", "500");
	    prop.setProperty("GAME_TIME", "10");
	    prop.setProperty("HAZARD_NUMBER", "10");
	    prop.setProperty("SURVEY_URL", "http://www.google.com");

	    prop.store(output, null);

	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(null, e.getMessage());
	    System.exit(0);
	}
    }

    public static ScenarioConfig GetInstance() {
	return GetConfig();
    }

    private static ScenarioConfig GetConfig() {
	Properties prop = new Properties();
	InputStream input = null;

	try {
	    input = new FileInputStream(CONFIG_PATH);
	    prop.load(input);
	    return new ScenarioConfig(Integer.parseInt(prop
		    .getProperty("GAME_TIME")), Integer.parseInt(prop
		    .getProperty("SLOW_VEHICLE_SPEED")), Integer.parseInt(prop
		    .getProperty("FAST_VEHICLE_SPEED")), Integer.parseInt(prop
		    .getProperty("SPEED_TIMER")), Integer.parseInt(prop
		    .getProperty("HAZARD_NUMBER")),
		    prop.getProperty("SURVEY_URL"));

	} catch (FileNotFoundException e) {
	    CreateDefaultConfig();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    JOptionPane.showMessageDialog(null,
		    "Reading configuration file error: " + e.getMessage());
	}
	return null;
    }
}
