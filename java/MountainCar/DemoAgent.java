import javax.swing.*;
import java.util.Arrays;

public class DemoAgent {

    private static MountainCarEnv game;
    private static MountainCarEnv gametest;
    private static double[] gamestate;
    private static ValueIteration ValueIteration;

    public static void main(String[] args) {
        ValueIteration vi = new ValueIteration();
        vi.iterate();
        int[][] optimalPolicy = vi.Policy;
        System.out.println("Optimal policy: ");
        for (int i = 0; i < ValueIteration.NUM_POSITIONS; i++) {
            for (int j = 0; j < ValueIteration.NUM_VELOCITIES; j++) {
                System.out.print(optimalPolicy[i][j] + " ");
            }
            System.out.println();
        }
        game = new MountainCarEnv(MountainCarEnv.RENDER);
        //Running 100 episodes
        for (int i=0; i<10; i++) {
            gamestate = game.randomReset();
//            System.out.println("The initial gamestate is: " + Arrays.toString(gamestate));
            long startTime = System.currentTimeMillis();
            while (gamestate[0] == 0 && System.currentTimeMillis() < startTime + (1000 * 15)) {
//                System.out.println("The car's position is " + gamestate[2]);
//                System.out.println("The car's velocity is " + gamestate[3]);
                // discretize the state
                int[] state = ValueIteration.getIndicesFromState(gamestate[2], gamestate[3]);
                int action = optimalPolicy[state[0]][state[1]];

                gamestate = game.step(action);
                System.out.println("Action: " + action);
//                System.out.println("I received a reward of " + gamestate[1]);
            }
            System.out.println();
        }
        try {
            double[][] valuesToShow = new double[1000][1000];
            for (int i = 0; i< ValueIteration.NUM_POSITIONS; i++)
                for (int j=0; j< ValueIteration.NUM_VELOCITIES; j++)
                    valuesToShow[i][j] = vi.V[i][j];
            HeatMapWindow hm = new HeatMapWindow(valuesToShow);
            hm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            hm.setSize(600,600);
            hm.setVisible(true);
            hm.update(valuesToShow);
        }
        catch (Exception e) {System.out.println(e.getMessage());}
    }

}