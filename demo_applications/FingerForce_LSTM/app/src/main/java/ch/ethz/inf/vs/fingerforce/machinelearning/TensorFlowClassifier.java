package ch.ethz.inf.vs.fingerforce.machinelearning;

import android.content.res.AssetManager;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Created by Vincent Becker on 26.10.2017.
 */

public class TensorFlowClassifier {

    private static final String MODEL_FILE = "file:///android_asset/frozen_model.pb";
    private static final String INPUT_NODE = "input";
    private static final String OUTPUT_NODE = "softmax";
    private static final String[] outputnames = {OUTPUT_NODE};
    private static final int inputLength = 16;


    private TensorFlowInferenceInterface inferenceInterface;
    private float[] imageValues;
    private float[] resultsArray;
    private int numberOutputs = 2;
    private int numberChannels;
    private int windowLength;
    private int sequenceLength;

    public TensorFlowClassifier(AssetManager manager, int numberClasses, int numberChannels,
                                int windowLength, int sequenceLength) {
        // Sequence dimension changed.
        this.numberChannels = numberChannels;
        this.windowLength = windowLength;
        this.sequenceLength = sequenceLength;
        imageValues = new float[sequenceLength * numberChannels * windowLength];
        numberOutputs = numberClasses;
        resultsArray = new float[numberOutputs];
        inferenceInterface = new TensorFlowInferenceInterface(manager, MODEL_FILE);
    }

    public float[] runInference(float[] input) {
        // Check input length
        if (input.length != inputLength) {
            throw new IllegalArgumentException("The input has the wrong length!");
        }

        // run inference
        inferenceInterface.feed(INPUT_NODE, input, 1, inputLength);
        inferenceInterface.run(outputnames);
        inferenceInterface.fetch(OUTPUT_NODE, resultsArray);

        // return result
        float[] r = resultsArray.clone();
        return r;
    }

    /**
     * For inputting sequences.
     * @param input
     * @return
     */
    public float[] runInference(float[][][] input) {
        int sequenceLength = input.length;
        int numberChannels = input[0].length;
        int windowLength = input[0][0].length;

        /*System.out.println(this.windowLength);
        System.out.println(this.numberChannels);
        System.out.println(this.sequenceLength);
        System.out.println(imageValues.length);*/

        // Flattening input sequence.
        for (int i = 0; i < sequenceLength; i++) {   // sequenceLength = width
            for (int j = 0; j < numberChannels; j++) {  // numberChannels = height
                for (int k = 0; k < windowLength; k++) {    // windowLength = depth
                    imageValues[i * numberChannels * windowLength + j * windowLength + k] = input[i][j][k];
                    // Width (k), Height (i), Depth (j)
                }
            }
        }

        // We feed sequences of 5 windows, all consisting of 8 channels with 10 values each.
        // The "colour channel" is set to 1.
        inferenceInterface.feed(INPUT_NODE, imageValues, 1, sequenceLength, numberChannels, windowLength, 1);
        // Run inference
        inferenceInterface.run(outputnames);
        inferenceInterface.fetch(OUTPUT_NODE, resultsArray);

        // return result
        float[] r = resultsArray.clone();
        return r;
    }
}
