package utils.MCTutils;

import java.time.Duration;

import utils.MoveGeneration.GameState;
import utils.MoveGeneration.MoveGen;
import utils.UserInterface.UIUtils;

/**
 * A wrapper for the MCT class designed to communicate with the UCI.
 */
public class Engine {

    private MCT gameTree;

    public Engine() {
        this.gameTree = new MCT(new GameState(true, true));
    }

    public void reset() {
        GameState initialState = new GameState(true, true);
        initialState.setBoardStartingPos();
        gameTree = new MCT(initialState);
    }

    public void setPosition(String fen) {
        GameState state = new GameState(true, true);
        if (fen.equals("startpos")) {
            state.setBoardStartingPos();
        } else {
            state.setBoardFEN(fen);
        }
        gameTree = new MCT(state);
    }

    public void applyMove(String move) {
        short applyMove = UIUtils.uciToMove(move);
        try {
            gameTree = new MCT(MoveGen.applyMove(applyMove, gameTree.root.state));
        } catch (Exception e) {
        }
    }

    public String search(int depth, int movetime, int wtime, int btime, int winc, int binc) {
        try {
            Duration time;
            if (gameTree.root.state.turnColor) {
                time = Duration.ofMillis((int) (wtime * 0.03) + winc);
            } else {
                time = Duration.ofMillis((int) (btime * 0.03) + binc);
            }
            if (movetime != 1) {
            time = Duration.ofMillis(movetime);
            }
            MCTNode bestState = gameTree.search(time, false);
            return bestState == null ? "0000" : UIUtils.moveToUCI(bestState.move);
        } catch (Exception e) {
            e.printStackTrace();
            return "0000"; // Fallback to null move
        }
    }

}