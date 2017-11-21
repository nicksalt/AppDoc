package ca.nicksalt.appdoc;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ColourTestActivity extends AppCompatActivity implements View.OnClickListener {

    Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colour_test);
        nextButton = findViewById(R.id.colour_test_next);
        new CountDownTimer(11000, 1000) {
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
        if (findViewById(R.id.colour_test_instructions).getVisibility() == View.VISIBLE &&
                view == nextButton) {
            startTest();
        }
    }

    public void startTest() {
        //Create random indexes, used to create random plates each test for a total of 10
        List<Integer> randomIndexRedGreen = new ArrayList<>();
        List<Integer> randomIndexTotal = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            randomIndexRedGreen.add(i);
        } for (int i = 0; i < 9; i++) {
            randomIndexTotal.add(i);
        }
        Collections.shuffle(randomIndexRedGreen);
        Collections.shuffle(randomIndexTotal);

        // Get string arrays
        int[] redGreenPlates = getResources().getIntArray(R.array.red_green_color_test_plates);
        int[] redGreenCorrect = getResources().getIntArray(R.array.red_green_color_test_correct);
        int[] redGreenPlatesIncorrect1 = getResources().getIntArray(R.array.red_green_color_test_incorrect_1);
        int[] redGreenIncorrect2 = getResources().getIntArray(R.array.red_green_color_test_incorrect_2);

        int[] totalPlates = getResources().getIntArray(R.array.total_color_test_plates);
        int[] totalCorrect = getResources().getIntArray(R.array.total_color_test_correct);
        int[] totalIncorrect = getResources().getIntArray(R.array.total_color_test_incorrect);
    }


}

