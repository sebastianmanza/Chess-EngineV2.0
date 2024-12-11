package engine;

import utils.MoveGeneration.GameState;

/**
 * The main class of the engine. Will prompt the user, initialize the MCT, etc.
 * @author Sebastian Manza
 */
public class TARS {
    public static void main(String[] args) {
        GameState board = new GameState(true, true);
        board.setBoardStartingPos();


        short[] arr = board.nextMoves();
        for (int i = 0; i < arr.length; i++) {
            System.out.println(Integer.toBinaryString(arr[i]));
        }
        board.printBoard();

    } // main(String[])
} // TARS