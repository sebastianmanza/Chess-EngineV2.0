package engine;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.Scanner;

import utils.MCTutils.MCT;
import utils.MoveGeneration.GameState;
import utils.MoveGeneration.MoveGen;
import utils.UserInterface.UIUtils;

/**
 * The main class of the engine. Will prompt the user, initialize the MCT, etc.
 * 
 * @author Sebastian Manza
 */
public class TARS {
    public static void main(String[] args) throws Exception {
        Scanner eyes = new Scanner(System.in);
        PrintWriter pen = new PrintWriter(System.out, true);
        String input;
        boolean engineColor;

        /* Prompt the user for engine color */
        pen.println("Enter engine color (W/B): ");
        input = eyes.nextLine();
        if (input.equals("W")) {
            engineColor = true;
        } else if (input.equals("B")) {
            engineColor = false;
        } else {
            pen.println("Next time please enter W or B. Engine automatically assigned to white.");
            engineColor = true;
        } // if/else

        /* Create a new board. */
        GameState playingBoard = new GameState(true, engineColor);
        playingBoard.setBoardStartingPos();

        /* The starting and ending squares */
        int start;
        int end;

        /* Prompt and set duration. */
        pen.println("Enter starting duration:");
        input = eyes.nextLine();
        Duration duration = Duration.ofSeconds(Integer.parseInt(input));

        /* Print the initial position */
        playingBoard.printBoard();
        pen.println("");
        input = "";

        /* If the engine is black. */
        if (playingBoard.engineColor == false) {
            playingBoard.printBoard();

            /* Prompt for squares */
            pen.println("Starting square:");
            input = eyes.nextLine();
            start = UIUtils.tosquareIndex(input);
            pen.println("\nEnding Square:");
            input = eyes.nextLine();
            end = UIUtils.tosquareIndex(input);

            /* Create the move */
            short nextMove = MoveGen.moves[start][end];
            playingBoard = MoveGen.applyMove(nextMove, playingBoard);
        }
        while (!input.equals("QUIT")) {
            pen.println("----------------");

            /* Create the MCT */
            MCT mct = new MCT(playingBoard);
            playingBoard = mct.search(duration, true);

            if (playingBoard == null) {
                pen.println("Game Over.");
                break;
            }

            playingBoard.printBoard();
            pen.println("Starting square:");
            input = eyes.nextLine();
            start = UIUtils.tosquareIndex(input);
            pen.println("\nEnding Square:");
            input = eyes.nextLine();
            end = UIUtils.tosquareIndex(input);

            pen.println("Duration to run:");
            input = eyes.nextLine();
            duration = Duration.ofSeconds(Integer.parseInt(input));
            pen.print("\n----------------\n");
            short nextMove = MoveGen.moves[start][end];
            playingBoard = MoveGen.applyMove(nextMove, playingBoard);

            playingBoard.printBoard();
        }
        eyes.close();
    } // main(String[])
} // TARS