package utils.CNNutils;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class TARSCNN {
    public static ComputationGraph BuildCNN() {
        ComputationGraphConfiguration config = new NeuralNetConfiguration.Builder()
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .updater(new Adam(0.001))
            .weightInit(WeightInit.XAVIER)
            .graphBuilder()
            .addInputs("input")
            .setInputTypes(InputType.convolutional(8, 8, 13))
            .addLayer("conv1", new ConvolutionLayer.Builder(3, 3).nOut(64).stride(1, 1).activation(Activation.RELU).build(), "input")
            .addLayer("conv2", new ConvolutionLayer.Builder(3, 3).nOut(64).stride(1, 1).activation(Activation.RELU).build(), "conv1")
            .addLayer("denseShared", new DenseLayer.Builder().nOut(128).activation(Activation.RELU).build())
            
            /* The move head. */
            .addLayer("moveDense", new DenseLayer.Builder().nOut(64).activation(Activation.RELU).build(), "denseShared")
            .addLayer("moveOutput", new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT).nOut(64 * 64).activation(Activation.SOFTMAX).build(), "moveDense")
            
            /* The evaluation head. */
            .addLayer("evalDense", new DenseLayer.Builder().nOut(64).activation(Activation.RELU).build(), "denseShared")
            .addLayer("evalOutput", new OutputLayer.Builder(LossFunctions.LossFunction.MSE).nOut(1).activation(Activation.IDENTITY).build(), "evalDense")
            .setOutputs("moveOutput", "evalOutput")
            .build();

            return new ComputationGraph(config);
    }
}
