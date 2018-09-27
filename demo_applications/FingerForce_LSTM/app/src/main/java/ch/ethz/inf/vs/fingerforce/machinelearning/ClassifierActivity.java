package ch.ethz.inf.vs.fingerforce.machinelearning;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ch.ethz.inf.vs.fingerforce.R;

public class ClassifierActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private TensorFlowClassifier classifier;

    private float[] resultsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classifier);

        final EditText number1 = (EditText) findViewById(R.id.number1);
        final EditText number2 = (EditText) findViewById(R.id.number2);
        Button button = (Button) findViewById(R.id.button);
        final TextView result = (TextView) findViewById(R.id.result);

        classifier = new TensorFlowClassifier(getAssets(), 2, 8, 10, 7);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float n1 = Float.parseFloat(number1.getText().toString());
                float n2 = Float.parseFloat(number2.getText().toString());
                float[] input = {n1, n2};
                long start = System.currentTimeMillis();
                float[] resultsArray = classifier.runInference(input);
                long end = System.currentTimeMillis();
                Log.d("RUNTIME", (end - start) + " ms");
                String resultText = "Result: " + resultsArray[0] + " " + resultsArray[1];
                Log.d("RESULTS", resultText);
                result.setText(resultText);
            }
        });
    }
}
