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
        //String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        String fen = "2kr1b1r/pppbqppp/2n5/3pp1Pn/8/P2PKP2/1PP1P2P/RNBQ1BNR w - - 1 10";
        playingBoard.setBoardFEN(fen);
        //playingBoard.setBoardStartingPos();
        /* The starting and ending squares */
        short move;
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
            pen.println("Move:");
            input = eyes.nextLine();
            move = UIUtils.uciToMove(input);

            playingBoard = MoveGen.applyMove(move, playingBoard);
        }
        while (!input.equals("QUIT")) {
            pen.println("----------------");

            /* Create the MCT */
            MCT mct = new MCT(playingBoard);
            playingBoard = mct.search(duration, true).state;
            if (playingBoard == null) {
                pen.println("Game Over.");
                break;
            }

            playingBoard.printBoard();
            pen.println("Move:");
            input = eyes.nextLine();
            move = UIUtils.uciToMove(input);

            pen.println("Duration to run:");
            input = eyes.nextLine();
            duration = Duration.ofSeconds(Integer.parseInt(input));
            pen.print("\n----------------\n");
            playingBoard = MoveGen.applyMove(move, playingBoard);

            playingBoard.printBoard();
        }
        eyes.close();
    } // main(String[])
} // TARS