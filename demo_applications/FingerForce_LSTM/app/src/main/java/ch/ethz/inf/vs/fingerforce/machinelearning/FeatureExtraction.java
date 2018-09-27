package ch.ethz.inf.vs.fingerforce.machinelearning;

/**
 * Created by Vincent Becker on 26.10.2017.
 */

public class FeatureExtraction {

    static {
        System.loadLibrary("native-lib");
    }

    public static float[] mav(float[][] a) {
        int numberChannels = a.length;
        float[] mav = new float[numberChannels];
        int n = a[0].length;
        for (int i = 0; i < numberChannels; i++) {
            for (int j = 0; j < n; j++) {
                mav[i] += Math.abs(a[i][j]);
            }
            mav[i] = mav[i] / n;
        }
        return mav;
    }

    // EMG signals usually have a zero mean, hence it is easy to calculate the variance
    public static float[] var(float[][] a) {
        int numberChannels = a.length;
        float[] var = new float[numberChannels];
        int n = a.length;
        for (int i = 0; i < numberChannels; i++) {
            for (int j = 0; j < n; j++) {
                var[i] += Math.pow(a[i][j], 2);
            }
            var[i] = var[i] / (n - 1);
        }
        return var;
    }

    public static float[] stdv(float[][] a) {
        int numberChannels = a.length;
        float[] stdv = var(a);
        for (int i = 0; i < numberChannels; i++) {
            stdv[i] = (float) Math.sqrt(stdv[i]);
        }
        return stdv;
    }

    public static float[] smadr(float[][] a) {
        int numberChannels = a.length;
        int n = a[0].length;
        // get the mean MAV
        float[] mav = mav(a);
        float mmav = 0;
        for (int i = 0; i < numberChannels; i++) {
            mmav += mav[i];
        }
        // calculate the sum of absolute differences and divide by mmav
        float[] smadr = new float[numberChannels];
        for (int i = 0; i < numberChannels; i++) {
            int adjacentChannelIndex = (i + 1) % numberChannels;
            for (int j = 0; j < n; j++) {
                smadr[i] = Math.abs(a[i][j] - a[adjacentChannelIndex][j]);
            }
            smadr[i] = smadr[i] / mmav;
        }
        return smadr;
    }

    public static float mmav(float[][][] a) {
        int numberChannels = a.length;
        // Get the mean MAV. We use the last window (most recent) of the sequence.
        float[] mav = mav(a[a.length - 1]);
        float mmav = 0;
        for (int i = 0; i < numberChannels; i++) {
            mmav += mav[i];
        }
        return mmav;
    }

    private static native float meanFromJNI(byte[] input);
}
