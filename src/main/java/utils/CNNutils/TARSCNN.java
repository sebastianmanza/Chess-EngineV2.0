package utils.CNNutils;

import java.io.File;
import java.io.IOException;

import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.ElementWiseVertex;
import org.deeplearning4j.nn.conf.graph.ReshapeVertex;
import org.deeplearning4j.nn.conf.graph.SubsetVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ActivationLayer;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.GlobalPoolingLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.PoolingType;
import org.deeplearning4j.nn.conf.layers.Upsampling2D;
import org.deeplearning4j.nn.graph.ComputationGraph;
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
        private static final int INPUT_CHANNELS = 13;
        private static final int BOARD_DIM = 8;
        private static final int FILTERS = 32;
        private static final int NUM_BLOCKS = 1;
        private static final int SE_CHANNELS = 16;
        
        public static ComputationGraphConfiguration buildGraphConfiguration() {
            ComputationGraphConfiguration.GraphBuilder gb = new NeuralNetConfiguration.Builder()
                .updater(new Adam(0.0001))
                .weightInit(WeightInit.XAVIER)
                .graphBuilder()
                .addInputs("input")
                .setInputTypes(InputType.convolutional(BOARD_DIM, BOARD_DIM, INPUT_CHANNELS))
                .addLayer("input_conv", new ConvolutionLayer.Builder(3, 3)
                    .nIn(INPUT_CHANNELS)
                    .nOut(FILTERS)
                    .stride(1, 1)
                    .padding(1, 1)
                    .activation(Activation.RELU)
                    .build(), "input");
    
                    String blockInput = "input_conv";
                    for (int i = 1; i <= NUM_BLOCKS; i++) {
                        String prefix = "res" + i;
                        gb.addLayer(prefix + "_conv1", new ConvolutionLayer.Builder(3, 3)
                            .nOut(FILTERS)
                            .stride(1, 1)
                            .padding(1, 1)
                            .activation(Activation.RELU)
                            .build(), blockInput);
                        gb.addLayer(prefix + "_conv2", new ConvolutionLayer.Builder(3, 3)
                            .nOut(FILTERS)
                            .stride(1, 1)
                            .padding(1, 1)
                            .activation(Activation.IDENTITY)
                            .build(), prefix + "_conv1");
                        gb.addLayer(prefix + "_se_pool", new GlobalPoolingLayer.Builder()
                            .poolingType(PoolingType.AVG)
                            .build(), prefix + "_conv2");
                        gb.addLayer(prefix + "_se_dense1", new DenseLayer.Builder()
                            .nOut(SE_CHANNELS)
                            .activation(Activation.RELU)
                            .build(), prefix + "_se_pool");
                        gb.addLayer(prefix + "_se_dense2", new DenseLayer.Builder()
                            .nOut(2 * FILTERS)
                            .activation(Activation.IDENTITY)
                            .build(), prefix + "_se_dense1");
                        gb.addVertex(prefix + "_se_W", new SubsetVertex(0, FILTERS - 1), prefix + "_se_dense2");
                        gb.addVertex(prefix + "_se_B", new SubsetVertex(FILTERS, 2 * FILTERS - 1), prefix + "_se_dense2");
                        gb.addLayer(prefix + "_se_W_sigmoid", new ActivationLayer.Builder()
                            .activation(Activation.SIGMOID)
                            .build(), prefix + "_se_W");
                        gb.addVertex(prefix + "_se_W_reshaped", new ReshapeVertex(-1, FILTERS, 1, 1), prefix + "_se_W_sigmoid");
                        gb.addVertex(prefix + "_se_B_reshaped", new ReshapeVertex(-1, FILTERS, 1, 1), prefix + "_se_B");
                        gb.addLayer(prefix + "_se_W_up", new Upsampling2D.Builder(BOARD_DIM).build(), prefix + "_se_W_reshaped");
                        gb.addLayer(prefix + "_se_B_up", new Upsampling2D.Builder(BOARD_DIM).build(), prefix + "_se_B_reshaped");
                        gb.addVertex(prefix + "_se_scale", new ElementWiseVertex(ElementWiseVertex.Op.Product), prefix + "_conv2", prefix + "_se_W_up");
                        gb.addVertex(prefix + "_se_out", new ElementWiseVertex(ElementWiseVertex.Op.Add), prefix + "_se_scale", prefix + "_se_B_up");
                        gb.addVertex(prefix + "_add", new ElementWiseVertex(ElementWiseVertex.Op.Add), blockInput, prefix + "_se_out");
                        gb.addLayer(prefix + "_out", new ActivationLayer.Builder()
                            .activation(Activation.RELU)
                            .build(), prefix + "_add");
                        blockInput = prefix + "_out";
                    }
                    gb.addLayer("global_pool", new GlobalPoolingLayer.Builder()
                        .poolingType(PoolingType.AVG)
                        .build(), blockInput)
                      .addLayer("dense_value", new DenseLayer.Builder()
                        .nOut(128)
                        .activation(Activation.RELU)
                        .build(), "global_pool")
                      .addLayer("output", new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nOut(1)
                        .activation(Activation.SIGMOID)
                        .build(), "dense_value")
                      .setOutputs("output");
                    return gb.build();
        }
    /**
     * Build the CNN.
     * 
     * @return a MultiLayerNetwork that is an initialized CNN
     */
    public static ComputationGraph BuildCNN() {
        // MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
        //         .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
        //         .updater(new Adam(0.0001))
        //         .weightInit(WeightInit.XAVIER)
        //         .list()
        //         .layer(new ConvolutionLayer.Builder(5, 5).nOut(16).stride(1, 1).padding(1, 1)
        //                 .activation(Activation.LEAKYRELU).build())
        //         // .layer(new ConvolutionLayer.Builder(3, 3).nOut(16).stride(1, 1).padding(1,
        //         // 1).activation(Activation.RELU).build())
        //         .layer(new ConvolutionLayer.Builder(3, 3).nOut(32).stride(1, 1).padding(1, 1)
        //                 .activation(Activation.RELU).build())
        //         .layer(new ConvolutionLayer.Builder(3, 3).nOut(32).stride(1, 1).padding(1, 1)
        //                 .activation(Activation.LEAKYRELU).build())
        //         .layer(new ConvolutionLayer.Builder(3, 3).nOut(64).stride(1, 1).padding(1, 1)
        //                 .activation(Activation.LEAKYRELU).build())
        //         .layer(new ConvolutionLayer.Builder(2, 2).nOut(128).stride(1, 1).activation(Activation.LEAKYRELU)
        //                 .build())
        //         .layer(new DenseLayer.Builder().nOut(512).activation(Activation.LEAKYRELU).build())
        //         // .layer(new
        //         // DenseLayer.Builder().nOut(128).activation(Activation.LEAKYRELU).build())
        //         .layer(new DenseLayer.Builder().nOut(32).activation(Activation.RELU).build())
        //         .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE).nOut(1).activation(Activation.SIGMOID)
        //                 .build())
        //         .setInputType(InputType.convolutional(8, 8, 13))
        //         .build();
        ComputationGraphConfiguration configComplex = buildGraphConfiguration();

        ComputationGraph model = new ComputationGraph(configComplex);
        //MultiLayerNetworkmodel = new MultiLayerNetwork(config);
        model.init();
        return model;
    } // BuildCNN()

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
    } // saveModel(MultiLayerNetwork, File)

    /**
     * Save the TARS model to a file.
     *
     * @param tars The MultiLayerModel to save.
     * @param file The file to save the model to.
     */
    public static void saveModel(ComputationGraph tars, File file) {
        try {
            tars.save(file, true);
            System.out.println("Model saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save model: " + e.getMessage(), e);
        } // try/catch
    } // saveModel(MultiLayerNetwork, File)

    /**
     * Load a TARS model from a file.
     *
     * @param file The file containing the saved model.
     * @return The loaded model.
     */
    public static ComputationGraph loadModelC(File file) {
        try {
            return ComputationGraph.load(file, true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load model: " + e.getMessage(), e);
        } // try/catch
    } // loadModel(File)

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
    } // loadModel(File)

} // TARSCNN
