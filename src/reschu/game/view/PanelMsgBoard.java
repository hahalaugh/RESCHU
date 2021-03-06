package reschu.game.view;

//import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import java.util.Calendar;
import java.text.SimpleDateFormat;

public class PanelMsgBoard extends JPanel implements ActionListener {
    public static enum MessageType {
	PayloadDescription, PayloadCorrect, PayloadInCorrect, SystemInfo, Chat
    };

    private static final long serialVersionUID = -6444398994914980642L;
    private static final String DATE_FORMAT_NOW = "HH:mm:ss";

    private GridBagLayout grid_bag_layout = new GridBagLayout();
    // private static JTextArea txtMsgBoard = new JTextArea(5, 5);
    private static JTextPane txtMsgBoard = new JTextPane();
    // Juntao: Delete the chat dialog and chat button
    // private static JTextField txtChat = new JTextField(1);
    // private static JLabel lblChat = new JLabel(">Msg: ");
    private JScrollPane scrollPane;
    // private JButton btnSend = new JButton("SEND");
    static SimpleAttributeSet f;

    public PanelMsgBoard() {
	TitledBorder bdrTitle = BorderFactory.createTitledBorder("Message");
	this.setBorder(bdrTitle);

	GridBagConstraints gbc = new GridBagConstraints();

	txtMsgBoard.setEditable(false);
	// txtMsgBoard.setLineWrap(true);

	scrollPane = new JScrollPane(txtMsgBoard);
	scrollPane.setAutoscrolls(true);
	scrollPane.remove(scrollPane.getHorizontalScrollBar());

	// txtChat.addActionListener(this);
	// btnSend.addActionListener(this);

	this.setLayout(grid_bag_layout);
	this.insert_grid(gbc, scrollPane, 0, 0, 3, 1, 1.0, 1.0, 0);
	this.add(scrollPane);
	// this.insert_grid(gbc, lblChat, 0, 1, 1, 1, 0.0, 0.0, 0);
	// this.add(lblChat);
	// this.insert_grid(gbc, txtChat, 1, 1, 1, 1, 1.0, 0.0, 0);
	// this.add(txtChat);
	// this.insert_grid(gbc, btnSend, 2, 1, 1, 1, 0.0, 0.0, 0);
	// this.add(btnSend);
    }

    public static void Msg(String msg, MessageType type) {
	// String timestamp = Calendar.HOUR + ":" + Calendar.MINUTE + ":" +
	// Calendar.SECOND;

	// txtMsgBoard.setText(txtMsgBoard.getText() + Now() + "   " + msg +
	// "\n");
	if (f == null) {
	    f = new SimpleAttributeSet();
	    StyleConstants.setFontFamily(f, "Calibri");
	}

	switch (type) {
	case PayloadDescription:
	    StyleConstants.setForeground(f, Color.BLUE);
	    break;

	case PayloadCorrect:
	    StyleConstants.setForeground(f, Color.green);
	    break;

	case PayloadInCorrect:
	    StyleConstants.setForeground(f, Color.red);
	    break;

	case SystemInfo:
	case Chat:
	default:
	    StyleConstants.setForeground(f, Color.black);
	    break;
	}

	try {
	    txtMsgBoard.getStyledDocument().insertString(
		    txtMsgBoard.getStyledDocument().getLength(),
		    Now() + "   " + msg + "\n", f);
	    txtMsgBoard.setCaretPosition(txtMsgBoard.getDocument().getLength());
	} catch (BadLocationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private static String Now() {
	Calendar cal = Calendar.getInstance();
	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	return sdf.format(cal.getTime());
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

    }
}
