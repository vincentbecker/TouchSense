package ch.ethz.inf.vs.fingerforce.machinelearning;

/**
 * Created by Vincent Becker on 29.10.2017.
 */

public class PredictionSmoother {

    private float a;
    private int numberClasses;
    private float[] smoothedPredictions;
    private float smoothedValue;

    public PredictionSmoother (int numberClasses, float a) {
        this.a = a;
        this.numberClasses = numberClasses;
        this.smoothedPredictions = new float[numberClasses];
        this.smoothedValue = 0;
    }

    public float[] addPredictions(float[] predictions, float value) {

        for (int i = 0; i < numberClasses; i++) {
            smoothedPredictions[i] = smooth(smoothedPredictions[i], predictions[i]);
        }

        smoothedValue = smooth(smoothedValue, value);

        float[] r = new float[numberClasses + 1];
        for (int i = 0; i < numberClasses; i++) {
            r[i] = smoothedPredictions[i];
        }
        r[numberClasses] = smoothedValue;
        return r;
    }

    private float smooth(float average, float value) {
        float newAverage = (1 - a) * average + a * value;
        return newAverage;
    }
}
