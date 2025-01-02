package utils.TablebaseUtils;

import utils.MoveGeneration.GameState;
import utils.MoveGeneration.MoveGen;

/**
 * A class that provides a bridge behind the tablebase and Java (Fathom, the method I used to extract the TBS
 * is written in C)
 * 
 * @author Sebastian Manza
 */
public class JavaTablebaseBridge {
    static {
        System.load("/Users/sebastianmanza/ChessEngineV2.0/Fathom/libFathomJNI.so");
        initializeTablebase("/Users/sebastianmanza/ChessEngineV2.0/src/main/java/Tablebase");
    }

    private static native void initializeTablebase(String path);

    /* Native method */
    public native int probeWDLNative(
            long white, long black, long kings, long queens, long rooks,
            long bishops, long knights, long pawns,
            int rule50, int castling, int ep, boolean turn);

    public double probeWDL(GameState state) {
        /* Grab the data from the gamestae */
        long white = state.bitBoards[GameState.WPIECES];
        long black = state.bitBoards[GameState.BPIECES];
        long kings = state.bitBoards[GameState.WKING] | state.bitBoards[GameState.BKING];
        long queens = state.bitBoards[GameState.WQUEEN] | state.bitBoards[GameState.BQUEEN];
        long rooks = state.bitBoards[GameState.WROOKS] | state.bitBoards[GameState.BROOKS];
        long bishops = state.bitBoards[GameState.WBISHOPS] | state.bitBoards[GameState.BBISHOPS];
        long knights = state.bitBoards[GameState.WKNIGHTS] | state.bitBoards[GameState.BKNIGHTS];
        long pawns = state.bitBoards[GameState.WPAWNS] | state.bitBoards[GameState.BPAWNS];
        int rule50 = 0;
        int castling = 0;
        int ep = 0;
        boolean turn = state.turnColor;

        /* Call the native method */
        int wdl = probeWDLNative(
                white, black, kings, queens, rooks, bishops, knights, pawns,
                rule50, castling, ep, turn);

        /* Translate the score */
        double score = switch (wdl) {
            case 0 -> 0.0;
            case 4 -> 1.0;
            default -> 0.5;
        };

        /* Change it to return engine score rather than turn */
        return (state.engineColor == turn) ? score : 1 - score;
    } // probeWDL(GameState)

    private static native long probeRootNative(
            long white, long black, long kings, long queens, long rooks,
            long bishops, long knights, long pawns,
            int rule50, int castling, int ep, boolean turn);

    public GameState probeBestMove(GameState state) {
        /* Extract bitboards and metadata from the GameState */
        long white = state.bitBoards[GameState.WPIECES];
        long black = state.bitBoards[GameState.BPIECES];
        long kings = state.bitBoards[GameState.WKING] | state.bitBoards[GameState.BKING];
        long queens = state.bitBoards[GameState.WQUEEN] | state.bitBoards[GameState.BQUEEN];
        long rooks = state.bitBoards[GameState.WROOKS] | state.bitBoards[GameState.BROOKS];
        long bishops = state.bitBoards[GameState.WBISHOPS] | state.bitBoards[GameState.BBISHOPS];
        long knights = state.bitBoards[GameState.WKNIGHTS] | state.bitBoards[GameState.BKNIGHTS];
        long pawns = state.bitBoards[GameState.WPAWNS] | state.bitBoards[GameState.BPAWNS];
        int rule50 = 0;
        int castling = 0;
        int ep = 0;
        boolean turn = state.turnColor;

        long move = probeRootNative(white, black, kings, queens, rooks, bishops, knights, pawns,
        rule50, castling, ep, turn);

        int fromSquare = (int)(move & 0x3F); // First 6 bits for source square
        int toSquare = (int)((move >> 6) & 0x3F);  // Next 6 bits for destination square
    
        /* Decode the promotion type */
        int promotion = (int)((move >> 12) & 0xF); // Promotion bits
        int promFlag = 0;
        if (promotion != 0) {
            promFlag = 1;
        }
        
        int promotionType = switch(promotion) {
            case 1 -> 3;
            case 2 -> 2;
            case 3 -> 1;
            case 4 -> 1;
            default -> 0;
        };

        /* Create the move */
        short bestMove = MoveGen.createMove(fromSquare, toSquare, promotionType, promFlag);
        GameState retState;
        try {
            retState = MoveGen.applyMove(bestMove, state);
        } catch (Exception e) {
            retState = null;
        }
        
        return retState;
    }
}
