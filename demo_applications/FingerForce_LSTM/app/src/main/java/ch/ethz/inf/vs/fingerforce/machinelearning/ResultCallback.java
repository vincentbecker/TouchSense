package ch.ethz.inf.vs.fingerforce.machinelearning;

/**
 * Created by Vincent Becker on 26.10.2017.
 */

public interface ResultCallback {

    public void onResult(float[] results, float mmav);
}
