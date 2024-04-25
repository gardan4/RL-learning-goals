import javax.swing.*;
import java.util.Arrays;

public class DemoAgent {

    private static MountainCarEnv game;
    private static MountainCarEnv gametest;
    private static double[] gamestate;
    private static PolicyIteration policyIteration;

    public static void main(String[] args) {
        policyIteration = new PolicyIteration();
        policyIteration.iterate();
        int[] optimalPolicy = policyIteration.getPolicy();
        game = new MountainCarEnv(MountainCarEnv.RENDER);

        //Running 100 episodes
        for (int i=0; i<5; i++) {
            gamestate = game.randomReset();
            long startTime = System.currentTimeMillis();
            int[] count = new int[3];
            while (gamestate[0] == 0 && System.currentTimeMillis() < startTime + (1000 * 30)) {
                // discretize the state
                int state = PolicyIteration.getIndicesFromState(gamestate[2], gamestate[3]);
                int action = optimalPolicy[state];

                gamestate = game.step(action);
               // count the type of actions taken

                // count action
                count[action + 1]++;
            }
            //print count of actions
            System.out.println("Number of reverse: " + count[0] + ", neutral: " + count[1] + ", forward: " + count[2]);
        }
        try {
            double[][] valuesToShow = new double[PolicyIteration.NUM_POSITIONS][PolicyIteration.NUM_POSITIONS];
            for (int i = 0; i< PolicyIteration.NUM_POSITIONS; i++)
                for (int j=0; j< PolicyIteration.NUM_VELOCITIES; j++)
                    valuesToShow[i][j] = policyIteration.getV()[i * PolicyIteration.NUM_POSITIONS + j];
            HeatMapWindow hm = new HeatMapWindow(valuesToShow);
            hm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            hm.setSize(600,600);
            hm.setVisible(true);
            hm.update(valuesToShow);
        }
        catch (Exception e) {System.out.println(e.getMessage());}
    }

}