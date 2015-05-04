package reschu.constants;

import reschu.app.ScenarioConfig;

public class MySpeed {
    final static public int VELOCITY = 1;

    final static public int SPEED_CONTROL = 1;
    // final static public int SPEED_TIMER = 500 / SPEED_CONTROL;
    final static public int SPEED_TIMER = ScenarioConfig.GetInstance()
	    .get_speedTimer() / SPEED_CONTROL;
    final static public int SPEED_CLOCK = 1000 / SPEED_CONTROL;
    // final static public int SPEED_CLOCK_DAMAGE_CHECK = SPEED_CLOCK * 1;
    final static public int SPEED_CLOCK_DAMAGE_CHECK = SPEED_TIMER;
    final static public int SPEED_CLOCK_HAZARD_AREA_UPDATE = SPEED_CLOCK * 10;
    final static public int SPEED_CLOCK_HAZARD_AREA_UPDATE_TUTORIAL = SPEED_CLOCK * 80;
    final static public int SPEED_CLOCK_TARGET_AREA_UPDATE = SPEED_CLOCK * 5;
    final static public int SPEED_CLOCK_AUTO_TARGET_ASSIGN_UPDATE = SPEED_CLOCK * 15;
    final static public int SPEED_LOG = SPEED_CLOCK * 1;
}
