import java.util.ArrayList;

public class MountainCarEnv {
	
	public static final double MIN_POS = -1.2;
	public static final double MAX_POS = 0.6;
	public static final double MAX_SPEED = 0.07;
	public static final double GOAL_POS = 0.5;
	public static final int NOTHING = 0;
	public static final int FORWARD = 1;
	public static final int REVERSE = -1;
	private static final double FORCE_INFLUENCE = 0.0005;
	private static final double GRAVITY = -0.0025;
	
	private double position;
	private double velocity;
	public static final int TEXT = 1;
	public static final int RENDER = 2;
	public static final int NONE = 0;
	private static int vizType = NONE;
	private static int vizDelay =10;
	public MountainCarEnv() {
		randomReset();
	}
	public MountainCarEnv(int vizType) {
		MountainCarEnv.vizType = vizType;
	}
	public MountainCarEnv(int vizType, int vizDelay) {
		MountainCarEnv.vizType = vizType; MountainCarEnv.vizDelay = vizDelay;
	}

	public double[] step(int force) {
		if (!(force== REVERSE || force==NOTHING || force==FORWARD))
			System.out.println("Please pick only -1 (Reverse), 0 (Nothing) or 1 (Forward) as actions.");
		velocity += force * FORCE_INFLUENCE + Math.cos(3*position)*(GRAVITY);
		velocity = Math.min(MAX_SPEED, Math.max(-MAX_SPEED,velocity));
		position += velocity;
		position = Math.min(MAX_POS, Math.max(MIN_POS,position));
		if (position==MIN_POS && velocity<0) velocity = 0;
		double[] state = getState();
		if (vizType == TEXT)
			printState(state);
		else if (vizType == RENDER)
			renderState(state);
		return state;
	}

	public double[] undo(int force) {
		if (!(force== REVERSE || force==NOTHING || force==FORWARD))
			System.out.println("Please pick only -1 (Reverse), 0 (Nothing) or 1 (Forward) as actions.");
		velocity -= force* FORCE_INFLUENCE + Math.cos(3*position)*(GRAVITY);
		velocity = Math.min(MAX_SPEED, Math.max(-MAX_SPEED,velocity));
		position -= velocity;
		position = Math.min(MAX_POS, Math.max(MIN_POS,position));
		if (position==MIN_POS && velocity<0) velocity = 0;
		double[] state = getState();
		if (vizType == TEXT)
			printState(state);
		else if (vizType == RENDER)
			renderState(state);
		return state;
	}

	public double[] setState(double position, double velocity) {
		this.position = Math.min(MAX_POS, Math.max(MIN_POS,position));
		this.velocity = Math.min(MAX_SPEED, Math.max(-MAX_SPEED,velocity));
		return getState();
	}

	public double[] getState() {
		double[] state = new double[4];
		if (position>GOAL_POS)
			state[0] = 1;
		else
			state[0] = 0;
		state[1] = getReward();
		state[2] = position;
		state[3] = velocity;
		return state;
	}

	public double getReward() {
		if (position>GOAL_POS)
			return 0;
			// Possible negative reward for hitting back wall
			//else if (position==MIN_POS)
			//	return -50;
		else
			return -1.0;
	}
	
	public double getPosition() {
		return position;
	}

	public double getVelocity() {
		return velocity;
	}

	public double[] randomReset() {
		this.position = -0.6 + Math.random()*0.2;
		this.velocity = 0.0;
		return getState();
	}
	
	public double[] reset() {
		this.position = -0.6;
		this.velocity = 0.0;
		return getState();
	}

	public static void printState(double[] state) {
		if (state[0] == 1)
			System.out.println("The car has escaped");
		else
			System.out.println("The episode is still going");
		System.out.println("The reward earned this step was " + state[1]);
		System.out.println("Car Position: " + state[2] +
				" Car Velocity: " + state[3]);
	}

	public static void renderState(double[] state) {
		if (!visibleState) {
			panel = new MountainCarPanel();
			visibleState = true;
		}
		panel.render(state);
		try {
			Thread.sleep(vizDelay);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private static boolean visibleState = false;
	private static MountainCarPanel panel;


}
