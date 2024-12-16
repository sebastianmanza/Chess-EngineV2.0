package utils.MoveGeneration;

/**
 * Precalculate all possible king attacks into an attack map.
 * 
 * @author Sebastian Manza
 */
public class KingMoves {
     /**
     * An array of king attack squares, with the index corresponding to the
     * starting square.
     */
    public static long[] kingAttacks = generateAttackMapsKing();

    /** The flag in a move representing that it is a castle. */
    public static final int CASTLE_FLAG = 2;

    /**
     * Generate attack maps for kings for all 64 squares.
     * 
     * @return An array of 64 longs representing king attack maps.
     */
    public static long[] generateAttackMapsKing() {
        long[] attacks = new long[64];
        for (int square = 0; square < 64; square++) {
            attacks[square] = generateKingAttacks(square);
        } // for
        return attacks;
    } // generateAttackMapsking()

    /**
     * Generate attack map for a king on a specific square.
     * 
     * @param square The square index (0-63)
     * @return A long representing the attack map for a king
     */
    private static long generateKingAttacks(int square) {
        long bitboard = 0L;

        /* The 8 king moves */
        int[] moves = { -9, -8, -7, -1, 1, 7, 8, 9 };

        for (int move : moves) {
            int targetSquare = square + move;

            /* Check that it stays in bounds */
            if (targetSquare >= 0 && targetSquare < 64) {
                int srcRow = square / 8;
                int srcCol = square % 8;
                int tgtRow = targetSquare / 8;
                int tgtCol = targetSquare % 8;

                /* Check that it doesn't wrap around rows. */
                if (Math.abs(srcRow - tgtRow) <= 1 && Math.abs(srcCol - tgtCol) <= 1) {
                    bitboard |= (1L << targetSquare);
                } // if
            } // if
        } // for
        return bitboard;
    } // generatekingAttacks(int)
} //kingMoves
