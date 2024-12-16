package utils.UserInterface;

import utils.MoveGeneration.KingMoves;
import utils.MoveGeneration.MoveGen;
import utils.MoveGeneration.PawnMoves;

public class UIUtils {
    public static int toSquareIndex(String input) {
        int squareIndex = 0;
        squareIndex += ((input.charAt(0) - (int) 'a'));
        squareIndex += ((Character.getNumericValue(input.charAt(1) - 1)) * 8);
        return squareIndex;
    }

    public static short uciToMove(String move) {
        /* Get the start and end squares */
        int start = toSquareIndex(move.substring(0, 2));
        int end = toSquareIndex(move.substring(2, 4));

        /* Check if it is a promotion */
        char prom = move.length() > 4 ? move.charAt(4) : 0;

        /* Create the move */
        short returnMove = MoveGen.moves[start][end];

        if ((start == 4 && end == 6) || (start == 4 && end == 2) || (start == 60 && end == 58) || (start == 60 && end == 62)) {
            returnMove |= ((KingMoves.CASTLE_FLAG & 0b11) << 12);
        } //if
        
        /* Deal with promotions */
        if (prom != 0) {
            int promotions = switch (prom) {
                case 'n' -> 0;
                case 'b' -> 1;
                case 'r' -> 2;
                case 'q' -> 3;
                default -> -1;
            };
            returnMove |= ((promotions & 0b11) << 12);
            returnMove |= ((PawnMoves.PROMOTION_FLAG & 0b11) << 14);
        }
        return returnMove;
    }

    public static String moveToUCI(short move) throws Exception {
        int col;
        int row;
        int startingSquare = Long.numberOfTrailingZeros(MoveGen.moveParts[move][0]);
        row = startingSquare / 8;
        col = startingSquare % 8;

        StringBuilder str = new StringBuilder();
        str.append((char) (col + (int) 'a'));
        str.append(Integer.toString(row + 1));

        int endingSquare = Long.numberOfTrailingZeros(MoveGen.moveParts[move][1]);
        row = endingSquare / 8;
        col = endingSquare % 8;
        str.append((char) (col + (int) 'a'));
        str.append(Integer.toString(row + 1));

        if (MoveGen.moveParts[move][3] == PawnMoves.PROMOTION_FLAG) {
            int promType = (int) MoveGen.moveParts[move][2];
            char prom = switch(promType) {
                case 0 -> 'n';
                case 1 -> 'b';
                case 2 -> 'r';
                case 3 -> 'q';
                default -> throw new Exception("Invalid move");
            };
            str.append(prom);
        }

        return str.toString();
    }

    public static String toNotation(short move) {
        int col;
        int row;
        int startingSquare = Long.numberOfTrailingZeros(MoveGen.moveParts[move][0]);
        row = startingSquare / 8;
        col = startingSquare % 8;

        StringBuilder str = new StringBuilder();
        str.append((char) (col + (int) 'a'));
        str.append(Integer.toString(row + 1));
        str.append("-");

        int endingSquare = Long.numberOfTrailingZeros(MoveGen.moveParts[move][1]);
        row = endingSquare / 8;
        col = endingSquare % 8;
        str.append((char) (col + (int) 'a'));
        str.append(Integer.toString(row + 1));

        return str.toString();
    } // toNotation(short)

}
