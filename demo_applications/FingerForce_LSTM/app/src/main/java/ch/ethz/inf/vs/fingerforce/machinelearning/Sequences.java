package ch.ethz.inf.vs.fingerforce.machinelearning;

import ch.ethz.inf.vs.fingerforce.BuildConfig;

/**
 * Created by Pietro Oldrati on 24.11.2017.
 */

public class Sequences {

    private WindowCallback callback;
    private float[][][] data;
    private int numberChannels;
    private int windowLength;
    private int sequenceLength;
    private int nonOverlap;
    private int counter;
    private boolean initialized = false;

    public Sequences(WindowCallback callback, int numberChannels, int windowLength, int sequenceLength) {
        this.callback = callback;
        this.windowLength = windowLength;                   // Number of samples in a window.
        this.sequenceLength = sequenceLength;               // Number of windows in the current sequence.
        this.numberChannels = numberChannels;               // Number of channels in a window.
        nonOverlap = windowLength;                          // counter % nonOverlap == 0 tests if we filled an overlapping sequence. We overlap a new sequence after each window.
        data = new float[sequenceLength][numberChannels][windowLength];
        counter = 0;
    }

    // A new sample arrived.
    public void addData(float[] datapoint) {
        // Add new sample's data to the currentIndex (current window in current sequence).
        int currentSampleIndex = counter % windowLength;                      // Sample index in the current window.
        int currentWindowIndex = (counter / windowLength) % sequenceLength;   // Window index in the current sequence.

        int previousIndex = currentWindowIndex;

        // Fill all the channels in the current sample.
        for (int i = 0; i < numberChannels; i++) {
            data[currentWindowIndex][i][currentSampleIndex] = datapoint[i];
        }

        // Go further in the sequence.
        counter++;

        // Set initialize to true after the first filled sequence.
        if (!initialized && (counter >= windowLength * sequenceLength)) {
            initialized = true;
        }

        // Once initialized we can overlap new windows.
        // As long as we collect the next window we can advance (jumps of single windows).
        if (initialized && (counter % nonOverlap == 0)) {
            // We always are at the start of a new window in the sequence.
            /*assert (counter % windowLength == 0);

            if (BuildConfig.DEBUG && !(counter % windowLength == 0)) {
                throw new AssertionError();
            }*/

            // Index of the first window in the sequence containing old data.
            int beginningWindowIndex = (counter / windowLength) % sequenceLength;

            /*assert(beginningWindowIndex == (previousIndex + 1) % sequenceLength);

            if (BuildConfig.DEBUG && !(beginningWindowIndex == (previousIndex + 1) % sequenceLength)) {
                throw new AssertionError();
            }*/

            // Return the filled sequence.
            // create a new array to hold the new sequence which ends with the newely gathered window.
            float[][][] filledWindow = new float[sequenceLength][numberChannels][windowLength];

            for (int i = 0; i < sequenceLength; i++) {
                // Write old windows first, then cycle back and proceed to write new data.
                currentWindowIndex = (beginningWindowIndex + i) % sequenceLength;

                for (int j = 0; j < numberChannels; j++) {
                    for (int k = 0; k < windowLength; k++) {
                        filledWindow[i][j][k] = data[currentWindowIndex][j][k];
                    }
                }
            }
            callback.onWindowFull(filledWindow);
        }
    }
}