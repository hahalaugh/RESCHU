package reschu.game.view;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.border.TitledBorder;

import reschu.app.ScenarioConfig;
import reschu.constants.*;
import reschu.game.controller.GUI_Listener;
import reschu.game.controller.Reschu;

public class FrameEnd extends JFrame {
    private static final long serialVersionUID = 1490485040395748916L;
    // private Gui_Listener lsnr;
    private TitledBorder bdrTitle;
    private JButton btnStart;
    private ImageIcon imgIcon;
    private JLabel lblHAL;

    public FrameEnd(GUI_Listener l) {
	// lsnr = l;
	super("RESCHU");

	setAlwaysOnTop(true);
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});
	setLayout(new GridLayout(0, 1));
	setResizable(false);

	bdrTitle = BorderFactory.createTitledBorder(MyGame.VERSION_INFO);
	JPanel pnl = new JPanel();
	pnl.setBorder(bdrTitle);
	pnl.setBackground(Color.WHITE);

	try {
	    imgIcon = new ImageIcon(new URL(MyURL.URL_PREFIX + "HAL.png"));
	} catch (MalformedURLException urle) {
	    urle.printStackTrace();
	}
	lblHAL = new JLabel("", imgIcon, JLabel.CENTER);

	btnStart = new JButton("THANKS FOR YOUR PARTICIPATION");
	btnStart.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnStart) {
		    try {
			if (Reschu.expermient()) {
			    openWebpage(new URL(ScenarioConfig.GetInstance()
				    .get_surveyURL()).toURI());
			}
			System.exit(0);
		    } catch (URISyntaxException ee) {
			ee.printStackTrace();
		    } catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		    }
		}

	    }
	});
	pnl.add(lblHAL);
	pnl.add(btnStart);
	this.setUndecorated(true);
	getRootPane().setWindowDecorationStyle(JRootPane.NONE);
	add(pnl);

    }

    public static void openWebpage(URI uri) {
	Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop()
		: null;
	if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	    try {
		desktop.browse(uri);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }
}
