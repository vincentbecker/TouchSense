package ch.ethz.inf.vs.fingerforce.emgdata.ui.fragments;

/**
 * Created by Vincent Becker on 25.01.2018.
 */

public class PathCharacteristic {

    private int paintIndex;
    private int width;

    PathCharacteristic(int paintIndex, int width) {
        this.paintIndex = paintIndex;
        this.width = width;
    }

    public int getPaintIndex() {
        return paintIndex;
    }

    public int getWidth() {
        return width;
    }
}