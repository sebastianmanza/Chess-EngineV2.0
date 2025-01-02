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
    private MCTMin gameTree;

    private MCT gameTreeMCT;

    /**
     * Build an Engine instance, creating a new MCT.
     */
    public Engine() {
        System.setProperty("OMP_NUM_THREADS", "1");
        this.gameTree = new MCTMin(new GameState(true, true));
        this.gameTreeMCT = new MCT(new GameState(true, true));
    } // Engine()

    /**
     * Reset the engine to the initial state.
     */
    public void reset() {
        GameState initialState = new GameState(true, true);
        initialState.setBoardStartingPos();
        gameTree = new MCTMin(initialState);
        gameTreeMCT = new MCT(initialState);
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
        gameTree = new MCTMin(state);
        gameTreeMCT = new MCT(state);
    } // setPosition(fen)

    /**
     * Apply a move to the current position.
     * 
     * @param move the move (in UCI format)
     */
    public void applyMove(String move) {
        short applyMove = UIUtils.uciToMove(move);
        try {
            gameTree = new MCTMin(MoveGen.applyMove(applyMove, gameTree.root.state));
            gameTreeMCT = new MCT(MoveGen.applyMove(applyMove, gameTreeMCT.root.state));
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
                time = Duration.ofMillis((int) ((wtime - winc) * 0.03) + winc - 200);
            } else {
                time = Duration.ofMillis((int) ((btime - binc) * 0.03) + binc - 200);
            } // if
            if (movetime != 1) {
                time = Duration.ofMillis(movetime);
            } // if
            short move;
            int compared = Duration.ofMillis(6000).compareTo(time);
            if (compared < 0) {
                CNNode bestState = gameTree.search(time, false);
                if (bestState == null) {
                    return "0000";
                }
                move = bestState.move;
            } else {
                MCTNode bestStateMCT = gameTreeMCT.search(time, false);
                if (bestStateMCT == null) {
                    return "0000";
                }
                move = bestStateMCT.move;
            }
            
            return UIUtils.moveToUCI(move);
        } catch (Exception e) {
            return "0000"; // Fallback to null move
        } // try/catch
    } // search(int, int, int, int, int, int)
} // Engine