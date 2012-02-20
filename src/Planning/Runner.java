package Planning;

import java.util.ArrayList;

import au.edu.jcu.v4l4j.V4L4JConstants;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;
import Planning.Move;
import JavaVision.*;

public class Runner extends Thread {

	// Objects
	public static Ball ball;
	public static Robot nxt;
	static WorldState state;
	static Runner instance = null;
	static Robot blueRobot;
	static Robot yellowRobot;
	boolean usingSimulator = false;
	private static ControlGUI thresholdsGUI;
	Vision vision;

	// game flags
	boolean teamYellow = false;
	public static final int DEFAULT_SPEED = 35;		// used for move_forward method in Robot
	public static final int EACH_WHEEL_SPEED = 900; // used for each_wheel_speed method in Robot

	public static void main(String args[]) {

		instance = new Runner();

	}

	/**
	 * Instantiate objects and start the planning thread
	 */
	public Runner() {

		blueRobot = new Robot();
		yellowRobot = new Robot();
		ball = new Ball();

		start();
	}

	/**
	 * Planning thread which begins planning loop
	 */
	public void run() {		
		if (teamYellow) {
			nxt = yellowRobot;
		} else {
			nxt = blueRobot;
		}

		startVision();

		// start communications with our robot
		nxt.startCommunications();
		
		mainLoop();
	}

	/**
	 * Method to initiate the vision
	 */
	private void startVision() {	    
		/**
		 * Creates the control
		 * GUI, and initialises the image processing.
		 * 
		 * @param args        Program arguments. Not used.
		 */
		WorldState worldState = new WorldState();
		ThresholdsState thresholdsState = new ThresholdsState();

		/* Default to main pitch. */
		PitchConstants pitchConstants = new PitchConstants(0);

		/* Default values for the main vision window. */
		String videoDevice = "/dev/video0";
		int width = 640;
		int height = 480;
		int channel = 0;
		int videoStandard = V4L4JConstants.STANDARD_PAL;
		int compressionQuality = 80;

		try {
			/* Create a new Vision object to serve the main vision window. */
			vision = new Vision(videoDevice, width, height, channel, videoStandard,
					compressionQuality, worldState, thresholdsState, pitchConstants);

			/* Create the Control GUI for threshold setting/etc. */
			thresholdsGUI = new ControlGUI(thresholdsState, worldState, pitchConstants);
			thresholdsGUI.initGUI();

		} catch (V4L4JException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void mainLoop() {
		int angle = 0;
		int[] prevResults = new int[10];
		ArrayList<Integer> goodAngles = new ArrayList<Integer>();

		// Get 10 angles from vision
		for(int i = 1; i <= 10; i++) {
			getPitchInfo();

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			int m = Move.getAngleToBall(nxt, ball);

			if (i < 11) {
				prevResults[i-1] = m;
			}
		}
		
		// Remove outliers
		goodAngles = removeVal(prevResults);

		// Sum angles
		for (int j = 0; j < goodAngles.size(); j++) {
			angle += goodAngles.get(j);
		}
		// Get average angle
		angle /= goodAngles.size();

		System.out.println("First angle(avg) calculated: " + (angle));

		int dist = Move.getDist(nxt, ball);
		
		nxt.rotateRobot(angle);
		
		// Wait for robot to complete rotation
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		nxt.moveForward(20);
		int counter = 0;

		while(dist > 30) { // dist in pixels

			if  (counter==15) {

				counter=0;

				getPitchInfo();
				dist = Move.getDist(nxt, ball);
				int n = Move.getAngleToBall(nxt, ball);

				if((Math.abs(n) > 20)) {
					nxt.rotateRobot(n);
					getPitchInfo();
					dist = Move.getDist(nxt, ball);
//					int n2 = Move.getAngleToBall(nxt, ball);
					// Wait for robot to complete rotation
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					nxt.moveForward(20);

				}
			}
			counter++;
		}
		nxt.stop();	
	}


	/*
	 * Compare the first element with all other elements.
	 * If difference > 40 then place in 2nd array
	 * Compare lengths of arrays, bigger array has more similar elements
	 * Repeat the comparison step on the bigger array
	 * to make sure that the array only contains similar elements
	 */

	private ArrayList<Integer> removeVal(int[] nums) {

		ArrayList<Integer> array1 = new ArrayList<Integer>();
		ArrayList<Integer> array2 = new ArrayList<Integer>();
		ArrayList<Integer> array3 = new ArrayList<Integer>(); 

		array1.add(nums[0]);
		/*
		 * if i-th element in the array differs by more than 40 from the first
		 * element, add to 2nd array instead of 1st array
		 */

		for (int i = 1; i < nums.length; i++ ) {
			if ((nums[i]-nums[0] >= -20) && (nums[i]-nums[0] <= 20)) {
				array1.add(nums[i]);
			} else {
				array2.add(nums[i]);
			}
		}

		/*
		 * Check which is bigger, set bigger one to array1
		 */

		if (array2.size() > array1.size()) {
			array1 = (ArrayList<Integer>) array2.clone();
		}

		array2.clear();
		array2.add(array1.get(0));

		/*
		 * same as previous for loop, but compare elements in the newly made array
		 */

		for (int i = 1; i < array1.size(); i++) {
			if ((array1.get(i)-array1.get(0) >= -20) && (array1.get(i)-array1.get(0) <= 20)) {
				array2.add(array1.get(i));
			} else {
				array3.add(array1.get(i));
			}
		}

		/*
		 * return larger array
		 */

		if (array3.size() > array2.size()) {
			return array3;
		} else {
			return array2;
		}
	}

	/**
	 * Get the most recent information from vision
	 */
	public void getPitchInfo() {

		// Get pitch information from vision
		state = vision.getWorldState();
//		System.out.println("______________new pitch info_______________________");

		ball.setCoors(new Position(state.getBallX(), state.getBallY()));	

		if(teamYellow) {
			nxt.setAngle(state.getYellowOrientation());
			nxt.setCoors(new Position(state.getYellowX(), state.getYellowY()));
//			System.out.println("Y: " + Math.toDegrees(yellowRobot.angle));

			blueRobot.setAngle(state.getBlueOrientation());
			blueRobot.setCoors(new Position(state.getBlueX(), state.getBlueY()));
			//			System.out.println(blueRobot.coors.getX() + " " + blueRobot.coors.getY() +" "+ blueRobot.angle);
		} else {
			nxt.setAngle(state.getBlueOrientation());
			nxt.setCoors(new Position(state.getBlueX(), state.getBlueY()));
			//			System.out.println("B: " + yellowRobot.coors.getX() + " " + yellowRobot.coors.getY() +" "+ yellowRobot.angle);

			//			System.out.println(blueRobot.coors.getX() + " " + blueRobot.coors.getY() +" "+ blueRobot.angle);
			yellowRobot.setAngle(state.getYellowOrientation());
			yellowRobot.setCoors(new Position(state.getYellowX(), state.getYellowY()));
		}

	}
}
