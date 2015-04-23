package reschu.tutorial;

import javax.swing.JOptionPane;

import reschu.app.AppMain;
import reschu.constants.MyDB;
import reschu.game.controller.GUI_Listener;

public class ControlTutorial extends TutorialModel {
    GUI_Listener lsnr;

    ControlTutorial(GUI_Listener lsnr, AppMain main) {
	super(main);
	this.lsnr = lsnr;
    }

    protected void showDialog() {
	switch (getState()) {
	case 0:
	    System.out.println("0");
	    JOptionPane.showMessageDialog(null, makePicPanel("ins1.png", ""));
	    // Intro: Explains the Purpose of the game
	    setDuration(1);
	    nextDialog();
	    break;

	case 1:
	    System.out.println("1");
	    JOptionPane.showMessageDialog(null, makePicPanel("ins2.png", ""));
	    // Intro: Explains the Purpose of the game
	    setDuration(1);
	    nextDialog();
	    // lsnr.TutorialAddVehicle();
	    break;
	case 2:
	    System.out.println("2");
	    JOptionPane.showMessageDialog(null, makePicPanel("GUI.png", ""));
	    // Step_2: Explains the Interface Elements
	    setDuration(3);
	    nextDialog();
	    break;
	case 3:
	    System.out.println("3");
	    JOptionPane.showMessageDialog(null, makePicPanel("icon.png", ""));
	    // Step_1: Explains the Map elements
	    setDuration(3);
	    nextDialog();
	    break;
	case 4:
	    System.out.println("4");
	    JOptionPane.showMessageDialog(null,
		    makePicPanel("changegoal1.png", ""));
	    // Step_3: Explains How to change a vehicle's destination
	    setDuration(25);

	    break;
	case 5:
	    System.out.println("5");
	    JOptionPane.showMessageDialog(null, makePicPanel("tl1.png", ""));
	    // Step_4: Explains How to add a waypoint
	    setDuration(1);
	    nextDialog();
	    break;
	case 6:
	    System.out.println("6");
	    JOptionPane.showMessageDialog(null,
		    makePicPanel("changegoal2.png", ""));
	    // Step_5: Explains how to move a waypoint
	    setDuration(25);
	    break;
	case 7:
	    System.out.println("7");
	    JOptionPane.showMessageDialog(null, "Good Job!");
	    setDuration(1);
	    nextDialog();
	    break;
	case 8:
	    System.out.println("8");
	    JOptionPane.showMessageDialog(null, makePicPanel("addwp1.png", ""));
	    setDuration(25);
	    break;
	case 9:
	    System.out.println("9");
	    JOptionPane.showMessageDialog(null, makePicPanel("tl2.png", ""));
	    setDuration(1);
	    nextDialog();
	    break;
	case 10:
	    System.out.println("10");
	    JOptionPane.showMessageDialog(null,
		    makePicPanel("changewp1.png", ""));
	    setDuration(25);
	    break;
	case 11:
	    System.out.println("11");
	    JOptionPane.showMessageDialog(null, "Good Job!");
	    setDuration(2);
	    nextDialog();
	    break;
	case 12:
	    System.out.println("12");
	    JOptionPane.showMessageDialog(null, makePicPanel("addwp2.png", ""));
	    setDuration(25);
	    break;
	case 13:
	    System.out.println("13");
	    JOptionPane.showMessageDialog(null, makePicPanel("tl3.png", ""));
	    setDuration(1);
	    nextDialog();
	    break;
	case 14:
	    System.out.println("14");
	    JOptionPane.showMessageDialog(null, makePicPanel("delwp1.png", ""));
	    setDuration(25);
	    break;
	case 15:
	    System.out.println("15");
	    JOptionPane.showMessageDialog(null, makePicPanel("wait.png", ""));
	    setDuration(500);
	    break;
	case 16:
	    System.out.println("16");
	    JOptionPane.showMessageDialog(null, makePicPanel("engage.png", ""));
	    // Explain engage operation
	    setDuration(20);
	    break;
	case 17:
	    System.out.println("17");
	    JOptionPane.showMessageDialog(null,
		    makePicPanel("engagedetail.png", ""));
	    // Explain the Red payload task. Detailed description required.
	    nextDialog();
	    break;
	case 18:
	    System.out.println("18");
	    JOptionPane
		    .showMessageDialog(null, makePicPanel("payload.png", ""));
	    // Explain the payload operation. Detailed description required
	    // setDuration(2);
	    nextDialog();
	    break;
	case 19:
	    // check
	    System.out.println("19");
	    JOptionPane.showMessageDialog(null, makePicPanel("check.png", ""));
	    // setDuration(2);
	    nextDialog();
	    break;
	case 20:
	    System.out.println("20");
	    JOptionPane.showMessageDialog(null, makePicPanel("red.png", ""));
	    // Explain following automatic operations by system
	    setDuration(500);
	    break;
	case 21:
	    System.out.println("21");
	    JOptionPane.showMessageDialog(null,
		    makePicPanel("submission.png", ""));
	    // Explain the Green payload task
	    nextDialog();
	    break;
	case 22:
	    System.out.println("22");
	    JOptionPane.showMessageDialog(null, makePicPanel("wait.png", ""));
	    setDuration(500);
	    break;
	case 23:
	    System.out.println("23");
	    JOptionPane.showMessageDialog(null, makePicPanel("yellow.png", ""));
	    // Explain the Orange payload task.
	    setDuration(500);
	    break;
	case 24:
	    System.out.println("24");
	    JOptionPane.showMessageDialog(null, makePicPanel("wait.png", ""));
	    setDuration(500);
	    break;
	case 25:
	    System.out.println("25");
	    JOptionPane.showMessageDialog(null, makePicPanel("green.png", ""));
	    // Explain the payload operation.
	    setDuration(500);
	    break;
	case 26:
	    System.out.println("26");
	    JOptionPane
		    .showMessageDialog(
			    null,
			    "Good Job! You have finished the first part of tutorial. The program will restart"
				    + "in 2 seconds for next part");
	    setDuration(2);
	    nextDialog();
	    break;
	case 27:
	    System.out.println("27");
	    // SESSION ENDED
	    main.Restart_Reschu();
	    break;
	default:
	    break;
	}
    }

    protected void checkEvent(int type, int vIdx, String target) {
	switch (getState()) {
	case 4:
	    checkCorrect(type == MyDB.GP_CHANGE_END_ASSIGNED && vIdx == 1
		    && target.equals("D"));
	    break;
	case 6:
	    checkCorrect(type == MyDB.GP_CHANGE_END_ASSIGNED && vIdx == 1
		    && target.equals("E"));
	    break;
	case 8:
	    checkCorrect(type == MyDB.WP_ADD_END && vIdx == 1);
	    break;
	case 10:
	    checkCorrect(type == MyDB.WP_MOVE_END && vIdx == 1);
	    break;
	case 12:
	    checkCorrect(type == MyDB.WP_ADD_END && vIdx == 1);
	    break;
	case 14:
	    checkCorrect(type == MyDB.WP_DELETE_END && vIdx == 1);
	    break;
	case 15:
	    checkCorrect(type == MyDB.VEHICLE_ARRIVES_TO_TARGET && vIdx == 1);
	    break;
	case 16:
	    checkCorrect(type == MyDB.PAYLOAD_ENGAGED && vIdx == 1);
	    break;
	case 22:
	case 24:
	    checkCorrect(type == MyDB.VEHICLE_ARRIVES_TO_TARGET && vIdx == 1);
	    break;
	case 20:
	case 23:
	case 25:
	    checkCorrect((type == MyDB.PAYLOAD_FINISHED_CORRECT || type == MyDB.PAYLOAD_FINISHED_INCORRECT)
		    && vIdx == 1);
	    break;
	default:
	    break;
	}
    }
}
