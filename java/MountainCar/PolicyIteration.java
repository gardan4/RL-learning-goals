import java.util.Arrays;

public class PolicyIteration {

    private static final double THETA = 0.0001; // A small positive number
    private static final double GAMMA = 0.9; // Discount factor

    private MountainCarEnv game;
    private double[][] V; // Value function
    private int[][] policy; // Current policy

    public PolicyIteration(MountainCarEnv game) {
        this.game = game;
        this.V = new double[game.getNumStates()][game.getNumActions()];
        this.policy = new int[game.getNumStates()][game.getNumActions()];
    }

    public void iterate() {
        boolean policyStable;
        do {
            policyStable = true;

            // Policy Evaluation
            double delta;
            do {
                delta = 0;
                for (int s = 0; s < game.getNumStates(); s++) {
                    double v = V[s][policy[s][0]];
                    double sum = 0;
                    for (int a = 0; a < game.getNumActions(); a++) {
                        double[] gamestate = game.step(a);
                        sum += game.getTransitionProbability(s, a, gamestate) * (game.getReward(s, a, gamestate) + GAMMA * V[gamestate[0]][policy[gamestate[0]][0]]);
                    }
                    V[s][policy[s][0]] = sum;
                    delta = Math.max(delta, Math.abs(v - V[s][policy[s][0]]));
                }
            } while (delta >= THETA);

            // Policy Improvement
            for (int s = 0; s < game.getNumStates(); s++) {
                int oldAction = policy[s][0];
                double maxActionValue = Double.NEGATIVE_INFINITY;
                for (int a = 0; a < game.getNumActions(); a++) {
                    double actionValue = 0;
                    for (int sPrime = 0; sPrime < game.getNumStates(); sPrime++) {
                        actionValue += game.getTransitionProbability(s, a, sPrime) * (game.getReward(s, a, sPrime) + GAMMA * V[sPrime][policy[sPrime][0]]);
                    }
                    if (actionValue > maxActionValue) {
                        maxActionValue = actionValue;
                        policy[s][0] = a;
                    }
                }
                if (oldAction != policy[s][0]) {
                    policyStable = false;
                }
            }
        } while (!policyStable);
    }

    public double[][] getValueFunction() {
        return V;
    }

    public int[][] getPolicy() {
        return policy;
    }
}