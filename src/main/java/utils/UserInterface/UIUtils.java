package utils.UserInterface;

public class UIUtils {
    public static int tosquareIndex(String input) {
        int squareIndex = 0;
        squareIndex += ((input.charAt(0) - (int) 'a'));
        squareIndex += ((Character.getNumericValue(input.charAt(1) - 1)) * 8);
        return squareIndex;
    }

    // public static String toNotation(short move) {
    //     int col;
    //     int row;
    //     col = move. % 8;
    //     row = move.startingSquare / 8;
    //     StringBuilder str = new StringBuilder();
    //     str.append((char) (col + (int) 'a'));
    //     str.append(Integer.toString(row + 1));
    //     str.append("-");

    //     col = move.endingSquare / 8;
    //     row = move.endingSquare % 8;
    //     str.append((char) (col + (int) 'a'));
    //     str.append(Integer.toString(row + 1));

    //     return str.toString();
    // }
}
