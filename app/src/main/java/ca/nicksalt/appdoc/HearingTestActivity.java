package ca.nicksalt.appdoc;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HearingTestActivity extends AppCompatActivity {

    MediaPlayer mp;

    private int testsRun = 0, totalTests = 13;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hearing_test);

        Button play = (Button) findViewById(R.id.playButton);
        Button yes = (Button) findViewById(R.id.yesButton);
        Button no = (Button) findViewById(R.id.noButton);


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //mp = MediaPlayer.create(HearingTestActivity.this,)
            }
        });



    }







}
