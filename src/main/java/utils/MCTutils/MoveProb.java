package utils.MCTutils;

public class MoveProb {
    short move;
    double probability;

    public MoveProb(short m, double prob) {
        this.move = m;
        this.probability = prob;
    }

    public static int maxProbabilityMove(MoveProb[] moves) {
        int bestMove = 0;
        double bestProb = moves[0].probability;
        for (int i = 0; i < moves.length; i++) {
            if (moves[i].probability > bestProb) {
                bestMove = i;
                bestProb = moves[i].probability;
            }
        }
        return bestMove;
    }

    public static int sampleMove(MoveProb[] moves) {
        double totalProbability = 0.0;
        for (MoveProb move : moves) {
            totalProbability += move.probability;
        }
    
        double rand = Math.random() * totalProbability;
        double cumulativeProbability = 0.0;
    
        for (int i = 0; i < moves.length; i++) {
            cumulativeProbability += moves[i].probability;
            if (rand <= cumulativeProbability) {
                return i;
            }
        }
        return moves.length - 1;
    }

}
