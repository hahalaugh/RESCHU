package reschu.game.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;

import reschu.constants.MySpeed;
import reschu.game.model.Game;

public class RemainingTime extends JPanel {
    private static final long serialVersionUID = -9112337331887745204L;
    private int current_time = 0;
    private int remaining_time = 0;
    private double progress = 0;

    public RemainingTime(Game g) {
	JLabel lblBlank = new JLabel("  ");
	this.add(lblBlank);
    }

    public void paint(Graphics g) {

	Graphics2D g2 = (Graphics2D) g.create();
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);

	g.setColor(new Color(238, 238, 238));
	g.fillRect(0, 0, getWidth(), getHeight());

	g.setColor(new Color(100, 100, 100, 255));
	g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
	g.setColor(new Color(150, 150, 150, 100));

	g.fillRect(0, 0, (int) Math.round(progress), getHeight());
	g.setColor(new Color(0, 0, 0, 255));
	g.drawString("REMAINS     " + setTimeFormat(remaining_time),
		getWidth() / 3, getHeight() / 2 + 5);

    }

    public void refresh(int milliseconds) {
	current_time = milliseconds / 1000;
	remaining_time = Game.TIME_TOTAL_GAME / 1000 - current_time;
	progress += (double) (getWidth() * MySpeed.SPEED_CLOCK / Game.TIME_TOTAL_GAME);
	repaint();
    }

    public String setTimeFormat(int time) {
	String time_min = "" + time / 60;
	String time_sec = "" + time % 60;

	if (time_min.length() == 1)
	    time_min = "0" + time_min;
	if (time_sec.length() == 1)
	    time_sec = "0" + time_sec;

	return time_min + ":" + time_sec;
    }
}
