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
public class TextFragment extends Fragment implements ResultCallback, SurfaceHolder.Callback {
    /**
     * TAG for debugging purpose
     */
    private static final String TAG = "TextFragment";

    private static final float TOUCH_TOLERANCE = 4;
    /**
     * Reference to sensor
     */
    private Sensor sensor;

    private MLRunner mlRunner;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Paint[] paint;
    private int currentPaintIndex;
    private int currentWidth = 10;
    private ArrayList<Path> paths;
    private ArrayList<PathCharacteristic> pathCharacteristics;
    private Path mPath;
    private float mX;
    private float mY;
    private ImageView imageViewColour;
    private TextView textViewWidth;
    private long lastThumbSwitch = 0;
    private long lastForefingerSwitch = 0;
    private long lastMiddleFingerSwitch = 0;
    private boolean lock = false;
    private static final float SWITCH_TIME = 1f;

    /**
     * Public constructor to create a new  GraphFragment
     */
    public TextFragment() {
        this.sensor = MySensorManager.getInstance().getMyo();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_text, container, false);

        mlRunner = new MLRunner(getActivity().getAssets(), this);

        surfaceView = view.findViewById(R.id.surfaceView);
        surfaceView.setZOrderOnTop(true);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        paths = new ArrayList<>();
        pathCharacteristics = new ArrayList<>();

        paint = new Paint[4];
        paint[0] = new Paint();
        paint[0].setColor(Color.RED);
        paint[0].setAlpha(75);
        paint[0].setStrokeWidth(50);
        paint[0].setStyle(Paint.Style.STROKE);
        paint[1] = new Paint();
        paint[1].setColor(Color.GREEN);
        paint[1].setAlpha(75);
        paint[1].setStrokeWidth(50);
        paint[1].setStyle(Paint.Style.STROKE);
        paint[2] = new Paint();
        paint[2].setColor(Color.BLUE);
        paint[2].setAlpha(75);
        paint[2].setStrokeWidth(50);
        paint[2].setStyle(Paint.Style.STROKE);
        paint[3] = new Paint();
        paint[3].setColor(Color.YELLOW);
        paint[3].setAlpha(75);
        paint[3].setStrokeWidth(50);
        paint[3].setStyle(Paint.Style.STROKE);
        imageViewColour = view.findViewById(R.id.imageViewColour);
        textViewWidth = view.findViewById(R.id.textViewWidth);

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
                        lock = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = Math.abs(x - mX);
                        float dy = Math.abs(y - mY);
                        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                            mX = x;
                            mY = y;
                        }
                        lock = false;
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

    private boolean isLocked() {
        return lock;
    }

    private void startNewPath() {
        mPath = new Path();
        paths.add(mPath);
        mPath.reset();
        mPath.moveTo(mX, mY);
        pathCharacteristics.add(new PathCharacteristic(currentPaintIndex, currentWidth));
        setPaint(currentPaintIndex, currentWidth);
        Log.d("NEW_PATH", "New path created!");
    }

    private void setPaint(int paintIndex, int width) {
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
                imageViewColour.setColorFilter(getActivity().getResources().getColor(R.color.ColorYellow));
                break;
        }
        textViewWidth.setText("Width: " + width);
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

    private void drawAllPaths() {
        // Empty the screen
        final Canvas canvas = surfaceHolder.lockCanvas(null);
        try {
            Log.d(TAG, "Drawing lines");
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

        int width = (int) mmav / 2;
        long currentTime = System.currentTimeMillis();

        if (!isLocked() && mmav >= 50) {
            if (p1 > p2 && p1 > p2 && mmav >= 60) {
                // THUMB
                // check if last switch is at least 1 sec ago
                if ((currentTime - lastThumbSwitch) / 1000.0 >= SWITCH_TIME) {
                    // switch paint
                    Log.d("TEXT", "Colour change");
                    currentPaintIndex = (currentPaintIndex + 1) % paint.length;
                    lastThumbSwitch = currentTime;
                    setPaint(currentPaintIndex, currentWidth);
                }
            } else if (p2 > p1 && p2 > p3) {
                // FOREFINGER
                if ((currentTime - lastForefingerSwitch) / 1000.0 < 0.5 && width > currentWidth) {
                    currentWidth = width;
                    lastForefingerSwitch = currentTime;
                    setPaint(currentPaintIndex, currentWidth);
                } else if ((currentTime - lastForefingerSwitch) / 1000.0 >= SWITCH_TIME) {
                    currentWidth = width;
                    lastForefingerSwitch = currentTime;
                    setPaint(currentPaintIndex, currentWidth);
                }
            } else if (p3 > p1 && p3 > p2) {
                // MIDDLE FINGER
                if ((currentTime - lastMiddleFingerSwitch) / 1000.0 >= SWITCH_TIME) {
                    Log.d("TEXT", "Revert");
                    // remove last element
                    if (!paths.isEmpty()) {
                        Log.d("TEXT", "Removing last line out of " + paths.size());
                        paths.remove(paths.size() - 1);
                        pathCharacteristics.remove(pathCharacteristics.size() - 1);
                        clearCanvas();
                        drawAllPaths();
                    }
                    lastMiddleFingerSwitch = currentTime;
                }
            }

            Log.d("RESULTS", "Probabilities: " + p1 + ", " + p2 + ", " + p3 + ", MMAV: " + mmav);
        }
    }
}
