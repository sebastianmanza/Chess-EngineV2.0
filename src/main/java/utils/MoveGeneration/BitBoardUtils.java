package utils.MoveGeneration;

/**
 * A class that provides utils for use with bitboards.
 * 
 * @author Sebastian Manza
 */
public class BitBoardUtils {
    /**
     * Set a bit at a square in a Bit Board.
     * 
     * @param square the square index to turn on
     * @return the bitboard (represented as a long).
     */
    public static long setBit(int square) {
        return 1L << square;
    } // setBit(int)

    /**
     * Check if a bit at a square index is set.
     * 
     * @param bitboard the bitboard to check
     * @param square   the square index to check
     * @return true if it is occupied, else false
     */
    public static boolean isBitSet(long bitboard, int square) {
        return (bitboard & setBit(square)) != 0;
    } // isBitSet(long, int)
} // BitBoardUtils