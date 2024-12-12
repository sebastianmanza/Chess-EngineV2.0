package utils.MoveGeneration;

/**
 * A class that generates all slide moves needed. Will hopefully be updated to magic bitboards at a later date.
 * 
 * @author Sebastian Manza
 * 
 * How it works:
 * 0 0 0 X 0 0 0 0
 * 0 0 0 X 0 0 0 0
 * 0 0 0 X 0 0 0 0
 * X X X 0 X X X X
 * 0 0 0 X 0 0 0 0   The relevant board (rook on e5)
 * 0 0 0 X 0 0 0 0
 * 0 0 0 X 0 0 0 0
 * 0 0 0 X 0 0 0 0
 * 
 * 0 0 0 0 0 0 0 0
 * 0 X 0 X 0 X 0 0
 * 0 0 X X X 0 0 0
 * 0 X X 0 X X X 0
 * 0 0 X X X 0 0 0   Slide move blocker mask
 * 0 X 0 X 0 X 0 0
 * 0 0 0 X 0 0 X 0
 * 0 0 0 0 0 0 0 0
 * 
 * 0 0 0 0 0 0 0 0
 * 0 0 0 X 0 0 0 0
 * 0 0 0 0 0 0 0 0
 * 0 X X 0 0 0 0 0
 * 0 0 0 X 0 0 0 0   End number of blockers
 * 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0
 * 
 * 0 0 0 X 0 0 0 0
 * 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0   Behind board 1 (impossible move)
 * 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0
 * 0 0 0 0 0 0 0 0
 * 
 */
public class SlideMoves {
    private static final int[] QUEEN_DIRECTIONS = { -9, -8, -7, -1, 1, 7, 8, 9 };
    private static final int[] ROOK_DIRECTIONS = { -8, -1, 1, 8};
    private static final int[] BISHOP_DIRECTIONS = { -9, -7, 7, 9};
    public static long[] QueenAttacks = generateQueenAttacks();
    public static long[] RookAttacks = generateRookAttacks();
    public static long[] BishopAttacks = generateBishopAttacks();
    public static long[] Blockers = generateBlockers(); // same as queen attacks but doesn't go fully to edge
    public static long[][] Behind = generateBehind(); // A bitboard with the first index representing the square and the second the single square.

    public static long slideAttacks(int square, long occupied, long[] pieceType) {
        long targets = pieceType[square];

        /* As long as there are relevant blockers left on the square: check each one */
        for (long bitBoard = (occupied & Blockers[square]); bitBoard != 0; bitBoard &= (bitBoard - 1)) {
            int blockerSquare = Long.numberOfTrailingZeros(bitBoard);

            /* Remove everything behind the blocker */
            targets &= ~Behind[square][blockerSquare];
        } //for
        return targets;

    } //queenAttacks

    public static long rookAttacks(int square, long occupied) {
        long targets = RookAttacks[square];
        long blockers = RookAttacks[square] & Blockers[square];

        /* As long as there are relevant blockers left on the square: check each one */
        for (long bitBoard = (occupied & blockers); bitBoard != 0; bitBoard &= (bitBoard - 1)) {
            int blockerSquare = Long.numberOfTrailingZeros(bitBoard);

            /* Remove everything behind the blocker */
            targets &= ~Behind[square][blockerSquare];
        } //for
        return targets;

    } //queenAttacks

    public static long bishopAttacks(int square, long occupied) {
        long targets = BishopAttacks[square];
        long blockers = BishopAttacks[square] & Blockers[square];

        /* As long as there are relevant blockers left on the square: check each one */
        for (long bitBoard = (occupied & blockers); bitBoard != 0; bitBoard &= (bitBoard - 1)) {
            int blockerSquare = Long.numberOfTrailingZeros(bitBoard);

            /* Remove everything behind the blocker */
            targets &= ~Behind[square][blockerSquare];
        } //for
        return targets;

    } //queenAttacks

    private static long[] generateBlockers() {
        long[] blockers = new long[64];
        for (int square = 0; square < 64; square++) {
            long bitBoard = 0L;
            for (int dir = 0; dir < 8; dir++) {
                bitBoard |= createShorterRay(square, QUEEN_DIRECTIONS[dir]);
            } //for
            blockers[square] = bitBoard;
        } //for
        return blockers;
    } //generateBlockers()

    private static long[] generateQueenAttacks() {
        long[] queenAttacks = new long[64];
        for (int square = 0; square < 64; square++) {
            long bitBoard = 0L;
            for (int dir = 0; dir < 8; dir++) {
                bitBoard |= createRay(square, QUEEN_DIRECTIONS[dir]);
            } //for
            queenAttacks[square] = bitBoard;
        } //for
        return queenAttacks;
    }

    private static long[] generateRookAttacks() {
        long[] rookAttacks = new long[64];
        for (int square = 0; square < 64; square++) {
            long bitBoard = 0L;
            for (int dir = 0; dir < 4; dir++) {
                bitBoard |= createRay(square, ROOK_DIRECTIONS[dir]);
            } //for
            rookAttacks[square] = bitBoard;
        } //for
        return rookAttacks;
    }

    private static long[] generateBishopAttacks() {
        long[] bishopAttacks = new long[64];
        for (int square = 0; square < 64; square++) {
            long bitBoard = 0L;
            for (int dir = 0; dir < 4; dir++) {
                bitBoard |= createRay(square, BISHOP_DIRECTIONS[dir]);
            } //for
            bishopAttacks[square] = bitBoard;
        } //for
        return bishopAttacks;
    }


    private static long[][] generateBehind() {
        long[][] behind = new long[64][64];
        for (int square = 0; square < 64; square++) {
            for (int blocker = 0; blocker < 64; blocker++) {
                int direction = direction(square, blocker);
                if (direction != 0) {
                    behind[square][blocker] = createRay(blocker, direction);
                }
            }
        }
        return behind;
    }

    private static int direction(int square, int blocker) {
        int rowDiff = (blocker / 8) - (square / 8);
        int colDiff = (blocker % 8) - (square % 8);
    
        if (rowDiff == 0 && colDiff > 0) return 1;       // Same row, moving right
        if (rowDiff == 0 && colDiff < 0) return -1;      // Same row, moving left
        if (colDiff == 0 && rowDiff > 0) return 8;       // Same column, moving up
        if (colDiff == 0 && rowDiff < 0) return -8;      // Same column, moving down
        if (Math.abs(rowDiff) == Math.abs(colDiff)) {
            if (rowDiff > 0 && colDiff > 0) return 9;    // Diagonal, up-right
            if (rowDiff > 0 && colDiff < 0) return 7;    // Diagonal, up-left
            if (rowDiff < 0 && colDiff > 0) return -7;   // Diagonal, down-right
            if (rowDiff < 0 && colDiff < 0) return -9;   // Diagonal, down-left
        }
    
        return 0; // Not in the same line
    }
    private static long createRay(int startSq, int dir) {
        long ray = 0L;
        int sq = startSq + dir;
        int row = sq / 8;
        int col = sq % 8;
        while (row < 8 && row >= 0 && col < 8 && col >= 0) {
            ray |= (1L << sq);
            sq += dir;
            row = sq / 8;
            col = sq % 8;
        }
        return ray;
    }

    private static long createShorterRay(int startSq, int dir) {
        long ray = 0L;
        int sq = startSq + dir;
        int row = sq / 8;
        int col = sq % 8;
        while (row < 7 && row > 0 && col < 7 && col > 0) {
            ray |= (1L << sq);
            sq += dir;
            row = sq / 8;
            col = sq % 8;
        }
        return ray;
    }

    
} //SlideMoves
