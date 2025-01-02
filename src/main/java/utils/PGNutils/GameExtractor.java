package utils.PGNutils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.pgn.PgnIterator;

/**
 * Extract games into a csv format (FEN,eval) for use with the CNN
 * 
 * @author Sebastian Manza
 */
public class GameExtractor {
    public static void main(String[] args) {
        try {
            /* Initialize the pgn iterator for the input file */
            PgnIterator pgnFile = new PgnIterator("lichess_db_standard_rated_2017-01.pgn");
            BufferedWriter writer = new BufferedWriter(new FileWriter("validation_game_database.csv"));

            int gamesProcessed = 0;

            for (Game game : pgnFile) {
                if (gamesProcessed > 1000000) {
                List<String[]> fenEvalPairs = processGame(game);
                for (String[] pair : fenEvalPairs) {
                    writer.write(pair[0] + "," + pair[1]);
                    writer.newLine();
                }

                System.out.print("\rProcessed games: " + gamesProcessed);
            }
            gamesProcessed++;
                if (gamesProcessed >= 1200000) {
                    break;
                }
            }
            System.out.println();

            writer.close();
            System.out.println("Processing complete. Data saved to validation_game_database.csv.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private static List<String[]> processGame(Game game) {
        List<String[]> fenEvalPairs = new ArrayList<>();
        Board board = new Board();

        /* Extract the moves */
        String[] moves = game.getHalfMoves().toSanArray(); // Array of moves in SAN
        Map<Integer, String> comments = game.getComments();

        boolean hasEvaluation = false;

        for (int i = 0; i < moves.length; i++) {
            /* Apply the move to the board and get the FEN */
            board.doMove(game.getHalfMoves().get(i));
            String fen = board.getFen();

            /* Extract the comment for the move */
            String comment = null;
            if (comments != null) {
                comment = comments.get(i + 1);
            }
            String evaluation = null;

            if (comment != null && comment.contains("[%eval")) {
                int start = comment.indexOf("[%eval") + 7;
                int end = comment.indexOf("]", start);
                evaluation = comment.substring(start, end).trim();
                hasEvaluation = true;
            }

            if (evaluation != null) {
                fenEvalPairs.add(new String[]{fen, evaluation});
            }
        }

        /* Return it if there was any evaluations, else return an empty arraylist */
        return hasEvaluation ? fenEvalPairs : new ArrayList<>();
    }
}
