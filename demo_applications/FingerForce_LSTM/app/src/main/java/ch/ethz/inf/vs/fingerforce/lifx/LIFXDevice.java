package ch.ethz.inf.vs.fingerforce.lifx;

import android.util.Log;

/**
 * Created by Vincent Becker on 13.05.2017.
 */

public class LIFXDevice {

    private static final String TAG = "LIFXDevice";

    private LIFXBulb[] mLights;
    private LightColour[] mColours = {new LightColour(0, 100, 100), new LightColour(60, 100, 100), new LightColour(120, 100, 100), new LightColour(180, 100, 100), new LightColour(240, 100, 100)};
    private int mIndex = 0;
    private int defaultHue = 60;
    private int defaultSaturation = 100;
    private int brightness = 100;

    public void connect() {
        Log.d("LIFX", "connect");
        mLights = LIFXClient.getLights();
        Log.d("LIFX", "Found " + mLights.length + " bulbs");
        for (LIFXBulb light : mLights) {
            light.setColor(defaultHue, defaultSaturation, brightness, 3000, 0);
        }
    }

    public void setColour(int direction) {
        mIndex += direction;
        if (mIndex < 0)
            mIndex = mColours.length - 1;
        else if (mIndex >= mColours.length)
            mIndex = 0;
        LightColour colour = mColours[mIndex];
        for (LIFXBulb light : mLights) {
            light.setColor(colour.mHue, colour.mSaturation, colour.mBrightness, 3000, 0);
        }
    }

    public void changeBrightness(int amount) {
        brightness += amount;
        if (brightness > 100) {
            brightness = 100;
        } else if (brightness < 0) {
            brightness = 0;
        }
        Log.d(TAG, "setting brightness to " + brightness);
        for (LightColour lc : mColours) {
            lc.mBrightness = brightness;
        }
        for (LIFXBulb light : mLights) {
            light.setColor(defaultHue, defaultSaturation, brightness, 3000, 0);
        }
    }

    private class LightColour {
        private int mHue;
        private int mSaturation;
        private int mBrightness;

        LightColour(int hue, int saturation, int brightness) {
            mHue = hue;
            mSaturation = saturation;
            mBrightness = brightness;
        }
    }
}