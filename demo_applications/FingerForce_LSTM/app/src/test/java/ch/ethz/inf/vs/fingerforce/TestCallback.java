package ch.ethz.inf.vs.fingerforce;

import ch.ethz.inf.vs.fingerforce.machinelearning.WindowCallback;

/**
 * Created by Vincent Becker on 26.10.2017.
 */

public class TestCallback implements WindowCallback {

    private int counter = 0;
    private float[][][] lastWindow;


    public int getCounter() {
        return counter;
    }

    public float[][][] getData() {
        return lastWindow;
    }

    @Override
    public void onWindowFull(float[][][] window) {
        counter++;
        lastWindow = window;
    }
}
