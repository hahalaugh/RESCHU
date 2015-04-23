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
	    JOptionPane.showMessageDialog(null, makePicPanel("0.png", ""));
	    // Intro: Explains the Purpose of the game
	    setDuration(1);
	    nextDialog();
	    break;
	case 1:
	    System.out.println("1");
	    JOptionPane.showMessageDialog(null, makePicPanel("1.1.png", ""));
	    // Intro: Explains the Purpose of the game
	    setDuration(30);
	    nextDialog();
	    break;
	case 2:
	    System.out.println("2");
	    JOptionPane.showMessageDialog(null, makePicPanel("1.2.png", ""));
	    // Step_2: Explains the Interface Elements
	    setDuration(30);
	    nextDialog();
	    break;
	case 3:
	    System.out.println("3");
	    JOptionPane.showMessageDialog(null, makePicPanel("1.3.png", ""));
	    // Step_1: Explains the Map elements
	    setDuration(3);
	    nextDialog();
	    break;
	case 4:
	    System.out.println("4");
	    JOptionPane.showMessageDialog(null, "Good Job!You have finished the second part of tutorial. The program will restart"
	    	+ "in 2 seconds for next part");
	    setDuration(2);
	    nextDialog();
	    break;
	case 5:
	    System.out.println("5");
	    // SESSION ENDED
	    main.Restart_Reschu();
	    break;
	default:
	    break;
	}
    }

    protected void checkEvent(int type, int vIdx, String target) {
	switch (getState()) {
	case 1:
	case 2:
	    checkCorrect(type == MyDB.WP_ADD_END);
	    break;
	default:
	    break;
	}
    }
}
