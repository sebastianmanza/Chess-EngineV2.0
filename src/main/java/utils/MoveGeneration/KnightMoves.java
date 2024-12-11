package utils.MoveGeneration;

/**
 * Precalculate all possible knight attacks into an attack map.
 * 
 * @author Sebastian Manza
 */
public class KnightMoves {
     /**
     * An array of knight attack squares, with the index corresponding to the
     * starting square.
     */
    public static long[] knightAttacks = generateAttackMapsKnight();

    /**
     * Generate attack maps for knights for all 64 squares.
     * 
     * @return An array of 64 longs representing knight attack maps.
     */
    public static long[] generateAttackMapsKnight() {
        long[] attacks = new long[64];
        for (int square = 0; square < 64; square++) {
            attacks[square] = generateKnightAttacks(square);
        } // for
        return attacks;
    } // generateAttackMapsKnight()

    /**
     * Generate attack map for a knight on a specific square.
     * 
     * @param square The square index (0-63)
     * @return A long representing the attack map for a knight
     */
    private static long generateKnightAttacks(int square) {
        long bitboard = 0L;

        /* The 8 knight moves */
        int[] moves = { 15, 17, 10, 6, -15, -17, -10, -6 };

        for (int move : moves) {
            int targetSquare = square + move;

            /* Check that it stays in bounds */
            if (targetSquare >= 0 && targetSquare < 64) {
                int srcRow = square / 8;
                int srcCol = square % 8;
                int tgtRow = targetSquare / 8;
                int tgtCol = targetSquare % 8;

                /* Check that it doesn't wrap around rows. */
                if (Math.abs(srcRow - tgtRow) <= 2 && Math.abs(srcCol - tgtCol) <= 2) {
                    bitboard |= (1L << targetSquare);
                } // if
            } // if
        } // for
        return bitboard;
    } // generateKnightAttacks(int)
} //KnightMoves
