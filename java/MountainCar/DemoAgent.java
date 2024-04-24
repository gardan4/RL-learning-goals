import javax.swing.*;
import java.util.Arrays;

public class DemoAgent {

    private static MountainCarEnv game;
    private static double[] gamestate;
    private static PolicyIteration policyIteration;

    public static void main(String[] args) {
        game = new MountainCarEnv(MountainCarEnv.RENDER);
        policyIteration = new PolicyIteration(game);
        policyIteration.iterate();
        System.out.println("The optimal policy is: " + Arrays.toString(policyIteration.getPolicy()));
        int[] optimalPolicy = policyIteration.getPolicy();

        //Running 100 episodes
        for (int i=0; i<10; i++) {
            gamestate = game.randomReset();
            System.out.println("The initial gamestate is: " + Arrays.toString(gamestate));
            while (gamestate[0] == 0) { // Game is not over yet
                System.out.println("The car's position is " + gamestate[2]);
                System.out.println("The car's velocity is " + gamestate[3]);
                // discretize the state
                int state = policyIteration.discretizeState(gamestate);
                int action = optimalPolicy[state];

                gamestate = game.step(action);
                System.out.println("The gamestate passed back to me was: " + Arrays.toString(gamestate));
                System.out.println("I received a reward of " + gamestate[1]);
            }
            System.out.println();
        }
        try {
            double[][] valuesToShow = new double[1000][1000];
            for (int i=0; i<1000; i++)
                for (int j=0; j<1000; j++)
                    valuesToShow[i][j] = Math.sin(0.00002*i*j);
            HeatMapWindow hm = new HeatMapWindow(valuesToShow);
            hm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            hm.setSize(600,600);
            hm.setVisible(true);
            hm.update(valuesToShow);
        }
        catch (Exception e) {System.out.println(e.getMessage());}
    }

}