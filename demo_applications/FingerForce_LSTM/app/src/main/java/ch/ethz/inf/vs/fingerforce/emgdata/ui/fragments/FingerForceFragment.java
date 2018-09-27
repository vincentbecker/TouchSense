package ch.ethz.inf.vs.fingerforce.emgdata.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import ch.ethz.inf.vs.fingerforce.R;
import ch.ethz.inf.vs.fingerforce.emgdata.model.EventBusProvider;
import ch.ethz.inf.vs.fingerforce.emgdata.model.RawDataPoint;
import ch.ethz.inf.vs.fingerforce.emgdata.model.Sensor;
import ch.ethz.inf.vs.fingerforce.emgdata.model.SensorConnectEvent;
import ch.ethz.inf.vs.fingerforce.emgdata.model.SensorMeasuringEvent;
import ch.ethz.inf.vs.fingerforce.emgdata.model.SensorRangeEvent;
import ch.ethz.inf.vs.fingerforce.emgdata.model.SensorUpdateEvent;
import ch.ethz.inf.vs.fingerforce.emgdata.ui.MySensorManager;
import ch.ethz.inf.vs.fingerforce.lifx.LIFXDevice;
import ch.ethz.inf.vs.fingerforce.machinelearning.MLRunner;
import ch.ethz.inf.vs.fingerforce.machinelearning.ResultCallback;

/**
 * Created by Vincent Becker on 26.10.2017.
 */
public class FingerForceFragment extends Fragment implements ResultCallback {

    /** TAG for debugging purpose */
    private static final String TAG = "FingerForceFragment";

    /** Reference to sensor */
    private Sensor sensor;
    /** Reference to layout for error message */
    private TextView resultsView1;
    private TextView resultsView2;
    private TextView resultsView3;
    private ProgressBar progressBar1;
    private ProgressBar progressBar2;
    private ProgressBar progressBar3;
    private TextView mmavView;
    private MLRunner mlRunner;
    private LIFXDevice lifx;

    private Button buttonMinus;
    private Button buttonPlus;
    private long lastSwitch = 0;

    /**
     * Public constructor to create a new  GraphFragment
     */
    public FingerForceFragment() {
        this.sensor = MySensorManager.getInstance().getMyo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_finger_force, container, false);
        resultsView1 = (TextView) view.findViewById(R.id.resultsView1);
        resultsView2 = (TextView) view.findViewById(R.id.resultsView2);
        resultsView3 = (TextView) view.findViewById(R.id.resultsView3);
        progressBar1 = (ProgressBar) view.findViewById(R.id.progress_bar1);
        progressBar2 = (ProgressBar) view.findViewById(R.id.progress_bar2);
        progressBar3 = (ProgressBar) view.findViewById(R.id.progress_bar3);

        mmavView = (TextView) view.findViewById(R.id.textViewMMAVvalue);

        mlRunner = new MLRunner(getActivity().getAssets(), this);


        lifx = new LIFXDevice();
        lifx.connect();
        /*
        buttonMinus = view.findViewById(R.id.buttonminus);
        buttonPlus = view.findViewById(R.id.buttonplus);
        buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lifx.changeBrightness(-5);
                Log.d(TAG, "Clicked on minus");
            }
        });
        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lifx.changeBrightness(5);
                Log.d(TAG, "Clicked on plus");
            }
        });
        */

        return view;
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
        mlRunner.close();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Callback for sensor connect event
     * @param event Just received event
     */
    @Subscribe
    public void onSensorConnectEvent(SensorConnectEvent event) {
        if (event.getSensor().getName().contentEquals(sensor.getName())) {
            Log.d(TAG, "Event connected received " + event.getState());
        }
    }

    /**
     * Callback for sensor measuring event
     * @param event Just received event
     */
    @Subscribe
    public void onSensorMeasuringEvent(SensorMeasuringEvent event) {
        if (event.getSensor().getName().contentEquals(sensor.getName())) {
            Log.d(TAG, "Event measuring received " + event.getState());
        }
    }

    /**
     * Callback for sensor updated
     * @param event Just received event
     */
    @Subscribe
    public void onSensorUpdatedEvent(SensorUpdateEvent event) {
        if (!event.getSensor().getName().contentEquals(sensor.getName())) return;
        RawDataPoint d = event.getDataPoint();
        //resultsView.setText(createString(d));
        mlRunner.addDataPoint(d);
    }

    /**
     * Callback for sensor connect event
     * @param event Just received event
     */
    @Subscribe
    public void onSensorRangeEvent(SensorRangeEvent event) {
        if (event.getSensor().getName().contentEquals(sensor.getName()))
            Log.d(TAG, "Sensor range event");
    }

    @Override
    public void onResult(float[] results, float mmav) {
        // for three fingers

        int p1 = (int) (results[0] * 100);
        int p2 = (int) (results[1] * 100);
        int p3 = (int) (results[2] * 100);
        int mmavInt = (int) mmav;
        resultsView1.setText(Integer.toString(p1));
        resultsView2.setText(Integer.toString(p2));
        resultsView3.setText(Integer.toString(p3));
        progressBar1.setProgress(p1);
        progressBar2.setProgress(p2);
        progressBar3.setProgress(p3);
        mmavView.setText(Integer.toString(mmavInt));

        // for two fingers
        /*
        int p1 = (int) (results[0] * 100);
        int p2 = 0;
        int p3 = (int) (results[1] * 100);
        int mmavInt = (int) mmav;
        resultsView1.setText(Integer.toString(p1));
        resultsView2.setText(Integer.toString(p2));
        resultsView3.setText(Integer.toString(p3));
        progressBar1.setProgress(p1);
        progressBar2.setProgress(p2);
        progressBar3.setProgress(p3);
        mmavView.setText(Integer.toString(mmavInt));
        */

        if (mmav >= 50) {
            int change = (int) (mmav / 40);
            if (p1 > p2 && p1 > p2) {
                //THUMB
                // check if last switch is at least 3 sec ago
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastSwitch) / 1000 >= 1) {
                    lifx.setColour(1);
                    lastSwitch = currentTime;
                }
            } else if (p2 > p1 && p2 > p3) {
              // FOREFINGER
                Log.d("CHANGE", "" + -1*change);
                lifx.changeBrightness(-1 * change);
            } else if (p3 > p1 && p3 > p2) {
                // MIDDLE FINGER
                Log.d("CHANGE", "" + change);
                lifx.changeBrightness(change);
            }
        }
        Log.d("RESULTS", "Probabilities: " + p1 + ", " + p2 + ", " + p3 + ", MMAV: " + mmav);
    }
}
