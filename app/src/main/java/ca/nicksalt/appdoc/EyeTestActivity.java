package ca.nicksalt.appdoc;

import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EyeTestActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout instructions, test, results;

    Button startTest, left, right, up, down, optometrist, share;

    TextView testE, resultsText;

    ImageView resultsImage;

    final float[] fontSizes = new float[]{14, 11.5f, 9.25f, 7.25f, 5.5f, 4};
    List<String> possibleText = Arrays.asList("3", "E", "M", "W");

    int level = -1;
    int attempts;

    boolean testingRightEye = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eye_test);
        // Set all layouts
        instructions = findViewById(R.id.eye_test_instructions);
        test = findViewById(R.id.eye_test_test);
        results = findViewById(R.id.eye_test_results);

        // Set Buttons and Listeners
        startTest = findViewById(R.id.eye_test_start);
        startTest.setOnClickListener(this);
        left = findViewById(R.id.eye_test_left);
        left.setOnClickListener(this);
        right = findViewById(R.id.eye_test_right);
        right.setOnClickListener(this);
        up = findViewById(R.id.eye_test_up);
        up.setOnClickListener(this);
        down = findViewById(R.id.eye_test_down);
        down.setOnClickListener(this);
        optometrist = findViewById(R.id.eye_test_optometrist_button);
        optometrist.setOnClickListener(this);
        share = findViewById(R.id.eye_test_share_button);
        share.setOnClickListener(this);

        // Set text views
        testE = findViewById(R.id.eye_test_E);
        resultsText = findViewById(R.id.eye_test_diagnostic);

        //Set Image view
        //TODO: Set the image view in the results

    }

    private void runTest(){
        Log.d("EyeTest", attempts+" attempts");
        testE.setText(possibleText.get(attempts));
    };

    private void setNextLevel() {
        attempts = 0;
        if(++level > fontSizes.length-1 && testingRightEye){
            Log.d("EyeTest", level+" level");
            Collections.shuffle(possibleText);
            testingRightEye = false;
            level = 0;
            testE.setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSizes[0]);
            //TODO: show dialog
            runTest();
        } else if (level <= fontSizes.length-1) {

            Log.d("EyeTest", level+" level");
            testE.setTextSize(TypedValue.COMPLEX_UNIT_PT, fontSizes[level]);
            Collections.shuffle(possibleText);
            runTest();
        } else{
            endTest();
        }
    }

    private void endTest(){
        if (testingRightEye) {
            level = fontSizes.length;
            setNextLevel();
        } else {
            Log.d("EyeTest", "ENDING");
            test.setVisibility(View.GONE);
            setResults();
        }
    }

    private void setResults(){};
    @Override
    public void onClick(View view) {
        if (view ==startTest){
            instructions.setVisibility(View.GONE);
            test.setVisibility(View.VISIBLE);
            setNextLevel();
        }

        else if(view == up && testE.getText().toString().equals("W"))
            setNextLevel();
        else if (view == down && testE.getText().toString().equals("M"))
            setNextLevel();
        else if (view == left && testE.getText().toString().equals("3"))
                setNextLevel();
        else if (view == right && testE.getText().toString().equals("E"))
            setNextLevel();
        else if (++attempts == 3)
            endTest();
        else
            runTest();
    }
}
