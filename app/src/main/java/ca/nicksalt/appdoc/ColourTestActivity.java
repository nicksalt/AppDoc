package ca.nicksalt.appdoc;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ColourTestActivity extends AppCompatActivity implements View.OnClickListener {

    Button nextButton;
    int level = 0;
    //Create random indexes
    List<Integer> randomIndexRedGreen;
    List<Integer> randomIndexTotal;
    // Access resources
    TypedArray redGreenPlates;
    String[] redGreenCorrect;
    String[] redGreenIncorrect1;
    String[] redGreenIncorrect2;
    TypedArray totalPlates;
    String[] totalCorrect;
    String[] totalIncorrect;

    ImageView plate;
    Button button1;
    Button button2;
    Button button3;

    String answer;
    int correct = 0;

    final String TAG = "Colour Test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colour_test);
        nextButton = findViewById(R.id.colour_test_next);
        new CountDownTimer(6000, 1000) {
            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {
                nextButton.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                nextButton.setText("Start Test");
                nextButton.setOnClickListener(ColourTestActivity.this);
            }
        }.start();
    }

    @Override
    public void onClick(View view) {
        //Click Button during instructions
        if (view == nextButton && nextButton.getText() == "Start Test") {
            findViewById(R.id.colour_test_top_icon).setVisibility(View.GONE);
            findViewById(R.id.colour_test_instructions).setVisibility(View.GONE);
            startTest();
        } else if(view == nextButton && nextButton.getText() == "Next") {
            level++;
            button1.getBackground().clearColorFilter();
            button2.getBackground().clearColorFilter();
            button3.getBackground().clearColorFilter();
            setLevel();
        } else if (view == button1) {
            button1.getBackground().setColorFilter(ContextCompat.getColor(
                    this, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
            button2.getBackground().clearColorFilter();
            button3.getBackground().clearColorFilter();
            nextButton.setVisibility(View.VISIBLE);
        } else if (view == button2) {
            button2.getBackground().setColorFilter(ContextCompat.getColor(
                    this, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
            button1.getBackground().clearColorFilter();
            button3.getBackground().clearColorFilter();
            nextButton.setVisibility(View.VISIBLE);
        } else if (view == button3) {
            button3.getBackground().setColorFilter(ContextCompat.getColor(
                    this, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
            button1.getBackground().clearColorFilter();
            button2.getBackground().clearColorFilter();
            nextButton.setVisibility(View.VISIBLE);
        }
    }

    public void startTest() {
        //Create random indexes, used to create random plates each test for a total of 10
        randomIndexRedGreen = new ArrayList<>();
         randomIndexTotal = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            randomIndexRedGreen.add(i);
        } for (int i = 0; i < 8; i++) {
            randomIndexTotal.add(i);
        }
        Collections.shuffle(randomIndexRedGreen);
        Collections.shuffle(randomIndexTotal);

        //Set id arrays
        redGreenPlates = getResources().obtainTypedArray(R.array.red_green_color_test_plates);
        totalPlates = getResources().obtainTypedArray(R.array.total_color_test_plates);

        // Get string arrays
        redGreenCorrect = getResources().getStringArray(R.array.red_green_color_test_correct);
        redGreenIncorrect1 = getResources().getStringArray(R.array.red_green_color_test_incorrect_1);
        redGreenIncorrect2 = getResources().getStringArray(R.array.red_green_color_test_incorrect_2);
        totalCorrect = getResources().getStringArray(R.array.total_color_test_correct);
        totalIncorrect = getResources().getStringArray(R.array.total_color_test_incorrect);
        // Set first level
        findViewById(R.id.colour_test_plate_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.colour_test_button_selections).setVisibility(View.VISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        nextButton.setText("Next");
        plate = findViewById(R.id.colour_test_plate);
        button1 = findViewById(R.id.colour_test_button_1);
        button2 = findViewById(R.id.colour_test_button_2);
        button3 = findViewById(R.id.colour_test_button_3);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        level++;
        setLevel();
    }

    public void setLevel(){
        int index;
        Log.d(TAG, level+"");
        if (level%2 == 1 || level==10){
            index = randomIndexRedGreen.get(level-1);
            plate.setImageDrawable(getResources().getDrawable(redGreenPlates.getResourceId(index, 0)));
            button1.setText(redGreenCorrect[index]);
            button2.setText(redGreenIncorrect1[index]);
            if (redGreenIncorrect2[index].length() > 0)
                button3.setText(redGreenIncorrect2[index]);
            else
                button3.setText("Nothing");
        } else {
            index = randomIndexTotal.get(level-1);
            Log.d(TAG, ""+index);
            plate.setImageDrawable(getResources().getDrawable(totalPlates.getResourceId(index, 0)));
            button1.setText(totalCorrect[index]);
            button2.setText(totalIncorrect[index]);
            button3.setText("Nothing");
        }
    }



}
