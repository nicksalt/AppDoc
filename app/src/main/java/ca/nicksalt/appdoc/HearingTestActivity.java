package ca.nicksalt.appdoc;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class HearingTestActivity extends AppCompatActivity {

    MediaPlayer mp;

    private int testsRun = 0, totalTests = 13;


    ProgressBar bar;
    LinearLayout examPage;
    LinearLayout openingPage;
    LinearLayout resultPage;
    LinearLayout finishPage;
    RelativeLayout buttonSelections;
    RelativeLayout completedButtons;
    Button play;
    Button yes;
    Button no;
    TextView highestFreq;
    TextView estimatedAge;
    TextView frequencyNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hearing_test);

        examPage = findViewById(R.id.hearing_test_exam_page);
        openingPage =  findViewById(R.id.hearing_test_instructions);
        bar = findViewById(R.id.frequency);
        frequencyNum = findViewById(R.id.frequencyText);
        play = findViewById(R.id.playButton);
        yes = findViewById(R.id.yesButton);
        no = findViewById(R.id.noButton);
        finishPage = findViewById(R.id.hearing_test_finish);
        highestFreq = findViewById(R.id.highfreq);
        estimatedAge = findViewById(R.id.estAge);
        buttonSelections = findViewById(R.id.hearing_test_button_selection);

        play.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);


        Button continueButton = findViewById(R.id.beginTest);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openingPage.setVisibility(View.GONE);
                examPage.setVisibility(View.VISIBLE);
                runTest();
            }
        });

    }

    private boolean isPlaying;
    int soundFile;

    private void runTest (){
        play.getBackground().clearColorFilter();
        buttonSelections.setVisibility(View.INVISIBLE);
        isPlaying = false;
        soundFile = R.raw.sound_80; //Sound file that will be played (change value to change sound)
        frequencyNum.setText("7,645Hz"); //Set initial frequency text
        mp = MediaPlayer.create(HearingTestActivity.this,soundFile); //Create initial sound file.
        final CountDownTimer[] playTimer = new CountDownTimer[1];
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonSelections.setVisibility(View.VISIBLE);
                play.getBackground().clearColorFilter();
                if(isPlaying){
                    mp.stop();
                    isPlaying = false;
                    mp.reset();
                    mp = MediaPlayer.create(HearingTestActivity.this,soundFile);
                    play.setText("Play");
                    play.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                }else {
                    mp.start();
                    isPlaying = true;
                    play.setText("Stop");
                    play.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                    playTimer[0] = new CountDownTimer(5000, 1000) {
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
                mp.reset();
                isPlaying = false;
                playTimer[0].cancel();

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
                findViewById(R.id.hearing_test_button_selection).setVisibility(View.INVISIBLE);
                play.setText("Play");
                play.getBackground().clearColorFilter();
                play.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                testsRun++;
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying){
                    mp.stop();
                    isPlaying = false;
                }
                endGame(false);
            }
        });
    }


    private void endGame(boolean finished){

        examPage.setVisibility(View.GONE);
        finishPage.setVisibility(View.VISIBLE);

        final String[] dataPass = new String[2];

        if(finished){
            highestFreq.setText("Highest Frequency: 20,095Hz");
            estimatedAge.setText("Estimated Age Range: 6-10");
        }else{
            switch (testsRun){
                case 1:
                    highestFreq.setText("Highest Frequency: 9,305Hz");
                    estimatedAge.setText("Estimated Age Range: 70-79");
                    dataPass[0] = "9305";
                    dataPass[1] = "70-79";
                    break;
                case 2:
                    highestFreq.setText("Highest Frequency: 10,288Hz");
                    estimatedAge.setText("Estimated Age Range: 65-69");
                    dataPass[0] = "10288";
                    dataPass[1] = "65-69";
                    break;
                case 3:
                    highestFreq.setText("Highest Frequency: 12,323Hz");
                    estimatedAge.setText("Estimated Age Range: 50-64");
                    dataPass[0] = "12323";
                    dataPass[1] = "50-64";
                    break;
                case 4:
                    highestFreq.setText("Highest Frequency: 13,852Hz");
                    estimatedAge.setText("Estimated Age Range: 45-49");
                    dataPass[0] = "13852";
                    dataPass[1] = "45-49";
                    break;
                case 5:
                    highestFreq.setText("Highest Frequency: 14,285Hz");
                    estimatedAge.setText("Estimated Age Range: 40-44");
                    dataPass[0] = "14285";
                    dataPass[1] = "40-44";
                    break;
                case 6:
                    highestFreq.setText("Highest Frequency: 15,115Hz");
                    estimatedAge.setText("Estimated Age Range: 35-39");
                    dataPass[0] = "15115";
                    dataPass[1] = "35-39";
                    break;
                case 7:
                    highestFreq.setText("Highest Frequency: 16,427Hz");
                    estimatedAge.setText("Estimated Age Range: 30-34");
                    dataPass[0] = "16427";
                    dataPass[1] = "30-34";
                    break;
                case 8:
                    highestFreq.setText("Highest Frequency: 16,775Hz");
                    estimatedAge.setText("Estimated Age Range: 25-29");
                    dataPass[0] = "16775";
                    dataPass[1] = "25-29";
                    break;
                case 9:
                    highestFreq.setText("Highest Frequency: 17,605Hz");
                    estimatedAge.setText("Estimated Age Range: 20-24");
                    dataPass[0] = "17605";
                    dataPass[1] = "20-24";
                    break;
                case 10:
                    highestFreq.setText("Highest Frequency: 18,435Hz");
                    estimatedAge.setText("Estimated Age Range: 15-19");
                    dataPass[0] = "18435";
                    dataPass[1] = "15-19";
                    break;
                case 11:
                    highestFreq.setText("Highest Frequency: 19,265Hz");
                    estimatedAge.setText("Estimated Age Range: 10-14");
                    dataPass[0] = "19265";
                    dataPass[1] = "10-14";
                    break;
                case 12:
                    highestFreq.setText("Highest Frequency: 20,095Hz");
                    estimatedAge.setText("Estimated Age Range: 6-10");
                    dataPass[0] = "20095";
                    dataPass[1] = "6-10";
                    break;

            }
        }

        try{
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            //Set to final array because it is being accessed in an inner class
            final DatabaseReference[] database = {FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("hearing-test")};
            database[0].addValueEventListener(new ValueEventListener() {
                boolean ran = false;
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getKey().equals("hearing-test") && !ran) {
                        // Just to be sure it doesn't keep iterating over the same node
                        ran=true;
                        database[0] = database[0].child("Test" + String.valueOf(dataSnapshot.getChildrenCount() + 1));
                        database[0].child("HighestFreq").setValue(dataPass[0]);
                        database[0].child("EstAge").setValue(dataPass[1]);
                        //Save time for sorting in Results:
                        database[0].child("Time").setValue(System.currentTimeMillis());
                    }
                }
                public void onCancelled(DatabaseError databaseError) {
                }
            });
         } catch (NullPointerException e){
            e.printStackTrace();
        }

        Button audiologist = findViewById(R.id.hearing_test_audiologist_button);
        Button share = findViewById(R.id.hearing_test_share_button);

        audiologist.getBackground().setColorFilter(ContextCompat.getColor(
                this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
        share.getBackground().setColorFilter(ContextCompat.getColor(
                this, R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);

        audiologist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=audiologist+near+me"));
                startActivity(mapIntent);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out AppDoc, the new greatest app available on Android! ");
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share with:"));
            }
        });
    }

    private void UpdateFirebase(final String[] dataPass){

}





    @Override
    public void onBackPressed(){
        if (openingPage.getVisibility() == View.VISIBLE ||
                finishPage.getVisibility() == View.VISIBLE){
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

    @Override
    public void onResume(){
        super.onResume();
        // Coming back to app from either selecting audiologist or share button.
        if (finishPage.getVisibility() == View.VISIBLE)
            super.onBackPressed();
    }



}
