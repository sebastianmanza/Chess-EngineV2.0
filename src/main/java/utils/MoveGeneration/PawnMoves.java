package utils.MoveGeneration;

/**
 * The class generating predetermined pawn moves.
 * Bug note: A starting pawn move can't be premapped because there could be a piece in the way
 * However we can generate it during GameState.
 * 
 * Maybe we should add a new long[] representing just those starting move options, that we can then
 * check in gameState
 * 
 * @author Sebastian Manza
 */
public class PawnMoves {

  /** The promotion flag */
  public static final int PROMOTION_FLAG = 1;

  /**
   * An array of pawn quiet squares, with the index corresponding to the starting square.
   */
  public static long[] pawnQuietsW = generateQuietMapsWPawn();

  /**
   * An array of pawn quiet squares, with the index corresponding to the starting square.
   */
  public static long[] pawnQuietsB = generateQuietMapsBPawn();

  /**
   * An array of pawn capture squares, with the index corresponding to the starting square.
   */
  public static long[] pawnCapturesW = generateCaptureMapsWPawn();

  /**
   * An array of pawn capture squares, with the index corresponding to the starting square.
   */
  public static long[] pawnCapturesB = generateCaptureMapsBPawn();

  public static long[] pawnStartsW = generateStartMapsWPawn();

  public static long[] pawnStartsB = generateStartMapsBPawn();

  public static final boolean[] WHITE_START_ROW = new boolean[64];
  public static final boolean[] BLACK_START_ROW = new boolean[64];
  public static final boolean[] WHITE_PROMOTION_ROW = new boolean[64];
  public static final boolean[] BLACK_PROMOTION_ROW = new boolean[64];

  static {
    for (int square = 0; square < 64; square++) {
        WHITE_START_ROW[square] = (square >= 8 && square < 16);
        BLACK_START_ROW[square] = (square >= 48 && square < 56);
        WHITE_PROMOTION_ROW[square] = (square >= 56);
        BLACK_PROMOTION_ROW[square] = (square < 8);
    }
}

  /**
   * Generate capture maps for pawns for all 64 squares;
   * 
   * @return An array of 64 longs representing pawn capture maps
   */
  private static long[] generateCaptureMapsWPawn() {
    long[] attacks = new long[64];
    for (int square = 0; square < 64; square++) {
      attacks[square] = generateCaptureMoveWPawn(square);
    } //for
    return attacks;
  } //generateCaptureMapsWPawn

    /**
   * Generate capture maps for pawns for all 64 squares;
   * 
   * @return An array of 64 longs representing pawn capture maps
   */
  private static long[] generateStartMapsWPawn() {
    long[] attacks = new long[64];
    for (int square = 0; square < 64; square++) {
      attacks[square] = generateStartMovesWPawn(square);
    } //for
    return attacks;
  } //generateCaptureMapsWPawn

  private static long[] generateCaptureMapsBPawn() {
    long[] attacks = new long[64];
    for (int square = 0; square < 64; square++) {
      attacks[square] = generateCaptureMoveBPawn(square);
    } //for
    return attacks;
  } //generateCaptureMapsWPawn

      /**
   * Generate capture maps for pawns for all 64 squares;
   * 
   * @return An array of 64 longs representing pawn capture maps
   */
  private static long[] generateStartMapsBPawn() {
    long[] attacks = new long[64];
    for (int square = 0; square < 64; square++) {
      attacks[square] = generateStartMovesBPawn(square);
    } //for
    return attacks;
  } //generateCaptureMapsWPawn

  /**
   * Generate quiet maps for pawns for all 64 squares.
   * 
   * @return An array of 64 longs representing pawn quiet maps.
   */
  private static long[] generateQuietMapsWPawn() {
    long[] attacks = new long[64];
    for (int square = 0; square < 64; square++) {
      attacks[square] = generateQuietMoveWPawn(square);
    } // for
    return attacks;
  } // generateQuietMapsWPawn()

  /**
   * Generate quiet maps for pawns for all 64 squares.
   * 
   * @return An array of 64 longs representing pawn quiet maps.
   */
  private static long[] generateQuietMapsBPawn() {
    long[] attacks = new long[64];
    for (int square = 0; square < 64; square++) {
      attacks[square] = generateQuietMoveBPawn(square);
    } // for
    return attacks;
  } // generateQuietMapsBPawn()

  private static long generateQuietMoveWPawn(int square) {
    long bitboard = 0L;
    /* The direction the pawn can move in */
    int direction = 8;
    int endingSquare = square + direction;
    bitboard |= 1L << endingSquare;
    return bitboard;
  } // generateQuietMoveWPawn

  private static long generateStartMovesWPawn(int square) {
    long bitboard = 0L;
    /* The direction the pawn can move in */
    int direction = 16;
    bitboard |= 1L << (square + direction);
    return bitboard;
  } // generateQuietMoveWPawn

  private static long generateStartMovesBPawn(int square) {
    long bitboard = 0L;
    /* The direction the pawn can move in */
    int direction = 16;
    bitboard |= 1L << (square - direction);
    return bitboard;
  } // generateQuietMoveWPawn

  private static long generateQuietMoveBPawn(int square) {
    long bitboard = 0L;
    /* The direction the pawn can move in */
    int direction = -8;
    int endingSquare = square + direction;
    bitboard |= 1L << endingSquare;
    return bitboard;
  } // generateQuietMoveWPawn

  private static long generateCaptureMoveBPawn(int square) {
    long bitboard = 0L;
    /* The direction the pawn can capture in */
    int[] directions = { -9, -7 };
    for (int direction : directions) {
      int endingSquare = square + direction;
      int col = square % 8;
      int endingCol = endingSquare % 8;
      if (endingSquare < 64 && endingSquare >= 0 && (Math.abs(endingCol - col) == 1)) {
      bitboard |= 1L << endingSquare;
      } //if
    } //for
    return bitboard;
  } // generateQuietMoveWPawn

  private static long generateCaptureMoveWPawn(int square) {
    long bitboard = 0L;
    /* The direction the pawn can capture in */
    int[] directions = { 9, 7 };
    for (int direction : directions) {
      int endingSquare = square + direction;
      int col = square % 8;
      int endingCol = endingSquare % 8;
      if (endingSquare < 64 && endingSquare >= 0 && (Math.abs(endingCol - col) == 1)) {
      bitboard |= 1L << endingSquare;
      } //if
    } //for
    return bitboard;
  } // generateQuietMoveWPawn



} // PawnMoves
