/* This file is part of EmgVisualizer.

    EmgVisualizer is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    EmgVisualizer is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EmgVisualizer.  If not, see <http://www.gnu.org/licenses/>.
*/
package ch.ethz.inf.vs.fingerforce.emgdata.model;


import android.util.Log;

/**
 * Abstract class representing sensor finding characteristics.
 *
 * @author pietrooldrati
 */
public class CharFoundEvent extends SensorEvent {

    /**
     * TAG for debugging purpose
     */
    private static final String TAG = "CharFoundEvent";

    /**
     * Internal flag for characteristics found or not.
     */
    private boolean found;

    /**
     * Public constructor with setting of new state
     *
     * @param sensor Sensor name
     * @param found True if characteristics found, false otherwise
     */
    public CharFoundEvent(Sensor sensor, boolean found) {
        super(sensor);
        this.found = found;
    }

    /**
     * Getter for new state
     *
     * @return The new connection state
     */
    public boolean getState(){ return this.found; }

    @Override
    public void fireEvent() {
        Log.d(TAG, "Sensor event from: " + getSensor().getName() + " char. found: " + found);
    }
}
