package utils.MCTutils;

import java.time.Duration;

import utils.MoveGeneration.GameState;
import utils.MoveGeneration.MoveGen;
import utils.UserInterface.UIUtils;

/**
 * A wrapper for the MCT class designed to communicate with the UCI. Behaves
 * more like an actual engine would.
 * 
 * @author Sebastian Manza
 */
public class Engine {

    /** The tree to do the work on (the real engine) */
    private MCT gameTree;

    /**
     * Build an Engine instance, creating a new MCT.
     */
    public Engine() {
        this.gameTree = new MCT(new GameState(true, true));
    } // Engine()

    /**
     * Reset the engine to the initial state.
     */
    public void reset() {
        GameState initialState = new GameState(true, true);
        initialState.setBoardStartingPos();
        gameTree = new MCT(initialState);
    } // reset()

    /**
     * Set the position using a fen.
     * 
     * @param fen the position input.
     */
    public void setPosition(String fen) {
        GameState state = new GameState(true, true);
        if (fen.equals("startpos")) {
            state.setBoardStartingPos();
        } else {
            state.setBoardFEN(fen);
        } // if/else
        gameTree = new MCT(state);
    } // setPosition(fen)

    /**
     * Apply a move to the current position.
     * 
     * @param move the move (in UCI format)
     */
    public void applyMove(String move) {
        short applyMove = UIUtils.uciToMove(move);
        try {
            gameTree = new MCT(MoveGen.applyMove(applyMove, gameTree.root.state));
        } catch (Exception e) {
        } // try/catch
    } // applyMove(String)

    /**
     * Search for the best move, based off of some paramaters.
     * 
     * @param depth    if searching by depth the depth to search
     * @param movetime the amount of time for the move, if entered manually
     * @param wtime    whites remaining time
     * @param btime    blacks remaining time
     * @param winc     whites time increment
     * @param binc     blacks time increment
     * @return a move in UCI format representing the best move.
     */
    public String search(int depth, int movetime, int wtime, int btime, int winc, int binc) {
        try {
            Duration time;
            if (gameTree.root.state.turnColor) {
                time = Duration.ofMillis((int) (wtime * 0.03) + winc);
            } else {
                time = Duration.ofMillis((int) (btime * 0.03) + binc);
            } // if
            if (movetime != 1) {
                time = Duration.ofMillis(movetime);
            } // if
            MCTNode bestState = gameTree.search(time, false);
            return bestState == null ? "0000" : UIUtils.moveToUCI(bestState.move);
        } catch (Exception e) {
            e.printStackTrace();
            return "0000"; // Fallback to null move
        } // try/catch
    } // search(int, int, int, int, int, int)
} // Engine