package utils.MoveGeneration;

/**
 * A class that generates all slide moves needed. Will hopefully be updated to
 * magic bitboards at a later date.
 * 
 * @author Sebastian Manza
 */
public class SlideMoves {
    /** The square directions each slide piece can move. */
    private static final int[] QUEEN_DIRECTIONS = { -9, -8, -7, -1, 1, 7, 8, 9 };
    private static final int[] ROOK_DIRECTIONS = { -8, -1, 1, 8 };
    private static final int[] BISHOP_DIRECTIONS = { -9, -7, 7, 9 };

    /** Some arrays to store the attack boards. */
    public static long[] QueenAttacks = generateQueenAttacks();
    public static long[] RookAttacks = generateRookAttacks();
    public static long[] BishopAttacks = generateBishopAttacks();
    public static long[] Blockers = generateBlockers();
    public static long[][] Behind = generateBehind();

    /**
     * The main function in this class that is meant to be called.
     * 
     * @param square    the origin square
     * @param occupied  the occupied bitboard (bitboard of the opposite color)
     * @param pieceType the type of piece attack board to use
     * @return a long representation of the bitboard with blockers included.
     */
    public static long slideAttacks(int square, long occupied, long[] attackBoard) {
        long targets = attackBoard[square];
        long blockers = Blockers[square] & targets;

        /* As long as there are relevant blockers left on the square: check each one */
        for (long bitBoard = (occupied & blockers); bitBoard != 0; bitBoard &= (bitBoard - 1)) {
            int blockerSquare = Long.numberOfTrailingZeros(bitBoard);

            /* Remove everything behind the blocker */
            targets &= ~Behind[square][blockerSquare];
        } // for
        return targets;

    } // queenAttacks

    private static long[] generateBlockers() {
        long[] blockers = new long[64];
        for (int square = 0; square < 64; square++) {
            long bitBoard = 0L;
            for (int dir = 0; dir < 8; dir++) {
                bitBoard |= createShorterRay(square, QUEEN_DIRECTIONS[dir]);
            } // for
            blockers[square] = bitBoard;
        } // for
        return blockers;
    } // generateBlockers()

    private static long[] generateQueenAttacks() {
        long[] queenAttacks = new long[64];
        for (int square = 0; square < 64; square++) {
            long bitBoard = 0L;
            for (int dir = 0; dir < 8; dir++) {
                bitBoard |= createRay(square, QUEEN_DIRECTIONS[dir]);
            } // for
            queenAttacks[square] = bitBoard;
        } // for
        return queenAttacks;
    } // generateQueenAttacks()

    private static long[] generateRookAttacks() {
        long[] rookAttacks = new long[64];
        for (int square = 0; square < 64; square++) {
            long bitBoard = 0L;
            for (int dir = 0; dir < 4; dir++) {
                bitBoard |= createRay(square, ROOK_DIRECTIONS[dir]);
            } // for
            rookAttacks[square] = bitBoard;
        } // for
        return rookAttacks;
    }

    private static long[] generateBishopAttacks() {
        long[] bishopAttacks = new long[64];
        for (int square = 0; square < 64; square++) {
            long bitBoard = 0L;
            for (int dir = 0; dir < 4; dir++) {
                bitBoard |= createRay(square, BISHOP_DIRECTIONS[dir]);
            } // for
            bishopAttacks[square] = bitBoard;
        } // for
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

        if (rowDiff == 0 && colDiff > 0)
            return 1; // Same row, moving right
        if (rowDiff == 0 && colDiff < 0)
            return -1; // Same row, moving left
        if (colDiff == 0 && rowDiff > 0)
            return 8; // Same column, moving up
        if (colDiff == 0 && rowDiff < 0)
            return -8; // Same column, moving down
        if (Math.abs(rowDiff) == Math.abs(colDiff)) {
            if (rowDiff > 0 && colDiff > 0)
                return 9; // Diagonal, up-right
            if (rowDiff > 0 && colDiff < 0)
                return 7; // Diagonal, up-left
            if (rowDiff < 0 && colDiff > 0)
                return -7; // Diagonal, down-right
            if (rowDiff < 0 && colDiff < 0)
                return -9; // Diagonal, down-left
        }

        return 0; // Not in the same line
    }

    private static long createRay(int startSq, int dir) {
        long ray = 0L;
        int sq = startSq;
        int startRow = startSq / 8;
        int startCol = startSq % 8;

        while (true) {
            sq += dir;
            int row = sq / 8;
            int col = sq % 8;

            /* Check if the square is in bounds */
            if (row < 0 || row >= 8 || col < 0 || col >= 8)
                break;

            /* If it is moving horizontally, the row must stay the same.*/ 
            if (dir == 1 || dir == -1) {
                if (row != startRow) {
                    break;
                }
            }

            if (dir == -8 || dir == 8) {
                if (col != startCol) {
                    break;
                }
            }

            if (dir == -9 || dir == -7 || dir == 7 || dir == 9) {
                if (Math.abs(col - startCol) != Math.abs(row - startRow)) {
                    break;
                }
            }

            // Add the square to the ray
            ray |= (1L << sq);
        }

        return ray;
    }

    private static long createShorterRay(int startSq, int dir) {
        long ray = 0L;
        int sq = startSq;
        int startRow = startSq / 8;
        int startCol = startSq % 8;

        while (true) {
            sq += dir;
            int row = sq / 8;
            int col = sq % 8;

            /* Check if the square is in bounds */
            if (row < 0 || row >= 8 || col < 0 || col >= 8) {
                ray &= ~(1L << (sq - dir));
                break;
            }


            /* If it is moving horizontally, the row must stay the same.*/ 
            if (dir == 1 || dir == -1) {
                if (row != startRow) {
                    ray &= ~(1L << (sq - dir));
                    break;
                }
            }

            if (dir == -8 || dir == 8) {
                if (col != startCol) {
                    ray &= ~(1L << (sq - dir));
                    break;
                }
            }

            if (dir == -9 || dir == -7 || dir == 7 || dir == 9) {
                if (Math.abs(col - startCol) != Math.abs(row - startRow)) {
                    ray &= ~(1L << (sq - dir));
                    break;
                }
            }

            // Add the square to the ray
            ray |= (1L << sq);
        }

        return ray;
    }

    public static void main(String[] args) {
        long queenAttacksE4 = SlideMoves.RookAttacks[7]; // E4 is square 27
        long queenBlockers = SlideMoves.Blockers[7];
        long occupied = BitBoardUtils.setBit(23);
        long queenBlocked = SlideMoves.slideAttacks(7, occupied, RookAttacks);
        BitBoardUtils.printBitboard(queenBlocked);
        BitBoardUtils.printBitboard(queenAttacksE4);
        BitBoardUtils.printBitboard(queenBlockers);
    }

} // SlideMoves
