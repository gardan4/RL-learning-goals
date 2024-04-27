import java.util.Random;

public class PolicyIteration {
    private static final double THETA = 0.001;
    private static final double GAMMA = 0.99;

    private static final int NUM_ACTIONS = 3;
    private static final int[] ACTIONS = {MountainCarEnv.NOTHING, MountainCarEnv.FORWARD, MountainCarEnv.REVERSE};
    private static final double[] POSITION_RANGE = {MountainCarEnv.MIN_POS, MountainCarEnv.MAX_POS};
    private static final double[] VELOCITY_RANGE = {-MountainCarEnv.MAX_SPEED, MountainCarEnv.MAX_SPEED};
    public static final int NUM_POSITIONS = 1000;
    public static final int NUM_VELOCITIES = 1000;
    private static final int NUM_STATES = NUM_POSITIONS*NUM_VELOCITIES;
    private static final double POSITION_STEP = calculateStep(POSITION_RANGE[0], POSITION_RANGE[1], NUM_POSITIONS);
    private static final double VELOCITY_STEP = calculateStep(VELOCITY_RANGE[0], VELOCITY_RANGE[1], NUM_VELOCITIES);

    private final MountainCarEnv game;
    private final double[] V;
    private final int[] policy;



    public PolicyIteration() {
        this.game = new MountainCarEnv();;
        this.V = new double[NUM_STATES];
        this.policy = new int[NUM_STATES];

        Random rand = new Random();
        for (int s = 0; s < NUM_STATES; s++) {
            V[s] = rand.nextDouble(); // Initialize value function randomly
            policy[s] = ACTIONS[rand.nextInt(NUM_ACTIONS)]; // Initialize policy randomly
        }

        // Test case for boundary and central values
        int testIndex = 0; // Start of the range
        double[] testState = getDiscretizedStateFromIndices(testIndex);
        int resultIndex = getIndicesFromState(testState[0], testState[1]);
        assert(testIndex == resultIndex);

        testIndex = NUM_STATES - 1; // End of the range
        testState = getDiscretizedStateFromIndices(testIndex);
        resultIndex = getIndicesFromState(testState[0], testState[1]);
        assert(testIndex == resultIndex);

        testIndex = NUM_STATES / 2; // Middle of the range
        testState = getDiscretizedStateFromIndices(testIndex);
        resultIndex = getIndicesFromState(testState[0], testState[1]);
        assert(testIndex == resultIndex);

    }

    public void policyEvaluation() {
        double delta;
        do {
            delta = 0;
            for (int s = 0; s < NUM_STATES; s++) {
                // Get the position and velocity from the state index
                double[] discretizedstate = getDiscretizedStateFromIndices(s);
                double[] gamestate = game.setState(discretizedstate[0], discretizedstate[1]);

                if (gamestate[0] == 1) {
                    V[s] = 0;
                    continue;
                }

                gamestate = game.step(policy[s]);

                double v = V[s];
                int s_prime = getIndicesFromState(gamestate[2], gamestate[3]);

                V[s] = 1 * (gamestate[1] + GAMMA * V[s_prime]);
                delta = Math.max(delta, Math.abs(v - V[s]));
            }

        } while (delta >= THETA);
    }

    public boolean policyImprovement() {
        boolean policyChanged = false;
        double[] gamestate;
        int changed = 0;
        for (int s = 0; s < NUM_STATES; s++) {
            // Get the position and velocity from the state index
            double[] discretizedstate = getDiscretizedStateFromIndices(s);
            game.setState(discretizedstate[0], discretizedstate[1]);

            int oldAction = policy[s];
            double bestActionValue = Double.NEGATIVE_INFINITY;
            int bestAction = 0;

            // Examine each possible action to find the best for the current state
            for (int a = 0; a < NUM_ACTIONS; a++) {
                // Take the action and observe the next state
                double[] oldgamestate = game.getState();
                gamestate = game.step(ACTIONS[a]);
                int nextState = getIndicesFromState(gamestate[2], gamestate[3]);
                double actionValue = gamestate[1] + GAMMA * V[nextState]; // Calculate the value of taking action 'a'

                // Choose the action that has the highest value
                if (actionValue > bestActionValue) {
                    bestActionValue = actionValue;
                    bestAction = ACTIONS[a];
                }

                //apparently game.undo has bugs, so we need to reset the state
                game.setState(oldgamestate[2], oldgamestate[3]);
            }

            if (bestAction != oldAction) {
                policy[s] = bestAction;
                policyChanged = true; // Mark the flag as true since the policy has been improved
                changed++;
            }
        }
        System.out.println("Changed: " + changed);
        return policyChanged; // Return whether the policy was improved or not
    }

    public void iterate() {
        boolean policyStable;
        int i = 0;
        do {
            if (i % 10 == 0) {
                System.out.println("Iteration: " + i);
            }
            policyEvaluation();
            policyStable = !policyImprovement();
            i++;
        } while (!policyStable);

        // print amount of 1, 0 or -1 in optimal policy
        int[] count = new int[3];
        for (int j : policy) {
            count[j + 1]++;
        }
        System.out.println("Number of -1: " + count[0] + ", 0: " + count[1] + ", 1: " + count[2]);
    }

    public int[] getPolicy() {
        return policy;
    }

    public double[] getV() {
        return V;
    }

    public static double[] getDiscretizedStateFromIndices(int stateIndex) {
        double position = POSITION_RANGE[0] + (stateIndex / NUM_VELOCITIES) * POSITION_STEP;
        double velocity = VELOCITY_RANGE[0] + (stateIndex % NUM_VELOCITIES) * VELOCITY_STEP;
        return new double[]{position, velocity};
    }

    public static int getIndicesFromState(double position, double velocity) {
        int positionIndex = (int) Math.round((position - POSITION_RANGE[0]) / POSITION_STEP);
        int velocityIndex = (int) Math.round((velocity - VELOCITY_RANGE[0]) / VELOCITY_STEP);
        int stateIndex = positionIndex * NUM_VELOCITIES + velocityIndex;
        return stateIndex;
    }

    private static double calculateStep(double minRange, double maxRange, int numSteps) {
        return (maxRange - minRange) / (numSteps - 1);
    }


}
