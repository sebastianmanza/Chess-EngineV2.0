package utils.MoveGeneration;

/**
 * Stores everything to do with the act of actually moving a piece.
 * Everything is done with array lookups for speed.
 */
public class MoveGen {

    /** A list of all possible moves, as short[start][end] */
    public static short[][] moves = generatePossibleMoves();
    /**
     * An array that returns the proper part of the move needed. long[move][type] (0
     * = start, 1 = end, 2 = promtype or other flags, 3 = promflag)
     */
    public static long[][] moveParts = generateMoveParts();

    public static long[] castleChecks = generateCastleChecks();


    /**
     * Generates all of the possible moves.
     * 
     * @return a 2d array with every start and end referencing the move that they
     *         are.
     */
    private static short[][] generatePossibleMoves() {
        short[][] mov = new short[64][64];
        for (int start = 0; start < 64; start++) {
            for (int end = 0; end < 64; end++) {
                mov[start][end] = createMove(start, end, 0, 0);
            } // for
        } // for
        return mov;
    } // generatePossibleMoves

    private static long[] generateCastleChecks() {
        long[] checks = new long[4];
        checks[0] = BitBoardUtils.setBit(5) | BitBoardUtils.setBit(6);
        checks[1] = BitBoardUtils.setBit(3) | BitBoardUtils.setBit(2) | BitBoardUtils.setBit(1);
        checks[2] = BitBoardUtils.setBit(61) | BitBoardUtils.setBit(62);
        checks[3] = BitBoardUtils.setBit(59) | BitBoardUtils.setBit(58) | BitBoardUtils.setBit(57);
        return checks;
    }
    /**
     * Create a move.
     * 
     * @param origin        the origin square
     * @param destination   the destination square
     * @param promotionType the type of promotion (if it is one), or another flag
     * @param flag          the promotion flag(0 or 1)
     * @return the move in short form
     */
    public static short createMove(int origin, int destination, int promotionType, int flag) {
        short move = 0;

        /* Add destination at bits 0-5 */
        move |= (destination & 0b111111);

        /* Origin at bits 6-12 */
        move |= ((origin & 0b111111) << 6);

        /* Promotion type at bits 13-14 */
        move |= ((promotionType & 0b11) << 12);

        /* Flags for en passant, promotion, and Castling */
        move |= ((flag & 0b11) << 14);

        return move;
    } // createMove

    /**
     * Get the parts of a move and create an array storing (for speed of lookup)
     * 
     * @return a 2d long array representing the move parts.
     */
    private static long[][] generateMoveParts() {
        long[][] parts = new long[Short.MAX_VALUE][4];
        for (short move = 0; move < parts.length; move++) {
            parts[move][0] = BitBoardUtils.setBit((move >> 6) & 0b111111);
            parts[move][1] = BitBoardUtils.setBit(move & 0b111111);
            parts[move][2] = (move >> 12) & 0b11;
            parts[move][3] = (move >> 14) & 0b11;
        } // for
        return parts;
    } // generateMoveParts

    /**
     * Apply a move to a gamestate
     * 
     * @param move      The move to apply
     * @param prevState The previous gamestate
     * @return a new gamestate with the move applied
     * @throws Exception if the move is impossible (nothing at start square)
     */
    public static GameState applyMove(short move, GameState prevState) throws Exception {
        GameState state = new GameState(prevState);
        long origMask = moveParts[move][0];
        long destMask = moveParts[move][1];

        int pieceType = -1;
        /*
         * We want to exclude the bitboards containing multiple piecetypes from our
         * search
         */
        for (int i = 0; i < 12; i++) {
            if ((state.bitBoards[i] & origMask) != 0) {
                pieceType = i;
                break;
            } // if
        } // for
        if (pieceType == -1) {
            throw new Exception("Move cannot be applied, nothing at start square.");
        } // if

        /* Check if we need to remove castling rights. */
        if (pieceType == GameState.WKING) {
            state.whiteKingSide = false;
            state.whiteQueenSide = false;
        } else if (pieceType == GameState.BKING) {
            state.blackKingSide = false;
            state.blackQueenSide = false;
        } //if
        int origSquare = Long.numberOfTrailingZeros(origMask);

        switch (origSquare) {
            case 0:
                state.whiteQueenSide = false;
                break;
            case 7:
                state.whiteKingSide = false;
                break;
            case 56:
                state.blackQueenSide = false;
                break;
            case 63:
                state.blackKingSide = false;
                break;
            default:
                break;
        }

        


        int turnBoard = (state.turnColor) ? GameState.WPIECES : GameState.BPIECES;
        int oppBoard = (state.turnColor) ? GameState.BPIECES : GameState.WPIECES;

        /* Cycle through the bitboards and clear the destination square */
        for (int i = 0; i < 12; i++) {
            state.bitBoards[i] &= ~destMask;
        } // for
        /* Modify the other important bitboards */
        state.bitBoards[pieceType] |= destMask;
        state.bitBoards[turnBoard] |= destMask;
        state.bitBoards[pieceType] &= ~origMask;
        state.bitBoards[turnBoard] &= ~origMask;
        state.bitBoards[GameState.ALLPIECES] &= ~origMask;
        state.bitBoards[GameState.ALLPIECES] |= destMask;
        state.bitBoards[oppBoard] &= ~destMask;

    /* Handle En Passant */
    if (state.enPassant != -1) {
        if (pieceType == GameState.WPAWNS || pieceType == GameState.BPAWNS) {
        int originSquare = Long.numberOfTrailingZeros(origMask);
        int destSquare = Long.numberOfTrailingZeros(destMask);

        if (destSquare == state.enPassant) {
            int capturedSquare = destSquare + (state.turnColor ? -8 : 8);
            long capturedMask = BitBoardUtils.setBit(capturedSquare);

            state.bitBoards[state.turnColor ? GameState.BPAWNS : GameState.WPAWNS] &= ~capturedMask;
            state.bitBoards[oppBoard] &= ~capturedMask;
            state.bitBoards[GameState.ALLPIECES] &= ~capturedMask;
        }
        } 
    } 
        /* Handle promotion moves */
        if (moveParts[move][3] == PawnMoves.PROMOTION_FLAG) {
            int promType = (int) moveParts[move][2];
            if (state.turnColor) {
                state.bitBoards[GameState.WPAWNS] &= ~destMask;
                switch (promType) {
                    case 0 -> state.bitBoards[GameState.WKNIGHTS] |= destMask;
                    case 1 -> state.bitBoards[GameState.WBISHOPS] |= destMask;
                    case 2 -> state.bitBoards[GameState.WROOKS] |= destMask;
                    case 3 -> state.bitBoards[GameState.WQUEEN] |= destMask;
                } // switch
            } else {
                state.bitBoards[GameState.BPAWNS] &= ~destMask;
                switch (promType) {
                    case 0 -> state.bitBoards[GameState.BKNIGHTS] |= destMask;
                    case 1 -> state.bitBoards[GameState.BBISHOPS] |= destMask;
                    case 2 -> state.bitBoards[GameState.BROOKS] |= destMask;
                    case 3 -> state.bitBoards[GameState.BQUEEN] |= destMask;
                } // switch
            } // if/else
            state.enPassant = -1;
        } // if its a promotion
        else {
            if (moveParts[move][2] == KingMoves.CASTLE_FLAG) {
                /* Kingside castle (W) */
                if (moveParts[move][1] == BitBoardUtils.setBit(6)) {
                    long clearMask = ~BitBoardUtils.setBit(7);
                    state.bitBoards[GameState.WROOKS] &= clearMask;
                    state.bitBoards[GameState.WPIECES] &= clearMask;
                    long addMask = BitBoardUtils.setBit(5);
                    state.bitBoards[GameState.WROOKS] |= addMask;
                    state.bitBoards[GameState.WPIECES] |= addMask;
                } else if (moveParts[move][1] == BitBoardUtils.setBit(62)) {
                    long clearMask = ~BitBoardUtils.setBit(63);
                    state.bitBoards[GameState.BROOKS] &= clearMask;
                    state.bitBoards[GameState.BPIECES] &= clearMask;
                    long addMask = BitBoardUtils.setBit(61);
                    state.bitBoards[GameState.BROOKS] |= addMask;
                    state.bitBoards[GameState.BPIECES] |= addMask;
                } else if (moveParts[move][1] == BitBoardUtils.setBit(2)) {
                    long clearMask = ~BitBoardUtils.setBit(0);
                    state.bitBoards[GameState.WROOKS] &= clearMask;
                    state.bitBoards[GameState.WPIECES] &= clearMask;
                    long addMask = BitBoardUtils.setBit(3);
                    state.bitBoards[GameState.WROOKS] |= addMask;
                    state.bitBoards[GameState.WPIECES] |= addMask;
                } else if (moveParts[move][1] == BitBoardUtils.setBit(58)) {
                    long clearMask = ~BitBoardUtils.setBit(56);
                    state.bitBoards[GameState.BROOKS] &= clearMask;
                    state.bitBoards[GameState.BPIECES] &= clearMask;
                    long addMask = BitBoardUtils.setBit(59);
                    state.bitBoards[GameState.BROOKS] |= addMask;
                    state.bitBoards[GameState.BPIECES] |= addMask;
                } // if/else

                state.bitBoards[GameState.ALLPIECES] = state.bitBoards[GameState.WPIECES]
                        | state.bitBoards[GameState.BPIECES];
            } // castlingmoves
            /* If it was a starting pawn move, the en passant flag should be raised. */
            if (moveParts[move][2] == PawnMoves.EN_PASSANT_FLAG) {
                if (origSquare < 16) {
                    state.enPassant = origSquare + 8;
                } else {
                    state.enPassant = origSquare - 8;
                }
            } else {
                state.enPassant = -1;
            }
        } // if/else
        /* If it is a check, notify the user by returning null. */
        if (!state.isLegal(state.turnColor)) {
            return null;
        } // if
        /* Switch whos turn it is */
        state.turnColor = state.oppColor();
        return state;
    } // applyMove(short, GameState)

    public static boolean canCastleKingSide(GameState state) {
        boolean hasRights = state.turnColor ? state.whiteKingSide : state.blackKingSide;
        if (hasRights) {
            long piecesInWay = state.turnColor ? castleChecks[0] : castleChecks[2];
            if ((state.bitBoards[GameState.ALLPIECES] & piecesInWay) != 0) {
                return false;
            }
            if (!state.isLegal(state.turnColor)) {
                return false;
            }
            try {
                int kingSquare = state.turnColor ? 4 : 60; 
                if (applyMove(moves[kingSquare][kingSquare + 1], state) == null) {
                    return false;
                }
                if (applyMove(moves[kingSquare][kingSquare + 2], state) == null) {
                    return false;
                }
            } catch (Exception e) {
                System.err.println("Castling checks not working.");
            }
            return true;
        } // if

        return false;
    }

    public static boolean canCastleQueenSide(GameState state) {
        boolean hasRights = state.turnColor ? state.whiteQueenSide : state.blackQueenSide;
        long piecesInWay = state.turnColor ? castleChecks[1] : castleChecks[3];
        if (hasRights) {
            if (!state.isLegal(state.turnColor)) {
                return false;
            }
            if ((state.bitBoards[GameState.ALLPIECES] & piecesInWay) != 0) {
                return false;
            }
            try {
                int kingSquare = state.turnColor ? 4 : 60; 
                if (applyMove(moves[kingSquare][kingSquare - 1], state) == null) {
                    return false;
                }
                if (applyMove(moves[kingSquare][kingSquare - 2], state) == null) {
                    return false;
                }
            } catch (Exception e) {
                System.err.println("Castling checks not working.");
            }
            return true;
        } // if

        return false;
    }

} // MoveGen
