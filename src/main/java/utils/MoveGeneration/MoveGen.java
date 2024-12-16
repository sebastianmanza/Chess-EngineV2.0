package utils.MoveGeneration;

public class MoveGen {
    public static short[][] moves = generatePossibleMoves();
    public static long[][] moveParts = generateMoveParts();

    private static short[][] generatePossibleMoves() {
        short[][] mov = new short[64][64];
        for (int start = 0; start < 64; start++) {
            for (int end = 0; end < 64; end++) {
                mov[start][end] = createMove(start, end, 0, 0);
            }
        }
        return mov;
    }

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

    private static long[][] generateMoveParts() {
        long[][] moveParts = new long[Short.MAX_VALUE][4];
        for (short move = 0; move < moveParts.length; move++) {
            moveParts[move][0] = BitBoardUtils.setBit((move >> 6) & 0b111111);
            moveParts[move][1] = BitBoardUtils.setBit(move & 0b111111);
            moveParts[move][2] = (move >> 12) & 0b11;
            moveParts[move][3] = (move >> 14) & 0b11;
        } // for
        return moveParts;
    } // generateMoveParts

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
        }
        if (pieceType == -1) {
            throw new Exception("Move cannot be applied, nothing at start square.");
        }

        int turnBoard = (state.turnColor) ? GameState.WPIECES : GameState.BPIECES;
        int oppBoard = (state.turnColor) ? GameState.BPIECES : GameState.WPIECES;

        for (int i = 0; i < 12; i++) {
            state.bitBoards[i] &= ~destMask;
        }
        state.bitBoards[pieceType] |= destMask;
        state.bitBoards[turnBoard] |= destMask;
        state.bitBoards[pieceType] &= ~origMask;
        state.bitBoards[turnBoard] &= ~origMask;
        state.bitBoards[GameState.ALLPIECES] &= ~origMask;
        state.bitBoards[GameState.ALLPIECES] |= destMask;
        state.bitBoards[oppBoard] &= ~destMask;

        if (moveParts[move][3] == PawnMoves.PROMOTION_FLAG) {
            int promType = (int) moveParts[move][2];
            if (state.turnColor) {
                state.bitBoards[GameState.WPAWNS] &= ~destMask;
                switch (promType) {
                    case 0 -> state.bitBoards[GameState.WKNIGHTS] |= destMask;
                    case 1 -> state.bitBoards[GameState.WBISHOPS] |= destMask;
                    case 2 -> state.bitBoards[GameState.WROOKS] |= destMask;
                    case 3 -> state.bitBoards[GameState.WQUEEN] |= destMask;
                }
            } else {
                state.bitBoards[GameState.BPAWNS] &= ~destMask;
                switch (promType) {
                    case 0 -> state.bitBoards[GameState.BKNIGHTS] |= destMask;
                    case 1 -> state.bitBoards[GameState.BBISHOPS] |= destMask;
                    case 2 -> state.bitBoards[GameState.BROOKS] |= destMask;
                    case 3 -> state.bitBoards[GameState.BQUEEN] |= destMask;
                }
            }
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
                }

                state.bitBoards[GameState.ALLPIECES] = state.bitBoards[GameState.WPIECES]
                        | state.bitBoards[GameState.BPIECES];
            }
        }
        if (!state.isLegal(state.turnColor)) {
            return null;
        } // if
        state.turnColor = state.oppColor();
        return state;
    }

}
