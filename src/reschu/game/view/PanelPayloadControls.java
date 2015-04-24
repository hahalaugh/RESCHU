package reschu.game.view;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;

import reschu.constants.MyURL;
import reschu.game.controller.GUI_Listener;

public class PanelPayloadControls extends JPanel implements ActionListener {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1072321384776271417L;
    private GUI_Listener listener;
    JButton btnUp, btnDown, btnCW, btnCCW, btnZoomIn, btnZoomOut;
    public static JButton btnCheck, btnSafe, btnHit;
    JButton btnSubmit;
    private URL urlImgCheck, urlImgCancel, urlImageAttack; // ,urlImgUp,
    // urlImgDown,
    // urlImgCW,
    // urlImgCCW;
    double origin_time;

    /** Creates a new instance of PanelPayloadControls */
    public PanelPayloadControls(GUI_Listener e, String strTitle, double o) {
	origin_time = o;
	String url_prefix = MyURL.URL_PREFIX;
	btnCheck = new JButton("CHECK");
	btnHit = new JButton("HIT");
	btnSafe = new JButton("SAFE");

	btnSubmit = new JButton("OK");
	btnSubmit.setEnabled(false);
	/*
	 * setLayout(new GridLayout(6,1)); add(btnUp);
	 * btnUp.addActionListener(this); add(btnDown);
	 * btnDown.addActionListener(this); add(btnCW);
	 * btnCW.addActionListener(this); add(btnCCW);
	 * btnCCW.addActionListener(this); add(btnZoomIn);
	 * btnZoomIn.addActionListener(this); add(btnZoomOut);
	 * btnZoomOut.addActionListener(this);
	 */
	setLayout(new GridLayout(3, 1));
	add(btnCheck);
	btnCheck.setEnabled(false);
	btnCheck.addActionListener(this);
	add(btnHit);
	btnHit.setEnabled(false);
	btnHit.addActionListener(this);
	add(btnSafe);
	btnSafe.setEnabled(false);
	btnSafe.addActionListener(this);
	// add(btnSubmit); btnSubmit.addActionListener(this);

    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == btnUp)
	    listener.Pan_Up_Selected();
	if (e.getSource() == btnDown)
	    listener.Pan_Down_Selected();
	if (e.getSource() == btnCW)
	    listener.Rotate_Clockwise_Selected();
	if (e.getSource() == btnCCW)
	    listener.Rotate_Counter_Selected();
	if (e.getSource() == btnZoomIn)
	    listener.Zoom_In();
	if (e.getSource() == btnZoomOut)
	    listener.Zoom_Out();
	if (e.getSource() == btnSubmit)
	    listener.HitPayload();
	if (e.getSource() == btnCheck)
	    listener.CheckPayload();
	if (e.getSource() == btnSafe)
	    listener.SafePayload();
	if (e.getSource() == btnHit)
	    listener.HitPayload();
    }

    public void enableSubmit(boolean enable) {
	btnSubmit.setEnabled(enable);
    }

    public void setListener(GUI_Listener l) {
	listener = l;
    }

}
