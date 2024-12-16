package utils.MCTutils;

import utils.MoveGeneration.GameState;

public class Evaluate {

    public static double evaluate(GameState state) {
        double score = 0;

        /* Calculate the material balance */
        score += 1 * Long.bitCount(state.bitBoards[GameState.WPAWNS])
                - 1 * Long.bitCount(state.bitBoards[GameState.BPAWNS]);
        score += 3 * Long.bitCount(state.bitBoards[GameState.WKNIGHTS])
                - 3 * Long.bitCount(state.bitBoards[GameState.BKNIGHTS]);
        score += 3 * Long.bitCount(state.bitBoards[GameState.WBISHOPS])
                - 3 * Long.bitCount(state.bitBoards[GameState.BBISHOPS]);
        score += 5 * Long.bitCount(state.bitBoards[GameState.WROOKS])
                - 5 * Long.bitCount(state.bitBoards[GameState.BROOKS]);
        score += 9 * Long.bitCount(state.bitBoards[GameState.WQUEEN])
                - 9 * Long.bitCount(state.bitBoards[GameState.BQUEEN]);

        if (!state.engineColor) {
            score = 0 - score;
        } // if
          // Convert the score to a probability using the sigmoid function
        return sigmoid(score / 5);
    }

    public static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }
}
