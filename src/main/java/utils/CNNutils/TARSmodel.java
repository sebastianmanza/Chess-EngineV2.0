package utils.CNNutils;

import java.io.File;
import java.time.Duration;
import java.util.List;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import utils.MCTutils.MCTCNN;
import utils.MCTutils.MCTNode;
import utils.MoveGeneration.GameState;

public class TARSmodel {

    private final MultiLayerNetwork TARS;
    private final int selfPlayGamesPerIteration = 256;
    private final int trainingEpochs = 10;
    private final int evaluationGames = 10;
    private final int batchsize = 32;
    private final double evaluationThreshold = 0.55; // Minimum win rate to replace the current model

    public TARSmodel() {
        this.TARS = TARSCNN.BuildCNN();
    }

    public TARSmodel(File modelFile) {
        this.TARS = TARSCNN.loadModel(modelFile);
    }

    /**
     * Main loop for self-play, training, and evaluation.
     */
    public void train() throws Exception {
        int iteration = 2;
        while (true) {
            System.out.printf("=== Iteration %d ===\n", iteration);

            /* Generate data by playing itself */
            System.out.println("Generating self-play games...");
            List<TrainingGame> trainingData = selfPlay(selfPlayGamesPerIteration);

            /* Train the neural network */
            System.out.println("Training the model...");
            trainModel(trainingData, trainingEpochs);

            // 3. Evaluate the model
            System.out.println("Evaluating the model...");
            File file = new File("TARSModel-1." + (iteration - 1) + ".zip");
            boolean passed = evaluateModel(file);

            // 4. Save the model if evaluation passes
            if (passed) {
                System.out.printf("Iteration %d: New model passed evaluation. Saving...\n", iteration);
                TARSCNN.saveModel(TARS, new File("TARSModel-1." + iteration + ".zip"));
            } else {
                System.out.printf("Iteration %d: New model failed evaluation. Retrying...\n", iteration);
            }
            iteration++;
        }
    }

    /**
     * Generates self-play games using the current model.
     * 
     * @param games The number of self-play games to generate.
     * @return List of training games with positions, policies, and values.
     * @throws Exception
     */
    private List<TrainingGame> selfPlay(int games) throws Exception {
        return TARSTrainer.selfPlay(games, TARS);
    }

    /**
     * Trains the model on the generated self-play data.
     * 
     * @param trainingData List of training games.
     * @param epochs       Number of epochs to train the model for.
     */
    private void trainModel(List<TrainingGame> trainingData, int epochs) {
        TARSTrainer.trainTARS(TARS, trainingData, epochs, batchsize);
    }

    /**
     * Evaluates the trained model against the previous one.
     * 
     * @return True if the new model wins at least `evaluationThreshold` of games.
     * @throws Exception
     */
    private boolean evaluateModel(File prevIter) throws Exception {
        int wins = 0;
        int draws = 0;

        for (int i = 0; i < evaluationGames; i++) {
            GameState state = new GameState(true, true);
            state.setBoardStartingPos();
            int pieceCount = 32;
            long PawnPos = state.bitBoards[GameState.WPAWNS] & state.bitBoards[GameState.BPAWNS];
            int FiftyMoveRule = 0;
            double result;
            MultiLayerNetwork tarsTwo = TARSCNN.loadModel(prevIter);

            /* Play a match between the models */
            while (true) {
                int lastPieceCount = pieceCount;
                long lastPawnPos = PawnPos;
                pieceCount = state.numPieces();
                PawnPos = state.bitBoards[GameState.WPAWNS] & state.bitBoards[GameState.BPAWNS];

                MCTCNN currentMCT = new MCTCNN(state, TARS);
                MCTNode node1 = currentMCT.search(Duration.ofMillis(2000), false);
                if (node1 == null) {
                    System.out.println("Current model returned null.");
                    break;
                }
                state = node1.state;
                state.printBoard();
                state.engineColor = state.oppEngineColor();

                
                MCTCNN opponentMCT = new MCTCNN(state, tarsTwo);
                MCTNode node2 = opponentMCT.search(Duration.ofMillis(2000), false);
                if (node2 == null) {
                    System.out.println("Previous model returned null.");
                    break;
                }
                state = node2.state;
                state.printBoard();
                state.engineColor = state.oppEngineColor();

                if (pieceCount == lastPieceCount && PawnPos == lastPawnPos) {
                    FiftyMoveRule++;
                } else {
                    FiftyMoveRule = 0;
                }
                if (FiftyMoveRule >= 25) {
                    break;
                }
            }
            state.engineColor = true;
            result = state.vicPoints();
            if (FiftyMoveRule >= 25) {
                result = 0.5;
            }
            if (result == 1.0)
                wins++;
            else if (result == 0.5)
                draws++;
            System.out.println(result);
        }

        double winRate = ((double) wins + (draws / 2)) / evaluationGames;
        System.out.printf("Evaluation Results: Wins = %d, Draws = %d, Win Rate = %.2f%%\n", wins, draws, winRate * 100);
        return winRate >= evaluationThreshold;
    }

    public static void main(String[] args) throws Exception {
        TARSmodel tars = new TARSmodel(new File("TARSModel-1.1.zip"));
        tars.train();
    }
}
