package engine;

import java.io.File;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import utils.CNNutils.TARSCNN;
import utils.CNNutils.TrainingGameIterator;

/**
 * The trainer for the tars NN.
 * 
 * @author Sebastian Manza
 */
public class TARSZeroTrainer {
    public static void main(String[] args) {
        try {
            String trainingCsv = "training_game_database.csv";
            String validationCsv = "validation_game_database.csv";

            /* Params */
            int batchSize = 128;
            int numFeatures = 13;
            int numEpochs = 25;

            /* Initializing the iterators */
            DataSetIterator trainIterator = new TrainingGameIterator(trainingCsv, batchSize, numFeatures);
            DataSetIterator validationIterator = new TrainingGameIterator(validationCsv, batchSize, numFeatures);

            // MultiLayerNetwork tars = TARSCNN.loadModel(new File("TARS-V4.1.zip"));
            ComputationGraph tars = TARSCNN.BuildCNN();
            for (int epoch = 0; epoch < numEpochs; epoch++) {
                System.out.println("Starting epoch " + (epoch + 1) + "/" + numEpochs);
                int currentBatch = 0;
                while (trainIterator.hasNext()) {
                    tars.fit(trainIterator.next());
                    currentBatch++;
		if ((currentBatch % 1000) == 0){
                  System.out.printf("\rPositions Analyzed: %d", currentBatch * 128);
		}
                } // while
                System.out.println();
  		TARSCNN.saveModel(tars, new File("TARS-V9." + epoch + ".zip"));

                /* Evaluate the training loss */
                double trainLoss = evaluateLoss(tars, trainIterator);
                System.out.println("Epoch " + (epoch + 1) + " Training Loss: " + trainLoss);

                /* Evaluate the validation loss off of our validation dataset */
                double validationLoss = evaluateLoss(tars, validationIterator);
                System.out.println("Epoch " + (epoch + 1) + " Validation Loss: " + validationLoss);

                //TARSCNN.saveModel(tars, new File("TARS-V9." + epoch + ".zip"));
                trainIterator.reset();
                validationIterator.reset();
            } // for

            System.out.println("Training complete.");

        } catch (Exception e) {
            e.printStackTrace();
        } // try/catch
    } // main

    /**
     * Evaluate the validation loss over the entire validation dataset.
     * 
     * @param model              the network to evaluate
     * @param validationIterator The data iterator to use
     */
    private static double evaluateLoss(MultiLayerNetwork model, DataSetIterator validationIterator) {
        double totalLoss = 0.0;
        int batches = 0;
        validationIterator.reset();

        while (validationIterator.hasNext() && batches < 5000) {
            DataSet batch = validationIterator.next();
            totalLoss += model.score(batch);
            batches++;
        } // while

        return totalLoss / batches; // Avg loss
    } // evaluateLoss(MultiLayerNetwork, DataSetIterator)
} // TARSZeroTrainer
    /**
     * Evaluate the validation loss over the entire validation dataset.
     * 
     * @param model              the network to evaluate
     * @param validationIterator The data iterator to use
     */
    private static double evaluateLoss(ComputationGraph model, DataSetIterator validationIterator) {
        double totalLoss = 0.0;
        int batches = 0;
        validationIterator.reset();

        while (validationIterator.hasNext() && batches < 5000) {
            DataSet batch = validationIterator.next();
            totalLoss += model.score(batch);
            batches++;
        } // while

        return totalLoss / batches; // Avg loss
    } // evaluateLoss(MultiLayerNetwork, DataSetIterator)
} // TARSZeroTrainer
