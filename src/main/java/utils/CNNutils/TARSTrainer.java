package utils.CNNutils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import utils.MCTutils.MCT;
import utils.MCTutils.MCTNode;
import utils.MoveGeneration.GameState;

public class TARSTrainer {

    public static List<TrainingGame> selfPlay(int gameSims, MultiLayerNetwork tars) throws Exception {
        /* Create a list to store all the games together. */
        List<TrainingGame> allGames = new ArrayList<>();
        for (int i = 0; i < gameSims; i++) {
            GameState state = new GameState(true, true);
            state.setBoardStartingPos();
            ArrayList<TrainingGame> games = new ArrayList<>();
            int pieceCount = 32;
            long PawnPos = state.bitBoards[GameState.WPAWNS] & state.bitBoards[GameState.BPAWNS];
            int FiftyMoveRule = 0;
            INDArray vicPoints = Nd4j.scalar(0.5);

            /* The game loop */
            while (true) {
                /* Check for 50 move rule. */
                int lastPieceCount = pieceCount;
                long lastPawnPos = PawnPos;
                pieceCount = state.numPieces();
                PawnPos = state.bitBoards[GameState.WPAWNS] & state.bitBoards[GameState.BPAWNS];

                /* Create the training data */
                INDArray inputPos = TrainingGen.createTensor(state);
                state.engineColor = state.oppEngineColor(); //switch engine color so it still plays best move
                // MCTCNN mct = new MCTCNN(state, tars);
                MCT mct = new MCT(state);
                games.add(new TrainingGame(inputPos, vicPoints));

                MCTNode node = mct.search(Duration.ofMillis(200), false);
                
                if (node == null) {
                    state.engineColor = true;
                    vicPoints = Nd4j.scalar(state.vicPoints());
                    break;
                } else {
                    state = node.state;
                    //state.printBoard();
                }
                if (pieceCount == lastPieceCount && PawnPos == lastPawnPos) {
                    FiftyMoveRule++;
                } else {
                    FiftyMoveRule = 0;
                }
                if (FiftyMoveRule >= 50) {
                    vicPoints = Nd4j.scalar(0.5);
                    break;
                }
            }
            for (TrainingGame game : games) {
                game.winPercent = vicPoints;
            }
            System.out.printf("\r Games Simulated: %d", i);
            allGames.addAll(games);
        }
        return allGames;

    }

    /**
     * Train the TARS model using the provided training data.
     *
     * @param tars   The computation graph to be trained
     * @param data   A list of training games containing state, policy, and value
     * @param epochs The number of training epochs
     */
    public static void trainTARS(MultiLayerNetwork tars, List<TrainingGame> data, int epochs, int batchsize) {
        // Convert the list of TrainingGame into a MultiDataSetIterator
        TrainingGameIterator dataIterator = new TrainingGameIterator(data, batchsize);

        // Add a listener for progress monitoring
        tars.setListeners(new ScoreIterationListener(10));

        // Train the model for the specified number of epochs
        for (int i = 0; i < epochs; i++) {
            tars.fit(dataIterator);
            System.out.println("Completed epoch: " + (i + 1));
        }
    }
}
