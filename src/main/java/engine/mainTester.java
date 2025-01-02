package engine;

import java.io.File;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import utils.CNNutils.TARSCNN;
import utils.CNNutils.TrainingGen;
import utils.MoveGeneration.GameState;

public class mainTester {
    public static void main(String[] args) {
        String FEN = "r1b2bkr/ppp3pp/2n5/3Qp3/8/8/PPPP1PPP/RNB1K2R b KQ - 0 9"; //#2
        String FEN2 = "r1b2rk1/2p2pnp/p1Nqp1p1/6B1/6P1/2PP1Q1P/P1P2P2/R4RK1 w - - 1 17"; //8.95
        String FEN3 = "r4b1r/pp3kpp/1q2pn2/1N1p1bB1/2P5/2Q5/PP3PPP/n2K1B1R b - - 1 14"; //-15.77
        String FEN4 = "rnb1kb1r/p4ppp/2p1p3/1p2P2q/3P2n1/2NB1N1P/PP3PP1/R1BQ1RK1 w kq b6 0 11"; //9.51
        String FEN5 = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0"; //0.2
        GameState board = new GameState(true, true);

        board.setBoardFEN(FEN);
        MultiLayerNetwork tars = TARSCNN.loadModel(new File("TARS-V5.6.zip"));
        System.out.println(tars.output(TrainingGen.createTensor(board)));
        board.printBoard();
    }
}
