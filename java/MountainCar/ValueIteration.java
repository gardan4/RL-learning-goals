import java.util.Random;

public class ValueIteration {
    private static final double THETA = 0.00001;
    private static final double GAMMA = 0.90;

    private static final int NUM_ACTIONS = 3;
    private static final int[] ACTIONS = {MountainCarEnv.REVERSE, MountainCarEnv.NOTHING, MountainCarEnv.FORWARD};
    private static final double[] POSITION_RANGE = {MountainCarEnv.MIN_POS, MountainCarEnv.MAX_POS};
    private static final double[] VELOCITY_RANGE = {-MountainCarEnv.MAX_SPEED, MountainCarEnv.MAX_SPEED};
    public static final int NUM_POSITIONS = 2000;
    public static final int NUM_VELOCITIES = 1000;
    private static final double POSITION_STEP = calculateStep(POSITION_RANGE[0], POSITION_RANGE[1], NUM_POSITIONS);
    private static final double VELOCITY_STEP = calculateStep(VELOCITY_RANGE[0], VELOCITY_RANGE[1], NUM_VELOCITIES);

    private final MountainCarEnv game;
    public final double[][] V = new double[NUM_POSITIONS][NUM_VELOCITIES];
    public final int[][] Policy = new int[NUM_POSITIONS][NUM_VELOCITIES];

    public ValueIteration() {
        this.game = new MountainCarEnv();
        initialize();
    }

    private void initialize() {
        // Initially set all state values to zero and random policies
        for (int i = 0; i < NUM_POSITIONS; i++) {
            for (int j = 0; j < NUM_VELOCITIES; j++) {
                V[i][j] = 0;  // Initialize value function to 0
            }
        }
    }

    public void iterate() {
        for (int i = 0; i < 1000; i++) {
            boolean converged = valueIteration();
//            if (!converged) {
//                break;
//            }
            if (i % 100 == 0) {
                System.out.println("Iteration: " + i);
            }
        }

        extractPolicy();
        printPolicyDistribution();
    }

    private boolean valueIteration() {
        double delta = 0;
        for (int i = 0; i < NUM_POSITIONS; i++) {
            for (int j = 0; j < NUM_VELOCITIES; j++) {
                double[] discretizedState = getDiscretizedStateFromIndices(i, j);
                double[] gamestate = game.setState(discretizedState[0], discretizedState[1]);

                double v = V[i][j];
                double bestValue = Double.NEGATIVE_INFINITY;

                for (int action : ACTIONS) {
                    gamestate = game.step(action);
                    int[] nextIndices = getIndicesFromState(gamestate[2], gamestate[3]);
                    double actionValue = gamestate[1] + GAMMA * V[nextIndices[0]][nextIndices[1]];

                    if (actionValue > bestValue) {
                        bestValue = actionValue;
                    }
                    game.undo(action);  // Reset to previous state
                }

                V[i][j] = bestValue;
                delta = Math.max(delta, Math.abs(v - V[i][j]));
            }
        }
        return delta > THETA;  // Continue if the improvement is significant
    }

    private void extractPolicy() {
        for (int i = 0; i < NUM_POSITIONS; i++) {
            for (int j = 0; j < NUM_VELOCITIES; j++) {
                double[] discretizedState = getDiscretizedStateFromIndices(i, j);
                double[] gamestate = game.setState(discretizedState[0], discretizedState[1]);

                double bestActionValue = Double.NEGATIVE_INFINITY;
                int bestAction = 0;

                for (int action : ACTIONS) {
                    gamestate = game.step(action);
                    int[] nextIndices = getIndicesFromState(gamestate[2], gamestate[3]);
                    double actionValue = gamestate[1] + GAMMA * V[nextIndices[0]][nextIndices[1]];

                    if (actionValue > bestActionValue) {
                        bestActionValue = actionValue;
                        bestAction = action;
                    }
                    game.undo(action);
                }

                Policy[i][j] = bestAction;
            }
        }
    }

    private void printPolicyDistribution() {
        int[] count = new int[NUM_ACTIONS];
        for (int i = 0; i < NUM_POSITIONS; i++) {
            for (int j = 0; j < NUM_VELOCITIES; j++) {
                count[Policy[i][j] + 1]++;
            }
        }
        System.out.println("Distribution of actions in the optimal policy: " + java.util.Arrays.toString(count));
    }

    public static double[] getDiscretizedStateFromIndices(int positionIndex, int velocityIndex) {
        double position = POSITION_RANGE[0] + (positionIndex * POSITION_STEP);
        double velocity = VELOCITY_RANGE[0] + (velocityIndex * VELOCITY_STEP);
        return new double[]{position, velocity};
    }

    public static int[] getIndicesFromState(double position, double velocity) {
        int positionIndex = (int)((position - POSITION_RANGE[0]) / POSITION_STEP);
        int velocityIndex = (int)((velocity - VELOCITY_RANGE[0]) / VELOCITY_STEP);
        return new int[]{positionIndex, velocityIndex};
    }

    private static double calculateStep(double minRange, double maxRange, int numSteps) {
        return (maxRange - minRange) / (numSteps - 1);
    }
    }