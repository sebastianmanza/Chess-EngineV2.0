package utils.CNNutils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import utils.MoveGeneration.GameState;
import utils.PGNutils.CentipawnToWin;

/**
 * An iterator to read in the data to train the CNN off of.
 * 
 * @author Sebastian Manza
 */
public class TrainingGameIterator implements DataSetIterator {

    private BufferedReader reader;
    private final int batchSize;
    private final int numFeatures;
    private final String csvFilePath;
    private boolean endOfFile = false;
    private DataSetPreProcessor preProcessor;

    /**
     * Initialize the iterator.
     * @param csvFilePath The path of the csv file
     * @param batchSize The size of the batch
     * @param numFeatures The number of planes
     * @throws IOException If the reader goes wrong
     */
    public TrainingGameIterator(String csvFilePath, int batchSize, int numFeatures) throws IOException {
        this.reader = new BufferedReader(new FileReader(csvFilePath));
        this.batchSize = batchSize;
        this.numFeatures = numFeatures;
        this.csvFilePath = csvFilePath;
    } //TrainingGameIterator

    /**
     * Check if there is something next
     */
    @Override
    public boolean hasNext() {
        return !endOfFile;
    } //hasNext

    /**
     * Iterate to the next line, adjusted for batch size
     */
    @Override
    public DataSet next() {
        return next(batchSize);
    }

    /**
     * Do the actal iterating (return the next batch)
     */
    @Override
    public DataSet next(int num) {
        if (!hasNext()) {
            throw new NoSuchElementException("No more elements in the iterator");
        } //if

        try {
            INDArray inputBatch = Nd4j.create(num, numFeatures, 8, 8);
            INDArray valueBatch = Nd4j.create(num, 1);

            int count = 0;
            String line = null;
            while (count < num && (line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String fen = parts[0];
                double winPercentage = CentipawnToWin.convert(parts[1]);

                /* Convert the FEN to a tensor */
                GameState state = new GameState(true, true);
                state.setBoardFEN(fen);
                INDArray fenTensor = TrainingGen.createTensor(state);

                /* Grab the input tensor */
                inputBatch.get(NDArrayIndex.point(count)).assign(fenTensor);

                /* Copy the output value */
                valueBatch.putScalar(count, winPercentage);

                count++;
            } //while

            if (line == null) {
                endOfFile = true;
            } //if

            DataSet dataSet = new DataSet(inputBatch, valueBatch);

            if (preProcessor != null) {
                preProcessor.preProcess(dataSet);
            } //if

            return dataSet;

        } catch (IOException e) {
            throw new RuntimeException("Error reading from CSV file", e);
        } //try/catch
    }

    @Override
    public int inputColumns() {
        return numFeatures * 8 * 8; // Total input size
    }

    @Override
    public int totalOutcomes() {
        return 1; //
    }

    @Override
    public boolean resetSupported() {
        return true;
    }

    @Override
    public void reset() {
        try {
            /* Close the reader if it isn't */
            if (reader != null) {
                reader.close();
            }
    
            /* Open the file and reinitialize the reader */
            this.reader = new BufferedReader(new FileReader(csvFilePath));
            this.endOfFile = false;
    
        } catch (IOException e) {
            throw new RuntimeException("Error resetting CSV reader", e);
        } //try/catch
    } //reset

    /**
     * I had to include this but I'm not sure what it is supposed to do.
     */
    @Override
    public boolean asyncSupported() {
        return false;
    }

    /**
     * Return the size of a batch
     */
    @Override
    public int batch() {
        return batchSize;
    }

    /**
     * Set the pre processor
     */
    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        this.preProcessor = preProcessor;
    }

    /**
     * Get the pre processor
     */
    @Override
    public DataSetPreProcessor getPreProcessor() {
        return preProcessor;
    }

    @Override
    public List<String> getLabels() {
        return null; //Not applicable for this iterator
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove not supported"); //not applicable for this iterator
    }
}
