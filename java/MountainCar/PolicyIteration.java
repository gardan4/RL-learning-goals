import java.util.Arrays;
import java.util.Random;

public class PolicyIteration {
    private static final double THETA = 0.0001;
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
            policy[s] = rand.nextInt(NUM_ACTIONS); // Initialize policy randomly
        }
    }

    public void iterate() {
        boolean policyStable;
        do {
            policyStable = true;

            // Policy Evaluation
            double delta;
            do {
                delta = 0;
                for (int s = 0; s < NUM_STATES; s++) {
                    double v = V[s];
                    double[] gamestate = game.step(ACTIONS[policy[s]]);
                    int nextState = discretizeState(gamestate);
                    V[s] = game.getReward() + GAMMA * V[nextState];
                    delta = Math.max(delta, Math.abs(v - V[s]));
                    game.undo(ACTIONS[policy[s]]); // Undo the step to return to the original state
                }
                System.out.println(delta);
            } while (delta >= THETA);

            // Policy Improvement
            for (int s = 0; s < NUM_STATES; s++) {
                int oldAction = policy[s];
                double maxActionValue = Double.NEGATIVE_INFINITY;
                int chosenAction = oldAction;
                for (int a = 0; a < ACTIONS.length; a++) {
                    double[] gamestate = game.step(ACTIONS[a]);
                    int nextState = discretizeState(gamestate);
                    double actionValue = game.getReward() + GAMMA * V[nextState];
                    if (actionValue > maxActionValue) {
                        maxActionValue = actionValue;
                        chosenAction = a;
                    }
                    game.undo(ACTIONS[a]); // Undo the step to return to the original state
                }
                policy[s] = chosenAction;

                if (oldAction != policy[s]) {
                    policyStable = false;
                }
            }
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