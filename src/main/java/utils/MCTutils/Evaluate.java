package utils.MCTutils;

import utils.MoveGeneration.GameState;

/**
 * Heuristically evaluates a position and returns a win chance [0-1]
 * 
 * @author Sebastian Manza
 */
public class Evaluate {

        /**
         * Evaluates the gamestate.
         * @param state the GameState to evaluate
         * @return a win chance between 0-1
         */
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

                /* Flip the score if the engine is black */
                if (!state.engineColor) {
                        score = 0 - score;
                } // if
                /* Use sigmoid to convert the score to a probability */
                return sigmoid(score / 5);
        } //evaluate

        /**
         * Apply the sigmoid function to a number
         * @param x the input number
         * @return a scaled function between 0 and 1 representing the win rate
         */
        public static double sigmoid(double x) {
                return 1 / (1 + Math.exp(-x));
        } //sigmoid
} //Evaluate
