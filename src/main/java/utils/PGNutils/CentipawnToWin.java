package utils.PGNutils;

/**
 * Convert a stockfish centipawn/ mate evaluation to a win probability
 * 
 * @author Sebastian Manza
 */
public class CentipawnToWin {
    public static double centipawnToProb(double centipawns) {
        /* According to lichess win% = 50 + 50 * (2 / (1 + exp(-0.00368208 * centipawns)) - 1) I need it in [0 1] */
        final double k = 0.00368208;
        return 0.5 + (0.5 * (2.0 / (1.0 + Math.exp(-k * centipawns)) - 1));
    }

    public static double mateMovesToProb(int mateInMoves) {
        double k = 0.001 * mateInMoves;
        return mateInMoves > 0 ? (1 - k) : (0 + k);
    }

    public static double convert(String evaluation) {
        if (evaluation.startsWith("#")) {
            /* Handle mates */
            int mateMoves = Integer.parseInt(evaluation.substring(1));
            return mateMovesToProb(mateMoves);
        } else {
            /* Convert the centipawns */
            double centipawns = Double.parseDouble(evaluation);
            centipawns *= 100; //Because the evaluations are given in pawns
            return centipawnToProb(centipawns);
        }
    }
}
