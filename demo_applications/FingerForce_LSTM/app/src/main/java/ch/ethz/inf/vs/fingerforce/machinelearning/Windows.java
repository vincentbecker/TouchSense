package ch.ethz.inf.vs.fingerforce.machinelearning;

/**
 * Created by Vincent Becker on 26.10.2017.
 */

public class Windows {

    private WindowCallback callback;
    private float[][] data;
    private int numberChannels;
    private int windowLength;
    private int nonOverlap;
    private int counter;
    private boolean initialized = false;

    public Windows(WindowCallback callback, int numberWindows, int numberChannels, int windowLength) {
        this.callback = callback;
        this.windowLength = windowLength;
        this.numberChannels = numberChannels;
        nonOverlap = windowLength / numberWindows;
        data = new float[numberChannels][windowLength];
        counter = 0;
    }

    // A new sample arrived.
    public void addData(float[] datapoint) {
        // Add new sample's data to the currentIndex (current sample in window).
        int currentIndex = counter % windowLength;

        // Fill all the channels in the current sample.
        for (int i = 0; i < numberChannels; i++) {
            data[i][currentIndex] = datapoint[i];
        }

        // Go further in the window.
        counter++;

        // Set initialize to true after the first filled window.
        if (!initialized && (counter >= windowLength)) {
            initialized = true;
        }

        // Once initialized we can overlap new windows.
        if (initialized && (counter % nonOverlap == 0)) {
            // return the filled window
            int beginningIndex = counter % windowLength;
            // create a new array to fill the values in
            float[][] filledWindow = new float[numberChannels][windowLength];

            for (int i = 0; i < numberChannels; i++) {
                for (int j = 0; j < windowLength; j++) {
                    currentIndex = (beginningIndex + j) % windowLength;
                    filledWindow[i][j] = data[i][currentIndex];
                }
            }
            // Uncomment this line if you want to use Windows instead of sequences...
            //callback.onWindowFull(filledWindow);
        }
    }
}