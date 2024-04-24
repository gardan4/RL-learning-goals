import java.util.Arrays;
import java.util.Random;

public class PolicyIteration {
    private static final double THETA = 0.001;
    private static final double GAMMA = 0.9;
    private static final int NUM_STATES = 100;
    private static final int NUM_ACTIONS = 3;
    private static final int[] ACTIONS = {MountainCarEnv.REVERSE, MountainCarEnv.NOTHING, MountainCarEnv.FORWARD};

    private final MountainCarEnv game;
    private final double[] V;
    private final int[] policy;
    private final TileCoder positionCoder;
    private final TileCoder velocityCoder;

    public PolicyIteration(MountainCarEnv game) {
        this.game = game;
        this.V = new double[NUM_STATES];
        this.policy = new int[NUM_STATES];
        this.positionCoder = new TileCoder(10, 10, MountainCarEnv.MIN_POS, MountainCarEnv.MAX_POS);
        this.velocityCoder = new TileCoder(10, 10, -MountainCarEnv.MAX_SPEED, MountainCarEnv.MAX_SPEED);

        Random rand = new Random();
        for (int s = 0; s < NUM_STATES; s++) {
            V[s] = rand.nextDouble(); // Initialize value function randomly
            policy[s] = rand.nextInt(NUM_ACTIONS); // Initialize policy randomly
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

                V[s] = 1 * (gamestate[1] + GAMMA * V[s_prime]);
                delta = Math.max(delta, Math.abs(v - V[s]));

                game.undo(ACTIONS[policy[s]]);
            }
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

                game.undo(a); // Undo the step to return to the original state
            }

            // Update the policy with the best action if it is better than the current one
            if (bestAction != oldAction) {
                policy[s] = bestAction;
                policyChanged = true; // Mark the flag as true since the policy has been improved
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
    }

    public double[] getValueFunction() {
        return V;
    }

    public int[] getPolicy() {
        return policy;
    }

    private int discretizeState(double[] state) {
        int[] positionTiles = positionCoder.getTiles(state[0]);
        int[] velocityTiles = velocityCoder.getTiles(state[1]);
        int combinedHash = Arrays.hashCode(positionTiles) * 31 + Arrays.hashCode(velocityTiles);
        return Math.abs(combinedHash % NUM_STATES); // Ensure index is non-negative and within bounds
    }


}
