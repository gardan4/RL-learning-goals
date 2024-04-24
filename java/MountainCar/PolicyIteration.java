import java.util.Arrays;
import java.util.Random;

public class PolicyIteration {
    private static final double THETA = 0.01;
    private static final double GAMMA = 0.9;
    private static final int NUM_STATES = 10000;
    private static final int NUM_ACTIONS = 3;
    private static final int[] ACTIONS = {MountainCarEnv.REVERSE, MountainCarEnv.NOTHING, MountainCarEnv.FORWARD};

    private static final int POSITION_BINS = 100;
    private static final int VELOCITY_BINS = 100;

    // The range of values for each state dimension
    private static final double POSITION_MIN = MountainCarEnv.MIN_POS;
    private static final double POSITION_MAX = MountainCarEnv.MAX_POS;
    private static final double VELOCITY_MIN = -MountainCarEnv.MAX_SPEED;
    private static final double VELOCITY_MAX = MountainCarEnv.MAX_SPEED;

    // The width of each bin for the uniform grid
    private static final double POSITION_BIN_WIDTH = (POSITION_MAX - POSITION_MIN) / POSITION_BINS;
    private static final double VELOCITY_BIN_WIDTH = (VELOCITY_MAX - VELOCITY_MIN) / VELOCITY_BINS;


    private final MountainCarEnv game;
    private final double[] V;
    private final int[] policy;

    public PolicyIteration(MountainCarEnv game) {
        this.game = game;
        this.V = new double[NUM_STATES];
        this.policy = new int[NUM_STATES];

        Random rand = new Random();
        for (int s = 0; s < NUM_STATES; s++) {
            V[s] = rand.nextDouble(); // Initialize value function randomly
            // initialize policy so that if velocity is negative, then reverse, else forward
            policy[s] = (s % VELOCITY_BINS < VELOCITY_BINS / 2) ? 0 : 2;
        }
    }

    public void policyEvaluation() {
        double delta;
        do {
            delta = 0;
            for (int s = 0; s < NUM_STATES; s++) {
                double[] gamestate = game.step(ACTIONS[policy[s]]);
                double v = V[s];
                int s_prime = discretizeState(gamestate);
                System.out.println( s_prime);

                V[s] = 1 * (gamestate[1] + GAMMA * V[s_prime]);
                delta = Math.max(delta, Math.abs(v - V[s]));

                game.undo(ACTIONS[policy[s]]);
            }
            System.out.println("Delta: " + delta);
        } while (delta >= THETA);
    }

    public boolean policyImprovement() {
        boolean policyChanged = false;
        for (int s = 0; s < NUM_STATES; s++) {
            int oldAction = policy[s];
            double bestActionValue = Double.NEGATIVE_INFINITY;
            int bestAction = oldAction;

            // Examine each possible action to find the best for the current state
            for (int a = 0; a < NUM_ACTIONS; a++) {
                double[] gamestate = game.step(ACTIONS[a]);
                int nextState = discretizeState(gamestate);
                double actionValue = gamestate[1] + GAMMA * V[nextState]; // Calculate the value of taking action 'a'

                // Choose the action that has the highest value
                if (actionValue > bestActionValue) {
                    bestActionValue = actionValue;
                    bestAction = a;
                }

                game.undo(ACTIONS[a]); // Undo the step to return to the original state
            }

            // Update the policy with the best action if it is better than the current one
            if (bestAction != oldAction) {
                policy[s] = bestAction;
                policyChanged = true; // Mark the flag as true since the policy has been improved
                System.out.println("Policy changed at state " + s + " from " + oldAction + " to " + bestAction);
            }
        }

        return policyChanged; // Return whether the policy was improved or not
    }

    public void iterate() {
        boolean policyStable;
        do {
            policyEvaluation();
            policyStable = !policyImprovement();
        } while (!policyStable);
        System.out.println("The optimal policy is: " + Arrays.toString(policy));
    }

    public int[] getPolicy() {
        return policy;
    }

    int discretizeState(double[] state) {
        int positionIndex = (int)((state[2] - POSITION_MIN) / POSITION_BIN_WIDTH);
        System.out.println("Position Index: " + state[2]);
        int velocityIndex = (int)((state[3] - VELOCITY_MIN) / VELOCITY_BIN_WIDTH);

        // Ensure the indices are within the range [0, BINS-1]
        positionIndex = Math.min(positionIndex, POSITION_BINS - 1);
        velocityIndex = Math.min(velocityIndex, VELOCITY_BINS - 1);

        // Combine the indices to get a single state index
        // The number of velocity bins is used as the base for the velocity index to ensure uniqueness
        return positionIndex * VELOCITY_BINS + velocityIndex;
    }


}
