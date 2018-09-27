package ch.ethz.inf.vs.fingerforce.emgdata.ui;

import android.util.Log;

import ch.ethz.inf.vs.fingerforce.emgdata.model.AbstractEvent;

/**
 * Class for representing a fragment changing event
 *
 * Created by nicola on 30/07/15.
 */
public class PositionEvent extends AbstractEvent {

    /** Reference to nav drawer prosition */
    private int position;

    /**
     * Generic constructor for PositionEvent
     * @param position
     */
    public PositionEvent(int position) {
        this.position = position;
    }

    @Override
    public void fireEvent() {
        Log.d("EVT", "Position " + position);
    }

    /**
     * Getter for position
     * @return Position of selected item
     */
    public int getPosition() {
        return position;
    }
}
