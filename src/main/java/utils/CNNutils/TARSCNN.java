package utils.CNNutils;

import java.io.File;
import java.io.IOException;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * The class that builds and saves the CNN. Utilizes the deeplearning4j library.
 * 
 * @author Sebastian Manza
 */
public class TARSCNN {
    /**
     * Build the CNN.
     * @return a MultiLayerNetwork that is an initialized CNN
     */
    public static MultiLayerNetwork BuildCNN() {
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.0001))
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(new ConvolutionLayer.Builder(5, 5).nOut(16).stride(1, 1).padding(1, 1)
                        .activation(Activation.LEAKYRELU).build())
                // .layer(new ConvolutionLayer.Builder(3, 3).nOut(16).stride(1, 1).padding(1,
                // 1).activation(Activation.RELU).build())
                // .layer(new ConvolutionLayer.Builder(3, 3).nOut(32).stride(1, 1).padding(1,
                // 1).activation(Activation.RELU).build())
                .layer(new ConvolutionLayer.Builder(3, 3).nOut(32).stride(1, 1).padding(1, 1)
                        .activation(Activation.LEAKYRELU).build())
                .layer(new ConvolutionLayer.Builder(2, 2).nOut(64).stride(1, 1).activation(Activation.LEAKYRELU)
                        .build())
                .layer(new DenseLayer.Builder().nOut(512).activation(Activation.LEAKYRELU).build())
                .layer(new DenseLayer.Builder().nOut(128).activation(Activation.LEAKYRELU).build())
                // .layer(new DenseLayer.Builder().nOut(32).activation(Activation.RELU).build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE).nOut(1).activation(Activation.SIGMOID)
                        .build())
                .setInputType(InputType.convolutional(8, 8, 13))
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();
        return model;
    } //BuildCNN()

    /**
     * Save the TARS model to a file.
     *
     * @param tars The MultiLayerModel to save.
     * @param file The file to save the model to.
     */
    public static void saveModel(MultiLayerNetwork tars, File file) {
        try {
            tars.save(file, true);
            System.out.println("Model saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save model: " + e.getMessage(), e);
        } // try/catch
    } //saveModel(MultiLayerNetwork, File)

    /**
     * Load a TARS model from a file.
     *
     * @param file The file containing the saved model.
     * @return The loaded model.
     */
    public static MultiLayerNetwork loadModel(File file) {
        try {
            return MultiLayerNetwork.load(file, true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load model: " + e.getMessage(), e);
        } // try/catch
    } //loadModel(File)

} //TARSCNN
