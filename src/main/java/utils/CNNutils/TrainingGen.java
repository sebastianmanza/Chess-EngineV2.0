package utils.CNNutils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import utils.MoveGeneration.GameState;

public class TrainingGen {

    public static INDArray createTensor(GameState state) {
        /* 13 planes (12 for pieces, 1 for turn color) */
        int planes = 13;
        int rows = 8;
        int cols = 8;
    
        /* Shape: [1, planes, rows, cols] (1 representing batch size, but currently dealing w single gamestates.)*/
        INDArray tensor = Nd4j.create(new int[]{1, planes, rows, cols}, 'c');
    
        /* Fill the array */
        for (int i = 0; i < 12; i++) {
            long bitboard = state.bitBoards[i];
            for (int square = 0; square < 64; square++) {
                int row = square / 8;
                int col = square % 8;
                tensor.putScalar(new int[]{0, i, row, col}, ((bitboard & (1L << square)) != 0 ? 1 : 0));
            }
        }
    
        /* Add the turn color to plane 13 */
        int turn = state.turnColor ? 1 : 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                tensor.putScalar(new int[]{0, 12, row, col}, turn);
            }
        }
    
        return tensor;
    }





    
}
