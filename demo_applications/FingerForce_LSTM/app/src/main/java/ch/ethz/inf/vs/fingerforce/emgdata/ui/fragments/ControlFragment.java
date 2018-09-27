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
package ch.ethz.inf.vs.fingerforce.emgdata.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import ch.ethz.inf.vs.fingerforce.R;
import ch.ethz.inf.vs.fingerforce.emgdata.model.CharFoundEvent;
import ch.ethz.inf.vs.fingerforce.emgdata.model.EventBusProvider;
import ch.ethz.inf.vs.fingerforce.emgdata.model.Sensor;
import ch.ethz.inf.vs.fingerforce.emgdata.model.SensorConnectEvent;
import ch.ethz.inf.vs.fingerforce.emgdata.model.SensorMeasuringEvent;
import ch.ethz.inf.vs.fingerforce.emgdata.ui.MySensorManager;

/**
 * Fragment for controlling sensors, allowing to connect and start raw data receiving
 * @author Nicola
 */
public class ControlFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    /** TAG for debugging purpose */
    private static final String TAG = "ControlFragment";

    /** Reference to controlled Myo sensor. */
    private Sensor controlledMyoSensor;

    /** Reference to Textview for Sensor name */
    private TextView txtSensorName;
    /** Reference to Textview for Sensor status */
    private TextView txtSensorStatus;
    /** Reference to Button to trigger connection */
    private Button btnMyoConnection;
    /** Reference to Switch to trigger measuring */
    private Switch swcStream;
    /** Reference to Seekbar for streaming speed */
    private SeekBar skbRatio;

    /**
     * Public constructor to create a new ControlFragment
     */
    public ControlFragment() {
        this.controlledMyoSensor = MySensorManager.getInstance().getMyo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_control, container, false);

        btnMyoConnection = (Button) view.findViewById(R.id.control_btn_connect);
        txtSensorName = (TextView) view.findViewById(R.id.control_sensor_name);
        txtSensorStatus = (TextView) view.findViewById(R.id.control_sensor_status);
        swcStream = (Switch) view.findViewById(R.id.control_swc_stream);
        swcStream.setOnCheckedChangeListener(this);

        txtSensorName.setText(controlledMyoSensor.getName());
        updateSensorStatusView();

        btnMyoConnection.setOnClickListener(this);
        setButtonMyoConnect(controlledMyoSensor.isConnected());

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.control_btn_connect) {
            if (!controlledMyoSensor.isConnected()) {
                controlledMyoSensor.startConnection();
                Toast.makeText(getActivity(), this.getString(R.string.connection_started), Toast.LENGTH_SHORT).show();
            } else {
                controlledMyoSensor.stopConnection();
                Toast.makeText(getActivity(), this.getString(R.string.connection_stopped), Toast.LENGTH_SHORT).show();
            }
            setButtonMyoConnect(controlledMyoSensor.isConnected());
        }
        updateSensorStatusView();
    }

    /**
     * Private method for updating Myo button connect state upon sensor status
     * @param connect True if sensor is connected, false otherwise
     */
    private void setButtonMyoConnect(boolean connect) {

        Resources res = this.getResources();

        if (connect) {
            // Display disconnect layout
            btnMyoConnection.setText(this.getString(R.string.disconnect));
            btnMyoConnection.setCompoundDrawablesWithIntrinsicBounds(res.getDrawable(R.drawable.ic_bluetooth_disabled_white_36dp), null, null, null);
        } else {
            btnMyoConnection.setText(this.getString(R.string.connect));
            btnMyoConnection.setCompoundDrawablesWithIntrinsicBounds(res.getDrawable(R.drawable.ic_bluetooth_searching_white_36dp), null, null, null);
        }
    }

    /**
     * Method for updating sensor status textview
     */
    private void updateSensorStatusView() {
        txtSensorStatus.setText(Html.fromHtml(controlledMyoSensor.getStatusString()));

        if (controlledMyoSensor.isConnected()) {
            swcStream.setEnabled(true);
        } else {
            swcStream.setEnabled(false);
        }

        if (controlledMyoSensor.isMeasuring()) {
            swcStream.setChecked(true);
        } else {
            swcStream.setChecked(false);
        }

        if (controlledMyoSensor.isSession()) {
            swcStream.setEnabled(false);
        } else {
            swcStream.setEnabled(true);
        }
    }

    /**
     * Callback for Sensor connect event
     * @param event Event just received
     */
    @Subscribe
    public void onSensorConnectEvent(SensorConnectEvent event) {
        if (event.getSensor().getName().contentEquals(controlledMyoSensor.getName())) {
            setButtonMyoConnect(event.getSensor().isConnected());
            updateSensorStatusView();
            Log.d(TAG, "Event connected received " + event.getState());
            if (!event.getState() && !(event instanceof SensorMeasuringEvent)) {
                Toast.makeText(getActivity(), this.getString(R.string.connection_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Callback for Sensor start measuring event
     * @param event Event just received
     */
    @Subscribe
    public void onSensorMeasuringEvent(SensorMeasuringEvent event) {
        if (event.getSensor().getName().contentEquals(controlledMyoSensor.getName())) {
            updateSensorStatusView();
            Log.d(TAG, "Event measuring received " + event.getState());
        }
    }

    /**
     * Callback for Sensor which found characteristics.
     * @param event Event just received.
     */
    @Subscribe
    public void onSensorCharFoundEvent(CharFoundEvent event) {
        if (event.getSensor().getName().contentEquals(controlledMyoSensor.getName())) {
            updateSensorStatusView();
            Log.d(TAG, "Event char received " + event.getState());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        EventBusProvider.register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBusProvider.unregister(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.control_swc_stream) {
            if (b) {
                if (!controlledMyoSensor.isMeasuring()) {
                    controlledMyoSensor.startMeasurement(false, "");
                }
            } else {
                controlledMyoSensor.stopMeasurement();
            }
        }
    }
}