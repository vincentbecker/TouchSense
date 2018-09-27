package ch.ethz.inf.vs.fingerforce.emgdata.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import ch.ethz.inf.vs.fingerforce.R;
import ch.ethz.inf.vs.fingerforce.emgdata.model.EventBusProvider;
import ch.ethz.inf.vs.fingerforce.emgdata.model.RawDataPoint;
import ch.ethz.inf.vs.fingerforce.emgdata.model.Sensor;
import ch.ethz.inf.vs.fingerforce.emgdata.model.SensorConnectEvent;
import ch.ethz.inf.vs.fingerforce.emgdata.model.SensorMeasuringEvent;
import ch.ethz.inf.vs.fingerforce.emgdata.model.SensorRangeEvent;
import ch.ethz.inf.vs.fingerforce.emgdata.model.SensorUpdateEvent;
import ch.ethz.inf.vs.fingerforce.emgdata.ui.MySensorManager;
import ch.ethz.inf.vs.fingerforce.machinelearning.MLRunner;
import ch.ethz.inf.vs.fingerforce.machinelearning.ResultCallback;

/**
 * Created by Vincent Becker on 26.10.2017.
 */
public class StylusFragment extends Fragment implements ResultCallback, SurfaceHolder.Callback {

    /**
     * TAG for debugging purpose
     */
    private static final String TAG = "StylusFragment";

    /**
     * Reference to sensor
     */
    private Sensor sensor;

    private MLRunner mlRunner;

    private static final float TOUCH_TOLERANCE = 4;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Paint[] paint;
    private ArrayList<PathCharacteristic> pathCharacteristics;
    private int currentPaintIndex;
    private int currentWidth = 10;
    private ArrayList<Path> paths;
    private Path mPath;
    private float mX;
    private float mY;
    private long lastSwitch = 0;
    private ImageView imageViewColour;
    private TextView textViewWidth;
    private boolean erase = false;

    /**
     * Public constructor to create a new  GraphFragment
     */
    public StylusFragment() {
        this.sensor = MySensorManager.getInstance().getMyo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_stylus, container, false);

        mlRunner = new MLRunner(getActivity().getAssets(), this);

        surfaceView = view.findViewById(R.id.surfaceView);
        surfaceView.setZOrderOnTop(true);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        paint = new Paint[5];
        paint[0] = new Paint();
        paint[0].setColor(Color.RED);
        paint[0].setStyle(Paint.Style.STROKE);
        paint[1] = new Paint();
        paint[1].setColor(Color.GREEN);
        paint[1].setStyle(Paint.Style.STROKE);
        paint[2] = new Paint();
        paint[2].setColor(Color.BLUE);
        paint[2].setStyle(Paint.Style.STROKE);
        paint[3] = new Paint();
        paint[3].setColor(Color.BLACK);
        paint[3].setStyle(Paint.Style.STROKE);
        paint[4] = new Paint();
        paint[4].setColor(Color.WHITE);
        paint[4].setStyle(Paint.Style.STROKE);
        paths = new ArrayList<>();
        pathCharacteristics = new ArrayList<>();

        imageViewColour = view.findViewById(R.id.imageViewColour);
        textViewWidth = view.findViewById(R.id.textViewWidth);
        Button resetButton = view.findViewById(R.id.button);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paths.clear();
                pathCharacteristics.clear();
                clearCanvas();
            }
        });

        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d(TAG, "onTouch");
                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "Action down");
                        mX = x;
                        mY = y;
                        startNewPath();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = Math.abs(x - mX);
                        float dy = Math.abs(y - mY);
                        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                            mX = x;
                            mY = y;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "Action up");
                        mPath.lineTo(mX, mY);
                        break;
                }
                drawAllPaths();
                return true;
            }
        });
        return view;
    }

    private void startNewPath() {
        mPath = new Path();
        paths.add(mPath);
        mPath.reset();
        mPath.moveTo(mX, mY);
        if (erase) {
            setPaint(4, 50);
        } else {
            setPaint(currentPaintIndex, currentWidth);
        }
    }

    private void setPaint (int paintIndex, int width) {
        pathCharacteristics.add(new PathCharacteristic(paintIndex, width));
        switch (paintIndex) {
            case 0:
                imageViewColour.setColorFilter(getActivity().getResources().getColor(R.color.ColorRed));
                break;
            case 1:
                imageViewColour.setColorFilter(getActivity().getResources().getColor(R.color.ColorGreen));
                break;
            case 2:
                imageViewColour.setColorFilter(getActivity().getResources().getColor(R.color.ColorBlue));
                break;
            case 3:
                imageViewColour.setColorFilter(getActivity().getResources().getColor(R.color.ColorBlack));
                break;
            case 4:
                imageViewColour.setColorFilter(getActivity().getResources().getColor(R.color.ColorWhite));
                break;
        }
        textViewWidth.setText("Width: " + width);
    }

    private void drawAllPaths() {
        final Canvas canvas = surfaceHolder.lockCanvas(null);
        try {
            Log.d(TAG, "Drawing lines");
            // Empty the screen
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            for (int i = 0; i < paths.size(); i++) {
                Paint p = paint[pathCharacteristics.get(i).getPaintIndex()];
                p.setStrokeWidth(pathCharacteristics.get(i).getWidth());
                canvas.drawPath(paths.get(i), p);
            }
        } finally {
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void clearCanvas() {
        final Canvas canvas = surfaceHolder.lockCanvas(null);
        try {
            Log.d(TAG, "Clear canvas");
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        } finally {
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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

    /**
     * Callback for sensor connect event
     *
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
     *
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
     *
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
     *
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

        long currentTime = System.currentTimeMillis();

        if (mmav >= 90) {
            if (!erase) {
                erase = true;
                Log.d("STYLUS", "Erase");
                startNewPath();
            }
        } else {
            if (erase) {
                erase = false;
                currentWidth = 10;
                startNewPath();
            }

            if (mmav >= 50) {
                if (p1 > p2 && p1 > p2) {
                    // THUMB
                    // check if last switch is at least 1 sec ago
                    if ((currentTime - lastSwitch) / 1000 >= 2) {
                        // switch paint
                        Log.d("STYLUS", "Colour change");
                        currentPaintIndex = (currentPaintIndex + 1) % (paint.length - 1);
                        currentWidth = 10;
                        startNewPath();
                        lastSwitch = currentTime;
                    }
                } else if (p2 > p1 && p2 > p3) {
                    // FOREFINGER
                } else if (p3 > p1 && p3 > p2) {
                    // MIDDLE FINGER
                }
            }
        }
        Log.d("RESULTS", "Probabilities: " + p1 + ", " + p2 + ", " + p3 + ", MMAV: " + mmav);
    }
}
