package utils.MoveGeneration;

import java.util.Arrays;
/**
 * An updated version of board that stores the game state in bitboards.
 * 
 * @author Sebastian Manza
 */
public class GameState {
    private static final int WKING = 0;
    private static final int BKING = 1;
    private static final int WQUEEN = 2;
    private static final int BQUEEN = 3;
    private static final int WROOKS = 4;
    private static final int BROOKS = 5;
    private static final int WBISHOPS = 6;
    private static final int BBISHOPS = 7;
    private static final int WKNIGHTS = 8;
    private static final int BKNIGHTS = 9;
    private static final int WPAWNS = 10;
    private static final int BPAWNS = 11;
    private static final int WPIECES = 12;
    private static final int BPIECES = 13;
    private static final int ALLPIECES = 14;

    /** The bitboards */
    private long[] bitBoards = new long[15];

    /** The turn color */
    private boolean turnColor;

    /** The engine color */
    private boolean engineColor;

    /**
     * Build a new Game State.
     * @param turnColor The color of the current turn (true for white, false for black)
     * @param engineColor The color of the engine.
     */
    public GameState(boolean turnColor, boolean engineColor) {
        this.turnColor = turnColor;
        this.engineColor = engineColor;
    } //GameState(boolean, boolean)

    /**
     * Set the starting position of the bitBoards.
     */
    public void setBoardStartingPos() {
        bitBoards[BPAWNS] = 0b0000000011111111000000000000000000000000000000000000000000000000L;
        bitBoards[WPAWNS] = 0b0000000000000000000000000000000000000000000000001111111100000000L;
    
        bitBoards[BKNIGHTS] = 0b0100001000000000000000000000000000000000000000000000000000000000L;
        bitBoards[WKNIGHTS]= 0b0000000000000000000000000000000000000000000000000000000001000010L;
    
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

        bitBoards[ALLPIECES] = bitBoards[WPIECES] & bitBoards[BPIECES];
    } //setBoardStartingPos()

    /**
     * Generate all possible nextmoves from the current GameState.
     * @return a list of moves(represented as 16-bit integers)
     */
    public short[] nextMoves() {
        short[] legalMoves = new short[256];
        int numMoves = 0;

        /* Set the appropriate bit boards */
        long turnBoard = (this.turnColor) ? bitBoards[WPIECES] : bitBoards[BPIECES];
        long oppBoard = (this.turnColor) ? bitBoards[BPIECES] : bitBoards[WPIECES];
        long knights = (this.turnColor) ? bitBoards[WKNIGHTS] : bitBoards[BKNIGHTS];
        long kings = (this.turnColor) ? bitBoards[WKING] : bitBoards[BKING];
        long pawns = (this.turnColor) ? bitBoards[WPAWNS] : bitBoards[BPAWNS];
        long[] pawnQuiets = (this.turnColor) ? PawnMoves.pawnQuietsW : PawnMoves.pawnQuietsB;
        long[] pawnCaptures = (this.turnColor) ? PawnMoves.pawnCapturesW : PawnMoves.pawnCapturesB;

        /* Check if there is anything on the knight board. */
        while (knights != 0) {
            int knightSquare = Long.numberOfTrailingZeros(knights);
            knights &= (knights - 1);

            /* Generate moves for the knight. It can't end on our piece. */
            long attackBoard = KnightMoves.knightAttacks[knightSquare] & ~turnBoard;

            /* As long as there are squares to be attacked, add them. */
            while (attackBoard != 0) {
                legalMoves[numMoves++] = createMove(knightSquare, Long.numberOfTrailingZeros(attackBoard), 0, 0);
                
                /* Clear the least significant bit, since we just generated that move. */
                attackBoard &= (attackBoard - 1);
            } //while
        }// while

        /* How about kings? */
        while (kings != 0) {
            int kingSquare = Long.numberOfTrailingZeros(kings);
            kings &= (kings - 1);

            /* Generate king moves. It can't eat our pieces. */
            long attackBoard = KingMoves.kingAttacks[kingSquare] & ~turnBoard;

            while (attackBoard != 0) {
                legalMoves[numMoves++] = createMove(kingSquare, Long.numberOfTrailingZeros(kings), 0, 0);
                attackBoard &= (attackBoard - 1);
            } //while
        } //while

        /* Now pawns! They have some special characteristics(starting square, promotions, captures, en passant)*/
        while(pawns != 0) {
            /* Find the pawns to look at. */
            int pawnSquare = Long.numberOfTrailingZeros(pawns);
            pawns &= pawns - 1;


            /* If the move is a quiet one (not a capture), there can't be any piece in front of it.*/
            long attackBoard = pawnQuiets[pawnSquare] & ~bitBoards[ALLPIECES];
            
            /* Join the 'quiet' moves with the captures. To capture, there must  */
            attackBoard |= (pawnCaptures[pawnSquare] & oppBoard);
            while(attackBoard != 0) {
                int endSquare = Long.numberOfTrailingZeros(attackBoard);

                /* We have to keep in mind promotions for our last 8 squares.*/
                if (endSquare > 56) {
                    /* There are four different pieces our pawn could promote to. */
                    for (int promotions = 0; promotions < 4; promotions++) {
                        legalMoves[numMoves++] = createMove(pawnSquare, endSquare, promotions, PawnMoves.PROMOTION_FLAG);
                    } //for
                } else {
                    legalMoves[numMoves++] = createMove(pawnSquare, endSquare, 0, 0);
                } //if/else
                
                /* Remove the last significant bit from the attack board. */
                attackBoard &= (attackBoard - 1);
            } //while
        } //while
        /* Return a dynamically sized Array */
        return Arrays.copyOfRange(legalMoves, 0, numMoves);
    } //nextMoves()

    public short createMove(int origin, int destination, int promotionType, int flag) {
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
    } //createMove

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

