package utils.MoveGeneration;

import java.util.Arrays;

/**
 * An updated version of board that stores the game state in bitboards. (And
 * performs some operations)
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

    /** The castling rights. */
    public boolean whiteKingSide;
    public boolean whiteQueenSide;
    public boolean blackKingSide;
    public boolean blackQueenSide;

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
        this.blackKingSide = true;
        this.whiteKingSide = true;
        this.blackQueenSide = true;
        this.whiteQueenSide = true;
    } // GameState(boolean, boolean)

    public GameState(GameState state) {
        this.turnColor = state.turnColor;
        this.engineColor = state.engineColor;
        this.blackKingSide = state.blackKingSide;
        this.whiteKingSide = state.whiteKingSide;
        this.blackQueenSide = state.blackQueenSide;
        this.whiteQueenSide = state.whiteQueenSide;
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

            if (MoveGen.canCastleKingSide(this)) {
                legalMoves[numMov++] = MoveGen.createMove(kingSquare, kingSquare + 2, KingMoves.CASTLE_FLAG, 0);
            }

            if (MoveGen.canCastleQueenSide(this)) {
                legalMoves[numMov++] = MoveGen.createMove(kingSquare, kingSquare - 2, KingMoves.CASTLE_FLAG, 0);
            }

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

    /**
     * Return the opposite color of the engine.
     * 
     * @return true if white, else false
     */
    public boolean oppEngineColor() {
        return !this.engineColor;
    } // oppEngineColor

    /**
     * Return the victory points of a position.
     * 
     * @return 1 if a win, 0.5 if a draw, and 0 if a loss
     */
    public double vicPoints() {
        if (isLegal(this.engineColor) && !isLegal(this.oppEngineColor())) {
            return 1.0;
        } else if (!isLegal(this.engineColor) && isLegal(this.oppEngineColor())) {
            return 0.0;
        } // if
        return 0.5;
    } // vicPoints()

    /**
     * Count the number of pieces on the board.
     * 
     * @return an integer representing the number of pieces
     */
    public int numPieces() {
        return Long.bitCount(this.bitBoards[ALLPIECES]);
    } // numPieces

    /**
     * Check if a board state is legal (king is not in check)
     * 
     * @param color The color to check the legality of.
     * @return true if the position is legal for that color, else false
     */
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
        } // if
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
        } // if
        /*
         * Move the king like a pawn of the opposite color that is capturing. If it hits
         * opp color pawns its in check.
         */
        attackBoard = pawnCaps[kingSquare];
        if ((attackBoard & oppPawn) != 0) {
            return false;
        } // if
// if
        /*
         * Can't forget to move the other king or the whole thing doesn't work and
         * throws
         * an indexOutOfBoundException
         */
        attackBoard = KingMoves.kingAttacks[kingSquare];
        return (attackBoard & oppKing) == 0;
    } // isLegal(boolean)

    /**
     * Get the opposite color of the turn.
     * 
     * @return true if opposite color is white, else false
     */
    public boolean oppColor() {
        return !this.turnColor;
    } // oppColor

    /**
     * Print the board out.
     */
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
                } // if/else
            } // for
            System.out.println();
        } // for
        System.out.println();
    } // printBoard()

    /**
     * Set a board according to an FEN.
     * What a nice file format.
     * 
     * @param FEN the position represented as a FEN
     */
    public void setBoardFEN(String FEN) {

        /* Split the parts of the FEN */
        String[] parts = FEN.split(" ");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid FEN string: " + FEN);
        } // if

        /* Parse the board */
        this.setPieces(parts[0]);

        /* Parse the turnColor */
        this.turnColor = parts[1].equals("w");

        /* parse the caslting rights */
        this.setCastlingRights(parts[2]);
    } // setBoardFEN

    /**
     * Set the pieces as according to the portion of a FEN string that is piece
     * placement.
     * 
     * @param piecePlacement the string representing pieces placement
     */
    public void setPieces(String piecePlacement) {
        /* Clear all of the bitBoards */
        Arrays.fill(bitBoards, 0L);

        int square = 56;
        for (char c : piecePlacement.toCharArray()) {
            if (c == '/') {
                /*
                 * We move 16 because we have gone past the edge of the square and must go two
                 * rows back
                 */
                square -= 16;
            } else if (Character.isDigit(c)) {
                square += (c - '0'); // Skip empty squares
            } else {
                long bit = 1L << square;

                /* Add to the correct bitboards */
                switch (c) {
                    case 'P' -> bitBoards[WPAWNS] |= bit;
                    case 'N' -> bitBoards[WKNIGHTS] |= bit;
                    case 'B' -> bitBoards[WBISHOPS] |= bit;
                    case 'R' -> bitBoards[WROOKS] |= bit;
                    case 'Q' -> bitBoards[WQUEEN] |= bit;
                    case 'K' -> bitBoards[WKING] |= bit;
                    case 'p' -> bitBoards[BPAWNS] |= bit;
                    case 'n' -> bitBoards[BKNIGHTS] |= bit;
                    case 'b' -> bitBoards[BBISHOPS] |= bit;
                    case 'r' -> bitBoards[BROOKS] |= bit;
                    case 'q' -> bitBoards[BQUEEN] |= bit;
                    case 'k' -> bitBoards[BKING] |= bit;
                    default -> throw new IllegalArgumentException("Invalid piece: " + c);
                } // switch
                square++;
            } // if/else
        } // for
        /* Update the big bitboards */
        bitBoards[WPIECES] = bitBoards[WPAWNS] | bitBoards[WBISHOPS] | bitBoards[WKNIGHTS] | bitBoards[WROOKS]
                | bitBoards[WKING] | bitBoards[WQUEEN];
        bitBoards[BPIECES] = bitBoards[BPAWNS] | bitBoards[BBISHOPS] | bitBoards[BKNIGHTS] | bitBoards[BROOKS]
                | bitBoards[BKING] | bitBoards[BQUEEN];
        bitBoards[ALLPIECES] = bitBoards[WPIECES] | bitBoards[BPIECES];
    } // setPieces(String)

    /**
 * Set castling rights according to the FEN castling string.
 * 
 * @param castling the string representing castling rights
 */
private void setCastlingRights(String castling) {
    this.whiteKingSide = castling.contains("K");
    this.whiteQueenSide = castling.contains("Q");
    this.blackKingSide = castling.contains("k");
    this.blackQueenSide = castling.contains("q");
}
} // GameState
