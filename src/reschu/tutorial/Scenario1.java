package reschu.tutorial;

import javax.swing.JOptionPane;

import reschu.app.AppMain;
import reschu.constants.MyDB;
import reschu.game.controller.GUI_Listener;

public class Scenario1 extends TutorialModel {
    GUI_Listener lsnr;

    Scenario1(GUI_Listener lsnr, AppMain main) {
	super(main);
	this.lsnr = lsnr;
    }

    protected void showDialog() {
	switch (getState()) {
	case 0:
	    System.out.println("0");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_0.png", ""));
	    // Intro: Explains the Purpose of the game
	    setDuration(1);
	    nextDialog();
	    break;

	case 1:
	    System.out.println("1");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_1.png", ""));
	    // Intro: Explains the Purpose of the game
	    setDuration(1);
	    nextDialog();
	    lsnr.TutorialAddVehicle();
	    break;
	case 2:
	    System.out.println("2");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_2.png", ""));
	    // Step_2: Explains the Interface Elements
	    setDuration(3);
	    nextDialog();
	    break;
	case 3:
	    System.out.println("3");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_3.png", ""));
	    // Step_1: Explains the Map elements
	    setDuration(3);
	    nextDialog();
	    break;
	case 4:
	    System.out.println("4");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_4.png", ""));
	    // Step_3: Explains How to change a vehicle's destination
	    setDuration(25);

	    break;
	case 5:
	    System.out.println("5");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_5.png", ""));
	    // Step_4: Explains How to add a waypoint
	    setDuration(1);
	    nextDialog();
	    break;
	case 6:
	    System.out.println("6");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_6.png", ""));
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
	    JOptionPane.showMessageDialog(null, makePicPanel("1_7.png", ""));
	    setDuration(25);
	    break;
	case 9:
	    System.out.println("9");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_8.png", ""));
	    setDuration(1);
	    nextDialog();
	    break;
	case 10:
	    System.out.println("10");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_9.png", ""));
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
	    JOptionPane.showMessageDialog(null, makePicPanel("1_10.png", ""));
	    setDuration(25);
	    break;
	case 13:
	    System.out.println("13");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_11.png", ""));
	    setDuration(1);
	    nextDialog();
	    break;
	case 14:
	    System.out.println("14");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_12.png", ""));
	    setDuration(25);
	    break;
	case 15:
	    System.out.println("15");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_13.png", ""));
	    setDuration(500);
	    break;
	case 16:
	    System.out.println("16");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_14.png", ""));
	    // Explain engage operation
	    setDuration(20);
	    break;
	case 17:
	    System.out.println("17");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_15.png", ""));
	    // Explain the Red payload task. Detailed description required.
	    setDuration(4);
	    nextDialog();
	    break;
	case 18:
	    System.out.println("18");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_16.png", ""));
	    // Explain the payload operation. Detailed description required
	    setDuration(30);
	    break;
	case 19:
	    System.out.println("19");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_17.png", ""));
	    // Explain following automatic operations by system
	    setDuration(500);
	    break;
	case 20:
	    System.out.println("20");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_15.png", ""));
	    // Explain the Orange payload task.
	    setDuration(4);
	    nextDialog();
	    break;
	case 21:
	    System.out.println("21");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_16.png", ""));
	    // Explain the payload operation.
	    setDuration(500);
	    break;
	case 22:
	    System.out.println("22");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_15.png", ""));
	    // Explain the Green payload task
	    setDuration(4);
	    nextDialog();
	    break;
	case 23:
	    System.out.println("23");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_16.png", ""));
	    // Explain the payload operation
	    setDuration(30);
	    main.TutorialFinished();
	    break;
	case 24:
	    System.out.println("24");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_18.png", ""));
	    setDuration(0);
	    nextDialog();
	    break;
	case 25:
	    System.out.println("25");
	    JOptionPane.showMessageDialog(null, makePicPanel("1_19.png", ""));
	    setDuration(0);
	    nextDialog();
	    break;
	case 26:
	    System.out.println("26");
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
	case 18:
	    checkCorrect((type == MyDB.PAYLOAD_FINISHED_CORRECT || type == MyDB.PAYLOAD_FINISHED_INCORRECT)
		    && vIdx == 1);
	    break;
	case 19:
	    checkCorrect(type == MyDB.VEHICLE_ARRIVES_TO_TARGET && vIdx == 1);
	    break;
	case 21:
	    checkCorrect(type == MyDB.VEHICLE_ARRIVES_TO_TARGET && vIdx == 1);
	    break;
	case 23:
	    checkCorrect((type == MyDB.PAYLOAD_FINISHED_CORRECT || type == MyDB.PAYLOAD_FINISHED_INCORRECT)
		    && vIdx == 1);
	    break;
	default:
	    break;
	}
    }
}
