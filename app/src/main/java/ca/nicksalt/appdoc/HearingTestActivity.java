package ca.nicksalt.appdoc;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

public class HearingTestActivity extends AppCompatActivity {

    MediaPlayer mp;

    private int testsRun = 0, totalTests = 13;


    ProgressBar bar;
    LinearLayout examPage;
    LinearLayout openingPage;
    LinearLayout resultPage;
    Button play;
    Button yes;
    Button no;
    Button finish;
    TextView highestFreq;
    TextView estimatedAge;
    TextView frequencyNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hearing_test);

        examPage = (LinearLayout) findViewById(R.id.test);
        openingPage = (LinearLayout) findViewById(R.id.splash);
        bar = (ProgressBar) findViewById(R.id.frequency);
        frequencyNum = (TextView) findViewById(R.id.frequencyText);
        openingPage.setVisibility(View.VISIBLE);
        examPage.setVisibility(View.INVISIBLE);
        play = (Button) findViewById(R.id.playButton);
        yes = (Button) findViewById(R.id.yesButton);
        no = (Button) findViewById(R.id.noButton);
        finish = (Button) findViewById(R.id.finishButton);
        highestFreq = (TextView) findViewById(R.id.highfreq);
        estimatedAge = (TextView) findViewById(R.id.estAge);
        Button continueButton = (Button) findViewById(R.id.beginTest);
        resultPage = (LinearLayout) findViewById(R.id.resultsPage);
        resultPage.setVisibility(View.INVISIBLE);

        play.setBackgroundColor(Color.GREEN);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openingPage.setVisibility(View.INVISIBLE);
                examPage.setVisibility(View.VISIBLE);
                runTest();
            }
        });

    }

    private boolean isPlaying;
    int soundFile;

    private void runTest (){
        isPlaying = false;
        soundFile = R.raw.sound_80; //Sound file that will be played (change value to change sound)
        frequencyNum.setText("7,645Hz"); //Set initial frequency text
        mp = MediaPlayer.create(HearingTestActivity.this,soundFile); //Create initial sound file.
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isPlaying){
                    mp.stop();
                    isPlaying = false;
                    mp.reset();
                    mp = MediaPlayer.create(HearingTestActivity.this,soundFile);
                    play.setText("Play");
                    play.setBackgroundColor(Color.GREEN);
                }else {
                    mp.start();
                    isPlaying = true;
                    play.setText("Stop");
                    play.setBackgroundColor(Color.RED);
                    new CountDownTimer(5000, 1000) {
                        public void onTick(long millisUntilFinished) {}
                        public void onFinish() {
                            if(isPlaying) {
                                mp.stop();
                                isPlaying = false;
                                play.setText("Play");
                                play.setBackgroundColor(Color.GREEN);
                                mp.reset();
                                mp = MediaPlayer.create(HearingTestActivity.this,soundFile);
                            }
                        }
                    }.start();
                }
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                testsRun++;
                mp.stop();

                isPlaying = false;

                switch (testsRun){
                    case 1:
                        frequencyNum.setText("9,305Hz");
                        soundFile = R.raw.sound_70;
                        break;
                    case 2:
                        frequencyNum.setText("10,288Hz");
                        soundFile = R.raw.sound_65;
                        break;
                    case 3:
                        frequencyNum.setText("12,323Hz");
                        soundFile = R.raw.sound_50;
                        break;
                    case 4:
                        frequencyNum.setText("13,852Hz");
                        soundFile = R.raw.sound_45;
                        break;
                    case 5:
                        frequencyNum.setText("14,285Hz");
                        soundFile = R.raw.sound_40;
                        break;
                    case 6:
                        frequencyNum.setText("15,115Hz");
                        soundFile = R.raw.sound_35;
                        break;
                    case 7:
                        frequencyNum.setText("16,427Hz");
                        soundFile = R.raw.sound_30;
                        break;
                    case 8:
                        frequencyNum.setText("16,775Hz");
                        soundFile = R.raw.sound_25;
                        break;
                    case 9:
                        frequencyNum.setText("17,605Hz");
                        soundFile = R.raw.sound_20;
                        break;
                    case 10:
                        frequencyNum.setText("18,435Hz");
                        soundFile = R.raw.sound_15;
                        break;
                    case 11:
                        frequencyNum.setText("19,265Hz");
                        soundFile = R.raw.sound_10;
                        break;
                    case 12:
                        frequencyNum.setText("20,095Hz");
                        soundFile = R.raw.sound_5;
                        break;
                    case 13:
                        mp.stop();
                        endGame(true);

                }
                mp.reset();
                mp = MediaPlayer.create(HearingTestActivity.this,soundFile);
                play.setText("Play");
                play.setBackgroundColor(Color.GREEN);
                testsRun++;
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endGame(false);
            }
        });
    }


    private void endGame(boolean finished){
        examPage.setVisibility(View.INVISIBLE);
        resultPage.setVisibility(View.VISIBLE);
        if(finished){
            highestFreq.setText("Highest Frequency: 20,095Hz");
            estimatedAge.setText("Estimated Age Range: 6-10");
        }else{
            switch (testsRun){
                case 1:
                    highestFreq.setText("Highest Frequency: 9,305Hz");
                    estimatedAge.setText("Estimated Age Range: 70-79");
                    break;
                case 2:
                    highestFreq.setText("Highest Frequency: 10,288Hz");
                    estimatedAge.setText("Estimated Age Range: 65-69");
                    break;
                case 3:
                    highestFreq.setText("Highest Frequency: 12,323Hz");
                    estimatedAge.setText("Estimated Age Range: 50-64");
                    break;
                case 4:
                    highestFreq.setText("Highest Frequency: 13,852Hz");
                    estimatedAge.setText("Estimated Age Range: 45-49");
                    break;
                case 5:
                    highestFreq.setText("Highest Frequency: 14,285Hz");
                    estimatedAge.setText("Estimated Age Range: 40-44");
                    break;
                case 6:
                    highestFreq.setText("Highest Frequency: 15,115Hz");
                    estimatedAge.setText("Estimated Age Range: 35-39");
                    break;
                case 7:
                    highestFreq.setText("Highest Frequency: 16,427Hz");
                    estimatedAge.setText("Estimated Age Range: 30-34");
                    break;
                case 8:
                    highestFreq.setText("Highest Frequency: 16,775Hz");
                    estimatedAge.setText("Estimated Age Range: 25-29");
                    break;
                case 9:
                    highestFreq.setText("Highest Frequency: 17,605Hz");
                    estimatedAge.setText("Estimated Age Range: 20-24");
                    break;
                case 10:
                    highestFreq.setText("Highest Frequency: 18,435Hz");
                    estimatedAge.setText("Estimated Age Range: 15-19");
                    break;
                case 11:
                    highestFreq.setText("Highest Frequency: 19,265Hz");
                    estimatedAge.setText("Estimated Age Range: 10-14");
                    break;
                case 12:
                    highestFreq.setText("Highest Frequency: 20,095Hz");
                    estimatedAge.setText("Estimated Age Range: 6-10");
                    break;

            }
        }

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HearingTestActivity.this, HomeActivity.class));
            }
        });

    }

    @Override
    public void onBackPressed(){
        if (findViewById(R.id.splash).getVisibility() ==
                View.VISIBLE){
            finish();
            super.onBackPressed();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Would you like to exit this test?")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            HearingTestActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder.show();
        }
    }



}
