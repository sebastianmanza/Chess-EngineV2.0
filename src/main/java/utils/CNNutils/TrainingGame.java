package utils.CNNutils;

import org.nd4j.linalg.api.ndarray.INDArray;

public class TrainingGame {
    public INDArray position;
    public INDArray winPercent;

    public TrainingGame(INDArray pos, INDArray wins) {
        this.position = pos;
        this.winPercent = wins;
    }
}
