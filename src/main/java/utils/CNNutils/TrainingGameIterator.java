package utils.CNNutils;

import java.util.List;
import java.util.NoSuchElementException;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

public class TrainingGameIterator implements DataSetIterator {

    private final List<TrainingGame> games;
    private final int batchSize;
    private int currentIndex;
    private DataSetPreProcessor preProcessor;

    public TrainingGameIterator(List<TrainingGame> games, int batchSize) {
        this.games = games;
        this.batchSize = batchSize;
        this.currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < games.size();
    }

    @Override
    public DataSet next() {
        return next(batchSize);
    }

    @Override
    public DataSet next(int num) {
        if (!hasNext()) {
            throw new NoSuchElementException("No more elements in the iterator");
        }

        int endIndex = Math.min(currentIndex + num, games.size());
        List<TrainingGame> batch = games.subList(currentIndex, endIndex);
        currentIndex = endIndex;

        // Prepare batch tensors
        int actualBatchSize = batch.size();
        INDArray inputBatch = Nd4j.create(actualBatchSize, 13, 8, 8); // [batch, channels, rows, cols]
        INDArray valueBatch = Nd4j.create(actualBatchSize, 1);       // [batch, 1]

        for (int i = 0; i < actualBatchSize; i++) {
            // Copy input tensor
            inputBatch.get(NDArrayIndex.point(i))
                    .assign(batch.get(i).position);

            // Copy output value
            valueBatch.putScalar(i, batch.get(i).winPercent.getDouble(0));
        }

        DataSet dataSet = new DataSet(inputBatch, valueBatch);

        if (preProcessor != null) {
            preProcessor.preProcess(dataSet);
        }
        return dataSet;
    }

    @Override
    public int inputColumns() {
        return 13 * 8 * 8; // 13 channels, 8x8 grid
    }

    @Override
    public int totalOutcomes() {
        return 1; // Single scalar value output
    }

    @Override
    public boolean resetSupported() {
        return true;
    }

    @Override
    public void reset() {
        this.currentIndex = 0;
    }

    @Override
    public boolean asyncSupported() {
        return false;
    }

    @Override
    public int batch() {
        return batchSize;
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        this.preProcessor = preProcessor;
    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        return preProcessor;
    }

    @Override
    public List<String> getLabels() {
        return null; // Not applicable for this iterator
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported");
    }
}
