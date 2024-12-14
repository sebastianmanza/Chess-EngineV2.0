package utils.MoveGeneration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * An updated version of board that stores the game state in bitboards.
 * 
 * PRECOMPUTE CREATEMOVE createMove[64]
 * 
 * @author Sebastian Manza
 */
public class GameState {
    public static final int WKING = 0;
    public static final int BKING = 1;
    public static final int WQUEEN = 2;
    public static final int BQUEEN = 3;
    public static final int WROOKS = 4;
    public static final int BROOKS = 5;
    public static final int WBISHOPS = 6;
    public static final int BBISHOPS = 7;
    public static final int WKNIGHTS = 8;
    public static final int BKNIGHTS = 9;
    public static final int WPAWNS = 10;
    public static final int BPAWNS = 11;
    public static final int WPIECES = 12;
    public static final int BPIECES = 13;
    public static final int ALLPIECES = 14;

    /** The bitboards */
    public long[] bitBoards = new long[15];

    /** The turn color */
    public boolean turnColor;

    /** The engine color */
    public boolean engineColor;

    /**
     * Build a new Game State.
     * 
     * @param turnColor   The color of the current turn (true for white, false for
     *                    black)
     * @param engineColor The color of the engine.
     */
    public GameState(boolean turnColor, boolean engineColor) {
        this.turnColor = turnColor;
        this.engineColor = engineColor;
    } // GameState(boolean, boolean)

    public GameState(GameState state) {
        this.turnColor = state.turnColor;
        this.engineColor = state.engineColor;
        this.bitBoards = Arrays.copyOf(state.bitBoards, state.bitBoards.length);
    }

    /**
     * Set the starting position of the bitBoards.
     */
    public void setBoardStartingPos() {
        bitBoards[BPAWNS] = 0b0000000011111111000000000000000000000000000000000000000000000000L;
        bitBoards[WPAWNS] = 0b0000000000000000000000000000000000000000000000001111111100000000L;

        bitBoards[BKNIGHTS] = 0b0100001000000000000000000000000000000000000000000000000000000000L;
        bitBoards[WKNIGHTS] = 0b0000000000000000000000000000000000000000000000000000000001000010L;

        bitBoards[BBISHOPS] = 0b0010010000000000000000000000000000000000000000000000000000000000L;
        bitBoards[WBISHOPS] = 0b0000000000000000000000000000000000000000000000000000000000100100L;

        bitBoards[BROOKS] = 0b1000000100000000000000000000000000000000000000000000000000000000L;
        bitBoards[WROOKS] = 0b0000000000000000000000000000000000000000000000000000000010000001L;

        bitBoards[BKING] = 0b0001000000000000000000000000000000000000000000000000000000000000L;
        bitBoards[WKING] = 0b0000000000000000000000000000000000000000000000000000000000010000L;

        bitBoards[BQUEEN] = 0b0000100000000000000000000000000000000000000000000000000000000000L;
        bitBoards[WQUEEN] = 0b0000000000000000000000000000000000000000000000000000000000001000L;

        bitBoards[WPIECES] = 0b00000000000000000000000000000000000000000000001111111111111111L;
        bitBoards[BPIECES] = 0b11111111111111110000000000000000000000000000000000000000000000L;

        bitBoards[ALLPIECES] = 0b1111111111111111000000000000000000000000000000001111111111111111L;
    } // setBoardStartingPos()

    /**
     * Generate all possible nextmoves from the current GameState.
     * 
     * @return a list of pseudo-legal moves(represented as 16-bit integers)
     */
    public short[] nextMoves() {
        short[] legalMoves = new short[256];
        int numMov = 0;

        /* Set the appropriate bit boards */
        long turnBoard = (this.turnColor) ? bitBoards[WPIECES] : bitBoards[BPIECES];
        long oppBoard = (this.turnColor) ? bitBoards[BPIECES] : bitBoards[WPIECES];
        long knights = (this.turnColor) ? bitBoards[WKNIGHTS] : bitBoards[BKNIGHTS];
        long kings = (this.turnColor) ? bitBoards[WKING] : bitBoards[BKING];
        long pawns = (this.turnColor) ? bitBoards[WPAWNS] : bitBoards[BPAWNS];
        long rooks = (this.turnColor) ? bitBoards[WROOKS] : bitBoards[BROOKS];
        long bishops = (this.turnColor) ? bitBoards[WBISHOPS] : bitBoards[BBISHOPS];
        long queens = (this.turnColor) ? bitBoards[WQUEEN] : bitBoards[BQUEEN];
        long[] pawnQuiets = (this.turnColor) ? PawnMoves.pawnQuietsW : PawnMoves.pawnQuietsB;
        long[] pawnCaptures = (this.turnColor) ? PawnMoves.pawnCapturesW : PawnMoves.pawnCapturesB;
        long[] pawnStarts = (this.turnColor) ? PawnMoves.pawnStartsW : PawnMoves.pawnStartsB;
        boolean[] startingRow = (this.turnColor) ? PawnMoves.WHITE_START_ROW : PawnMoves.BLACK_START_ROW;
        boolean[] promotionRow = (this.turnColor) ? PawnMoves.WHITE_PROMOTION_ROW : PawnMoves.BLACK_START_ROW;
        /* Check if there is anything on the knight board. */
        while (knights != 0) {
            int knightSquare = Long.numberOfTrailingZeros(knights);
            knights &= (knights - 1);

            /* Generate moves for the knight. It can't end on our piece. */
            long attackBoard = KnightMoves.knightAttacks[knightSquare] & ~turnBoard;

            /* As long as there are squares to be attacked, add them. */
            while (attackBoard != 0) {
                legalMoves[numMov++] = MoveGen.moves[knightSquare][Long.numberOfTrailingZeros(attackBoard)];

                /* Clear the least significant bit, since we just generated that move. */
                attackBoard &= (attackBoard - 1);
            } // while
        } // while

        /* How about kings? */
        while (kings != 0) {
            int kingSquare = Long.numberOfTrailingZeros(kings);
            kings &= (kings - 1);

            /* Generate king moves. It can't eat our pieces. */
            long attackBoard = KingMoves.kingAttacks[kingSquare] & ~turnBoard;

            while (attackBoard != 0) {
                legalMoves[numMov++] = MoveGen.moves[kingSquare][Long.numberOfTrailingZeros(attackBoard)];
                attackBoard &= (attackBoard - 1);
            } // while
        } // while

        /*
         * Now pawns! They have some special characteristics(starting square,
         * promotions, captures, en passant)
         */
        while (pawns != 0) {
            /* Find the pawns to look at. */
            int pawnSquare = Long.numberOfTrailingZeros(pawns);
            pawns &= (pawns - 1);

            /*
             * If the move is a quiet one (not a capture), there can't be any piece in front
             * of it.
             */
            long attackBoard = pawnQuiets[pawnSquare] & ~bitBoards[ALLPIECES];

            /*
             * If there was nothing in the way before, and the pawn is on it's starting row,
             * add the start possibility.
             */
            if (attackBoard != 0 && startingRow[pawnSquare]) {
                attackBoard |= pawnStarts[pawnSquare] & ~bitBoards[ALLPIECES];
            } // if

            /*
             * Join the 'quiet' moves with the captures. To capture, there must be a piece
             * from the opponent in that slot.
             */
            attackBoard |= (pawnCaptures[pawnSquare] & oppBoard);
            while (attackBoard != 0) {
                int endSquare = Long.numberOfTrailingZeros(attackBoard);

                /* We have to keep in mind promotions for our last 8 squares. */
                if (promotionRow[endSquare]) {
                    /* There are four different pieces our pawn could promote to. */
                    for (int promotions = 0; promotions < 4; promotions++) {
                        short move = MoveGen.moves[pawnSquare][endSquare];
                                /* Promotion type at bits 13-14 */
                        move |= ((promotions & 0b11) << 12);
                        move |= ((PawnMoves.PROMOTION_FLAG & 0b11) << 14);
                        legalMoves[numMov++] = move;
                    } // for
                } else {
                    legalMoves[numMov++] = MoveGen.moves[pawnSquare][endSquare];
                }
                /* Remove the last significant bit from the attack board. */
                attackBoard &= (attackBoard - 1);
            } // while
        } // while

        /* Rooks: the first of the dreaded three slide moves. */
        while (rooks != 0) {
            /* Find the first rook. */
            int rookSquare = Long.numberOfTrailingZeros(rooks);
            rooks &= (rooks - 1);

            long attackBoard = ~turnBoard
                    & SlideMoves.slideAttacks(rookSquare, bitBoards[ALLPIECES], SlideMoves.RookAttacks);
            while (attackBoard != 0) {
                int endSquare = Long.numberOfTrailingZeros(attackBoard);
                legalMoves[numMov++] = MoveGen.moves[rookSquare][endSquare];
                attackBoard &= (attackBoard - 1);
            } // while
        } // while

        /* Bishops */
        while (bishops != 0) {
            /* Find the bishop */
            int bishopSquare = Long.numberOfTrailingZeros(bishops);
            bishops &= (bishops - 1);

            long attackBoard = ~turnBoard
                    & SlideMoves.slideAttacks(bishopSquare, bitBoards[ALLPIECES], SlideMoves.BishopAttacks);

            while (attackBoard != 0) {
                int endSquare = Long.numberOfTrailingZeros(attackBoard);
                legalMoves[numMov++] = MoveGen.moves[bishopSquare][endSquare];
                attackBoard &= (attackBoard - 1);
            }
        }
        /* Queens */
        while (queens != 0) {
            /* Find the bishop */
            int queenSquare = Long.numberOfTrailingZeros(queens);
            queens &= (queens - 1);

            long attackBoard = ~turnBoard
                    & SlideMoves.slideAttacks(queenSquare, bitBoards[ALLPIECES], SlideMoves.QueenAttacks);

            while (attackBoard != 0) {
                int endSquare = Long.numberOfTrailingZeros(attackBoard);
                legalMoves[numMov++] = MoveGen.moves[queenSquare][endSquare];
                attackBoard &= (attackBoard - 1);
            }
        }
        /* Return a dynamically sized Array */
        return Arrays.copyOfRange(legalMoves, 0, numMov);
    } // nextMoves()


    public boolean oppEngineColor() {
        return !this.engineColor;
    }

    public double vicPoints() {
        if (isLegal(this.engineColor) && !isLegal(this.oppEngineColor())) {
            return 1.0;
        } else if (!isLegal(this.engineColor) && isLegal(this.oppEngineColor())) {
            return 0.0;
        } // if
        return 0.5;
    }

    public int numPieces() {
        return Long.bitCount(this.bitBoards[ALLPIECES]);
    }

    public boolean isLegal(boolean color) {
        int kingColor = (color) ? WKING : BKING;
        int kingSquare = Long.numberOfTrailingZeros(this.bitBoards[kingColor]);
        long oppKnight = (kingColor == WKING) ? this.bitBoards[BKNIGHTS] : this.bitBoards[WKNIGHTS];
        long oppBishop = (kingColor == WKING) ? this.bitBoards[BBISHOPS] : this.bitBoards[WBISHOPS];
        long oppQueen = (kingColor == WKING) ? this.bitBoards[BQUEEN] : this.bitBoards[WQUEEN];
        long oppRook = (kingColor == WKING) ? this.bitBoards[BROOKS] : this.bitBoards[WROOKS];
        long oppPawn = (kingColor == WKING) ? this.bitBoards[BPAWNS] : this.bitBoards[WPAWNS];
        long oppKing = (kingColor == WKING) ? this.bitBoards[BKING] : this.bitBoards[WKING];
        long[] pawnCaps = (kingColor == WKING) ? PawnMoves.pawnCapturesW : PawnMoves.pawnCapturesB;
        if (kingSquare == 64) {
            return false;
        }
        /* Move the king like a knight. If it hits knights its in check. */
        long attackBoard = KnightMoves.knightAttacks[kingSquare];
        if ((attackBoard & oppKnight) != 0) {
            return false;
        } // if
        /* Move the king like a bishop. If it hits bishops or queens its in check. */
        attackBoard = SlideMoves.slideAttacks(kingSquare, this.bitBoards[ALLPIECES], SlideMoves.BishopAttacks);
        if ((attackBoard & (oppBishop | oppQueen)) != 0) {
            return false;
        } // if
        /* Move the king like a rook. If it hits rooks or queens its in check. */
        attackBoard = SlideMoves.slideAttacks(kingSquare, this.bitBoards[ALLPIECES], SlideMoves.RookAttacks);
        if ((attackBoard & (oppRook | oppQueen)) != 0) {
            return false;
        }
        /*
         * Move the king like a pawn of the opposite color that is capturing. If it hits
         * opp color pawns its in check.
         */
        attackBoard = pawnCaps[kingSquare];
        if ((attackBoard & oppPawn) != 0) {
            return false;
        }
        /*
         * Can't forget to move the other king or the whole thing doesn't work and
         * throws
         * an indexOutOfBoundException
         */
        attackBoard = KingMoves.kingAttacks[kingSquare];
        if ((attackBoard & oppKing) != 0) {
            return false;
        }
        return true;
    }

    public boolean oppColor() {
        return !this.turnColor;
    }

    public void printBoard() {
        for (int row = 7; row >= 0; row--) {
            for (int col = 0; col < 8; col++) {
                int square = row * 8 + col;

                if ((bitBoards[WPAWNS] & (1L << square)) != 0) {
                    System.out.print("\u2659 ");
                } else if ((bitBoards[BPAWNS] & (1L << square)) != 0) {
                    System.out.print("\u265F ");
                } else if ((bitBoards[WKNIGHTS] & (1L << square)) != 0) {
                    System.out.print("\u2658 ");
                } else if ((bitBoards[BKNIGHTS] & (1L << square)) != 0) {
                    System.out.print("\u265E ");
                } else if ((bitBoards[WBISHOPS] & (1L << square)) != 0) {
                    System.out.print("\u2657 ");
                } else if ((bitBoards[BBISHOPS] & (1L << square)) != 0) {
                    System.out.print("\u265D ");
                } else if ((bitBoards[WROOKS] & (1L << square)) != 0) {
                    System.out.print("\u2656 ");
                } else if ((bitBoards[BROOKS] & (1L << square)) != 0) {
                    System.out.print("\u265C ");
                } else if ((bitBoards[WQUEEN] & (1L << square)) != 0) {
                    System.out.print("\u2655 ");
                } else if ((bitBoards[BQUEEN] & (1L << square)) != 0) {
                    System.out.print("\u265B ");
                } else if ((bitBoards[WKING] & (1L << square)) != 0) {
                    System.out.print("\u2654 ");
                } else if ((bitBoards[BKING] & (1L << square)) != 0) {
                    System.out.print("\u265A ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

}
