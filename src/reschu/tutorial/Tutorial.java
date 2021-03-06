package reschu.tutorial;

import reschu.app.AppMain;
import reschu.game.controller.GUI_Listener;
import reschu.game.controller.Reschu;

public class Tutorial {
    private TutorialModel scenario;

    public Tutorial(GUI_Listener lsnr, AppMain main) {

	if (Reschu.tutorial()) {
	    scenario = new ControlTutorial(lsnr, main);
	} else if (Reschu.extraTutorial()) {
	    switch (Reschu._scenario) {
	    case 1:
		scenario = new Scenario1(lsnr, main);
		break;
	    case 2:
		scenario = new Scenario2(lsnr, main);
		break;
	    case 3:
		scenario = new Scenario3(lsnr, main);
		break;
	    case 4:
		scenario = new Scenario4(lsnr, main);
		break;
	    default:
		break;
	    }
	}
    }

    public void tick() {
	scenario.tick();
    }

    public void event(int type, int vIdx, String target) {
	scenario.checkEvent(type, vIdx, target);
    }
}
