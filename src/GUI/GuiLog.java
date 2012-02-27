package GUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import JavaVision.Position;

public class GuiLog extends JPanel {

	// Components to be added to the control panel
	
	JTextArea logging;
	Position ourCoords = new Position(0, 0);
	Position enemyCoords = new Position(0, 0);
	Position ballCoords = new Position(0, 0);
	
	JPanel buttons = new JPanel(new GridLayout(2, 2));
	JButton start = new JButton("Start");
	JButton apply = new JButton("Apply");

	String currentPitchConstants = "...";
	String currentColour ="Yellow";
	String currentAttackGoal ="Right";
	String currentMode = "Normal";
	
	String ballCoordsStr ="";
	String ourCoordsStr = "";
	String enemyCoordsStr = "";
	

	/** Constructor */
	public GuiLog() {
		setBackground(Color.gray);
		setLayout(new BorderLayout(5,5));
		// Log text area
		logging = new JTextArea(5,35);
		JScrollPane scrollPane = new JScrollPane(logging);
		logging.setForeground(Color.GREEN);
		logging.setBackground(Color.BLACK);
		logging.setEditable(false);
		logging.setTabSize(3);
		
		ballCoordsStr = positionToString(ballCoords); 
		ourCoordsStr = positionToString(ourCoords);
		enemyCoordsStr = positionToString(enemyCoords);
		updateJTextArea();
		
		// apply is disabled at launch
		apply.setEnabled(false);
		// Add buttons to buttons JPanel
		buttons.add(start);
		buttons.add(apply);
		// add components to control panel
		add(scrollPane, BorderLayout.WEST);
		add(buttons);

	
	}
	// Setters for the strings for the log
	public void setCurrentColour(String currentColour) {
		this.currentColour = currentColour;
		updateJTextArea();
	}

	public void setCurrentAttackGoal(String currentAttackGoal) {
		this.currentAttackGoal = currentAttackGoal;
		updateJTextArea();
	}


	public void setCurrentMode(String currentMode) {
		this.currentMode = currentMode;
		updateJTextArea();
	}

	public void setBallCoors(Position newBallCoords) {
		ballCoordsStr = positionToString(newBallCoords);
		updateJTextArea();
	}
	
	public void setOurCoors(Position newOurCoords) {
		ourCoordsStr = positionToString(newOurCoords);
		updateJTextArea();
	}
	
	public void setEnemyCoors(Position newEnemyCoords) {
		enemyCoordsStr = positionToString(newEnemyCoords);
		updateJTextArea();
	}
	
	public void setCurrentPitchConstants(String newPitchConstants) {
		currentPitchConstants = newPitchConstants;
		updateJTextArea();
	}
	
	/**
	 * Return a string from a Position
	 * @param p - The Position to create String from
	 * @return - The Position as a String
	 */
	public String positionToString(Position p) {
		return ("(" + p.getX() + ", " + p.getY() + ")");
	}
	
	/**
	 * Method to update the logging text area
	 */
	public void updateJTextArea() {
		logging.setText("Ball Coords:\t" + ballCoordsStr  + "\t\t" + "Current Colour: "+ currentColour + "\n" +
				"Our Coords:\t"  + ourCoordsStr + "\t\t" + "Current Attack Goal: "+ currentAttackGoal +"\n" +
				"Enemy Coords:\t" + enemyCoordsStr + "\t\t" + "Current Mode: "+ currentMode + "\n\n" + 
				"Loaded Constants:\t" + currentPitchConstants);
	}
}

