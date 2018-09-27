package ch.ethz.inf.vs.fingerforce.machinelearning;

import android.content.res.AssetManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import ch.ethz.inf.vs.fingerforce.emgdata.model.RawDataPoint;

/**
 * Created by Vincent Becker on 26.10.2017.
 */

public class MLRunner implements WindowCallback {

    private Sequences windows;
    private ResultCallback callback;
    private TensorFlowClassifier classifier;
    private Handler mlHandler;
    private HandlerThread mlHandlerThread;
    private Handler uiHandler;
    private PredictionSmoother smoother;
    private static final int NUMBER_CLASSES = 3;

    // Data shape parameters.
    private static final int NUMBER_CHANNELS = 8;
    private static final int WINDOW_LENGTH = 10;
    private static final int SEQUENCE_LENGTH = 5;

    public MLRunner(AssetManager manager, ResultCallback callback) {
        // Initialize the sequence. 5 Windows with 10 samples each.
        this.windows = new Sequences(this, NUMBER_CHANNELS, WINDOW_LENGTH, SEQUENCE_LENGTH);
        this.callback = callback;
        classifier = new TensorFlowClassifier(manager, NUMBER_CLASSES, NUMBER_CHANNELS, WINDOW_LENGTH, SEQUENCE_LENGTH);

        mlHandlerThread = new HandlerThread("inference");
        mlHandlerThread.start();
        mlHandler = new Handler(mlHandlerThread.getLooper());

        uiHandler = new Handler();

        smoother = new PredictionSmoother(NUMBER_CLASSES, 0.1f);
    }

    public void addDataPoint(RawDataPoint dataPoint) {
        float[] a = dataPoint.getValues().clone();
        windows.addData(a);
    }

    @Override
    public void onWindowFull(final float[][][] window) {

        runInBackground(new Runnable() {
            @Override
            public void run() {
                /*
                // calculate mean for each channel
                int numberChannels = window.length;
                int numberFeatuersSets = 2;
                long start = System.currentTimeMillis();
                float[] features = new float[numberChannels * numberFeatuersSets];
                //float[] mavFeatures = FeatureExtraction.mav(window);
                float[] varFeatures = FeatureExtraction.var(window);
                float[] smadrFeatures = FeatureExtraction.smadr(window);
                //concatenate all the features
                for (int i = 0; i < numberChannels; i++) {
                    features[i] = varFeatures[i];
                }
                for (int i = 0; i < numberChannels; i++) {
                    features[i] = smadrFeatures[i];
                }
                long end = System.currentTimeMillis();
                Log.d("RUNTIME features", (end - start) + " ms");

                // run the inference
                start = System.currentTimeMillis();
                final float[] resultsArray = classifier.runInference(features);
                end = System.currentTimeMillis();
                Log.d("RUNTIME", (end - start) + " ms");
                String resultText = "Result: " + resultsArray[0] + " " + resultsArray[1];
                Log.d("RESULTS", resultText);

                // smooth predictions

                */
                // run the inference
                long start = System.currentTimeMillis();
                final float[] resultsArray = classifier.runInference(window);
                long end = System.currentTimeMillis();
                Log.d("RUNTIME", (end - start) + " ms");
                String resultText = "Result: " + resultsArray[0] + " " + resultsArray[1]; //+ " " + resultsArray[2];
                Log.d("RESULTS", resultText);

                // get mmav in place of regression
                final float mmav = FeatureExtraction.mmav(window);

                // smooth predictions and mmav
                final float[] smoothedValues = smoother.addPredictions(resultsArray, mmav);
                final float[] r = new float[NUMBER_CLASSES];
                for (int i = 0; i < NUMBER_CLASSES; i++) {
                    r[i] = smoothedValues[i];
                }

                // change UI
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(r, smoothedValues[NUMBER_CLASSES]);
                    }
                });
            }
        });
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (mlHandler != null) {
            mlHandler.post(r);
        }
    }

    public void close() {
        mlHandlerThread.quitSafely();
        try {
            mlHandlerThread.join();
            mlHandlerThread = null;
            mlHandler = null;
            uiHandler = null;
        } catch (final InterruptedException e) {
            Log.e("MLRunner", "Exception!");
        }
    }

}
