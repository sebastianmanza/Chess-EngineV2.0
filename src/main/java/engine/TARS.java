package engine;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.Scanner;

import utils.MCTutils.CNNode;
import utils.MCTutils.MCTMin;
import utils.MoveGeneration.GameState;
import utils.MoveGeneration.MoveGen;
import utils.TablebaseUtils.JavaTablebaseBridge;
import utils.UserInterface.UIUtils;

/**
 * The main class of the engine. Will prompt the user, initialize the MCT, etc.
 * 
 * @author Sebastian Manza
 */
public class TARS {
    public static void main(String[] args) throws Exception {
        // System.setProperty("OMP_NUM_THREADS", "1");
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
            String StartFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            // String fenMistakeOne = "r1bqk2r/ppp3pp/3bpn2/3pPp2/5B1P/3Q4/PPPNPPP1/R3KB1R b
            // KQkq - 0 8";
            // String fenMistakeTwo = "r1b2rk1/1pQ3pp/p3p3/4BpqP/4p3/8/PPP1PPP1/3RKB1R b K -
            // 0 14";
            // String fen = "1rbq1rk1/p1b1nppp/1p2p3/8/1B1pN3/P2B4/1P3PPP/2RQ1R1K w - - 0
            // 1";
            // String fen = "r1bqk1nr/pppp1ppp/2n5/2b1p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq -
            // 4 4";
            /* Run on 45 sec time increments */
            String KaufmanOne = "1rbq1rk1/p1b1nppp/1p2p3/8/1B1pN3/P2B4/1P3PPP/2RQ1R1K w - - 0 1"; //Nf6+
            String KaufmanTwo = "3r2k1/p2r1p1p/1p2p1p1/q4n2/3P4/PQ5P/1P1RNPP1/3R2K1 b - - 0 1"; //Nxd4 5.6 passed on 10s
            String KaufmanThree = "3r2k1/1p3ppp/2pq4/p1n5/P6P/1P6/1PB2QP1/1K2R3 w - - 0 1"; //Rd1

            playingBoard.setBoardFEN(KaufmanOne);
            // playingBoard.setBoardStartingPos();
            /* The starting and ending squares */
            short move;
            /* Prompt and set duration. */
            pen.println("Enter starting duration:");
            input = eyes.nextLine();
            Duration duration = Duration.ofMillis(Integer.parseInt(input));

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
                // MultiLayerNetwork tars = TARSCNN.loadModel(new File("TARS-V2.9.zip"));

                /* Create the MCT */
                MCTMin mct = new MCTMin(playingBoard);
                if (playingBoard.numPieces() < 6) {
                    JavaTablebaseBridge bridge = new JavaTablebaseBridge();
                    GameState board = bridge.probeBestMove(playingBoard);
                    if (board == null) {
                        pen.println("Game Over.");
                        break;
                    } else {
                        playingBoard = board;
                    }
                } else {
                    CNNode node = mct.search(duration, true);
                    if (node == null) {
                        pen.println("Game Over.");
                        break;
                    }
                    playingBoard = node.state;
                }
                playingBoard.printBoard();
                pen.println("Move:");
                input = eyes.nextLine();
                move = UIUtils.uciToMove(input);

                pen.println("Duration to run:");
                input = eyes.nextLine();
                duration = Duration.ofMillis(Integer.parseInt(input));
                pen.print("\n----------------\n");
                playingBoard = MoveGen.applyMove(move, playingBoard);

                playingBoard.printBoard();
            }
        }
    } // main(String[])
} // TARS