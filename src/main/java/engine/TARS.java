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
        try (Scanner eyes = new Scanner(System.in)) {
            PrintWriter pen = new PrintWriter(System.out, true);
            String input;
            boolean engineColor;
            
            /* Prompt the user for engine color */
            pen.println("Enter engine color (W/B): ");
            input = eyes.nextLine();
            switch (input) {
                case "W" -> engineColor = true;
                case "B" -> engineColor = false;
                default -> {
                    pen.println("Next time please enter W or B. Engine automatically assigned to white.");
                    engineColor = true;
                    // if/else
                }
            }
            
            /* Create a new board. */
            GameState playingBoard = new GameState(true, engineColor);
            //String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            String fen = "3rkbnr/Rp2qppp/2n5/3p1b2/8/1PP2P2/3PP1PP/3QKBNR w Kk - 1 13";
            //String fen = "r1bqk1nr/pppp1ppp/2n5/2b1p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 4";
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
            if (playingBoard.engineColor == false && playingBoard.turnColor == true) {
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
        }
    } // main(String[])
} // TARS